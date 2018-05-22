package com.adashrod.timeperiod;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Used for parsing Strings and turning them into {@link com.adashrod.timeperiod.TimePeriod}s and for formatting
 * {@link com.adashrod.timeperiod.TimePeriod}s as Strings.
 * Formats are specified by pattern strings. Within date and time pattern strings, unquoted letters from 'A' to 'Z' and
 * from 'a' to 'z' are interpreted as pattern letters representing the components of a time period string. Text can be
 * quoted using single quotes (') to avoid interpretation. "''" within a single-quoted section represents a literal
 * single quote. All other characters are not interpreted; they're simply copied into the output string during formatting
 * or matched against the input string during parsing.
 *
 * The following pattern letters are defined (all other characters from 'A' to 'Z' and from 'a' to 'z' are reserved):
 * &lt;table&gt;
 *     &lt;thead&gt;
 *         &lt;tr&gt;
 *             &lt;th&gt;Letter&lt;/th&gt;&lt;th&gt;TimePeriod Component&lt;/th&gt;&lt;th&gt;Example&lt;/th&gt;
 *         &lt;/tr&gt;
 *     &lt;/thead&gt;
 *     &lt;tbody&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;w&lt;/td&gt;     &lt;td&gt;weeks&lt;/td&gt;               &lt;td&gt;645&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;d&lt;/td&gt;     &lt;td&gt;days&lt;/td&gt;                &lt;td&gt;5&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;h&lt;/td&gt;     &lt;td&gt;hours&lt;/td&gt;               &lt;td&gt;23&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;m&lt;/td&gt;     &lt;td&gt;minutes&lt;/td&gt;             &lt;td&gt;2&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;s&lt;/td&gt;     &lt;td&gt;seconds&lt;/td&gt;             &lt;td&gt;0&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;z&lt;/td&gt;     &lt;td&gt;milliseconds&lt;/td&gt;        &lt;td&gt;0&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;W&lt;/td&gt;     &lt;td&gt;weeks unit name&lt;/td&gt;     &lt;td&gt;weeks&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;D&lt;/td&gt;     &lt;td&gt;days unit name&lt;/td&gt;      &lt;td&gt;days&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;H&lt;/td&gt;     &lt;td&gt;hours unit name&lt;/td&gt;     &lt;td&gt;hours&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;M&lt;/td&gt;     &lt;td&gt;minutes unit name&lt;/td&gt;   &lt;td&gt;minutes&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;S&lt;/td&gt;     &lt;td&gt;seconds unit name&lt;/td&gt;   &lt;td&gt;seconds&lt;/td&gt;
 *         &lt;/tr&gt;
 *         &lt;tr&gt;
 *             &lt;td&gt;Z&lt;/td&gt;     &lt;td&gt;milliseconds unit name&lt;/td&gt;&lt;td&gt;milliseconds&lt;/td&gt;
 *         &lt;/tr&gt;
 *     &lt;/tbody&gt;
 * &lt;/table&gt;
 *
 * When using the letters for numbers of units (wdhmsZ), the number of repeated letters determines how many leading
 * zeroes will be used in formatting.
 * E.g. given a pattern "mm", minutes less than 10 will be formatted as "01", "02", etc.
 * The number of repeated letters also determines how many chars to look at when parsing. How many chars to look at is
 * the max of how many format chars are in the format string and the max number of digits needed to represent the
 * maximum normalized value of that unit.
 * Examples
 *  pattern "m:s.z" looks at at most 2 chars for minutes, 2 chars for seconds, and 3 chars for milliseconds when parsing
 *      because the maximum normalized values of minutes, seconds, and milliseconds are 59, 59, and 999 respectively.
 *  pattern "mmmm:ssss.zzzz" looks at at most 4 chars for each
 * See note on parsing at {@link com.adashrod.timeperiod.TimePeriodFormat#setMaxUnit(TimeUnit)}
 *
 * When using the letters for names of units (WDHMSZ), a singular letter will mean the abbreviation for that unit and
 * two or more will mean the full name of the unit. When parsing, trailing 's' are ignored for plural unit names; when
 * formatting, names are pluralized whenever the number of that unit is not 1.
 * Examples
 *  pattern "hH, mM, sS", format output: "16h, 2m, 1s"
 *  pattern "hhH, mmM, ssS", format output: "16h, 02m, 01s"
 *  pattern "h HH, m MM, s SS", format output: "16 hours, 2 minutes, 1 second"
 *  pattern "h HH, m MM, 'and' s SS", format output: "16 hours, 2 minutes, and 1 second"
 *
 *  TimePeriodFormat does not currently support other locales.
 */
public class TimePeriodFormat {
    private TimeUnit maxTimeUnit;
    /**
     * passed as first argument to String.format() in {@link com.adashrod.timeperiod.TimePeriodFormat#format(TimePeriod)}
     */
    private final String compiledFormatString;
    /**
     * Used for evaluating values to get arguments to pass to String.format()
     */
    private final List<Evaluator> evaluators = new ArrayList<>();
    /**
     * Used for reading tokens in {@link com.adashrod.timeperiod.TimePeriodFormat#parse(String)}
     */
    private final List<Reader> readers = new ArrayList<>();

    private static final char weekChar = 'w';
    private static final char dayChar = 'd';
    private static final char hourChar = 'h';
    private static final char minuteChar = 'm';
    private static final char secondChar = 's';
    private static final char millisecondChar = 'z';
    private static final char weekStringChar = 'W';
    private static final char dayStringChar = 'D';
    private static final char hourStringChar = 'H';
    private static final char minuteStringChar = 'M';
    private static final char secondStringChar = 'S';
    private static final char millisecondStringChar = 'Z';
    private static final Set<Character> numberFormatChars = new HashSet<>();
    private static final Set<Character> unitFormatChars = new HashSet<>();
    /**
     * maximum acceptable lengths of numbers for each unit type
     */
    private static final Map<Character, Integer> maxLengths = new HashMap<>();
    private static final Map<Character, TimeUnit> unitMap = new HashMap<>();
    private static final Set<Character> regexSpecialChars = new HashSet<>();

    static {
        numberFormatChars.add(weekChar);
        numberFormatChars.add(dayChar);
        numberFormatChars.add(hourChar);
        numberFormatChars.add(minuteChar);
        numberFormatChars.add(secondChar);
        numberFormatChars.add(millisecondChar);
        unitFormatChars.add(weekStringChar);
        unitFormatChars.add(dayStringChar);
        unitFormatChars.add(hourStringChar);
        unitFormatChars.add(minuteStringChar);
        unitFormatChars.add(secondStringChar);
        unitFormatChars.add(millisecondStringChar);
        maxLengths.put(weekChar, null);
        maxLengths.put(dayChar, 1);
        maxLengths.put(hourChar, 2);
        maxLengths.put(minuteChar, 2);
        maxLengths.put(secondChar, 2);
        maxLengths.put(millisecondChar, 3);
        unitMap.put(weekStringChar, TimeUnit.WEEK);
        unitMap.put(dayStringChar, TimeUnit.DAY);
        unitMap.put(hourStringChar, TimeUnit.HOUR);
        unitMap.put(minuteStringChar, TimeUnit.MINUTE);
        unitMap.put(secondStringChar, TimeUnit.SECOND);
        unitMap.put(millisecondStringChar, TimeUnit.MILLISECOND);
        for (final char c: "$^*()[]{}.".toCharArray()) {
            regexSpecialChars.add(c);
        }
    }

    /**
     * Builds a TimePeriodFormat using the formatString as a template
     * @param formatString a string describing the format for parsing and formatting. See the class-level description
     *                     for details.
     */
    public TimePeriodFormat(final String formatString) {
        final StringBuilder templateBuilder = new StringBuilder();
        for (int i = 0; i < formatString.length(); i++) {
            final char c = formatString.charAt(i);
            if (c == '\'') {
                final StringBuilder readerBuilder = new StringBuilder();
                while (i + 1 < formatString.length()) {
                    final char nextC = formatString.charAt(i + 1);
                    if (nextC != '\'') {
                        // capturing a string within single quotes
                        templateBuilder.append(nextC);
                        readerBuilder.append(nextC);
                        i++;
                    } else {
                        if (i + 2 == formatString.length() || formatString.charAt(i + 2) != '\'') {
                            // end of string or end of single-quoted section
                            i++;
                            break;
                        } else {
                            // found two single quotes in a row within a single-quoted string- literal single quote
                            templateBuilder.append('\'');
                            readerBuilder.append('\'');
                            i += 2;
                        }
                    }
                }
                readers.add(new Reader(readerBuilder.toString()));
            } else if (numberFormatChars.contains(c)) {
                int length = 1;
                while (i + 1 < formatString.length() && formatString.charAt(i + 1) == c) {
                    length++;
                    i++;
                }
                templateBuilder.append("%s");
                evaluators.add(new Evaluator(length, c));
                readers.add(new Reader(length, c));
            } else if (unitFormatChars.contains(c)) {
                int length = 1;
                final StringBuilder readerBuilder = new StringBuilder();
                while (i + 1 < formatString.length() && formatString.charAt(i + 1) == c) {
                    length++;
                    i++;
                }
                templateBuilder.append("%s");
                evaluators.add(new Evaluator(length, c));
                readerBuilder.append(length == 1 ? unitMap.get(c).getAbbreviation() :
                    String.format("(%s|%s)", unitMap.get(c).getSingularName(), unitMap.get(c).getPluralName()));
                readers.add(new Reader(readerBuilder.toString()));
            } else if (Character.isAlphabetic(c)) {
                throw new IllegalArgumentException(String.format("Illegal pattern character '%s'", c));
            } else {
                templateBuilder.append(c);
                final String addition = regexSpecialChars.contains(c) ? String.format("\\%s", c) : Character.toString(c);
                if (!readers.isEmpty() && readers.get(readers.size() - 1).text != null) {
                    readers.get(readers.size() - 1).append(addition);
                } else {
                    readers.add(new Reader(addition));
                }
            }
        }
        compiledFormatString = templateBuilder.toString();
    }

    /**
     * Formats the timePeriod as a string, according to the format passed into the constructor
     * @param timePeriod an object to format
     * @return a formatted string
     */
    public String format(final TimePeriod timePeriod) {
        timePeriod.denormalize(maxTimeUnit);
        final List<String> strings = evaluators.stream().map((final Evaluator evaluator) -> {
            return evaluator.toString(timePeriod);
        }).collect(Collectors.toList());
        timePeriod.normalize();
        return String.format(compiledFormatString, strings.toArray());
    }

    /**
     * Parses a formatted string using the format to create a TimePeriod
     * @param timeString a string formatted according to the format passed into the constructor
     * @return a TimePeriod
     * @throws ParseException if the timeString doesn't match the format
     */
    public TimePeriod parse(final String timeString) throws ParseException {
        final TimePeriod result = new TimePeriod();
        int i = 0;
        for (final Reader reader: readers) {
            if (reader.text != null) {
                // read plain text input against regex
                final int start = i;
                final StringBuilder stringBuilder = new StringBuilder();
                boolean startedMatching = false;
                while (i < timeString.length()) {
                    stringBuilder.append(timeString.charAt(i));
                    if (stringBuilder.toString().matches(reader.text)) {
                        startedMatching = true;
                    } else if (startedMatching) {
                        // once it has started matching and then fails to match- quit
                        break;
                    }
                    i++;
                }
                if (!startedMatching) {
                    // pattern never matched - plain text of the format string was not found in the input
                    throw new ParseException("Non-numeric token not found", start);
                }
            } else {
                // read a number
                final StringBuilder numberReader = new StringBuilder();
                // if there's a maxTimeUnit set, then have no limit on how many chars to read for that unit
                final Integer length = maxTimeUnit == unitMap.get(Character.toUpperCase(reader.field)) ? null : Math.max(reader.length, maxLengths.get(reader.field));
                // loop until the max number of chars have been read
                for (int j = 0; (length == null || j < length) && i + j < timeString.length(); j++) {
                    if (Character.isDigit(timeString.charAt(i + j))) {
                        numberReader.append(timeString.charAt(i + j));
                    } else {
                        // quit early if possible
                        break;
                    }
                }
                if (numberReader.length() == 0) {
                    throw new ParseException("Missing numeric token", i);
                }
                i += numberReader.length();
                reader.read(result, new Integer(numberReader.toString()));
            }
        }
        if (i != timeString.length()) {
            throw new ParseException("Encountered extra characters after expected end of input", i);
        }
        return result;
    }

    /**
     * This can be used to make the format display denormalized times.
     * E.g. given a format "hh:mm", and a TimePeriod of 1.5 days, calling setMaxUnit(HOUR), then format would give
     * "36:00"
     * This also affects parsing. Normally units are limited to max(n, {number of repeated chars in format string})
     * in parsing, where n is the assumed max number of digits that would be needed to represent that unit- 3 for ms, 2
     * for s, 2 for h, etc. When setMaxUnit(u) has been called, parsing for that unit u will have no limit of chars to
     * parse. It will keep grabbing digits until it reaches a non-digit char or the end of the string.
     * Examples:
     *  formatString = "hhmm"
     *  parse() only looks at 2 chars for hours; parse("1030") is (10 hours, 30 minutes)
     *  formatString = "hhhmm"
     *  parse() looks at 3 chars for hours; parse("12300") is (123 hours, 0 minutes)
     *  formatString = "hhmm", setMaxUnit(HOUR) has been called
     *  parse() will look at all digit chars for hours until end of string and the operation will fail. In this scenario
     *  it would be possible to have an unlimited number of digits for hours, but only if there was a delimiter separating
     *  hours and minutes, e.g. "123456789:12"
     *  formatString = "hh:mm", setMaxUnit(HOUR) has been called
     *  parse() will grab all digits it finds until it finds the semicolon for hours - no limit
     * @param timeUnit the largest size unit that will be guaranteed non-zero in any TimePeriods passed to format for
     *                 formatting
     * @return this
     */
    public TimePeriodFormat setMaxUnit(final TimeUnit timeUnit) {
        this.maxTimeUnit = timeUnit;
        return this;
    }

    /**
     * Evaluators are used to convert a field on a TimePeriod into a string for formatting
     */
    private static class Evaluator {
        private final int length;
        private final char field;

        /**
         * @param length determines how much padding numbers get/whether to display unit abbreviations or names
         * @param field which field of a TimePeriod/unit to display
         */
        public Evaluator(final int length, final char field) {
            this.length = length;
            this.field = field;
        }

        /**
         * Given the length and field type determined at construction, create the appropriate string using timePeriod's
         * data
         * @param timePeriod the TimePeriod to use for getting data
         * @return a string version of the data
         */
        public String toString(final TimePeriod timePeriod) {
            switch (field) {
                case weekChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedWeeks(), length);
                case dayChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedDays(), length);
                case hourChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedHours(), length);
                case minuteChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedMinutes(), length);
                case secondChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedSeconds(), length);
                case millisecondChar:
                    return Util.padWithZeroes(timePeriod.getDenormalizedMilliseconds(), length);
                case weekStringChar:
                    return length == 1 ? TimeUnit.WEEK.getAbbreviation() :
                        (timePeriod.getDenormalizedWeeks() == 1 ? TimeUnit.WEEK.getSingularName() : TimeUnit.WEEK.getPluralName());
                case dayStringChar:
                    return length == 1 ? TimeUnit.DAY.getAbbreviation() :
                        (timePeriod.getDenormalizedDays() == 1 ? TimeUnit.DAY.getSingularName() : TimeUnit.DAY.getPluralName());
                case hourStringChar:
                    return length == 1 ? TimeUnit.HOUR.getAbbreviation() :
                        (timePeriod.getDenormalizedHours() == 1 ? TimeUnit.HOUR.getSingularName() : TimeUnit.HOUR.getPluralName());
                case minuteStringChar:
                    return length == 1 ? TimeUnit.MINUTE.getAbbreviation() :
                        (timePeriod.getDenormalizedMinutes() == 1 ? TimeUnit.MINUTE.getSingularName() : TimeUnit.MINUTE.getPluralName());
                case secondStringChar:
                    return length == 1 ? TimeUnit.SECOND.getAbbreviation() :
                        (timePeriod.getDenormalizedSeconds() == 1 ? TimeUnit.SECOND.getSingularName() : TimeUnit.SECOND.getPluralName());
                case millisecondStringChar:
                    return length == 1 ? TimeUnit.MILLISECOND.getAbbreviation() :
                        (timePeriod.getDenormalizedMilliseconds() == 1 ? TimeUnit.MILLISECOND.getSingularName() : TimeUnit.MILLISECOND.getPluralName());
                default:
                    return "";
            }
        }
    }

    /**
     * Readers are used during parsing to take input from a string and set properties on a TimePeriod
     */
    private static class Reader {
        private final char field;
        private final int length;
        private String text;

        /**
         * Creates a reader that will be used for reading numbers
         * @param length the max number of chars to read for the field
         * @param field what type of number is being read
         */
        public Reader(final int length, final char field) {
            this.field = field;
            this.length = length;
            this.text = null;
        }

        /**
         * Creates a reader used for matching plain text
         * @param text text to match
         */
        public Reader(final String text) {
            this.field = 0;
            this.length = 0;
            this.text = text;
        }

        /**
         * Sets a field on timePeriod equal to number. Which field was determined by what was passed to the constructor.
         * @param timePeriod a TimePeriod to update
         * @param number the value to set on a field in timePeriod
         */
        public void read(final TimePeriod timePeriod, final int number) {
            switch (field) {
                case weekChar:
                    timePeriod.setWeeks(number);
                    break;
                case dayChar:
                    timePeriod.setDays(number);
                    break;
                case hourChar:
                    timePeriod.setHours(number);
                    break;
                case minuteChar:
                    timePeriod.setMinutes(number);
                    break;
                case secondChar:
                    timePeriod.setSeconds(number);
                    break;
                case millisecondChar:
                    timePeriod.setMilliseconds(number);
                    break;
            }
        }

        /**
         * Appends a string onto the text field of the reader
         * @param s to append
         */
        public void append(final String s) {
            this.text += s;
        }
    }
}
