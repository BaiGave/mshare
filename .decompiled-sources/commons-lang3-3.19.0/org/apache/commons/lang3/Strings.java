/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.CharSequenceUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.AbstractSupplier;
import org.apache.commons.lang3.function.ToBooleanBiFunction;

public abstract class Strings {
    public static final Strings CI = new CiStrings(true);
    public static final Strings CS = new CsStrings(true);
    private final boolean ignoreCase;
    private final boolean nullIsLess;

    public static final Builder builder() {
        return new Builder();
    }

    private static boolean containsAny(ToBooleanBiFunction<CharSequence, CharSequence> test, CharSequence cs, CharSequence ... searchCharSequences) {
        if (StringUtils.isEmpty(cs) || ArrayUtils.isEmpty(searchCharSequences)) {
            return false;
        }
        for (CharSequence searchCharSequence : searchCharSequences) {
            if (!test.applyAsBoolean(cs, searchCharSequence)) continue;
            return true;
        }
        return false;
    }

    private static boolean eq(Object o1, Object o2) {
        return o1 == null ? o2 == null : o1.equals(o2);
    }

    private Strings(boolean ignoreCase, boolean nullIsLess) {
        this.ignoreCase = ignoreCase;
        this.nullIsLess = nullIsLess;
    }

    public String appendIfMissing(String str, CharSequence suffix, CharSequence ... suffixes) {
        if (str == null || StringUtils.isEmpty(suffix) || this.endsWith(str, suffix)) {
            return str;
        }
        if (ArrayUtils.isNotEmpty(suffixes)) {
            for (CharSequence s : suffixes) {
                if (!this.endsWith(str, s)) continue;
                return str;
            }
        }
        return str + suffix;
    }

    public abstract int compare(String var1, String var2);

    public abstract boolean contains(CharSequence var1, CharSequence var2);

    public boolean containsAny(CharSequence cs, CharSequence ... searchCharSequences) {
        return Strings.containsAny(this::contains, cs, searchCharSequences);
    }

    public boolean endsWith(CharSequence str, CharSequence suffix) {
        if (str == null || suffix == null) {
            return str == suffix;
        }
        int sufLen = suffix.length();
        if (sufLen > str.length()) {
            return false;
        }
        return CharSequenceUtils.regionMatches(str, this.ignoreCase, str.length() - sufLen, suffix, 0, sufLen);
    }

    public boolean endsWithAny(CharSequence sequence, CharSequence ... searchStrings) {
        if (StringUtils.isEmpty(sequence) || ArrayUtils.isEmpty(searchStrings)) {
            return false;
        }
        for (CharSequence searchString : searchStrings) {
            if (!this.endsWith(sequence, searchString)) continue;
            return true;
        }
        return false;
    }

    public abstract boolean equals(CharSequence var1, CharSequence var2);

    public abstract boolean equals(String var1, String var2);

    public boolean equalsAny(CharSequence string, CharSequence ... searchStrings) {
        if (ArrayUtils.isNotEmpty(searchStrings)) {
            for (CharSequence next : searchStrings) {
                if (!this.equals(string, next)) continue;
                return true;
            }
        }
        return false;
    }

    public int indexOf(CharSequence seq, CharSequence searchSeq) {
        return this.indexOf(seq, searchSeq, 0);
    }

    public abstract int indexOf(CharSequence var1, CharSequence var2, int var3);

    public boolean isCaseSensitive() {
        return !this.ignoreCase;
    }

    boolean isNullIsLess() {
        return this.nullIsLess;
    }

    public int lastIndexOf(CharSequence str, CharSequence searchStr) {
        if (str == null) {
            return -1;
        }
        return this.lastIndexOf(str, searchStr, str.length());
    }

    public abstract int lastIndexOf(CharSequence var1, CharSequence var2, int var3);

    public String prependIfMissing(String str, CharSequence prefix, CharSequence ... prefixes) {
        if (str == null || StringUtils.isEmpty(prefix) || this.startsWith(str, prefix)) {
            return str;
        }
        if (ArrayUtils.isNotEmpty(prefixes)) {
            for (CharSequence p : prefixes) {
                if (!this.startsWith(str, p)) continue;
                return str;
            }
        }
        return prefix + str;
    }

    public String remove(String str, String remove) {
        return this.replace(str, remove, "", -1);
    }

