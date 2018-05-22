package com.adashrod.timeperiod;

import javafx.util.Pair;
import org.junit.Test;

import java.text.ParseException;

import static com.adashrod.timeperiod.TimeUnit.DAY;
import static com.adashrod.timeperiod.TimeUnit.HOUR;
import static com.adashrod.timeperiod.TimeUnit.WEEK;
import static junit.framework.Assert.assertEquals;

/**
* unit tests for the TimePeriod class
*/
public class TimePeriodTests {
    @Test
    public void testGetLargestUnit() {
        final TimePeriod t1 = new TimePeriod(2, WEEK);

        final Pair<Long, TimeUnit> t1InWeeks = t1.getLargestUnit(WEEK);
        final Pair<Long, TimeUnit> t1InDays = t1.getLargestUnit(DAY);
        final Pair<Long, TimeUnit> t1InHours = t1.getLargestUnit(HOUR);

        assertEquals(2, (long) t1InWeeks.getKey());
        assertEquals(WEEK, t1InWeeks.getValue());
        assertEquals(14, (long) t1InDays.getKey());
        assertEquals(DAY, t1InDays.getValue());
        assertEquals(336, (long) t1InHours.getKey());
        assertEquals(HOUR, t1InHours.getValue());

        final TimePeriod t2 = new TimePeriod();
        t2.setWeeks(0).setDays(3).setHours(5).setMinutes(0).setSeconds(0);

        final Pair<Long, TimeUnit> t2InWeeks = t2.getLargestUnit(WEEK);
        final Pair<Long, TimeUnit> t2InDays = t2.getLargestUnit(DAY);
        final Pair<Long, TimeUnit> t2InHours = t2.getLargestUnit(HOUR);

        assertEquals(77, (long) t2InWeeks.getKey());
        assertEquals(HOUR, t2InWeeks.getValue());
        assertEquals(77, (long) t2InDays.getKey());
        assertEquals(HOUR, t2InDays.getValue());
        assertEquals(77, (long) t2InHours.getKey());
        assertEquals(HOUR, t2InHours.getValue());
    }

    @Test
    public void testParseGood() throws ParseException {
        final TimePeriod t1 = TimePeriod.parseAsWords("23 hours");
        assertEquals(0, t1.getWeeks());
        assertEquals(0, t1.getDays());
        assertEquals(23, t1.getHours());
        assertEquals(0, t1.getMinutes());
        assertEquals(0, t1.getSeconds());
        final TimePeriod t2 = TimePeriod.parseAsWords("100 day");
        assertEquals(14, t2.getWeeks());
        assertEquals(2, t2.getDays());
        assertEquals(0, t2.getHours());
        assertEquals(0, t2.getMinutes());
        assertEquals(0, t2.getSeconds());
        final TimePeriod t3 = TimePeriod.parseAsWords("1 week");
        assertEquals(1, t3.getWeeks());
        assertEquals(0, t3.getDays());
        assertEquals(0, t3.getHours());
        assertEquals(0, t3.getMinutes());
        assertEquals(0, t3.getSeconds());
    }


    @Test
    public void testTimeParseMisspelledDays() {
        try {
            TimePeriod.parseAsWords("2 daays");
        } catch (final ParseException pe) {
            assertEquals(4, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseMisspelledHours() {
        try {
            TimePeriod.parseAsWords("33 hoors");
        } catch (final ParseException pe) {
            assertEquals(5, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseUnsupportedUnit() {
        try {
            TimePeriod.parseAsWords("4 months");
        } catch (final ParseException pe) {
            assertEquals(3, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testNormalize() {
        final TimePeriod t = new TimePeriod(0, 0, 49, 0, 0, 0);
        assertEquals(2, t.getDays());
        assertEquals(1, t.getHours());
    }
}
