package com.adashrod.timeperiod;

import org.junit.Test;

import java.text.ParseException;

import static junit.framework.Assert.assertEquals;

public class TimePeriodFormatTests {
    private static final TimePeriodFormat HH_MM_SS_TIMESTAMP = new TimePeriodFormat("hh:mm:ss");
    private static final TimePeriodFormat MM_SS_TIMESTAMP = new TimePeriodFormat("mm:ss");
    private static final TimePeriodFormat MMMM_SS_TIMESTAMP = new TimePeriodFormat("mmmm:ss");
    private static final TimePeriodFormat FULL_TIMESTAMP = new TimePeriodFormat("hh:mm:ss.zzz");
    private static final TimePeriodFormat WITH_PLAIN_TEXT = new TimePeriodFormat("h HH 'and' m MM 'and' s SS 'and' z ZZ");
    private static final TimePeriodFormat WITH_LITERAL_SINGLE_QUOTE = new TimePeriodFormat("'I''m a quantity of' hH, mM, sS, zZ");
    private static final TimePeriodFormat FULLY_QUOTED_ASCENDING_UNITS = new TimePeriodFormat("''''zZ, sS, mM, hH''''");

    @Test
    public void testTimeParseGood() throws ParseException {
        final TimePeriod t1 = MM_SS_TIMESTAMP.parse("1:00");
        assertEquals(0, t1.getWeeks());
        assertEquals(0, t1.getDays());
        assertEquals(0, t1.getHours());
        assertEquals(1, t1.getMinutes());
        assertEquals(0, t1.getSeconds());

        final TimePeriod t2 = HH_MM_SS_TIMESTAMP.parse("2:34:56");
        assertEquals(0, t2.getWeeks());
        assertEquals(0, t2.getDays());
        assertEquals(2, t2.getHours());
        assertEquals(34, t2.getMinutes());
        assertEquals(56, t2.getSeconds());
    }

