/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.util;

import com.ibm.icu.impl.CalendarAstronomer;
import com.ibm.icu.impl.CalendarCache;
import com.ibm.icu.text.DateFormat;
import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.SimpleTimeZone;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Date;
import java.util.Locale;

public class ChineseCalendar
extends Calendar {
    private static final long serialVersionUID = 7312110751940929420L;
    private int epochYear;
    private TimeZone zoneAstro;
    private transient CalendarCache winterSolsticeCache = new CalendarCache();
    private transient CalendarCache newYearCache = new CalendarCache();
    private transient boolean hasLeapMonthBetweenWinterSolstices;
    private static final int[][] LIMITS = new int[][]{{1, 1, 83333, 83333}, {1, 1, 60, 60}, {0, 0, 11, 11}, {1, 1, 50, 55}, new int[0], {1, 1, 29, 30}, {1, 1, 353, 385}, new int[0], {-1, -1, 5, 5}, new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], new int[0], {-5000000, -5000000, 5000000, 5000000}, new int[0], {-5000000, -5000000, 5000000, 5000000}, new int[0], new int[0], {0, 0, 1, 1}, {0, 0, 11, 12}};
    static final int[][][] CHINESE_DATE_PRECEDENCE = new int[][][]{new int[][]{{5}, {3, 7}, {4, 7}, {8, 7}, {3, 18}, {4, 18}, {8, 18}, {6}, {37, 22}}, new int[][]{{3}, {4}, {8}, {40, 7}, {40, 18}}};
    private static final int CHINESE_EPOCH_YEAR = -2636;
    private static final TimeZone CHINA_ZONE = new SimpleTimeZone(28800000, "CHINA_ZONE").freeze();
    private static final int SYNODIC_GAP = 25;
    private static String[] gTemporalLeapMonthCodes = new String[]{"M01L", "M02L", "M03L", "M04L", "M05L", "M06L", "M07L", "M08L", "M09L", "M10L", "M11L", "M12L"};

    public ChineseCalendar() {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), -2636, CHINA_ZONE);
    }

    public ChineseCalendar(Date date) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), -2636, CHINA_ZONE);
        this.setTime(date);
    }

    public ChineseCalendar(int year, int month, int isLeapMonth, int date) {
        this(year, month, isLeapMonth, date, 0, 0, 0);
    }

    public ChineseCalendar(int year, int month, int isLeapMonth, int date, int hour, int minute, int second) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), -2636, CHINA_ZONE);
        this.set(14, 0);
        this.set(1, year);
        this.set(2, month);
        this.set(22, isLeapMonth);
        this.set(5, date);
        this.set(11, hour);
        this.set(12, minute);
        this.set(13, second);
    }

    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date) {
        this(era, year, month, isLeapMonth, date, 0, 0, 0);
    }

    public ChineseCalendar(int era, int year, int month, int isLeapMonth, int date, int hour, int minute, int second) {
        this(TimeZone.getDefault(), ULocale.getDefault(ULocale.Category.FORMAT), -2636, CHINA_ZONE);
        this.set(14, 0);
        this.set(0, era);
        this.set(1, year);
        this.set(2, month);
        this.set(22, isLeapMonth);
        this.set(5, date);
        this.set(11, hour);
        this.set(12, minute);
        this.set(13, second);
    }

    public ChineseCalendar(Locale aLocale) {
        this(TimeZone.forLocaleOrDefault(aLocale), ULocale.forLocale(aLocale), -2636, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone) {
        this(zone, ULocale.getDefault(ULocale.Category.FORMAT), -2636, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone, Locale aLocale) {
        this(zone, ULocale.forLocale(aLocale), -2636, CHINA_ZONE);
    }

    public ChineseCalendar(ULocale locale) {
        this(TimeZone.forULocaleOrDefault(locale), locale, -2636, CHINA_ZONE);
    }

    public ChineseCalendar(TimeZone zone, ULocale locale) {
        this(zone, locale, -2636, CHINA_ZONE);
    }

    @Deprecated
    protected ChineseCalendar(TimeZone zone, ULocale locale, int epochYear, TimeZone zoneAstroCalc) {
        super(zone, locale);
        this.epochYear = epochYear;
        this.zoneAstro = zoneAstroCalc;
        this.setTimeInMillis(System.currentTimeMillis());
    }

    @Override
    protected int handleGetLimit(int field, int limitType) {
        return LIMITS[field][limitType];
    }

    @Override
    protected int handleGetExtendedYear() {
        int year;
        if (this.newestStamp(0, 1, 0) <= this.getStamp(19)) {
            year = this.internalGet(19, 1);
        } else {
            int cycle = this.internalGet(0, 1) - 1;
            year = cycle * 60 + this.internalGet(1, 1) - (this.epochYear - -2636);
        }
        return year;
    }

    @Override
    protected int handleGetMonthLength(int extendedYear, int month) {
        int isLeapMonth = this.internalGet(22);
        return this.handleGetMonthLengthWithLeap(extendedYear, month, isLeapMonth);
    }

    private int handleGetMonthLengthWithLeap(int extendedYear, int month, int isLeap) {
        int thisStart = this.handleComputeMonthStartWithLeap(extendedYear, month, isLeap) - 2440588 + 1;
        int nextStart = this.newMoonNear(thisStart + 25, true);
        return nextStart - thisStart;
    }

    @Override
    protected DateFormat handleGetDateFormat(String pattern, String override, ULocale locale) {
        return super.handleGetDateFormat(pattern, override, locale);
    }

    @Override
    protected int[][][] getFieldResolutionTable() {
        return CHINESE_DATE_PRECEDENCE;
    }

    private void offsetMonth(int newMoon, int dom, int delta) {
        newMoon += (int)(29.530588853 * ((double)delta - 0.5));
        newMoon = this.newMoonNear(newMoon, true);
        int jd = newMoon + 2440588 - 1 + dom;
        if (dom > 29) {
            this.set(20, jd - 1);
            this.complete();
            if (this.getActualMaximum(5) >= dom) {
                this.set(20, jd);
            }
        } else {
            this.set(20, jd);
        }
    }

    @Override
    public void add(int field, int amount) {
        switch (field) {
            case 2: 
            case 23: {
                if (amount == 0) break;
                int dom = this.get(5);
                int day = this.get(20) - 2440588;
                int moon = day - dom + 1;
                this.offsetMonth(moon, dom, amount);
                break;
            }
            default: {
                super.add(field, amount);
            }
        }
    }

    @Override
    public void roll(int field, int amount) {
        switch (field) {
            case 2: 
            case 23: {
                int n;
                int newM;
                if (amount == 0) break;
                int dom = this.get(5);
                int day = this.get(20) - 2440588;
                int moon = day - dom + 1;
                int m = this.get(2);
                if (this.hasLeapMonthBetweenWinterSolstices) {
                    if (this.get(22) == 1) {
                        ++m;
                    } else {
                        int moon1 = moon - (int)(29.530588853 * ((double)m - 0.5));
                        if (this.isLeapMonthBetween(moon1 = this.newMoonNear(moon1, true), moon)) {
                            ++m;
                        }
                    }
                }
                if ((newM = (m + amount) % (n = this.hasLeapMonthBetweenWinterSolstices ? 13 : 12)) < 0) {
                    newM += n;
                }
                if (newM == m) break;
                this.offsetMonth(moon, dom, newM - m);
                break;
            }
            default: {
                super.roll(field, amount);
            }
        }
    }

    private final long daysToMillis(int days) {
        long millis = (long)days * 86400000L;
        return millis - (long)this.zoneAstro.getOffset(millis);
    }

    private final int millisToDays(long millis) {
        return (int)ChineseCalendar.floorDivide(millis + (long)this.zoneAstro.getOffset(millis), 86400000L);
    }

    private int winterSolstice(int gyear) {
        long cacheValue = this.winterSolsticeCache.get(gyear);
        if (cacheValue == CalendarCache.EMPTY) {
            long ms = this.daysToMillis(this.computeGregorianMonthStart(gyear, 11) + 1 - 2440588);
            long solarLong = new CalendarAstronomer(ms).getSunTime(CalendarAstronomer.WINTER_SOLSTICE, true);
            cacheValue = this.millisToDays(solarLong);
            this.winterSolsticeCache.put(gyear, cacheValue);
        }
        return (int)cacheValue;
    }

    private int newMoonNear(int days, boolean after) {
        long newMoon = new CalendarAstronomer(this.daysToMillis(days)).getMoonTime(CalendarAstronomer.NEW_MOON, after);
        return this.millisToDays(newMoon);
    }

    private int synodicMonthsBetween(int day1, int day2) {
        return (int)Math.round((double)(day2 - day1) / 29.530588853);
    }

    private int majorSolarTerm(int days) {
        int term = ((int)Math.floor(6.0 * new CalendarAstronomer(this.daysToMillis(days)).getSunLongitude() / Math.PI) + 2) % 12;
        if (term < 1) {
            term += 12;
        }
        return term;
    }

    private boolean hasNoMajorSolarTerm(int newMoon) {
        int nmn;
        int mstt;
        int mst = this.majorSolarTerm(newMoon);
        return mst == (mstt = this.majorSolarTerm(nmn = this.newMoonNear(newMoon + 25, true)));
    }

    private boolean isLeapMonthBetween(int newMoon1, int newMoon2) {
        if (this.synodicMonthsBetween(newMoon1, newMoon2) >= 50) {
            throw new IllegalArgumentException("isLeapMonthBetween(" + newMoon1 + ", " + newMoon2 + "): Invalid parameters");
        }
        return newMoon2 >= newMoon1 && (this.isLeapMonthBetween(newMoon1, this.newMoonNear(newMoon2 - 25, false)) || this.hasNoMajorSolarTerm(newMoon2));
    }

    @Override
    protected void handleComputeFields(int julianDay) {
        this.computeChineseFields(julianDay - 2440588, this.getGregorianYear(), this.getGregorianMonth(), true);
    }

    private void computeChineseFields(int days, int gyear, int gmonth, boolean setAllFields) {
        int ordinalMonth;
        int solsticeBefore;
        int solsticeAfter = this.winterSolstice(gyear);
        if (days < solsticeAfter) {
            solsticeBefore = this.winterSolstice(gyear - 1);
        } else {
            solsticeBefore = solsticeAfter;
            solsticeAfter = this.winterSolstice(gyear + 1);
        }
        int firstMoon = this.newMoonNear(solsticeBefore + 1, true);
        int lastMoon = this.newMoonNear(solsticeAfter + 1, false);
        int thisMoon = this.newMoonNear(days + 1, false);
        this.hasLeapMonthBetweenWinterSolstices = this.synodicMonthsBetween(firstMoon, lastMoon) == 12;
        int month = this.synodicMonthsBetween(firstMoon, thisMoon);
        int theNewYear = this.newYear(gyear);
        if (days < theNewYear) {
            theNewYear = this.newYear(gyear - 1);
        }
        if (this.hasLeapMonthBetweenWinterSolstices && this.isLeapMonthBetween(firstMoon, thisMoon)) {
            --month;
        }
        if (month < 1) {
            month += 12;
        }
        if ((ordinalMonth = this.synodicMonthsBetween(theNewYear, thisMoon)) < 0) {
            ordinalMonth += 12;
        }
        boolean isLeapMonth = this.hasLeapMonthBetweenWinterSolstices && this.hasNoMajorSolarTerm(thisMoon) && !this.isLeapMonthBetween(firstMoon, this.newMoonNear(thisMoon - 25, false));
        this.internalSet(2, month - 1);
        this.internalSet(23, ordinalMonth);
        this.internalSet(22, isLeapMonth ? 1 : 0);
        if (setAllFields) {
            int extended_year = gyear - this.epochYear;
            int cycle_year = gyear - -2636;
            if (month < 11 || gmonth >= 6) {
                ++extended_year;
                ++cycle_year;
            }
            int dayOfMonth = days - thisMoon + 1;
            this.internalSet(19, extended_year);
            int[] yearOfCycle = new int[1];
            int cycle = ChineseCalendar.floorDivide(cycle_year - 1, 60, yearOfCycle);
            this.internalSet(0, cycle + 1);
            this.internalSet(1, yearOfCycle[0] + 1);
            this.internalSet(5, dayOfMonth);
            int newYear = this.newYear(gyear);
            if (days < newYear) {
                newYear = this.newYear(gyear - 1);
            }
            this.internalSet(6, days - newYear + 1);
        }
    }

    private int newYear(int gyear) {
        long cacheValue = this.newYearCache.get(gyear);
        if (cacheValue == CalendarCache.EMPTY) {
            int solsticeBefore = this.winterSolstice(gyear - 1);
            int solsticeAfter = this.winterSolstice(gyear);
            int newMoon1 = this.newMoonNear(solsticeBefore + 1, true);
            int newMoon2 = this.newMoonNear(newMoon1 + 25, true);
            int newMoon11 = this.newMoonNear(solsticeAfter + 1, false);
            cacheValue = this.synodicMonthsBetween(newMoon1, newMoon11) == 12 && (this.hasNoMajorSolarTerm(newMoon1) || this.hasNoMajorSolarTerm(newMoon2)) ? (long)this.newMoonNear(newMoon2 + 25, true) : (long)newMoon2;
            this.newYearCache.put(gyear, cacheValue);
        }
        return (int)cacheValue;
    }

    @Override
    protected int handleComputeMonthStart(int eyear, int month, boolean useMonth) {
        int isLeapMonth = 0;
        if (useMonth) {
            isLeapMonth = this.internalGet(22);
        }
        return this.handleComputeMonthStartWithLeap(eyear, month, isLeapMonth);
    }

    private int handleComputeMonthStartWithLeap(int eyear, int month, int isLeapMonth) {
        if (month < 0 || month > 11) {
            int[] rem = new int[1];
            eyear += ChineseCalendar.floorDivide(month, 12, rem);
            month = rem[0];
        }
        int gyear = eyear + this.epochYear - 1;
        int newYear = this.newYear(gyear);
        int newMoon = this.newMoonNear(newYear + month * 29, true);
        int julianDay = newMoon + 2440588;
        int saveMonth = this.internalGet(2);
        int saveOrdinalMonth = this.internalGet(23);
        int saveIsLeapMonth = this.internalGet(22);
        this.computeGregorianFields(julianDay);
        this.computeChineseFields(newMoon, this.getGregorianYear(), this.getGregorianMonth(), false);
        if (month != this.internalGet(2) || isLeapMonth != this.internalGet(22)) {
            newMoon = this.newMoonNear(newMoon + 25, true);
            julianDay = newMoon + 2440588;
        }
        this.internalSet(2, saveMonth);
        this.internalSet(23, saveOrdinalMonth);
        this.internalSet(22, saveIsLeapMonth);
        return julianDay - 1;
    }

    @Override
    public String getType() {
        return "chinese";
    }

    @Override
    @Deprecated
    public boolean haveDefaultCentury() {
        return false;
    }

    private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        this.epochYear = -2636;
        this.zoneAstro = CHINA_ZONE;
        stream.defaultReadObject();
        this.winterSolsticeCache = new CalendarCache();
        this.newYearCache = new CalendarCache();
    }

    @Override
    public boolean inTemporalLeapYear() {
        return this.getActualMaximum(6) > 360;
    }

    @Override
    public String getTemporalMonthCode() {
        int is_leap = this.get(22);
        if (is_leap != 0) {
            return gTemporalLeapMonthCodes[this.get(2)];
        }
        return super.getTemporalMonthCode();
    }

    @Override
    public void setTemporalMonthCode(String temporalMonth) {
        if (temporalMonth.length() != 4 || temporalMonth.charAt(0) != 'M' || temporalMonth.charAt(3) != 'L') {
            this.set(22, 0);
            super.setTemporalMonthCode(temporalMonth);
            return;
        }
        for (int m = 0; m < gTemporalLeapMonthCodes.length; ++m) {
            if (!temporalMonth.equals(gTemporalLeapMonthCodes[m])) continue;
            this.set(2, m);
            this.set(22, 1);
            return;
        }
        throw new IllegalArgumentException("Incorrect temporal Month code: " + temporalMonth);
    }

    @Override
    @Deprecated
    protected int internalGetMonth() {
        if (this.resolveFields(MONTH_PRECEDENCE) == 2) {
            return this.internalGet(2);
        }
        Calendar temp = (Calendar)this.clone();
        temp.set(2, 0);
        temp.set(22, 0);
        temp.set(5, 1);
        temp.roll(2, this.internalGet(23));
        this.internalSet(22, temp.get(22));
        int month = temp.get(2);
        this.internalSet(2, month);
        return month;
    }

    @Override
    @Deprecated
    protected int internalGetMonth(int defaultValue) {
        if (this.resolveFields(MONTH_PRECEDENCE) == 2) {
            return this.internalGet(2, defaultValue);
        }
        return this.internalGetMonth();
    }

    @Override
    public int getActualMaximum(int field) {
        if (field == 5) {
            Calendar cal = (Calendar)this.clone();
            cal.setLenient(true);
            cal.prepareGetActual(field, false);
            int eyear = cal.get(19);
            int month = cal.get(2);
            int isLeap = cal.get(22);
            return this.handleGetMonthLengthWithLeap(eyear, month, isLeap);
        }
        return super.getActualMaximum(field);
    }
}

