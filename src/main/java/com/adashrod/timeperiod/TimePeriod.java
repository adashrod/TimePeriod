package com.adashrod.timeperiod;

import javafx.util.Pair;

import java.text.ParseException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A TimePeriod object represents a period of time, i.e. a length of time without context of the beginning or end of the
 * period. It currently supports the units described in {@link com.adashrod.timeperiod.TimeUnit}. It doesn't support anything
 * larger than weeks since they have variable conversions, e.g. months can be 28, 29, 30, or 31 days; years can be 365
 * or 366 days, etc.
 */
public class TimePeriod {
    private static final Pattern unitWordPattern;

    static {
        final StringBuilder unitsBuilder = new StringBuilder();
        for (final TimeUnit timeUnit: TimeUnit.values()) {
            unitsBuilder.append(timeUnit.getPluralName()).append("?|");
        }
        unitsBuilder.deleteCharAt(unitsBuilder.length() - 1);
        unitWordPattern = Pattern.compile(String.format("\\d+\\s*(%s)", unitsBuilder.toString()), Pattern.CASE_INSENSITIVE);
    }

    private long weeks;
    private long days;
    private long hours;
    private long minutes;
    private long seconds;
    private long milliseconds;
    private boolean needsNormalization = true;

    /**
     * Constructs a TimePeriod of length 0.
     */
    public TimePeriod() {
        weeks = days = hours = minutes = seconds = 0;
        needsNormalization = false;
    }

    /**
     * Constructs a TimePeriod with the specified units
     * @param weeks weeks
     * @param days days
     * @param hours hours
     * @param minutes minutes
     * @param seconds seconds
     * @param milliseconds milliseconds
     */
    public TimePeriod(final long weeks, final long days, final long hours, final long minutes, final long seconds, final long milliseconds) {
        this.weeks = weeks;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        this.seconds = seconds;
        this.milliseconds = milliseconds;
        normalize();
    }

    /**
     * Constructs a TimePeriod with only one type of unit
     * @param number how many units
     * @param timeUnit what type of units
     */
    public TimePeriod(final long number, final TimeUnit timeUnit) {
        weeks = 0;
        days = 0;
        hours = 0;
        minutes = 0;
        seconds = 0;
        if (timeUnit == TimeUnit.SECOND) {
            seconds = number;
        } else if (timeUnit == TimeUnit.MINUTE) {
            minutes = number;
        } else if (timeUnit == TimeUnit.HOUR) {
            hours = number;
        } else if (timeUnit == TimeUnit.DAY) {
            days = number;
        } else if (timeUnit == TimeUnit.WEEK) {
            weeks = number;
        }
        normalize();
    }

    /**
     * Parses a formatted string into a TimePeriod. The expected format is "n units" where n is a number, units is the
     * name of a supported unit (singular or plural), and there is at least one whitespace char in between
     * Note: this doesn't support other locales
     * @param timeString a formatted string, e.g. "2 weeks", "5 days", "36 hours"
     * @return the corresponding TimePeriod
     * @throws ParseException if the string doesn't match the format
     */
    public static TimePeriod parseAsWords(final String timeString) throws ParseException {
        final Matcher matcher = unitWordPattern.matcher(timeString);
        if (matcher.matches()) {
            final String[] parts = timeString.split("\\s");
            return new TimePeriod(new Long(parts[0]), TimeUnit.parseTimeUnit(parts[1]));
        }
        int i, state;
        for (i = 0, state = 0; i < timeString.length() && state < 2; i++) {
            final char c = timeString.charAt(i);
            if ((state == 0 && Character.isWhitespace(c)) || (state == 1 && Character.isAlphabetic(c))) {
                state++;
            } else if ((state == 0 && Character.isAlphabetic(c)) || (state == 1 && Character.isDigit(c))) {
                i++;
                break;
            }
        }
        if (state < 2) {
            throw new ParseException("Couldn't parse as time units", i - 1);
        } else {
            final Set<String> unitNames = new HashSet<>();
            for (final TimeUnit timeUnit: TimeUnit.values()) {
                unitNames.add(timeUnit.getPluralName().toLowerCase());
            }
            final int start = i - 1;
            int j;
            for (j = start + 1; j <= timeString.length(); j++) {
                final String substring = timeString.substring(start, j).toLowerCase();
                for (final Iterator<String> iterator = unitNames.iterator(); iterator.hasNext(); ) {
                    final String unit = iterator.next();
                    if (!unit.startsWith(substring)) {
                        iterator.remove();
                    }
                }
                if (unitNames.isEmpty()) {
                    break;
                }
            }
            throw new ParseException("Misspelled/Unrecognized units", j - 1);
        }
    }

