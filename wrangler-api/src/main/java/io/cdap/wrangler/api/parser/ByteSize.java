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
public class ByteSize implements Token {
    private final String rawValue;
    private final long bytes;

    public ByteSize(String value) {
        this.rawValue = value;
        this.bytes = parseBytes(value);
    }

    private long parseBytes(String value) {
        value = value.trim();
        String numStr = value.replaceAll("[^0-9.]", "");
        String unit = value.replaceAll("[0-9.]", "").trim().toLowerCase();
        
        double num = Double.parseDouble(numStr);
        
        switch (unit) {
            case "b":
            case "bytes":
                return (long) num;
            case "kb":
                return (long) (num * 1024);
            case "mb":
                return (long) (num * 1024 * 1024);
            case "gb":
                return (long) (num * 1024 * 1024 * 1024);
            case "tb":
                return (long) (num * 1024 * 1024 * 1024 * 1024);
            case "pb":
                return (long) (num * 1024 * 1024 * 1024 * 1024 * 1024);
            default:
                throw new IllegalArgumentException("Unknown byte unit: " + unit);
        }
    }

    public long getBytes() {
        return bytes;
    }

    public double getKilobytes() {
        return bytes / 1024.0;
    }

    public double getMegabytes() {
        return bytes / (1024.0 * 1024.0);
    }

    public double getGigabytes() {
        return bytes / (1024.0 * 1024.0 * 1024.0);
    }

    @Override
    public Object value() {
        return rawValue;
    }

    @Override
    public TokenType type() {
        return TokenType.BYTE_SIZE;
    }

    @Override
    public JsonElement toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("type", type().name());
        object.addProperty("value", rawValue);
        object.addProperty("bytes", bytes);
        return object;
    }
}


