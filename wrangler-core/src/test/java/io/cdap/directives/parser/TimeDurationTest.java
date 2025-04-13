package io.cdap.directives.parser;

// Path: wrangler-core/src/test/java/io/cdap/wrangler/api/parser/TimeDurationTest.java

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