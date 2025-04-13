package io.cdap.directives.parser;

// Path: wrangler-core/src/test/java/io/cdap/wrangler/steps/transformation/AggregateStatsTest.java

// package io.cdap.directives.parser;

import io.cdap.wrangler.api.Row;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.cdap.wrangler.TestingRig;

public class AggregateStatsTest {

    @Test
    public void testBasicAggregation() throws Exception {
        // Create test data
        List<Row> rows = new ArrayList<>();
        
        Row row1 = new Row();
        row1.add("data_transfer_size", "500KB");
        row1.add("response_time", "100ms");
        rows.add(row1);
        
        Row row2 = new Row();
        row2.add("data_transfer_size", "1MB");
        row2.add("response_time", "250ms");
        rows.add(row2);
        
        Row row3 = new Row();
        row3.add("data_transfer_size", "1.5MB");
        row3.add("response_time", "500ms");
        rows.add(row3);
        
        // Define the recipe
        String[] recipe = new String[] {
            "aggregate-stats data_transfer_size response_time total_size_mb total_time_sec MB s"
        };
        
        // Execute the recipe
        List<Row> results = io.cdap.wrangler.TestingRig.execute(recipe, rows);
        
        // Verify results
        Assert.assertEquals(1, results.size());
        
        // Expected values:
        // Total bytes: 500KB + 1MB + 1.5MB = 500*1024 + 1*1024*1024 + 1.5*1024*1024 = 3,072,000 bytes
        // In MB: 3,072,000 / (1024*1024) = ~2.93MB
        double expectedSizeMB = 2.93;
        
        // Total time: 100ms + 250ms + 500ms = 850ms = 0.85s
        double expectedTimeSec = 0.85;
        
        Assert.assertEquals(expectedSizeMB, (double) results.get(0).getValue("total_size_mb"), 0.01);
        Assert.assertEquals(expectedTimeSec, (double) results.get(0).getValue("total_time_sec"), 0.01);
    }
    
    @Test
    public void testDifferentOutputUnits() throws Exception {
        // Create test data
        List<Row> rows = new ArrayList<>();
        
        Row row1 = new Row();
        row1.add("size", "10MB");
        row1.add("time", "2s");
        rows.add(row1);
        
        Row row2 = new Row();
        row2.add("size", "20MB");
        row2.add("time", "3s");
        rows.add(row2);
        
        // Define recipe with GB and minutes as output units
        String[] recipe = new String[] {
            "aggregate-stats size time total_gb total_min GB min"
        };
        
        // Execute recipe
        List<Row> results = TestingRig.execute(recipe, rows);
        
        // Expected values:
        // Total size: 30MB = 0.029296875GB
        double expectedSizeGB = 0.029296875;
        
        // Total time: 5s = 0.08333333min
        double expectedTimeMin = 0.08333333;
        
        Assert.assertEquals(expectedSizeGB, (double) results.get(0).getValue("total_gb"), 0.0001);
        Assert.assertEquals(expectedTimeMin, (double) results.get(0).getValue("total_min"), 0.0001);
    }
    
    @Test
    public void testHandlesMissingValues() throws Exception {
        // Create test data with some missing values
        List<Row> rows = new ArrayList<>();
        
        Row row1 = new Row();
        row1.add("data", "1MB");
        row1.add("time", "100ms");
        rows.add(row1);
        
        Row row2 = new Row();
        // Missing data value
        row2.add("time", "200ms");
        rows.add(row2);
        
        Row row3 = new Row();
        row3.add("data", "3MB");
        // Missing time value
        rows.add(row3);
        
        // Define recipe
        String[] recipe = new String[] {
            "aggregate-stats data time total_data total_time"
        };
        
        // Execute recipe
        List<Row> results = TestingRig.execute(recipe, rows);
        
        // Should ignore missing values and calculate totals from available data
        Assert.assertEquals(1, results.size());
        
        // Expected: 1MB + 3MB = 4MB
        double expectedDataMB = 4.0;
        
        // Expected: 100ms + 200ms = 300ms = 0.3s
        double expectedTimeSec = 0.3;
        
        Assert.assertEquals(expectedDataMB, (double) results.get(0).getValue("total_data"), 0.01);
        Assert.assertEquals(expectedTimeSec, (double) results.get(0).getValue("total_time"), 0.01);
    }
}
