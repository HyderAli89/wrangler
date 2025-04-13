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

// package io.cdap.wrangler.api.parser;

import org.junit.Assert;
import org.junit.Test;

import io.cdap.wrangler.api.parser.TimeDuration;

public class TimeDurationTest {

    @Test
    public void testTimeDurationParsing() {
        // Test milliseconds
        TimeDuration duration1 = new TimeDuration("500ms");
        Assert.assertEquals(500_000_000L, duration1.getNanoseconds());
        Assert.assertEquals(500.0, duration1.getMilliseconds(), 0.001);
        
        // Test seconds
        TimeDuration duration2 = new TimeDuration("2s");
        Assert.assertEquals(2_000_000_000L, duration2.getNanoseconds());
        Assert.assertEquals(2.0, duration2.getSeconds(), 0.001);
        
        // Test minutes
        TimeDuration duration3 = new TimeDuration("1.5min");
        Assert.assertEquals(90_000_000_000L, duration3.getNanoseconds());
        Assert.assertEquals(90.0, duration3.getSeconds(), 0.001);
        Assert.assertEquals(1.5, duration3.getMinutes(), 0.001);
        
        // Test hours
        TimeDuration duration4 = new TimeDuration("2h");
        Assert.assertEquals(7200.0, duration4.getSeconds(), 0.001);
        Assert.assertEquals(2.0, duration4.getHours(), 0.001);
        
        // Test with spaces
        TimeDuration duration5 = new TimeDuration("30 s");
        Assert.assertEquals(30.0, duration5.getSeconds(), 0.001);
        
        // Test with full names
        TimeDuration duration6 = new TimeDuration("45seconds");
        Assert.assertEquals(45.0, duration6.getSeconds(), 0.001);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidTimeDuration() {
        new TimeDuration("xyz");
    }
}