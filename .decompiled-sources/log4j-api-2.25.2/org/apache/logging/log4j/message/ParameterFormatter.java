/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.Serializable;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Set;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.StringBuilders;

final class ParameterFormatter {
    static final String RECURSION_PREFIX = "[...";
    static final String RECURSION_SUFFIX = "...]";
    static final String ERROR_PREFIX = "[!!!";
    static final String ERROR_SEPARATOR = "=>";
    static final String ERROR_MSG_SEPARATOR = ":";
    static final String ERROR_SUFFIX = "!!!]";
    private static final char DELIM_START = '{';
    private static final char DELIM_STOP = '}';
    private static final char ESCAPE_CHAR = '\\';
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ").withZone(ZoneId.systemDefault());
    private static final Logger STATUS_LOGGER = StatusLogger.getLogger();

    private ParameterFormatter() {
    }

    static MessagePatternAnalysis analyzePattern(String pattern, int argCount) {
        MessagePatternAnalysis analysis = new MessagePatternAnalysis();
        ParameterFormatter.analyzePattern(pattern, argCount, analysis);
        return analysis;
    }

    static void analyzePattern(String pattern, int argCount, MessagePatternAnalysis analysis) {
        int l;
        if (pattern == null || (l = pattern.length()) < 2) {
            analysis.placeholderCount = 0;
            return;
        }
        boolean escaped = false;
        analysis.placeholderCount = 0;
        analysis.escapedCharFound = false;
        for (int i = 0; i < l - 1; ++i) {
            char c = pattern.charAt(i);
            if (c == '\\') {
                analysis.escapedCharFound = true;
                escaped = !escaped;
                continue;
            }
            if (escaped) {
                escaped = false;
                continue;
            }
            if (c != '{' || pattern.charAt(i + 1) != '}') continue;
            if (argCount < 0 || analysis.placeholderCount < argCount) {
                analysis.ensurePlaceholderCharIndicesCapacity(argCount);
                analysis.placeholderCharIndices[analysis.placeholderCount++] = i++;
                continue;
            }
            ++analysis.placeholderCount;
            ++i;
        }
    }

    static String format(String pattern, Object[] args, int argCount) {
        StringBuilder result = new StringBuilder();
        MessagePatternAnalysis analysis = ParameterFormatter.analyzePattern(pattern, argCount);
        ParameterFormatter.formatMessage(result, pattern, args, argCount, analysis);
        return result.toString();
    }

    static void formatMessage(StringBuilder buffer, String pattern, Object[] args, int argCount, MessagePatternAnalysis analysis) {
        if (pattern == null || args == null || analysis.placeholderCount == 0) {
            buffer.append(pattern);
            return;
        }
        if (analysis.placeholderCount != argCount) {
            int noThrowableArgCount;
            int n = argCount < 1 ? 0 : (noThrowableArgCount = argCount - (args[argCount - 1] instanceof Throwable ? 1 : 0));
            if (analysis.placeholderCount != noThrowableArgCount) {
                STATUS_LOGGER.warn("found {} argument placeholders, but provided {} for pattern `{}`", (Object)analysis.placeholderCount, (Object)argCount, (Object)pattern);
            }
        }
        if (analysis.escapedCharFound) {
            ParameterFormatter.formatMessageContainingEscapes(buffer, pattern, args, argCount, analysis);
        } else {
            ParameterFormatter.formatMessageContainingNoEscapes(buffer, pattern, args, argCount, analysis);
        }
    }

    private static void formatMessageContainingNoEscapes(StringBuilder buffer, String pattern, Object[] args, int argCount, MessagePatternAnalysis analysis) {
        int precedingTextStartIndex = 0;
        int argLimit = Math.min(analysis.placeholderCount, argCount);
        for (int argIndex = 0; argIndex < argLimit; ++argIndex) {
            int placeholderCharIndex = analysis.placeholderCharIndices[argIndex];
            buffer.append(pattern, precedingTextStartIndex, placeholderCharIndex);
            ParameterFormatter.recursiveDeepToString(args[argIndex], buffer);
            precedingTextStartIndex = placeholderCharIndex + 2;
        }
        buffer.append(pattern, precedingTextStartIndex, pattern.length());
    }

    private static void formatMessageContainingEscapes(StringBuilder buffer, String pattern, Object[] args, int argCount, MessagePatternAnalysis analysis) {
        int precedingTextStartIndex = 0;
        int argLimit = Math.min(analysis.placeholderCount, argCount);
        for (int argIndex = 0; argIndex < argLimit; ++argIndex) {
            int placeholderCharIndex = analysis.placeholderCharIndices[argIndex];
            ParameterFormatter.copyMessagePatternContainingEscapes(buffer, pattern, precedingTextStartIndex, placeholderCharIndex);
            ParameterFormatter.recursiveDeepToString(args[argIndex], buffer);
            precedingTextStartIndex = placeholderCharIndex + 2;
        }
        ParameterFormatter.copyMessagePatternContainingEscapes(buffer, pattern, precedingTextStartIndex, pattern.length());
    }

