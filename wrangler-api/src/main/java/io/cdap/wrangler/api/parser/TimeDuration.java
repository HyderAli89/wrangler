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


package io.cdap.wrangler.api.parser;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Represents a parsed byte size, supporting units like KB, MB, GB, etc.
 */
public class TimeDuration implements Token {
    private final String rawValue;
    private final long nanoseconds;

    public TimeDuration(String value) {
        this.rawValue = value;
        this.nanoseconds = parseToNanos(value);
    }

    private long parseToNanos(String value) {
        value = value.trim();
        String numStr = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").trim().toLowerCase();
        
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "ns":
                return (long) num;
            case "ms":
                return (long) (num * 1_000_000);
            case "s":
            case "seconds":
                return (long) (num * 1_000_000_000);
            case "min":
            case "minutes":
                return (long) (num * 60 * 1_000_000_000L);
            case "h":
            case "hours":
                return (long) (num * 60 * 60 * 1_000_000_000L);
            case "d":
            case "days":
                return (long) (num * 24 * 60 * 60 * 1_000_000_000L);
            default:
                throw new IllegalArgumentException("Unknown time unit: " + unit);
        }
    }

    public long getNanoseconds() {
        return nanoseconds;
    }

    public double getMilliseconds() {
        return nanoseconds / 1_000_000.0;
    }

    public double getSeconds() {
        return nanoseconds / 1_000_000_000.0;
    }

    public double getMinutes() {
        return nanoseconds / (60.0 * 1_000_000_000.0);
    }

    public double getHours() {
        return nanoseconds / (60.0 * 60.0 * 1_000_000_000.0);
    }

    @Override
    public Object value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.TIME_DURATION;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name());
        object.addProperty("value", rawValue);
        object.addProperty("nanoseconds", nanoseconds);
        return object;
    }
}


