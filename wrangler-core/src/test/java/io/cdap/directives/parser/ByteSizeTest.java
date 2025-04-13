/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package io.cdap.directives.parser;

// package io.cdap.wranglerapi.parser;

import org.junit.Assert;
import org.junit.Test;

import io.cdap.wrangler.api.parser.ByteSize;

public class ByteSizeTest {

    @Test
    public void testByteSizeParsing() {
        // Test bytes
        ByteSize size1 = new ByteSize("100bytes");
        Assert.assertEquals(100L, size1.getBytes());
        
        // Test kilobytes
        ByteSize size2 = new ByteSize("2KB");
        Assert.assertEquals(2048L, size2.getBytes());
        Assert.assertEquals(2.0, size2.getKilobytes(), 0.001);
        
        // Test megabytes
        ByteSize size3 = new ByteSize("1.5MB");
        Assert.assertEquals(1572864L, size3.getBytes());
        Assert.assertEquals(1.5, size3.getMegabytes(), 0.001);
        
        // Test gigabytes
        ByteSize size4 = new ByteSize("3GB");
        Assert.assertEquals(3221225472L, size4.getBytes());
        Assert.assertEquals(3.0, size4.getGigabytes(), 0.001);
        
        // Test with spaces
        ByteSize size5 = new ByteSize("10 KB");
        Assert.assertEquals(10240L, size5.getBytes());
        
        // Test case insensitivity
        ByteSize size6 = new ByteSize("5mb");
        Assert.assertEquals(5242880L, size6.getBytes());
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidByteSize() {
        new ByteSize("xyz");
    }
}