    @Test
    public void testTimeParseTooManyHourDigits() {
        try {
            HH_MM_SS_TIMESTAMP.parse("100:12:45");
        } catch (final ParseException pe) {
            assertEquals(2, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseTooManyMinuteDigits() {
        try {
            HH_MM_SS_TIMESTAMP.parse("1:123:45");
        } catch (final ParseException pe) {
            assertEquals(4, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseNotEnoughMinuteDigits() {
        try {
            HH_MM_SS_TIMESTAMP.parse("1::45");
        } catch (final ParseException pe) {
            assertEquals(2, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseTooManySecondDigits() {
        try {
            HH_MM_SS_TIMESTAMP.parse("1:13:450");
        } catch (final ParseException pe) {
            assertEquals(7, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testTimeParseNotEnoughSecondDigits() {
        try {
            HH_MM_SS_TIMESTAMP.parse("1:13:");
        } catch (final ParseException pe) {
            assertEquals(5, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testFormatHhMmSs() {
        final TimePeriod t = new TimePeriod(0, 0, 5, 12, 34, 0);
        assertEquals("05:12:34", HH_MM_SS_TIMESTAMP.format(t));
    }

    @Test
    public void testFormatMmSs() {
        final TimePeriod t = new TimePeriod(0, 0, 0, 12, 34, 0);
        assertEquals("12:34", MM_SS_TIMESTAMP.format(t));
    }

    @Test
    public void testFormatFullTimestamp() {
        final TimePeriod t = new TimePeriod(0, 0, 1, 47, 32, 134);
        assertEquals("01:47:32.134", FULL_TIMESTAMP.format(t));
    }

    @Test
    public void testWordyParsingGood() throws ParseException {
        final TimePeriod t1 = WITH_PLAIN_TEXT.parse("7 hours and 56 minutes and 4 seconds and 123 milliseconds");
        final TimePeriod t2 = WITH_LITERAL_SINGLE_QUOTE.parse("I'm a quantity of 7h, 56m, 4s, 123ms");
        final TimePeriod t3 = FULLY_QUOTED_ASCENDING_UNITS.parse("'123ms, 4s, 56m, 7h'");
        final TimePeriod[] timePeriods = {t1, t2, t3};
        for (final TimePeriod t: timePeriods) {
            assertEquals(7, t.getHours());
            assertEquals(56, t.getMinutes());
            assertEquals(4, t.getSeconds());
            assertEquals(123, t.getMilliseconds());
        }
    }

    @Test(expected = ParseException.class)
    public void testWordyParsingBad() throws ParseException {
        WITH_PLAIN_TEXT.parse("7 hours and 56 minnutes and 4 seconds and 123 milliseconds");
    }

    @Test
    public void testWordyFormats() {
        final TimePeriod t = new TimePeriod(0, 0, 7, 56, 4, 123);
        assertEquals("7 hours and 56 minutes and 4 seconds and 123 milliseconds", WITH_PLAIN_TEXT.format(t));
        assertEquals("I'm a quantity of 7h, 56m, 4s, 123ms", WITH_LITERAL_SINGLE_QUOTE.format(t));
        assertEquals("'123ms, 4s, 56m, 7h'", FULLY_QUOTED_ASCENDING_UNITS.format(t));
    }

    @Test
    public void testSpecialCharParsingBad() {
        // the dot in FULL_TIMESTAMP should not become a wildcard in the compiled regex used for parsing
        try {
            FULL_TIMESTAMP.parse("12:34:56x789");
        } catch (final ParseException pe) {
            assertEquals(8, pe.getErrorOffset());
            return;
        }
        assert false;
    }

    @Test
    public void testSpecialCharParsingGood() throws ParseException {
        final TimePeriodFormat format1 = new TimePeriodFormat("{z}");
        final TimePeriod timePeriod1 = format1.parse("{12}");
        assertEquals(12, timePeriod1.getMilliseconds());
        final TimePeriodFormat format2 = new TimePeriodFormat("[s]");
        final TimePeriod timePeriod2 = format2.parse("[30]");
        assertEquals(30, timePeriod2.getSeconds());
        final TimePeriodFormat format3 = new TimePeriodFormat("(m)");
        final TimePeriod timePeriod3 = format3.parse("(5)");
        assertEquals(5, timePeriod3.getMinutes());
        final TimePeriodFormat format4 = new TimePeriodFormat("h$m^s*z");
        final TimePeriod timePeriod4 = format4.parse("1$2^3*4");
        assertEquals(1, timePeriod4.getHours());
        assertEquals(2, timePeriod4.getMinutes());
        assertEquals(3, timePeriod4.getSeconds());
        assertEquals(4, timePeriod4.getMilliseconds());
    }

    @Test
    public void testSetMaxUnitFormat() {
        MM_SS_TIMESTAMP.setMaxUnit(TimeUnit.MINUTE);
        final String s1 = MM_SS_TIMESTAMP.format(new TimePeriod(0, 0, 1, 30, 15, 0));
        assertEquals("90:15", s1);
        MM_SS_TIMESTAMP.setMaxUnit(null);

        MMMM_SS_TIMESTAMP.setMaxUnit(TimeUnit.MINUTE);
        final String s2 = MMMM_SS_TIMESTAMP.format(new TimePeriod(0, 0, 2, 30, 10, 0));
        assertEquals("0150:10", s2);
        MMMM_SS_TIMESTAMP.setMaxUnit(null);
    }

    @Test
    public void testSetMaxUnitParse() throws ParseException {
        MM_SS_TIMESTAMP.setMaxUnit(TimeUnit.MINUTE);
        final TimePeriod t1 = MM_SS_TIMESTAMP.parse("120:15");
        assertEquals(2, t1.getHours());
        assertEquals(0, t1.getMinutes());
        assertEquals(15, t1.getSeconds());
        MM_SS_TIMESTAMP.setMaxUnit(null);

        MMMM_SS_TIMESTAMP.setMaxUnit(TimeUnit.MINUTE);
        final TimePeriod t2 = MMMM_SS_TIMESTAMP.parse("1212:00");
        assertEquals(20, t2.getHours());
        assertEquals(12, t2.getMinutes());
        assertEquals(0, t2.getSeconds());
        MMMM_SS_TIMESTAMP.setMaxUnit(null);
    }
}
