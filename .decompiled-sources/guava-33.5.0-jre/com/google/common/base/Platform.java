/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.base;

import com.google.common.annotations.GwtCompatible;
import com.google.common.base.CharMatcher;
import com.google.common.base.CommonPattern;
import com.google.common.base.Enums;
import com.google.common.base.JdkPattern;
import com.google.common.base.Optional;
import com.google.common.base.PatternCompiler;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.lang.ref.WeakReference;
import java.util.Locale;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

@GwtCompatible
final class Platform {
    private static final PatternCompiler patternCompiler = Platform.loadPatternCompiler();

    private Platform() {
    }

    static CharMatcher precomputeCharMatcher(CharMatcher matcher) {
        return matcher.precomputedInternal();
    }

    static <T extends Enum<T>> Optional<T> getEnumIfPresent(Class<T> enumClass, String value) {
        WeakReference<Enum<?>> ref = Enums.getEnumConstants(enumClass).get(value);
        return ref == null ? Optional.absent() : Optional.fromNullable((Enum)enumClass.cast(ref.get()));
    }

    static String formatCompact4Digits(double value) {
        return String.format(Locale.ROOT, "%.4g", value);
    }

    static boolean stringIsNullOrEmpty(@Nullable String string) {
        return string == null || string.isEmpty();
    }

    static String nullToEmpty(@Nullable String string) {
        return string == null ? "" : string;
    }

    static @Nullable String emptyToNull(@Nullable String string) {
        return Platform.stringIsNullOrEmpty(string) ? null : string;
    }

    static String lenientFormat(@Nullable String template, Object ... args) {
        return Strings.lenientFormat(template, args);
    }

    static String stringValueOf(@Nullable Object o) {
        return String.valueOf(o);
    }

    static CommonPattern compilePattern(String pattern) {
        Preconditions.checkNotNull(pattern);
        return patternCompiler.compile(pattern);
    }

    static boolean patternCompilerIsPcreLike() {
        return patternCompiler.isPcreLike();
    }

    private static PatternCompiler loadPatternCompiler() {
        return new JdkPatternCompiler();
    }

    private static final class JdkPatternCompiler
    implements PatternCompiler {
        private JdkPatternCompiler() {
        }

        @Override
        public CommonPattern compile(String pattern) {
            return new JdkPattern(Pattern.compile(pattern));
        }

        @Override
        public boolean isPcreLike() {
            return true;
        }
    }
}