    private static void copyMessagePatternContainingEscapes(StringBuilder buffer, String pattern, int startIndex, int endIndex) {
        boolean escaped = false;
        for (int i = startIndex; i < endIndex; ++i) {
            char c = pattern.charAt(i);
            if (c == '\\') {
                if (escaped) {
                    escaped = false;
                    continue;
                }
                escaped = true;
                buffer.append(c);
                continue;
            }
            if (escaped) {
                if (c == '{' && pattern.charAt(i + 1) == '}') {
                    buffer.setLength(buffer.length() - 1);
                    buffer.append("{}");
                    ++i;
                } else {
                    buffer.append(c);
                }
                escaped = false;
                continue;
            }
            buffer.append(c);
        }
    }

    static String deepToString(Object o) {
        if (o == null) {
            return null;
        }
        if (o instanceof String) {
            return (String)o;
        }
        if (o instanceof Integer) {
            return Integer.toString((Integer)o);
        }
        if (o instanceof Long) {
            return Long.toString((Long)o);
        }
        if (o instanceof Double) {
            return Double.toString((Double)o);
        }
        if (o instanceof Boolean) {
            return Boolean.toString((Boolean)o);
        }
        if (o instanceof Character) {
            return Character.toString(((Character)o).charValue());
        }
        if (o instanceof Short) {
            return Short.toString((Short)o);
        }
        if (o instanceof Float) {
            return Float.toString(((Float)o).floatValue());
        }
        if (o instanceof Byte) {
            return Byte.toString((Byte)o);
        }
        StringBuilder str = new StringBuilder();
        ParameterFormatter.recursiveDeepToString(o, str);
        return str.toString();
    }

    static void recursiveDeepToString(Object o, StringBuilder str) {
        ParameterFormatter.recursiveDeepToString(o, str, null);
    }

    private static void recursiveDeepToString(Object o, StringBuilder str, Set<Object> dejaVu) {
        if (ParameterFormatter.appendSpecialTypes(o, str)) {
            return;
        }
        if (ParameterFormatter.isMaybeRecursive(o)) {
            ParameterFormatter.appendPotentiallyRecursiveValue(o, str, dejaVu);
        } else {
            ParameterFormatter.tryObjectToString(o, str);
        }
    }

    private static boolean appendSpecialTypes(Object o, StringBuilder str) {
        return StringBuilders.appendSpecificTypes(str, o) || ParameterFormatter.appendDate(o, str);
    }

    private static boolean appendDate(Object o, StringBuilder str) {
        if (!(o instanceof Date)) {
            return false;
        }
        DATE_FORMATTER.formatTo(((Date)o).toInstant(), str);
        return true;
    }

    private static boolean isMaybeRecursive(Object o) {
        return o.getClass().isArray() || o instanceof Map || o instanceof Collection;
    }

    private static void appendPotentiallyRecursiveValue(Object o, StringBuilder str, Set<Object> dejaVu) {
        Class<?> oClass = o.getClass();
        if (oClass.isArray()) {
            ParameterFormatter.appendArray(o, str, dejaVu, oClass);
        } else if (o instanceof Map) {
            ParameterFormatter.appendMap(o, str, dejaVu);
        } else if (o instanceof Collection) {
            ParameterFormatter.appendCollection(o, str, dejaVu);
        } else {
            throw new IllegalArgumentException("was expecting a container, found " + oClass);
        }
    }

    private static void appendArray(Object o, StringBuilder str, Set<Object> dejaVu, Class<?> oClass) {
        if (oClass == byte[].class) {
            ParameterFormatter.appendArray((byte[])o, str);
        } else if (oClass == short[].class) {
            ParameterFormatter.appendArray((short[])o, str);
        } else if (oClass == int[].class) {
            ParameterFormatter.appendArray((int[])o, str);
        } else if (oClass == long[].class) {
            ParameterFormatter.appendArray((long[])o, str);
        } else if (oClass == float[].class) {
            ParameterFormatter.appendArray((float[])o, str);
        } else if (oClass == double[].class) {
            ParameterFormatter.appendArray((double[])o, str);
        } else if (oClass == boolean[].class) {
            ParameterFormatter.appendArray((boolean[])o, str);
        } else if (oClass == char[].class) {
            ParameterFormatter.appendArray((char[])o, str);
        } else {
            boolean seen;
            Set<Object> effectiveDejaVu = ParameterFormatter.getOrCreateDejaVu(dejaVu);
            boolean bl = seen = !effectiveDejaVu.add(o);
            if (seen) {
                String id = ParameterFormatter.identityToString(o);
                str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
            } else {
                Object[] oArray = (Object[])o;
                str.append('[');
                boolean first = true;
                for (Object current : oArray) {
                    if (first) {
                        first = false;
                    } else {
                        str.append(", ");
                    }
                    ParameterFormatter.recursiveDeepToString(current, str, ParameterFormatter.cloneDejaVu(effectiveDejaVu));
                }
                str.append(']');
            }
        }
    }