    /**
     * Returns the Time object as two pieces of data:
     * - a number
     * - the largest unit that the Time object comprises
     * The largestAllowed parameter determines the largest unit type that will be returned. The TimeUnit return can be
     * smaller than largestAllowed if necessary to prevent loss of data. Note Example 2 below where all return values
     * are in hours. This is because returning &lt;3, DAY&gt; would obscure the remaining 5 hours.
     * Example 1
     * A Time object t is "2 weeks"
     * t.getLargestUnit(TimeUnit.WEEK) == &lt;2, WEEK&gt;
     * t.getLargestUnit(TimeUnit.DAY) == &lt;14, DAY&gt;
     * t.getLargestUnit(TimeUnit.HOUR) == &lt;336, HOUR&gt;
     *
     * Example 2
     * t is 3 days + 5 hours
     * t.getLargestUnit(TimeUnit.WEEK) == &lt;77, HOUR&gt;
     * t.getLargestUnit(TimeUnit.DAY) == &lt;77, HOUR&gt;
     * t.getLargestUnit(TimeUnit.HOUR) == &lt;77, HOUR&gt;
     * @param largestAllowed the largest unit type that will be returned, regardless of how much time is in the object
     * @return a pair with an Long and a TimeUnit
     */
    public Pair<Long, TimeUnit> getLargestUnit(final TimeUnit largestAllowed) {
        final boolean hasDays = days > 0;
        final boolean hasHours = hours > 0;
        final boolean hasMinutes = minutes > 0;
        final boolean hasSeconds = seconds > 0;
        final boolean hasMilliseconds = milliseconds > 0;

        // highestResolutionNecessary is the most granular unit (milliseconds being the highest resolution) needed to
        // represent the time without truncating any data.
        final TimeUnit highestResolutionNecessary;
        if (hasMilliseconds) {
            highestResolutionNecessary = TimeUnit.MILLISECOND;
        } else if (hasSeconds) {
            highestResolutionNecessary = TimeUnit.SECOND;
        } else if (hasMinutes) {
            highestResolutionNecessary = TimeUnit.MINUTE;
        } else if (hasHours) {
            highestResolutionNecessary = TimeUnit.HOUR;
        } else if (hasDays) {
            highestResolutionNecessary = TimeUnit.DAY;
        } else {
            highestResolutionNecessary = TimeUnit.WEEK;
        }

        denormalize(TimeUnit.min(largestAllowed, highestResolutionNecessary));
        if (weeks != 0) {
            return new Pair<>(weeks, TimeUnit.WEEK);
        } else if (days != 0) {
            return new Pair<>(days, TimeUnit.DAY);
        } else if (hours != 0) {
            return new Pair<>(hours, TimeUnit.HOUR);
        } else if (minutes != 0) {
            return new Pair<>(minutes, TimeUnit.MINUTE);
        } else {
            return new Pair<>(seconds, TimeUnit.SECOND);
        }
    }

