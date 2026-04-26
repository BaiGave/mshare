/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.message2.Directionality;
import com.ibm.icu.util.ULocale;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class OptUtils {
    private static final Pattern RE_NUMBER_LITERAL = Pattern.compile("^-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+\\-]?[0-9]+)?$");

    private OptUtils() {
    }

    static Number asNumber(Object value) {
        if (value instanceof Number) {
            return (Number)value;
        }
        if (value instanceof CharSequence) {
            try {
                Matcher m = RE_NUMBER_LITERAL.matcher(value.toString());
                if (m.find()) {
                    return Double.parseDouble(value.toString());
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return null;
    }

    static Number asNumber(boolean reportErrors, String keyName, Object value) {
        if (value instanceof Number) {
            return (Number)value;
        }
        if (value instanceof CharSequence) {
            try {
                Matcher m = RE_NUMBER_LITERAL.matcher(value.toString());
                if (m.find()) {
                    return Double.parseDouble(value.toString());
                }
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (reportErrors) {
            throw new IllegalArgumentException("bad-operand: " + keyName + " must be numeric");
        }
        return null;
    }

    static Integer getInteger(Map<String, Object> options, boolean reportErrors, String key) {
        Object value = options.get(key);
        if (value == null) {
            return null;
        }
        Number nrValue = OptUtils.asNumber(reportErrors, key, value);
        if (nrValue != null) {
            return nrValue.intValue();
        }
        return null;
    }

    static String getString(Map<String, Object> options, String key, String defaultVal) {
        Object value = options.get(key);
        if (value instanceof CharSequence) {
            return value.toString();
        }
        return defaultVal;
    }

    static String getString(Map<String, Object> options, String key) {
        return OptUtils.getString(options, key, null);
    }

    static boolean reportErrors(Map<String, Object> options) {
        String reportErrors = OptUtils.getString(options, "icu:impl:errorPolicy");
        return "STRICT".equals(reportErrors);
    }

    static boolean reportErrors(Map<String, Object> fixedOptions, Map<String, Object> variableOptions) {
        return OptUtils.reportErrors(fixedOptions) || OptUtils.reportErrors(variableOptions);
    }

    static Locale getBestLocale(Map<String, Object> options, Locale defaultValue) {
        Locale result;
        block4: {
            result = null;
            String localeOverride = OptUtils.getString(options, "u:locale");
            if (localeOverride != null) {
                try {
                    result = Locale.forLanguageTag(localeOverride.replace('_', '-'));
                }
                catch (Exception e) {
                    if (!OptUtils.reportErrors(options)) break block4;
                    throw new IllegalArgumentException("bad-operand: u:locale must be a valid BCP 47 language tag");
                }
            }
        }
        if (result == null) {
            result = defaultValue == null ? Locale.getDefault() : defaultValue;
        }
        return result;
    }

    static Directionality getBestDirectionality(Map<String, Object> options, Locale locale) {
        Directionality result = OptUtils.getDirectionality(options);
        return result == Directionality.UNKNOWN ? Directionality.of(ULocale.forLocale(locale)) : result;
    }

    static Directionality getDirectionality(Map<String, Object> options) {
        Directionality result;
        String value = OptUtils.getString(options, "u:dir");
        if (value == null) {
            return Directionality.UNKNOWN;
        }
        switch (value) {
            case "rtl": {
                result = Directionality.RTL;
                break;
            }
            case "ltr": {
                result = Directionality.LTR;
                break;
            }
            case "auto": {
                result = Directionality.AUTO;
                break;
            }
            case "inherit": {
                result = Directionality.INHERIT;
                break;
            }
            default: {
                result = Directionality.UNKNOWN;
            }
        }
        return result;
    }

    static String getUId(Map<String, Object> options) {
        return OptUtils.getString(options, "u:id");
    }
}

