package com.adashrod.timeperiod;

import java.util.HashMap;
import java.util.Map;

/**
 * A TimeUnit is a unit used for measuring time (seconds, minutes, hours, etc.).
 */
public enum TimeUnit {
    // it's important that the smaller units have smaller rank values, so if any more are added, be sure that
    // smaller times have smaller ranks so that min() works properly
    MILLISECOND(0, "millisecond", "ms"),
    SECOND(0, "second", "s"),
    MINUTE(1, "minute", "m"),
    HOUR(2, "hour", "h"),
    DAY(3, "day", "d"),
    WEEK(4, "week", "w");

    private final int rank;
    private final String name;
    private final String abbreviation;
    private final static Map<String, TimeUnit> TIME_UNIT_MAP = new HashMap<>();

    static {
        TIME_UNIT_MAP.put(SECOND.getSingularName(), SECOND);
        TIME_UNIT_MAP.put(SECOND.getPluralName(), SECOND);
        TIME_UNIT_MAP.put(MINUTE.getSingularName(), MINUTE);
        TIME_UNIT_MAP.put(MINUTE.getPluralName(), MINUTE);
        TIME_UNIT_MAP.put(HOUR.getSingularName(), HOUR);
        TIME_UNIT_MAP.put(HOUR.getPluralName(), HOUR);
        TIME_UNIT_MAP.put(DAY.getSingularName(), DAY);
        TIME_UNIT_MAP.put(DAY.getPluralName(), DAY);
        TIME_UNIT_MAP.put(WEEK.getSingularName(), WEEK);
        TIME_UNIT_MAP.put(WEEK.getPluralName(), WEEK);
    }

    private TimeUnit(final int rank, final String name, final String abbreviation) {
        this.rank = rank;
        this.name = name;
        this.abbreviation = abbreviation;
    }

    public String getSingularName() {
        return name;
    }

    public String getPluralName() {
        return name + "s";
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public static TimeUnit parseTimeUnit(final String timeString) {
        return TIME_UNIT_MAP.get(timeString.toLowerCase());
    }

    public static TimeUnit min(final TimeUnit a, final TimeUnit b) {
        return a.rank < b.rank ? a : b;
    }
}
