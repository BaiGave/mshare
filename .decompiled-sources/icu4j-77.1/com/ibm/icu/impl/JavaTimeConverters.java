/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl;

import com.ibm.icu.util.Calendar;
import com.ibm.icu.util.GregorianCalendar;
import com.ibm.icu.util.SimpleTimeZone;
import com.ibm.icu.util.TimeZone;
import com.ibm.icu.util.ULocale;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.temporal.ChronoField;
import java.time.temporal.Temporal;
import java.util.Date;

@Deprecated
public class JavaTimeConverters {
    private static final long MILLIS_PER_DAY = 86400000L;

    private JavaTimeConverters() {
    }

    @Deprecated
    public static Calendar temporalToCalendar(ZonedDateTime dateTime) {
        long epochMillis = dateTime.toEpochSecond() * 1000L + (long)dateTime.get(ChronoField.MILLI_OF_SECOND);
        TimeZone icuTimeZone = JavaTimeConverters.zoneIdToTimeZone(dateTime.getZone());
        return JavaTimeConverters.millisToCalendar(epochMillis, icuTimeZone);
    }

    @Deprecated
    public static Calendar temporalToCalendar(OffsetTime time) {
        return JavaTimeConverters.temporalToCalendar(time.atDate(LocalDate.now()));
    }

    @Deprecated
    public static Calendar temporalToCalendar(OffsetDateTime dateTime) {
        long epochMillis = dateTime.toEpochSecond() * 1000L + (long)dateTime.get(ChronoField.MILLI_OF_SECOND);
        TimeZone icuTimeZone = JavaTimeConverters.zoneOffsetToTimeZone(dateTime.getOffset());
        return JavaTimeConverters.millisToCalendar(epochMillis, icuTimeZone);
    }

    @Deprecated
    static Calendar temporalToCalendar(ChronoLocalDate date) {
        long epochMillis = date.toEpochDay() * 86400000L;
        return JavaTimeConverters.millisToCalendar(epochMillis);
    }

    @Deprecated
    public static Calendar temporalToCalendar(LocalTime time) {
        long epochMillis = time.toNanoOfDay() / 1000000L;
        return JavaTimeConverters.millisToCalendar(epochMillis);
    }

    @Deprecated
    public static Calendar temporalToCalendar(LocalDateTime dateTime) {
        ZoneOffset zoneOffset = ZoneId.systemDefault().getRules().getOffset(dateTime);
        long epochMillis = dateTime.toEpochSecond(zoneOffset) * 1000L + (long)dateTime.get(ChronoField.MILLI_OF_SECOND);
        return JavaTimeConverters.millisToCalendar(epochMillis, TimeZone.getDefault());
    }

    @Deprecated
    public static Calendar temporalToCalendar(Temporal temp) {
        if (temp instanceof Instant) {
            throw new IllegalArgumentException("java.time.Instant cannot be formatted, it does not have enough information");
        }
        if (temp instanceof ZonedDateTime) {
            return JavaTimeConverters.temporalToCalendar((ZonedDateTime)temp);
        }
        if (temp instanceof OffsetDateTime) {
            return JavaTimeConverters.temporalToCalendar((OffsetDateTime)temp);
        }
        if (temp instanceof OffsetTime) {
            return JavaTimeConverters.temporalToCalendar((OffsetTime)temp);
        }
        if (temp instanceof LocalDate) {
            return JavaTimeConverters.temporalToCalendar((LocalDate)temp);
        }
        if (temp instanceof LocalDateTime) {
            return JavaTimeConverters.temporalToCalendar((LocalDateTime)temp);
        }
        if (temp instanceof LocalTime) {
            return JavaTimeConverters.temporalToCalendar((LocalTime)temp);
        }
        if (temp instanceof ChronoLocalDate) {
            return JavaTimeConverters.temporalToCalendar((ChronoLocalDate)temp);
        }
        if (temp instanceof ChronoLocalDateTime) {
            return JavaTimeConverters.temporalToCalendar((ChronoLocalDateTime)temp);
        }
        throw new IllegalArgumentException("This type cannot be formatted: " + temp.getClass().getName());
    }

    @Deprecated
    public static TimeZone zoneIdToTimeZone(ZoneId zoneId) {
        return TimeZone.getTimeZone(zoneId.getId());
    }

    @Deprecated
    public static TimeZone zoneOffsetToTimeZone(ZoneOffset zoneOffset) {
        return new SimpleTimeZone(zoneOffset.getTotalSeconds() * 1000, zoneOffset.getId());
    }

    private static Calendar millisToCalendar(long epochMillis) {
        return JavaTimeConverters.millisToCalendar(epochMillis, TimeZone.GMT_ZONE);
    }

    private static Calendar millisToCalendar(long epochMillis, TimeZone timeZone) {
        GregorianCalendar calendar = new GregorianCalendar(timeZone, ULocale.US);
        calendar.setGregorianChange(new Date(Long.MIN_VALUE));
        calendar.setTimeInMillis(epochMillis);
        return calendar;
    }
}

