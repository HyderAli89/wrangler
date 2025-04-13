/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */



package io.cdap.wrangler.steps.transformation;

import io.cdap.cdap.api.annotation.Description;
import io.cdap.cdap.api.annotation.Name;
import io.cdap.cdap.api.annotation.Plugin;
import io.cdap.wrangler.api.Arguments;
import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.lineage.Lineage;
import io.cdap.wrangler.api.lineage.Many;
import io.cdap.wrangler.api.lineage.Mutation;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;
import java.util.Map;


@Plugin(type = Directive.TYPE)
@Name("aggregate-stats")
@Categories(categories = { "aggregator" })
@Description("Aggregates byte size and time duration statistics from columns.")
public class AggregateStats implements Directive {
    private String byteSizeColumn;
    private String timeDurationColumn;
    private String totalSizeColumn;
    private String totalTimeColumn;
    private String sizeUnit;
    private String timeUnit;

    @Override
    public UsageDefinition define() {
        UsageDefinition.Builder builder = UsageDefinition.builder("aggregate-stats");
        builder.define("byte_size_column", TokenType.COLUMN_NAME);
        builder.define("time_duration_column", TokenType.COLUMN_NAME);
        builder.define("output_size_column", TokenType.COLUMN_NAME);
        builder.define("output_time_column", TokenType.COLUMN_NAME);
        builder.define("size_unit", TokenType.TEXT, "MB");
        builder.define("time_unit", TokenType.TEXT, "s");
        return builder.build();
    }

    @Override
    public void initialize(Arguments args) throws DirectiveParseException {
        this.byteSizeColumn = ((ColumnName) args.value("byte_size_column")).value().toString();
        this.timeDurationColumn = ((ColumnName) args.value("time_duration_column")).value().toString();
        this.totalSizeColumn = ((ColumnName) args.value("output_size_column")).value().toString();
        this.totalTimeColumn = ((ColumnName) args.value("output_time_column")).value().toString();
        this.sizeUnit = ((Text) args.value("size_unit")).value().toString();
        this.timeUnit = ((Text) args.value("time_unit")).value().toString();
    }

    @Override
    public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
        // Get or initialize store for tracking aggregates
        Map<String, Object> store = (Map<String, Object>) context.getTransientStore();
        String bytesKey = "total_bytes_" + byteSizeColumn;
        String nanosKey = "total_nanos_" + timeDurationColumn;
        String countKey = "row_count";
        
        // Initialize if not present
        if (!store.containsKey(bytesKey)) {
            store.put(bytesKey, 0L);
            store.put(nanosKey, 0L);
            store.put(countKey, 0);
        }
        
        // Extract current values
        long totalBytes = (long) store.get(bytesKey);
        long totalNanos = (long) store.get(nanosKey);
        int count = (int) store.get(countKey);
        
        // Process rows
        for (Row row : rows) {
            // Process byte size
            if (row.find(byteSizeColumn) != -1) {
                Object byteValue = row.getValue(byteSizeColumn);
                if (byteValue != null) {
                    long bytes;
                    if (byteValue instanceof ByteSize) {
                        bytes = ((ByteSize) byteValue).getBytes();
                    } else if (byteValue instanceof String) {
                        bytes = new ByteSize((String) byteValue).getBytes();
                    } else {
                        throw new DirectiveExecutionException(
                            String.format("Column '%s' is not a valid byte size", byteSizeColumn)
                        );
                    }
                    totalBytes += bytes;
                }
            }
            
            // Process time duration
            if (row.find(timeDurationColumn) != -1) {
                Object timeValue = row.getValue(timeDurationColumn);
                if (timeValue != null) {
                    long nanos;
                    if (timeValue instanceof TimeDuration) {
                        nanos = ((TimeDuration) timeValue).getNanoseconds();
                    } else if (timeValue instanceof String) {
                        nanos = new TimeDuration((String) timeValue).getNanoseconds();
                    } else {
                        throw new DirectiveExecutionException(
                            String.format("Column '%s' is not a valid time duration", timeDurationColumn)
                        );
                    }
                    totalNanos += nanos;
                }
            }
            
            count++;
        }
        
        // Store updated values
        store.put(bytesKey, totalBytes);
        store.put(nanosKey, totalNanos);
        store.put(countKey, count);
        
        // Check if this is the last batch of rows (finalization)
        if (context.isLast()) {
            Row resultRow = new Row();
            
            // Convert to requested units
            double sizeValue = convertBytes(totalBytes, sizeUnit);
            double timeValue = convertNanos(totalNanos, timeUnit);
            
            resultRow.add(totalSizeColumn, sizeValue);
            resultRow.add(totalTimeColumn, timeValue);
            
            return List.of(resultRow);
        }
        
        // Not the last batch, pass through rows unchanged
        return rows;
    }
    
    private double convertBytes(long bytes, String unit) {
        switch (unit.toUpperCase()) {
            case "B":
            case "BYTES":
                return bytes;
            case "KB":
                return bytes / 1024.0;
            case "MB":
                return bytes / (1024.0 * 1024.0);
            case "GB":
                return bytes / (1024.0 * 1024.0 * 1024.0);
            case "TB":
                return bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
            default:
                return bytes / (1024.0 * 1024.0); // Default to MB
        }
    }
    
    private double convertNanos(long nanos, String unit) {
        switch (unit.toLowerCase()) {
            case "ns":
                return nanos;
            case "ms":
                return nanos / 1_000_000.0;
            case "s":
            case "seconds":
                return nanos / 1_000_000_000.0;
            case "min":
            case "minutes":
                return nanos / (60.0 * 1_000_000_000.0);
            case "h":
            case "hours":
                return nanos / (60.0 * 60.0 * 1_000_000_000.0);
            default:
                return nanos / 1_000_000_000.0; // Default to seconds
        }
    }

    
//    @Override
public Mutation lineage() {
    return Mutation.builder()
        .readable("Aggregating stats from '%s' and '%s' into '%s' and '%s'",
            byteSizeColumn, timeDurationColumn, totalSizeColumn, totalTimeColumn)
        .relation(totalSizeColumn, Many.of(byteSizeColumn))
        .relation(totalTimeColumn, Many.of(timeDurationColumn))
        .build();
}


    @Override
    public void destroy() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'destroy'");
    }
}