    private static void appendMap(Object o, StringBuilder str, Set<Object> dejaVu) {
        boolean seen;
        Set<Object> effectiveDejaVu = ParameterFormatter.getOrCreateDejaVu(dejaVu);
        boolean bl = seen = !effectiveDejaVu.add(o);
        if (seen) {
            String id = ParameterFormatter.identityToString(o);
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
        } else {
            Map oMap = (Map)o;
            str.append('{');
            boolean isFirst = true;
            for (Map.Entry entry : oMap.entrySet()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    str.append(", ");
                }
                Object key = entry.getKey();
                Object value = entry.getValue();
                ParameterFormatter.recursiveDeepToString(key, str, ParameterFormatter.cloneDejaVu(effectiveDejaVu));
                str.append('=');
                ParameterFormatter.recursiveDeepToString(value, str, ParameterFormatter.cloneDejaVu(effectiveDejaVu));
            }
            str.append('}');
        }
    }

    private static void appendCollection(Object o, StringBuilder str, Set<Object> dejaVu) {
        boolean seen;
        Set<Object> effectiveDejaVu = ParameterFormatter.getOrCreateDejaVu(dejaVu);
        boolean bl = seen = !effectiveDejaVu.add(o);
        if (seen) {
            String id = ParameterFormatter.identityToString(o);
            str.append(RECURSION_PREFIX).append(id).append(RECURSION_SUFFIX);
        } else {
            Collection oCol = (Collection)o;
            str.append('[');
            boolean isFirst = true;
            for (Object anOCol : oCol) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    str.append(", ");
                }
                ParameterFormatter.recursiveDeepToString(anOCol, str, ParameterFormatter.cloneDejaVu(effectiveDejaVu));
            }
            str.append(']');
        }
    }

    private static Set<Object> getOrCreateDejaVu(Set<Object> dejaVu) {
        return dejaVu == null ? ParameterFormatter.createDejaVu() : dejaVu;
    }

    private static Set<Object> createDejaVu() {
        return Collections.newSetFromMap(new IdentityHashMap());
    }

    private static Set<Object> cloneDejaVu(Set<Object> dejaVu) {
        Set<Object> clonedDejaVu = ParameterFormatter.createDejaVu();
        clonedDejaVu.addAll(dejaVu);
        return clonedDejaVu;
    }

    private static void tryObjectToString(Object o, StringBuilder str) {
        try {
            str.append(o.toString());
        }
        catch (Throwable t) {
            ParameterFormatter.handleErrorInObjectToString(o, str, t);
        }
    }

    private static void handleErrorInObjectToString(Object o, StringBuilder str, Throwable t) {
        str.append(ERROR_PREFIX);
        str.append(ParameterFormatter.identityToString(o));
        str.append(ERROR_SEPARATOR);
        String msg = t.getMessage();
        String className = t.getClass().getName();
        str.append(className);
        if (!className.equals(msg)) {
            str.append(ERROR_MSG_SEPARATOR);
            str.append(msg);
        }
        str.append(ERROR_SUFFIX);
    }

    static String identityToString(Object obj) {
        if (obj == null) {
            return null;
        }
        return obj.getClass().getName() + '@' + Integer.toHexString(System.identityHashCode(obj));
    }

    private static void appendArray(byte[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(short[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    static void appendArray(int[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(long[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(float[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(double[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(boolean[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    private static void appendArray(char[] a, StringBuilder str) {
        int len = a.length;
        if (len == 0) {
            str.append("[]");
            return;
        }
        str.append('[').append(a[0]);
        for (int i = 1; i < len; ++i) {
            str.append(", ").append(a[i]);
        }
        str.append(']');
    }

    static final class MessagePatternAnalysis
    implements Serializable {
        private static final long serialVersionUID = -5974082575968329887L;
        private static final int PLACEHOLDER_CHAR_INDEX_BUFFER_INITIAL_SIZE = 8;
        private static final int PLACEHOLDER_CHAR_INDEX_BUFFER_SIZE_INCREMENT = 8;
        int placeholderCount;
        int[] placeholderCharIndices;
        boolean escapedCharFound;

        MessagePatternAnalysis() {
        }

        private void ensurePlaceholderCharIndicesCapacity(int argCount) {
            if (this.placeholderCharIndices == null) {
                int length = Math.max(argCount, 8);
                this.placeholderCharIndices = new int[length];
            } else if (this.placeholderCount >= this.placeholderCharIndices.length) {
                int newLength = argCount > 0 ? argCount : Math.addExact(this.placeholderCharIndices.length, 8);
                int[] newPlaceholderCharIndices = new int[newLength];
                System.arraycopy(this.placeholderCharIndices, 0, newPlaceholderCharIndices, 0, this.placeholderCount);
                this.placeholderCharIndices = newPlaceholderCharIndices;
            }
        }
    }
}

