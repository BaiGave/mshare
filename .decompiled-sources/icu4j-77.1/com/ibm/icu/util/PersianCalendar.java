/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.util;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import java.util.BitSet;
import java.util.Date;
import java.util.Locale;

@Deprecated
public class PersianCalendar
extends Calendar {
    private static final long serialVersionUID = -6727306982975111643L;
    private static final int[][] MONTH_COUNT = new int[][]{{31, 31, 0}, {31, 31, 31}, {31, 31, 62}, {31, 31, 93}, {31, 31, 124}, {31, 31, 155}, {30, 30, 186}, {30, 30, 216}, {30, 30, 246}, {30, 30, 276}, {30, 30, 306}, {29, 30, 336}};
    private static final int PERSIAN_EPOCH = 1948320;
    private static NonLeapYears LEAP_CORRECTION = new NonLeapYears();
    private static final int[][] LIMITS = new int[][]{{0, 0, 0, 0}, {-5000000, -5000000, 5000000, 5000000}, {0, 0, 11, 11}, {1, 1, 52, 53}, new int[0], {1, 1, 29, 31}, {1, 1, 365, 366}, new int[0], {-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], {-5000000, -5000000, 5000000, 5000000}, new int[0], {-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0], new int[0], {0, 0, 11, 11}};

    @Deprecated
    public PersianCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public PersianCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(ULocale.Category.FORMAT));
    }

    @Deprecated
    public PersianCalendar(Locale aLocale) {
        this(TimeZone.forLocaleOrDefault(aLocale), aLocale);
    }

    @Deprecated
    public PersianCalendar(ULocale locale) {
        this(TimeZone.forULocaleOrDefault(locale), locale);
    }

    @Deprecated
    public PersianCalendar(TimeZone zone, Locale aLocale) {
        super(zone, aLocale);
        this.setTimeInMillis(System.currentTimeMillis());
    }

    @Deprecated
    public PersianCalendar(TimeZone zone, ULocale locale) {
        super(zone, locale);
        this.setTimeInMillis(System.currentTimeMillis());
    }

    @Deprecated
    public PersianCalendar(Date date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.setTime(date);
    }

    @Deprecated
    public PersianCalendar(int year, int month, int date) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.set(1, year);
        this.set(2, month);
        this.set(5, date);
    }

    @Deprecated
    public PersianCalendar(int year, int month, int date, int hour, int minute, int second) {
        super(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT));
        this.set(1, year);
        this.set(2, month);
        this.set(5, date);
        this.set(11, hour);
        this.set(12, minute);
        this.set(13, second);
    }

    @Override
    @Deprecated
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    private static final boolean isLeapYear(int year) {
        if (LEAP_CORRECTION.contains(year)) {
            return false;
        }
        if (LEAP_CORRECTION.contains(year - 1)) {
            return true;
        }
        int[] remainder = new int[1];
        PersianCalendar.floorDivide(25 * year + 11, 33, remainder);
        return remainder[0] < 8;
    }

    @Override
    @Deprecated
    protected int handleGetMonthLength(int extendedYear, int month) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            extendedYear += PersianCalendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        return MONTH_COUNT[month][PersianCalendar.isLeapYear(extendedYear) ? 1 : 0];
    }

    @Override
    @Deprecated
    protected int handleGetYearLength(int extendedYear) {
        return PersianCalendar.isLeapYear(extendedYear) ? 366 : 365;
    }

    @Override
    @Deprecated
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += PersianCalendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        long julianDay = 1948319L + PersianCalendar.firstJulianOfYear(eyear);
        if (month != 0) {
            julianDay += (long)MONTH_COUNT[month][2];
        }
        return (int)julianDay;
    }

    @Override
    @Deprecated
    protected int handleGetExtendedYear() {
        int year = this.newerField(19, 1) == 19 ? this.internalGet(19, 1) : this.internalGet(1, 1);
        return year;
    }

    private static long firstJulianOfYear(int year) {
        long julianDay = 365L * ((long)year - 1L) + PersianCalendar.floorDivide(8L * (long)year + 21L, 33L);
        if (LEAP_CORRECTION.contains(year - 1)) {
            --julianDay;
        }
        return julianDay;
    }

    @Override
    @Deprecated
    protected void handleComputeFields(int julianDay) {
        long daysSinceEpoch = julianDay - 1948320;
        int year = 1 + (int)PersianCalendar.floorDivide(33L * daysSinceEpoch + 3L, 12053L);
        long farvardin1 = PersianCalendar.firstJulianOfYear(year);
        int dayOfYear = (int)(daysSinceEpoch - farvardin1);
        if (dayOfYear == 365 && LEAP_CORRECTION.contains(year)) {
            ++year;
            dayOfYear = 0;
        }
        int month = dayOfYear < 216 ? dayOfYear / 31 : (dayOfYear - 6) / 30;
        int dayOfMonth = ++dayOfYear - MONTH_COUNT[month][2];
        this.internalSet(0, 0);
        this.internalSet(1, year);
        this.internalSet(19, year);
        this.internalSet(2, month);
        this.internalSet(23, month);
        this.internalSet(5, dayOfMonth);
        this.internalSet(6, dayOfYear);
    }

    @Override
    @Deprecated
    public String getType() {
        return "persian";
    }

    private static final class NonLeapYears {
        private static final int[] NON_LEAP_YEARS = new int[]{1502, 1601, 1634, 1667, 1700, 1733, 1766, 1799, 1832, 1865, 1898, 1931, 1964, 1997, 2030, 2059, 2063, 2096, 2129, 2158, 2162, 2191, 2195, 2224, 2228, 2257, 2261, 2290, 2294, 2323, 2327, 2356, 2360, 2389, 2393, 2422, 2426, 2455, 2459, 2488, 2492, 2521, 2525, 2554, 2558, 2587, 2591, 2620, 2624, 2653, 2657, 2686, 2690, 2719, 2723, 2748, 2752, 2756, 2781, 2785, 2789, 2818, 2822, 2847, 2851, 2855, 2880, 2884, 2888, 2913, 2917, 2921, 2946, 2950, 2954, 2979, 2983, 2987};
        private int minYear = NON_LEAP_YEARS[0];
        private int maxYear = NON_LEAP_YEARS[NON_LEAP_YEARS.length - 1];
        private BitSet offsetYears = new BitSet(this.maxYear - this.minYear + 1);

        public NonLeapYears() {
            for (int nonLeap : NON_LEAP_YEARS) {
                this.offsetYears.set(nonLeap - this.minYear);
            }
        }

        public boolean contains(int year) {
            return this.minYear <= year && year <= this.maxYear && this.offsetYears.get(year - this.minYear);
        }
    }
}

