/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.stream.Collectors;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.pattern.ArrayPatternConverter;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.internal.instant.InstantFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantNumberFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternFormatter;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Plugin(name="DatePatternConverter", category="Converter")
@ConverterKeys(value={"d", "date"})
@NullMarked
@PerformanceSensitive(value={"allocation"})
public final class DatePatternConverter
extends LogEventPatternConverter
implements ArrayPatternConverter {
    private static final String CLASS_NAME = DatePatternConverter.class.getSimpleName();
    private static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";
    private final InstantFormatter formatter;

    private DatePatternConverter(@Nullable String[] options) {
        super("Date", "date");
        this.formatter = DatePatternConverter.createFormatter(options);
    }

    private static InstantFormatter createFormatter(@Nullable String[] options) {
        try {
            return DatePatternConverter.createFormatterUnsafely(options);
        }
        catch (Exception error) {
            DatePatternConverter.logOptionReadFailure(options, error, "failed for options: {}, falling back to the default instance");
            return InstantPatternFormatter.newBuilder().setPattern(DEFAULT_PATTERN).build();
        }
    }

    private static InstantFormatter createFormatterUnsafely(@Nullable String[] options) {
        String pattern = DatePatternConverter.readPattern(options);
        TimeZone timeZone = DatePatternConverter.readTimeZone(options);
        Locale locale = DatePatternConverter.readLocale(options);
        if ("UNIX".equals(pattern)) {
            return InstantNumberFormatter.EPOCH_SECONDS_ROUNDED;
        }
        if ("UNIX_MILLIS".equals(pattern)) {
            return InstantNumberFormatter.EPOCH_MILLIS_ROUNDED;
        }
        return InstantPatternFormatter.newBuilder().setPattern(pattern).setTimeZone(timeZone).setLocale(locale).build();
    }

    private static String readPattern(@Nullable String[] options) {
        return options != null && options.length > 0 && options[0] != null ? DatePatternConverter.decodeNamedPattern(options[0]) : DEFAULT_PATTERN;
    }

    static String decodeNamedPattern(String pattern) {
        boolean compat = InstantPatternFormatter.LEGACY_FORMATTERS_ENABLED;
        switch (pattern) {
            case "ABSOLUTE": {
                return "HH:mm:ss,SSS";
            }
            case "ABSOLUTE_MICROS": {
                return "HH:mm:ss," + (compat ? "nnnnnn" : "SSSSSS");
            }
            case "ABSOLUTE_NANOS": {
                return "HH:mm:ss," + (compat ? "nnnnnnnnn" : "SSSSSSSSS");
            }
            case "ABSOLUTE_PERIOD": {
                return "HH:mm:ss.SSS";
            }
            case "COMPACT": {
                return "yyyyMMddHHmmssSSS";
            }
            case "DATE": {
                return "dd MMM yyyy HH:mm:ss,SSS";
            }
            case "DATE_PERIOD": {
                return "dd MMM yyyy HH:mm:ss.SSS";
            }
            case "DEFAULT": {
                return DEFAULT_PATTERN;
            }
            case "DEFAULT_MICROS": {
                return "yyyy-MM-dd HH:mm:ss," + (compat ? "nnnnnn" : "SSSSSS");
            }
            case "DEFAULT_NANOS": {
                return "yyyy-MM-dd HH:mm:ss," + (compat ? "nnnnnnnnn" : "SSSSSSSSS");
            }
            case "DEFAULT_PERIOD": {
                return "yyyy-MM-dd HH:mm:ss.SSS";
            }
            case "ISO8601_BASIC": {
                return "yyyyMMdd'T'HHmmss,SSS";
            }
            case "ISO8601_BASIC_PERIOD": {
                return "yyyyMMdd'T'HHmmss.SSS";
            }
            case "ISO8601": {
                return "yyyy-MM-dd'T'HH:mm:ss,SSS";
            }
            case "ISO8601_OFFSET_DATE_TIME_HH": {
                return "yyyy-MM-dd'T'HH:mm:ss,SSS" + (compat ? "X" : "x");
            }
            case "ISO8601_OFFSET_DATE_TIME_HHMM": {
                return "yyyy-MM-dd'T'HH:mm:ss,SSS" + (compat ? "XX" : "xx");
            }
            case "ISO8601_OFFSET_DATE_TIME_HHCMM": {
                return "yyyy-MM-dd'T'HH:mm:ss,SSS" + (compat ? "XXX" : "xxx");
            }
            case "ISO8601_PERIOD": {
                return "yyyy-MM-dd'T'HH:mm:ss.SSS";
            }
            case "ISO8601_PERIOD_MICROS": {
                return "yyyy-MM-dd'T'HH:mm:ss." + (compat ? "nnnnnn" : "SSSSSS");
            }
            case "US_MONTH_DAY_YEAR2_TIME": {
                return "dd/MM/yy HH:mm:ss.SSS";
            }
            case "US_MONTH_DAY_YEAR4_TIME": {
                return "dd/MM/yyyy HH:mm:ss.SSS";
            }
        }
        return pattern;
    }

    private static TimeZone readTimeZone(@Nullable String[] options) {
        try {
            if (options != null && options.length > 1 && options[1] != null) {
                return TimeZone.getTimeZone(options[1]);
            }
        }
        catch (Exception error) {
            DatePatternConverter.logOptionReadFailure(options, error, "failed to read the time zone at index 1 of options: {}, falling back to the default time zone");
        }
        return TimeZone.getDefault();
    }

    private static Locale readLocale(@Nullable String[] options) {
        try {
            if (options != null && options.length > 2 && options[2] != null) {
                return Locale.forLanguageTag(options[2]);
            }
        }
        catch (Exception error) {
            DatePatternConverter.logOptionReadFailure(options, error, "failed to read the locale at index 2 of options: {}, falling back to the default locale");
        }
        return Locale.getDefault();
    }

    private static void logOptionReadFailure(String[] options, Exception error, String message) {
        if (LOGGER.isWarnEnabled()) {
            String quotedOptions = Arrays.stream(options).map(option -> '`' + option + '`').collect(Collectors.joining(", "));
            LOGGER.warn("[{}] " + message, (Object)CLASS_NAME, (Object)quotedOptions, (Object)error);
        }
    }

    public static DatePatternConverter newInstance(String[] options) {
        return new DatePatternConverter(options);
    }

    @Deprecated
    public void format(Date date, StringBuilder buffer) {
        this.format(date.getTime(), buffer);
    }

    @Override
    public void format(LogEvent event, StringBuilder output) {
        this.format(event.getInstant(), output);
    }

    @Deprecated
    public void format(long epochMillis, StringBuilder buffer) {
        MutableInstant instant = new MutableInstant();
        instant.initFromEpochMilli(epochMillis, 0);
        this.format(instant, buffer);
    }

    @Deprecated
    public void format(Instant instant, StringBuilder buffer) {
        this.formatter.formatTo(buffer, instant);
    }

    @Override
    public void format(@Nullable Object object, StringBuilder buffer) {
        Objects.requireNonNull(buffer, "buffer");
        if (object == null) {
            return;
        }
        if (object instanceof LogEvent) {
            this.format((LogEvent)object, buffer);
        } else if (object instanceof Date) {
            this.format((Date)object, buffer);
        } else if (object instanceof Instant) {
            this.format((Instant)object, buffer);
        } else if (object instanceof Long) {
            this.format((Long)object, buffer);
        }
        LOGGER.warn("[{}]: unsupported object type `{}`", (Object)CLASS_NAME, (Object)object.getClass().getCanonicalName());
    }

    @Override
    public void format(StringBuilder buffer, Object ... objects) {
        Objects.requireNonNull(buffer, "buffer");
        if (objects != null) {
            for (Object object : objects) {
                if (!(object instanceof Date)) continue;
                this.format((Date)object, buffer);
                break;
            }
        }
    }

    public String getPattern() {
        return this.formatter instanceof InstantPatternFormatter ? ((InstantPatternFormatter)this.formatter).getPattern() : null;
    }

    public TimeZone getTimeZone() {
        return this.formatter instanceof InstantPatternFormatter ? ((InstantPatternFormatter)this.formatter).getTimeZone() : null;
    }
}

