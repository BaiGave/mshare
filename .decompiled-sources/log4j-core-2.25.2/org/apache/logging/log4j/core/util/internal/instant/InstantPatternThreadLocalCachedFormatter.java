/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal.instant;

import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.function.Function;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.internal.instant.InstantFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternFormatter;

final class InstantPatternThreadLocalCachedFormatter
implements InstantPatternFormatter {
    private final InstantPatternFormatter formatter;
    private final Function<Instant, Long> epochInstantExtractor;
    private final ThreadLocal<Object[]> epochInstantAndBufferRef = ThreadLocal.withInitial(InstantPatternThreadLocalCachedFormatter::createEpochInstantAndBuffer);
    private final ChronoUnit precision;

    private static Object[] createEpochInstantAndBuffer() {
        return new Object[]{-1L, new StringBuilder()};
    }

    private InstantPatternThreadLocalCachedFormatter(InstantPatternFormatter formatter, Function<Instant, Long> epochInstantExtractor, ChronoUnit precision) {
        this.formatter = formatter;
        this.epochInstantExtractor = epochInstantExtractor;
        this.precision = precision;
    }

    static InstantPatternThreadLocalCachedFormatter ofMilliPrecision(InstantPatternFormatter formatter) {
        ChronoUnit precision = InstantPatternThreadLocalCachedFormatter.effectivePrecision(formatter, ChronoUnit.MILLIS);
        return new InstantPatternThreadLocalCachedFormatter(formatter, Instant::getEpochMillisecond, precision);
    }

    static InstantPatternThreadLocalCachedFormatter ofSecondPrecision(InstantPatternFormatter formatter) {
        ChronoUnit precision = InstantPatternThreadLocalCachedFormatter.effectivePrecision(formatter, ChronoUnit.SECONDS);
        return new InstantPatternThreadLocalCachedFormatter(formatter, Instant::getEpochSecond, precision);
    }

    private static ChronoUnit effectivePrecision(InstantFormatter formatter, ChronoUnit cachePrecision) {
        ChronoUnit formatterPrecision = formatter.getPrecision();
        int comparison = cachePrecision.compareTo(formatterPrecision);
        if (comparison == 0) {
            return formatterPrecision;
        }
        if (comparison > 0) {
            String message = String.format("instant formatter `%s` is of `%s` precision, whereas the requested cache precision is `%s`", formatter, formatterPrecision, cachePrecision);
            throw new IllegalArgumentException(message);
        }
        return cachePrecision;
    }

    @Override
    public ChronoUnit getPrecision() {
        return this.precision;
    }

    @Override
    public void formatTo(StringBuilder buffer, Instant instant) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(instant, "instant");
        Object[] epochInstantAndBuffer = this.epochInstantAndBufferRef.get();
        long prevEpochInstant = (Long)epochInstantAndBuffer[0];
        StringBuilder localBuffer = (StringBuilder)epochInstantAndBuffer[1];
        long nextEpochInstant = this.epochInstantExtractor.apply(instant);
        if (prevEpochInstant != nextEpochInstant) {
            epochInstantAndBuffer[0] = nextEpochInstant;
            localBuffer.setLength(0);
            this.formatter.formatTo(localBuffer, instant);
        }
        buffer.append((CharSequence)localBuffer);
    }

    @Override
    public String getPattern() {
        return this.formatter.getPattern();
    }

    @Override
    public Locale getLocale() {
        return this.formatter.getLocale();
    }

    @Override
    public TimeZone getTimeZone() {
        return this.formatter.getTimeZone();
    }
}

