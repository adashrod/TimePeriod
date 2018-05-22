package com.adashrod.timeperiod;

import org.junit.Test;

import static com.adashrod.timeperiod.TimeUnit.WEEK;
import static com.adashrod.timeperiod.TimeUnit.DAY;
import static com.adashrod.timeperiod.TimeUnit.HOUR;
import static com.adashrod.timeperiod.TimeUnit.MINUTE;
import static com.adashrod.timeperiod.TimeUnit.SECOND;
import static junit.framework.Assert.assertEquals;

public class TimeUnitTests {
    @Test
    public void testTimeUnitMin() {
        assertEquals(SECOND, TimeUnit.min(SECOND, MINUTE));
        assertEquals(MINUTE, TimeUnit.min(MINUTE, HOUR));
        assertEquals(HOUR, TimeUnit.min(HOUR, DAY));
        assertEquals(DAY, TimeUnit.min(DAY, WEEK));
    }
}