    public String removeEnd(String str, CharSequence remove) {
        if (StringUtils.isEmpty(str) || StringUtils.isEmpty(remove)) {
            return str;
        }
        if (this.endsWith(str, remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
    }

    public String removeStart(String str, CharSequence remove) {
        if (str != null && this.startsWith(str, remove)) {
            return str.substring(StringUtils.length(remove));
        }
        return str;
    }

    public String replace(String text, String searchString, String replacement) {
        return this.replace(text, searchString, replacement, -1);
    }

    public String replace(String text, String searchString, String replacement, int max) {
        int start;
        int end;
        if (StringUtils.isEmpty(text) || StringUtils.isEmpty(searchString) || replacement == null || max == 0) {
            return text;
        }
        if (this.ignoreCase) {
            searchString = searchString.toLowerCase();
        }
        if ((end = this.indexOf(text, searchString, start = 0)) == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = Math.max(replacement.length() - replLength, 0);
        StringBuilder buf = new StringBuilder(text.length() + (increase *= max < 0 ? 16 : Math.min(max, 64)));
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) break;
            end = this.indexOf(text, searchString, start);
        }
        buf.append(text, start, text.length());
        return buf.toString();
    }

    public String replaceOnce(String text, String searchString, String replacement) {
        return this.replace(text, searchString, replacement, 1);
    }

    public boolean startsWith(CharSequence str, CharSequence prefix) {
        if (str == null || prefix == null) {
            return str == prefix;
        }
        int preLen = prefix.length();
        if (preLen > str.length()) {
            return false;
        }
        return CharSequenceUtils.regionMatches(str, this.ignoreCase, 0, prefix, 0, preLen);
    }

    public boolean startsWithAny(CharSequence sequence, CharSequence ... searchStrings) {
        if (StringUtils.isEmpty(sequence) || ArrayUtils.isEmpty(searchStrings)) {
            return false;
        }
        for (CharSequence searchString : searchStrings) {
            if (!this.startsWith(sequence, searchString)) continue;
            return true;
        }
        return false;
    }

    public static class Builder
    extends AbstractSupplier<Strings, Builder, RuntimeException> {
        private boolean ignoreCase;
        private boolean nullIsLess;

        private Builder() {
        }

        @Override
        public Strings get() {
            return this.ignoreCase ? new CiStrings(this.nullIsLess) : new CsStrings(this.nullIsLess);
        }

        public Builder setIgnoreCase(boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            return (Builder)this.asThis();
        }

        public Builder setNullIsLess(boolean nullIsLess) {
            this.nullIsLess = nullIsLess;
            return (Builder)this.asThis();
        }
    }

    private static final class CiStrings
    extends Strings {
        private CiStrings(boolean nullIsLess) {
            super(true, nullIsLess);
        }

        @Override
        public int compare(String s1, String s2) {
            if (s1 == s2) {
                return 0;
            }
            if (s1 == null) {
                return this.isNullIsLess() ? -1 : 1;
            }
            if (s2 == null) {
                return this.isNullIsLess() ? 1 : -1;
            }
            return s1.compareToIgnoreCase(s2);
        }

        @Override
        public boolean contains(CharSequence str, CharSequence searchStr) {
            if (str == null || searchStr == null) {
                return false;
            }
            int len = searchStr.length();
            int max = str.length() - len;
            for (int i = 0; i <= max; ++i) {
                if (!CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, len)) continue;
                return true;
            }
            return false;
        }

        @Override
        public boolean equals(CharSequence cs1, CharSequence cs2) {
            if (cs1 == cs2) {
                return true;
            }
            if (cs1 == null || cs2 == null) {
                return false;
            }
            if (cs1.length() != cs2.length()) {
                return false;
            }
            return CharSequenceUtils.regionMatches(cs1, true, 0, cs2, 0, cs1.length());
        }

        @Override
        public boolean equals(String s1, String s2) {
            return s1 == null ? s2 == null : s1.equalsIgnoreCase(s2);
        }

        @Override
        public int indexOf(CharSequence str, CharSequence searchStr, int startPos) {
            int endLimit;
            if (str == null || searchStr == null) {
                return -1;
            }
            if (startPos < 0) {
                startPos = 0;
            }
            if (startPos > (endLimit = str.length() - searchStr.length() + 1)) {
                return -1;
            }
            if (searchStr.length() == 0) {
                return startPos;
            }
            for (int i = startPos; i < endLimit; ++i) {
                if (!CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStr.length())) continue;
                return i;
            }
            return -1;
        }

        @Override
        public int lastIndexOf(CharSequence str, CharSequence searchStr, int startPos) {
            if (str == null || searchStr == null) {
                return -1;
            }
            int searchStrLength = searchStr.length();
            int strLength = str.length();
            if (startPos > strLength - searchStrLength) {
                startPos = strLength - searchStrLength;
            }
            if (startPos < 0) {
                return -1;
            }
            if (searchStrLength == 0) {
                return startPos;
            }
            for (int i = startPos; i >= 0; --i) {
                if (!CharSequenceUtils.regionMatches(str, true, i, searchStr, 0, searchStrLength)) continue;
                return i;
            }
            return -1;
        }
    }

    private static final class CsStrings
    extends Strings {
        private CsStrings(boolean nullIsLess) {
            super(false, nullIsLess);
        }

        @Override
        public int compare(String s1, String s2) {
            if (s1 == s2) {
                return 0;
            }
            if (s1 == null) {
                return this.isNullIsLess() ? -1 : 1;
            }
            if (s2 == null) {
                return this.isNullIsLess() ? 1 : -1;
            }
            return s1.compareTo(s2);
        }

        @Override
        public boolean contains(CharSequence seq, CharSequence searchSeq) {
            return CharSequenceUtils.indexOf(seq, searchSeq, 0) >= 0;
        }

        @Override
        public boolean equals(CharSequence cs1, CharSequence cs2) {
            if (cs1 == cs2) {
                return true;
            }
            if (cs1 == null || cs2 == null) {
                return false;
            }
            if (cs1.length() != cs2.length()) {
                return false;
            }
            if (cs1 instanceof String && cs2 instanceof String) {
                return cs1.equals(cs2);
            }
            int length = cs1.length();
            for (int i = 0; i < length; ++i) {
                if (cs1.charAt(i) == cs2.charAt(i)) continue;
                return false;
            }
            return true;
        }

        @Override
        public boolean equals(String s1, String s2) {
            return Strings.eq(s1, s2);
        }

        @Override
        public int indexOf(CharSequence seq, CharSequence searchSeq, int startPos) {
            return CharSequenceUtils.indexOf(seq, searchSeq, startPos);
        }

        @Override
        public int lastIndexOf(CharSequence seq, CharSequence searchSeq, int startPos) {
            return CharSequenceUtils.lastIndexOf(seq, searchSeq, startPos);
        }
    }
}

