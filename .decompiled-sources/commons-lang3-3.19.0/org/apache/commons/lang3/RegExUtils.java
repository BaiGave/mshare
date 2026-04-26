/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ObjectUtils;

public class RegExUtils {
    static final Pattern VERSION_SPLIT_PATTERN = Pattern.compile("\\.");

    public static Pattern dotAll(String regex) {
        return Pattern.compile(regex, 32);
    }

    public static Matcher dotAllMatcher(String regex, CharSequence text) {
        return RegExUtils.dotAll(regex).matcher(text);
    }

    @Deprecated
    public static Matcher dotAllMatcher(String regex, String text) {
        return RegExUtils.dotAll(regex).matcher(text);
    }

    public static String removeAll(CharSequence text, Pattern regex) {
        return RegExUtils.replaceAll(text, regex, "");
    }

    @Deprecated
    public static String removeAll(String text, Pattern regex) {
        return RegExUtils.replaceAll((CharSequence)text, regex, "");
    }

    public static String removeAll(String text, String regex) {
        return RegExUtils.replaceAll(text, regex, "");
    }

    public static String removeFirst(CharSequence text, Pattern regex) {
        return RegExUtils.replaceFirst(text, regex, "");
    }

    @Deprecated
    public static String removeFirst(String text, Pattern regex) {
        return RegExUtils.replaceFirst(text, regex, "");
    }

    public static String removeFirst(String text, String regex) {
        return RegExUtils.replaceFirst(text, regex, "");
    }

    public static String removePattern(CharSequence text, String regex) {
        return RegExUtils.replacePattern(text, regex, "");
    }

    @Deprecated
    public static String removePattern(String text, String regex) {
        return RegExUtils.replacePattern((CharSequence)text, regex, "");
    }

    public static String replaceAll(CharSequence text, Pattern regex, String replacement) {
        if (ObjectUtils.anyNull(text, regex, replacement)) {
            return RegExUtils.toStringOrNull(text);
        }
        return regex.matcher(text).replaceAll(replacement);
    }

    @Deprecated
    public static String replaceAll(String text, Pattern regex, String replacement) {
        return RegExUtils.replaceAll((CharSequence)text, regex, replacement);
    }

    public static String replaceAll(String text, String regex, String replacement) {
        if (ObjectUtils.anyNull(text, regex, replacement)) {
            return text;
        }
        return text.replaceAll(regex, replacement);
    }

    public static String replaceFirst(CharSequence text, Pattern regex, String replacement) {
        if (text == null || regex == null || replacement == null) {
            return RegExUtils.toStringOrNull(text);
        }
        return regex.matcher(text).replaceFirst(replacement);
    }

    @Deprecated
    public static String replaceFirst(String text, Pattern regex, String replacement) {
        return RegExUtils.replaceFirst((CharSequence)text, regex, replacement);
    }

    public static String replaceFirst(String text, String regex, String replacement) {
        if (text == null || regex == null || replacement == null) {
            return text;
        }
        return text.replaceFirst(regex, replacement);
    }

    public static String replacePattern(CharSequence text, String regex, String replacement) {
        if (ObjectUtils.anyNull(text, regex, replacement)) {
            return RegExUtils.toStringOrNull(text);
        }
        return RegExUtils.dotAllMatcher(regex, text).replaceAll(replacement);
    }

    @Deprecated
    public static String replacePattern(String text, String regex, String replacement) {
        return RegExUtils.replacePattern((CharSequence)text, regex, replacement);
    }

    private static String toStringOrNull(CharSequence text) {
        return Objects.toString(text, null);
    }

    @Deprecated
    public RegExUtils() {
    }
}

