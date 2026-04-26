/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal.instant;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.internal.instant.InstantFormatter;
import org.apache.logging.log4j.core.util.internal.instant.InstantPatternFormatter;
import org.apache.logging.log4j.util.BiConsumer;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.Nullable;

final class InstantPatternDynamicFormatter
implements InstantPatternFormatter {
    static final ChronoUnit PRECISION_THRESHOLD = ChronoUnit.MINUTES;
    private final AtomicReference<TimestampedFormatter> timestampedFormatterRef;

    InstantPatternDynamicFormatter(String pattern, Locale locale, TimeZone timeZone) {
        TimestampedFormatter timestampedFormatter = InstantPatternDynamicFormatter.createTimestampedFormatter(pattern, locale, timeZone, null);
        this.timestampedFormatterRef = new AtomicReference<TimestampedFormatter>(timestampedFormatter);
    }

    @Override
    public String getPattern() {
        return this.timestampedFormatterRef.get().formatter.getPattern();
    }

    @Override
    public Locale getLocale() {
        return this.timestampedFormatterRef.get().formatter.getLocale();
    }

    @Override
    public TimeZone getTimeZone() {
        return this.timestampedFormatterRef.get().formatter.getTimeZone();
    }

    @Override
    public ChronoUnit getPrecision() {
        return this.timestampedFormatterRef.get().formatter.getPrecision();
    }

    @Override
    public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
        Objects.requireNonNull(buffer, "buffer");
        Objects.requireNonNull(instant, "instant");
        this.getEffectiveFormatter(instant).formatTo(buffer, instant);
    }

    private InstantPatternFormatter getEffectiveFormatter(org.apache.logging.log4j.core.time.Instant instant) {
        TimestampedFormatter oldTimestampedFormatter = this.timestampedFormatterRef.get();
        long instantEpochMinutes = InstantPatternDynamicFormatter.toEpochMinutes(instant);
        InstantPatternFormatter oldFormatter = oldTimestampedFormatter.formatter;
        if (oldTimestampedFormatter.instantEpochMinutes == instantEpochMinutes) {
            return oldFormatter;
        }
        TimestampedFormatter newTimestampedFormatter = InstantPatternDynamicFormatter.createTimestampedFormatter(oldFormatter.getPattern(), oldFormatter.getLocale(), oldFormatter.getTimeZone(), instant);
        this.timestampedFormatterRef.compareAndSet(oldTimestampedFormatter, newTimestampedFormatter);
        return newTimestampedFormatter.formatter;
    }

    private static TimestampedFormatter createTimestampedFormatter(String pattern, Locale locale, TimeZone timeZone, @Nullable org.apache.logging.log4j.core.time.Instant creationInstant) {
        if (creationInstant == null) {
            creationInstant = new MutableInstant();
            Instant currentInstant = Instant.now();
            ((MutableInstant)creationInstant).initFromEpochSecond(currentInstant.getEpochSecond(), creationInstant.getNanoOfSecond());
        }
        InstantPatternFormatter formatter = InstantPatternDynamicFormatter.createFormatter(pattern, locale, timeZone, PRECISION_THRESHOLD, creationInstant);
        long creationInstantEpochMinutes = InstantPatternDynamicFormatter.toEpochMinutes(creationInstant);
        return new TimestampedFormatter(creationInstantEpochMinutes, formatter);
    }

    private static InstantPatternFormatter createFormatter(String pattern, Locale locale, TimeZone timeZone, ChronoUnit precisionThreshold, org.apache.logging.log4j.core.time.Instant creationInstant) {
        List<PatternSequence> sequences = InstantPatternDynamicFormatter.sequencePattern(pattern, precisionThreshold);
        final InstantPatternFormatter[] formatters = (InstantPatternFormatter[])sequences.stream().map(sequence -> {
            InstantPatternFormatter formatter = sequence.createFormatter(locale, timeZone);
            boolean constant = sequence.isConstantForDurationOf(precisionThreshold);
            if (!constant) {
                return formatter;
            }
            StringBuilder buffer = new StringBuilder();
            formatter.formatTo(buffer, creationInstant);
            final String formattedInstant = buffer.toString();
            return new AbstractFormatter(formatter.getPattern(), locale, timeZone, formatter.getPrecision()){

                @Override
                public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                    buffer.append(formattedInstant);
                }
            };
        }).toArray(InstantPatternFormatter[]::new);
        switch (formatters.length) {
            case 0: {
                return new AbstractFormatter(pattern, locale, timeZone, ChronoUnit.FOREVER){

                    @Override
                    public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                    }
                };
            }
            case 1: {
                return formatters[0];
            }
            case 2: {
                final InstantPatternFormatter first = formatters[0];
                final InstantPatternFormatter second = formatters[1];
                return new AbstractFormatter(pattern, locale, timeZone, InstantPatternDynamicFormatter.min(first.getPrecision(), second.getPrecision())){

                    @Override
                    public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                        first.formatTo(buffer, instant);
                        second.formatTo(buffer, instant);
                    }
                };
            }
        }
        ChronoUnit precision = Stream.of(formatters).map(InstantFormatter::getPrecision).min(Comparator.comparing(ChronoUnit::getDuration)).get();
        return new AbstractFormatter(pattern, locale, timeZone, precision){

            @Override
            public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                for (int formatterIndex = 0; formatterIndex < formatters.length; ++formatterIndex) {
                    InstantPatternFormatter formatter = formatters[formatterIndex];
                    formatter.formatTo(buffer, instant);
                }
            }
        };
    }

    private static ChronoUnit min(ChronoUnit left, ChronoUnit right) {
        return left.getDuration().compareTo(right.getDuration()) < 0 ? left : right;
    }

    static List<PatternSequence> sequencePattern(String pattern, ChronoUnit precisionThreshold) {
        List<PatternSequence> sequences = InstantPatternDynamicFormatter.sequencePattern(pattern);
        return InstantPatternDynamicFormatter.mergeFactories(sequences, precisionThreshold);
    }

    private static List<PatternSequence> sequencePattern(String pattern) {
        if (pattern.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<PatternSequence> sequences = new ArrayList<PatternSequence>();
        int startIndex = 0;
        while (startIndex < pattern.length()) {
            char c = pattern.charAt(startIndex);
            boolean dynamic = InstantPatternDynamicFormatter.isDynamicPatternLetter(c);
            if (dynamic) {
                PatternSequence sequence;
                int endIndex;
                for (endIndex = startIndex + 1; endIndex < pattern.length() && pattern.charAt(endIndex) == c; ++endIndex) {
                }
                String sequenceContent = pattern.substring(startIndex, endIndex);
                switch (c) {
                    case 's': {
                        sequence = new SecondPatternSequence(sequenceContent.length(), "", 0);
                        break;
                    }
                    case 'S': {
                        sequence = new SecondPatternSequence(0, "", sequenceContent.length());
                        break;
                    }
                    default: {
                        sequence = new DynamicPatternSequence(sequenceContent);
                    }
                }
                sequences.add(sequence);
                startIndex = endIndex;
                continue;
            }
            if (c == '\'') {
                int endIndex = startIndex + 1;
                while (endIndex < pattern.length()) {
                    if (pattern.charAt(endIndex) == '\'') {
                        if (endIndex + 1 >= pattern.length() || pattern.charAt(endIndex + 1) != '\'') break;
                        endIndex += 2;
                        continue;
                    }
                    ++endIndex;
                }
                if (endIndex >= pattern.length()) {
                    endIndex = -1;
                }
                PatternSequence sequence = InstantPatternDynamicFormatter.getStaticPatternSequence(pattern, startIndex, endIndex);
                sequences.add(sequence);
                startIndex = endIndex + 1;
                continue;
            }
            StaticPatternSequence sequence = new StaticPatternSequence("" + c);
            sequences.add(sequence);
            ++startIndex;
        }
        return sequences;
    }

    private static PatternSequence getStaticPatternSequence(String pattern, int startIndex, int endIndex) {
        if (endIndex < 0) {
            String message = String.format("pattern ends with an incomplete string literal that started at index %d: `%s`", startIndex, pattern);
            throw new IllegalArgumentException(message);
        }
        String sequenceLiteral = startIndex + 1 == endIndex ? "'" : pattern.substring(startIndex + 1, endIndex).replace("''", "'");
        return new StaticPatternSequence(sequenceLiteral);
    }

    private static boolean isDynamicPatternLetter(char c) {
        return "GuyDMLdgQqYwWEecFaBhKkHmsSAnNVvzOXxZ".indexOf(c) >= 0;
    }

    private static List<PatternSequence> mergeFactories(List<PatternSequence> sequences, ChronoUnit precisionThreshold) {
        if (sequences.size() < 2) {
            return sequences;
        }
        ArrayList<PatternSequence> mergedSequences = new ArrayList<PatternSequence>();
        PatternSequence currentFactory = sequences.get(0);
        for (int i = 1; i < sequences.size(); ++i) {
            PatternSequence nextFactory = sequences.get(i);
            PatternSequence mergedFactory = currentFactory.tryMerge(nextFactory, precisionThreshold);
            if (mergedFactory == null) {
                mergedSequences.add(currentFactory);
                currentFactory = nextFactory;
                continue;
            }
            currentFactory = mergedFactory;
        }
        mergedSequences.add(currentFactory);
        return mergedSequences;
    }

    private static long toEpochMinutes(org.apache.logging.log4j.core.time.Instant instant) {
        return instant.getEpochSecond() / 60L;
    }

    private static TemporalAccessor toTemporalAccessor(org.apache.logging.log4j.core.time.Instant instant) {
        return instant instanceof TemporalAccessor ? (TemporalAccessor)((Object)instant) : Instant.ofEpochSecond(instant.getEpochSecond(), instant.getNanoOfSecond());
    }

    private static final class TimestampedFormatter {
        private final long instantEpochMinutes;
        private final InstantPatternFormatter formatter;

        private TimestampedFormatter(long instantEpochMinutes, InstantPatternFormatter formatter) {
            this.instantEpochMinutes = instantEpochMinutes;
            this.formatter = formatter;
        }
    }

    static class SecondPatternSequence
    extends PatternSequence {
        private static final int[] POWERS_OF_TEN = new int[]{100000000, 10000000, 1000000, 100000, 10000, 1000, 100, 10, 1};
        private final int secondDigits;
        private final String separator;
        private final int fractionalDigits;

        SecondPatternSequence(int secondDigits, String separator, int fractionalDigits) {
            super(SecondPatternSequence.createPattern(secondDigits, separator, fractionalDigits), SecondPatternSequence.determinePrecision(secondDigits, fractionalDigits));
            int maxSecondDigits = 2;
            if (secondDigits > 2) {
                String message = String.format("More than %d `s` pattern letters are not supported, found: %d", 2, secondDigits);
                throw new IllegalArgumentException(message);
            }
            int maxFractionalDigits = 9;
            if (fractionalDigits > 9) {
                String message = String.format("More than %d `S` pattern letters are not supported, found: %d", 9, fractionalDigits);
                throw new IllegalArgumentException(message);
            }
            this.secondDigits = secondDigits;
            this.separator = separator;
            this.fractionalDigits = fractionalDigits;
        }

        private static String createPattern(int secondDigits, String separator, int fractionalDigits) {
            return Strings.repeat("s", secondDigits) + StaticPatternSequence.escapeLiteral(separator) + Strings.repeat("S", fractionalDigits);
        }

        private static ChronoUnit determinePrecision(int secondDigits, int digits) {
            if (digits > 6) {
                return ChronoUnit.NANOS;
            }
            if (digits > 3) {
                return ChronoUnit.MICROS;
            }
            if (digits > 0) {
                return ChronoUnit.MILLIS;
            }
            return secondDigits > 0 ? ChronoUnit.SECONDS : ChronoUnit.FOREVER;
        }

        private static void formatUnpaddedSeconds(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
            buffer.append(instant.getEpochSecond() % 60L);
        }

        private static void formatPaddedSeconds(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
            long secondsInMinute = instant.getEpochSecond() % 60L;
            buffer.append((char)(secondsInMinute / 10L + 48L));
            buffer.append((char)(secondsInMinute % 10L + 48L));
        }

        private void formatFractionalDigits(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
            int nanos = instant.getNanoOfSecond();
            int moreDigits = 0;
            for (int idx = 0; idx < this.fractionalDigits; ++idx) {
                int digits = moreDigits;
                moreDigits = nanos / POWERS_OF_TEN[idx];
                buffer.append((char)(48 + moreDigits - 10 * digits));
            }
        }

        private static void formatMillis(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
            int ms = instant.getNanoOfSecond() / 1000000;
            int cs = ms / 10;
            int ds = cs / 10;
            buffer.append((char)(48 + ds));
            buffer.append((char)(48 + cs - 10 * ds));
            buffer.append((char)(48 + ms - 10 * cs));
        }

        @Override
        InstantPatternFormatter createFormatter(Locale locale, TimeZone timeZone) {
            BiConsumer<StringBuilder, org.apache.logging.log4j.core.time.Instant> fractionDigitsFormatter;
            final BiConsumer<StringBuilder, org.apache.logging.log4j.core.time.Instant> secondDigitsFormatter = this.secondDigits == 2 ? SecondPatternSequence::formatPaddedSeconds : SecondPatternSequence::formatUnpaddedSeconds;
            BiConsumer<StringBuilder, org.apache.logging.log4j.core.time.Instant> biConsumer = fractionDigitsFormatter = this.fractionalDigits == 3 ? SecondPatternSequence::formatMillis : this::formatFractionalDigits;
            if (this.secondDigits == 0) {
                return new AbstractFormatter(this.pattern, locale, timeZone, this.precision){

                    @Override
                    public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                        buffer.append(separator);
                        fractionDigitsFormatter.accept(buffer, instant);
                    }
                };
            }
            if (this.fractionalDigits == 0) {
                return new AbstractFormatter(this.pattern, locale, timeZone, this.precision){

                    @Override
                    public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                        secondDigitsFormatter.accept(buffer, instant);
                        buffer.append(separator);
                    }
                };
            }
            return new AbstractFormatter(this.pattern, locale, timeZone, this.precision){

                @Override
                public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                    secondDigitsFormatter.accept(buffer, instant);
                    buffer.append(separator);
                    fractionDigitsFormatter.accept(buffer, instant);
                }
            };
        }

        @Override
        @Nullable PatternSequence tryMerge(PatternSequence other, ChronoUnit thresholdPrecision) {
            if (other instanceof StaticPatternSequence) {
                StaticPatternSequence staticOther = (StaticPatternSequence)other;
                if (this.fractionalDigits == 0) {
                    return new SecondPatternSequence(this.secondDigits, this.separator + staticOther.literal, this.fractionalDigits);
                }
            }
            if (other instanceof SecondPatternSequence) {
                SecondPatternSequence secondOther = (SecondPatternSequence)other;
                if (secondOther.secondDigits == 0 && secondOther.separator.isEmpty()) {
                    return new SecondPatternSequence(this.secondDigits, this.separator, this.fractionalDigits + secondOther.fractionalDigits);
                }
            }
            return null;
        }
    }

    static final class DynamicPatternSequence
    extends PatternSequence {
        DynamicPatternSequence(String singlePattern) {
            this(singlePattern, DynamicPatternSequence.patternPrecision(singlePattern));
        }

        DynamicPatternSequence(String pattern, ChronoUnit precision) {
            super(pattern, precision);
        }

        @Override
        InstantPatternFormatter createFormatter(Locale locale, TimeZone timeZone) {
            final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(this.pattern, locale).withZone(timeZone.toZoneId());
            return new AbstractFormatter(this.pattern, locale, timeZone, this.precision){

                @Override
                public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                    TemporalAccessor instantAccessor = InstantPatternDynamicFormatter.toTemporalAccessor(instant);
                    dateTimeFormatter.formatTo(instantAccessor, buffer);
                }
            };
        }

        @Override
        @Nullable PatternSequence tryMerge(PatternSequence other, ChronoUnit thresholdPrecision) {
            if (other instanceof DynamicPatternSequence) {
                DynamicPatternSequence otherDtf = (DynamicPatternSequence)other;
                if (this.isConstantForDurationOf(thresholdPrecision) == otherDtf.isConstantForDurationOf(thresholdPrecision)) {
                    ChronoUnit precision = this.precision.getDuration().compareTo(otherDtf.precision.getDuration()) < 0 ? this.precision : otherDtf.precision;
                    return new DynamicPatternSequence(DynamicPatternSequence.mergePatterns(this.pattern, otherDtf.pattern), precision);
                }
            }
            if (other instanceof StaticPatternSequence) {
                StaticPatternSequence otherStatic = (StaticPatternSequence)other;
                return new DynamicPatternSequence(DynamicPatternSequence.mergePatterns(this.pattern, otherStatic.pattern), this.precision);
            }
            return null;
        }

        private static ChronoUnit patternPrecision(String singlePattern) {
            DynamicPatternSequence.validateContent(singlePattern);
            String paddingRemovedContent = DynamicPatternSequence.removePadding(singlePattern);
            if (paddingRemovedContent.matches("G+")) {
                return ChronoUnit.ERAS;
            }
            if (paddingRemovedContent.matches("[uyY]+")) {
                return ChronoUnit.YEARS;
            }
            if (paddingRemovedContent.matches("[MLQq]+")) {
                return ChronoUnit.MONTHS;
            }
            if (paddingRemovedContent.matches("w+")) {
                return ChronoUnit.WEEKS;
            }
            if (paddingRemovedContent.matches("[DdgEecFW]+")) {
                return ChronoUnit.DAYS;
            }
            if (paddingRemovedContent.matches("[aBhKkH]+")) {
                return ChronoUnit.HOURS;
            }
            if (paddingRemovedContent.contains("m") || paddingRemovedContent.matches("[ZxXOzVv]+")) {
                return ChronoUnit.MINUTES;
            }
            if (paddingRemovedContent.contains("s")) {
                return ChronoUnit.SECONDS;
            }
            if (paddingRemovedContent.matches("S{1,3}") || paddingRemovedContent.contains("A")) {
                return ChronoUnit.MILLIS;
            }
            if (paddingRemovedContent.matches("S{4,6}")) {
                return ChronoUnit.MICROS;
            }
            if (paddingRemovedContent.matches("S{7,9}") || paddingRemovedContent.matches("[nN]+")) {
                return ChronoUnit.NANOS;
            }
            String message = String.format("unrecognized pattern: `%s`", singlePattern);
            throw new IllegalArgumentException(message);
        }

        private static void validateContent(String content) {
            String paddingRemovedContent = DynamicPatternSequence.removePadding(content);
            if (paddingRemovedContent.isEmpty()) {
                String message = String.format("empty content: `%s`", content);
                throw new IllegalArgumentException(message);
            }
            char letter = paddingRemovedContent.charAt(0);
            boolean dynamic = InstantPatternDynamicFormatter.isDynamicPatternLetter(letter);
            if (!dynamic) {
                String message = String.format("pattern sequence doesn't start with a dynamic pattern letter: `%s`", content);
                throw new IllegalArgumentException(message);
            }
            boolean repeated = paddingRemovedContent.matches("^(\\Q" + letter + "\\E)+$");
            if (!repeated) {
                String message = String.format("was expecting letter `%c` to be repeated through the entire pattern sequence: `%s`", Character.valueOf(letter), content);
                throw new IllegalArgumentException(message);
            }
        }

        private static String removePadding(String content) {
            return content.replaceAll("^p+", "");
        }
    }

    static abstract class PatternSequence {
        final String pattern;
        final ChronoUnit precision;

        PatternSequence(String pattern, ChronoUnit precision) {
            assert (!"''".equals(pattern));
            DateTimeFormatter.ofPattern(pattern);
            this.pattern = pattern;
            this.precision = precision;
        }

        abstract InstantPatternFormatter createFormatter(Locale var1, TimeZone var2);

        abstract @Nullable PatternSequence tryMerge(PatternSequence var1, ChronoUnit var2);

        boolean isConstantForDurationOf(ChronoUnit thresholdPrecision) {
            return this.precision.compareTo(thresholdPrecision) >= 0;
        }

        static String escapeLiteral(String literal) {
            return literal.isEmpty() ? "" : "'" + literal.replace("'", "''") + "'";
        }

        static String mergePatterns(String left, String right) {
            if (left.isEmpty()) {
                return right;
            }
            if (right.isEmpty()) {
                return left;
            }
            if (left.charAt(left.length() - 1) == '\'' && right.charAt(0) == '\'') {
                return left.substring(0, left.length() - 1) + right.substring(1);
            }
            return left + right;
        }

        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null || this.getClass() != object.getClass()) {
                return false;
            }
            PatternSequence sequence = (PatternSequence)object;
            return Objects.equals(this.pattern, sequence.pattern) && this.precision == sequence.precision;
        }

        public int hashCode() {
            return Objects.hash(this.pattern, this.precision);
        }

        public String toString() {
            return this.getClass().getSimpleName() + "[pattern='" + this.pattern + '\'' + ", precision=" + this.precision + ']';
        }
    }

    static final class StaticPatternSequence
    extends PatternSequence {
        private final String literal;

        StaticPatternSequence(String literal) {
            super(StaticPatternSequence.escapeLiteral(literal), ChronoUnit.FOREVER);
            this.literal = literal;
        }

        @Override
        InstantPatternFormatter createFormatter(Locale locale, TimeZone timeZone) {
            return new AbstractFormatter(this.pattern, locale, timeZone, this.precision){

                @Override
                public void formatTo(StringBuilder buffer, org.apache.logging.log4j.core.time.Instant instant) {
                    buffer.append(literal);
                }
            };
        }

        @Override
        @Nullable PatternSequence tryMerge(PatternSequence other, ChronoUnit thresholdPrecision) {
            if (other instanceof StaticPatternSequence) {
                StaticPatternSequence otherStatic = (StaticPatternSequence)other;
                return new StaticPatternSequence(StaticPatternSequence.mergePatterns(this.literal, otherStatic.literal));
            }
            if (other instanceof DynamicPatternSequence) {
                DynamicPatternSequence otherDtf = (DynamicPatternSequence)other;
                return new DynamicPatternSequence(StaticPatternSequence.mergePatterns(this.pattern, otherDtf.pattern), otherDtf.precision);
            }
            return null;
        }
    }

    private static abstract class AbstractFormatter
    implements InstantPatternFormatter {
        private final String pattern;
        private final Locale locale;
        private final TimeZone timeZone;
        private final ChronoUnit precision;

        AbstractFormatter(String pattern, Locale locale, TimeZone timeZone, ChronoUnit precision) {
            this.pattern = pattern;
            this.locale = locale;
            this.timeZone = timeZone;
            this.precision = precision;
        }

        @Override
        public ChronoUnit getPrecision() {
            return this.precision;
        }

        @Override
        public String getPattern() {
            return this.pattern;
        }

        @Override
        public Locale getLocale() {
            return this.locale;
        }

        @Override
        public TimeZone getTimeZone() {
            return this.timeZone;
        }
    }
}

