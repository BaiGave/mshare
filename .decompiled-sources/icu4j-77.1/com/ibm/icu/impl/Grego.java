/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.impl.Pair;
import java.util.Locale;

public class Grego {
    public static final long MIN_MILLIS = -184303902528000000L;
    public static final long MAX_MILLIS = 183882168921600000L;
    public static final int MILLIS_PER_SECOND = 1000;
    public static final int MILLIS_PER_MINUTE = 60000;
    public static final int MILLIS_PER_HOUR = 3600000;
    public static final int MILLIS_PER_DAY = 86400000;
    private static final int JULIAN_1_CE = 1721426;
    private static final int JULIAN_1970_CE = 2440588;
    private static final int[] MONTH_LENGTH = new int[]{31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    private static final int[] DAYS_BEFORE = new int[]{0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335};

    public static final boolean isLeapYear(int year) {
        return (year & 3) == 0 && (year % 100 != 0 || year % 400 == 0);
    }

    public static final int monthLength(int year, int month) {
        return MONTH_LENGTH[month + (Grego.isLeapYear(year) ? 12 : 0)];
    }

    public static final int previousMonthLength(int year, int month) {
        return month > 0 ? Grego.monthLength(year, month - 1) : 31;
    }

    public static long fieldsToDay(int year, int month, int dom) {
        int y = year - 1;
        long julian = (long)(365 * y) + Grego.floorDivide(y, 4L) + 1721423L + Grego.floorDivide(y, 400L) - Grego.floorDivide(y, 100L) + 2L + (long)DAYS_BEFORE[month + (Grego.isLeapYear(year) ? 12 : 0)] + (long)dom;
        return julian - 2440588L;
    }

    public static int dayOfWeek(long day) {
        Pair<Long, Integer> result = Grego.floorDivideAndRemainer(day + 5L, 7);
        int dayOfWeek = (Integer)result.second;
        dayOfWeek = dayOfWeek == 0 ? 7 : dayOfWeek;
        return dayOfWeek;
    }

    public static Pair<Integer, Integer> dayToYear(long day) {
        Pair<Long, Integer> n400 = Grego.floorDivideAndRemainer(day += 719162L, 146097);
        Pair<Long, Integer> n100 = Grego.floorDivideAndRemainer(((Integer)n400.second).intValue(), 36524);
        Pair<Long, Integer> n4 = Grego.floorDivideAndRemainer(((Integer)n100.second).intValue(), 1461);
        Pair<Long, Integer> n1 = Grego.floorDivideAndRemainer(((Integer)n4.second).intValue(), 365);
        int year = (int)(400L * (Long)n400.first + 100L * (Long)n100.first + 4L * (Long)n4.first + (Long)n1.first);
        int dayOfYear = (Integer)n1.second;
        if ((Long)n100.first == 4L || (Long)n1.first == 4L) {
            dayOfYear = 365;
        } else {
            ++year;
        }
        return new Pair<Integer, Integer>(year, ++dayOfYear);
    }

    public static int[] dayToFields(long day, int[] fields) {
        int march1;
        if (fields == null || fields.length < 5) {
            fields = new int[5];
        }
        Pair<Integer, Integer> result = Grego.dayToYear(day);
        int year = (Integer)result.first;
        int dayOfYear = (Integer)result.second;
        day += 719162L;
        boolean isLeap = Grego.isLeapYear(year);
        int correction = 0;
        int n = march1 = isLeap ? 60 : 59;
        if (dayOfYear > march1) {
            correction = isLeap ? 1 : 2;
        }
        int month = (12 * (dayOfYear - 1 + correction) + 6) / 367;
        int dayOfMonth = dayOfYear - DAYS_BEFORE[isLeap ? month + 12 : month];
        int dayOfWeek = (int)((day + 2L) % 7L);
        if (dayOfWeek < 1) {
            dayOfWeek += 7;
        }
        fields[0] = year;
        fields[1] = month;
        fields[2] = dayOfMonth;
        fields[3] = dayOfWeek;
        fields[4] = dayOfYear;
        return fields;
    }

    public static int[] timeToFields(long time, int[] fields) {
        if (fields == null || fields.length < 6) {
            fields = new int[6];
        }
        Pair<Long, Integer> result = Grego.floorDivideAndRemainer(time, 86400000);
        Grego.dayToFields((Long)result.first, fields);
        fields[5] = (Integer)result.second;
        return fields;
    }

    public static int timeToYear(long time) {
        return (Integer)Grego.dayToYear((long)((Long)Grego.floorDivideAndRemainer((long)time, (int)86400000).first).longValue()).first;
    }

    public static long floorDivide(long numerator, long denominator) {
        return numerator >= 0L ? numerator / denominator : (numerator + 1L) / denominator - 1L;
    }

    private static Pair<Long, Integer> floorDivideAndRemainer(long numerator, int denominator) {
        if (numerator >= 0L) {
            return new Pair<Long, Integer>(Grego.floorDivide(numerator, denominator), (int)(numerator % (long)denominator));
        }
        long quotient = Grego.floorDivide(numerator, denominator);
        return new Pair<Long, Integer>(quotient, (int)(numerator - quotient * (long)denominator));
    }

    public static int getDayOfWeekInMonth(int year, int month, int dayOfMonth) {
        int weekInMonth = (dayOfMonth + 6) / 7;
        if (weekInMonth == 4) {
            if (dayOfMonth + 7 > Grego.monthLength(year, month)) {
                weekInMonth = -1;
            }
        } else if (weekInMonth == 5) {
            weekInMonth = -1;
        }
        return weekInMonth;
    }

    public static String timeToString(long time) {
        int[] fields = Grego.timeToFields(time, null);
        int millis = fields[5];
        int hour = millis / 3600000;
        int min = (millis %= 3600000) / 60000;
        int sec = (millis %= 60000) / 1000;
        return String.format((Locale)null, "%04d-%02d-%02dT%02d:%02d:%02d.%03dZ", fields[0], fields[1] + 1, fields[2], hour, min, sec, millis %= 1000);
    }
}