    TimePeriod normalize() {
        if (needsNormalization) {
            if (milliseconds >= 1000) {
                seconds += milliseconds / 1000;
                milliseconds %= 1000;
            }
            if (seconds >= 60) {
                minutes += seconds / 60;
                seconds %= 60;
            }
            if (minutes >= 60) {
                hours += minutes / 60;
                minutes %= 60;
            }
            if (hours >= 24) {
                days += hours / 24;
                hours %= 24;
            }
            if (days >= 7) {
                weeks += days / 7;
                days %= 7;
            }
            needsNormalization = false;
        }
        return this;
    }

    /**
     * Modifies this to be in a de-normalized state. All units greater than largestAllowed will be zeroed and their
     * values put into the largestAllowed unit. E.g. if this == 2 days, after denormalize(HOUR), this == 48 hours
     * @param largestAllowed the largest TimeUnit that won't be zeroed out
     * @return self
     */
    TimePeriod denormalize(final TimeUnit largestAllowed) {
        if (largestAllowed == TimeUnit.DAY) {
            days += weeks * 7;
            weeks = 0;
        } else if (largestAllowed == TimeUnit.HOUR) {
            days += weeks * 7;
            weeks = 0;
            hours += days * 24;
            days = 0;
        } else if (largestAllowed == TimeUnit.MINUTE) {
            days += weeks * 7;
            weeks = 0;
            hours += days * 24;
            days = 0;
            minutes += hours * 60;
            hours = 0;
        } else if (largestAllowed == TimeUnit.SECOND) {
            days += weeks * 7;
            weeks = 0;
            hours += days * 24;
            days = 0;
            minutes += hours * 60;
            hours = 0;
            seconds += minutes * 60;
            minutes = 0;
        } else if (largestAllowed == TimeUnit.MILLISECOND) {
            days += weeks * 7;
            weeks = 0;
            hours += days * 24;
            days = 0;
            minutes += hours * 60;
            hours = 0;
            seconds += minutes * 60;
            minutes = 0;
            milliseconds += seconds * 1000;
            seconds = 0;
        }
        needsNormalization = true;
        return this;
    }

    @Override
    public String toString() {
        return String.format("%d week(s), %d day(s), %d hour(s), %d minute(s), %d second(s), %d millisecond(s)",
            weeks, days, hours, minutes, seconds, milliseconds);
    }

    public long getWeeks() {
        return weeks;
    }
    long getDenormalizedWeeks() {
        return weeks;
    }
    public TimePeriod setWeeks(final long weeks) {
        if (weeks >= 0) {
            this.weeks = weeks;
        }
        return this;
    }
    public long getDays() {
        return normalize().days;
    }
    long getDenormalizedDays() {
        return days;
    }
    public TimePeriod setDays(final long days) {
        if (days >= 0) {
            this.days = days;
            needsNormalization = true;
            normalize();
        }
        return this;
    }
    public long getHours() {
        return normalize().hours;
    }
    long getDenormalizedHours() {
        return hours;
    }
    public TimePeriod setHours(final long hours) {
        if (hours >= 0) {
            this.hours = hours;
            needsNormalization = true;
            normalize();
        }
        return this;
    }
    public long getMinutes() {
        return normalize().minutes;
    }
    long getDenormalizedMinutes() {
        return minutes;
    }
    public TimePeriod setMinutes(final long minutes) {
        if (minutes >= 0) {
            this.minutes = minutes;
            needsNormalization = true;
            normalize();
        }
        return this;
    }
    public long getSeconds() {
        return normalize().seconds;
    }
    long getDenormalizedSeconds() {
        return seconds;
    }
    public TimePeriod setSeconds(final long seconds) {
        if (seconds >= 0) {
            this.seconds = seconds;
            needsNormalization = true;
            normalize();
        }
        return this;
    }
    public long getMilliseconds() {
        return normalize().milliseconds;
    }
    long getDenormalizedMilliseconds() {
        return milliseconds;
    }
    public TimePeriod setMilliseconds(final long milliseconds) {
        if (milliseconds >= 0) {
            this.milliseconds = milliseconds;
            needsNormalization = true;
            normalize();
        }
        return this;
    }
}
