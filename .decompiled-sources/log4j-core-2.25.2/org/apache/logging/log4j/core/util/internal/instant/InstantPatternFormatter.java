/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal.instant;

import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.internal.instant.InstantFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternDynamicFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternLegacyFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternThreadLocalCachedFormatter;
import org.apache.logging.log4j.util.PropertiesUtil;
import org.apache.logging.log4j.util.Strings;

public interface InstantPatternFormatter
extends InstantFormatter {
    public static final boolean LEGACY_FORMATTERS_ENABLED = "legacy".equalsIgnoreCase(PropertiesUtil.getProperties().getStringProperty("log4j2.instantFormatter"));

    public String getPattern();

    public Locale getLocale();

    public TimeZone getTimeZone();

    public static Builder newBuilder() {
        return new Builder();
    }

    public static final class Builder {
        private String pattern;
        private Locale locale = Locale.getDefault();
        private TimeZone timeZone = TimeZone.getDefault();
        private boolean cachingEnabled = Constants.ENABLE_THREADLOCALS;
        private boolean legacyFormattersEnabled = LEGACY_FORMATTERS_ENABLED;

        private Builder() {
        }

        public String getPattern() {
            return this.pattern;
        }

        public Builder setPattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Locale getLocale() {
            return this.locale;
        }

        public Builder setLocale(Locale locale) {
            this.locale = locale;
            return this;
        }

        public TimeZone getTimeZone() {
            return this.timeZone;
        }

        public Builder setTimeZone(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
        }

        public boolean isCachingEnabled() {
            return this.cachingEnabled;
        }

        public Builder setCachingEnabled(boolean cachingEnabled) {
            this.cachingEnabled = cachingEnabled;
            return this;
        }

        public boolean isLegacyFormattersEnabled() {
            return this.legacyFormattersEnabled;
        }

        public Builder setLegacyFormattersEnabled(boolean legacyFormattersEnabled) {
            this.legacyFormattersEnabled = legacyFormattersEnabled;
            return this;
        }

        public InstantPatternFormatter build() {
            Objects.requireNonNull(this.locale, "locale");
            Objects.requireNonNull(this.timeZone, "timeZone");
            if (Strings.isBlank(this.pattern)) {
                return Builder.createLiteralFormatter(this.pattern, this.locale, this.timeZone);
            }
            if (this.legacyFormattersEnabled) {
                return new InstantPatternLegacyFormatter(this.pattern, this.locale, this.timeZone);
            }
            InstantPatternDynamicFormatter formatter = new InstantPatternDynamicFormatter(this.pattern, this.locale, this.timeZone);
            if (!this.cachingEnabled) {
                return formatter;
            }
            switch (formatter.getPrecision()) {
                case NANOS: 
                case MICROS: {
                    return formatter;
                }
                case MILLIS: {
                    return InstantPatternThreadLocalCachedFormatter.ofMilliPrecision(formatter);
                }
            }
            return InstantPatternThreadLocalCachedFormatter.ofSecondPrecision(formatter);
        }

        private static InstantPatternFormatter createLiteralFormatter(final String literal, final Locale locale, final TimeZone timeZone) {
            return new InstantPatternFormatter(){

                @Override
                public String getPattern() {
                    return literal;
                }

                @Override
                public Locale getLocale() {
                    return locale;
                }

                @Override
                public TimeZone getTimeZone() {
                    return timeZone;
                }

                @Override
                public ChronoUnit getPrecision() {
                    return ChronoUnit.FOREVER;
                }

                @Override
                public void formatTo(StringBuilder buffer, Instant instant) {
                    buffer.append(literal);
                }
            };
        }
    }
}

