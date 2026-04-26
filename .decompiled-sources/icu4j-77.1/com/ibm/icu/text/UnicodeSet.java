/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.BMPSet;
import com.ibm.icu.impl.CharacterPropertiesImpl;
import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.impl.RuleCharacterIterator;
import com.ibm.icu.impl.SortedSetRelation;
import com.ibm.icu.impl.StringRange;
import com.ibm.icu.impl.UCaseProps;
import com.ibm.icu.impl.UCharacterProperty;
import com.ibm.icu.impl.UPropertyAliases;
import com.ibm.icu.impl.UnicodeSetStringSpan;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.lang.CharSequences;
import com.ibm.icu.lang.CharacterProperties;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.BreakIterator;
import com.ibm.icu.text.Replaceable;
import com.ibm.icu.text.SymbolTable;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeFilter;
import com.ibm.icu.text.UnicodeMatcher;
import com.ibm.icu.text.UnicodeSetIterator;
import com.ibm.icu.util.Freezable;
import com.ibm.icu.util.ICUUncheckedIOException;
import com.ibm.icu.util.OutputInt;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.VersionInfo;
import java.io.IOException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class UnicodeSet
extends UnicodeFilter
implements Iterable<String>,
Comparable<UnicodeSet>,
Freezable<UnicodeSet> {
    private static final SortedSet<String> EMPTY_STRINGS = Collections.unmodifiableSortedSet(new TreeSet());
    public static final UnicodeSet EMPTY = new UnicodeSet().freeze();
    public static final UnicodeSet ALL_CODE_POINTS = new UnicodeSet(0, 0x10FFFF).freeze();
    private static XSymbolTable XSYMBOL_TABLE = null;
    private static final int LOW = 0;
    private static final int HIGH = 0x110000;
    private static final int INITIAL_CAPACITY = 25;
    private static final int MAX_LENGTH = 0x110001;
    public static final int MIN_VALUE = 0;
    public static final int MAX_VALUE = 0x10FFFF;
    private int len;
    private int[] list;
    private int[] rangeList;
    private int[] buffer;
    SortedSet<String> strings = EMPTY_STRINGS;
    private String pat = null;
    private static final String ANY_ID = "ANY";
    private static final String ASCII_ID = "ASCII";
    private static final String ASSIGNED = "Assigned";
    private volatile BMPSet bmpSet;
    private volatile UnicodeSetStringSpan stringSpan;
    private static final int LAST0_START = 0;
    private static final int LAST1_RANGE = 1;
    private static final int LAST2_SET = 2;
    private static final int MODE0_NONE = 0;
    private static final int MODE1_INBRACKET = 1;
    private static final int MODE2_OUTBRACKET = 2;
    private static final int SETMODE0_NONE = 0;
    private static final int SETMODE1_UNICODESET = 1;
    private static final int SETMODE2_PROPERTYPAT = 2;
    private static final int SETMODE3_PREPARSED = 3;
    private static final int MAX_DEPTH = 100;
    private static final VersionInfo NO_VERSION = VersionInfo.getInstance(0, 0, 0, 0);
    public static final int IGNORE_SPACE = 1;
    @Deprecated
    public static final int CASE = 2;
    public static final int CASE_INSENSITIVE = 2;
    public static final int ADD_CASE_MAPPINGS = 4;
    public static final int SIMPLE_CASE_INSENSITIVE = 6;
    private static final int CASE_MASK = 6;

    public UnicodeSet() {
        this.list = new int[25];
        this.list[0] = 0x110000;
        this.len = 1;
    }

    public UnicodeSet(UnicodeSet other) {
        this.set(other);
    }

    public UnicodeSet(int start, int end) {
        this();
        this.add(start, end);
    }

    public UnicodeSet(int ... pairs) {
        if ((pairs.length & 1) != 0) {
            throw new IllegalArgumentException("Must have even number of integers");
        }
        this.list = new int[pairs.length + 1];
        this.len = this.list.length;
        int last = -1;
        int i = 0;
        while (i < pairs.length) {
            int start = pairs[i];
            if (last >= start) {
                throw new IllegalArgumentException("Must be monotonically increasing.");
            }
            this.list[i++] = start;
            int limit = pairs[i] + 1;
            if (start >= limit) {
                throw new IllegalArgumentException("Must be monotonically increasing.");
            }
            this.list[i++] = last = limit;
        }
        this.list[i] = 0x110000;
    }

    public UnicodeSet(String pattern) {
        this();
        this.applyPattern(pattern, null, null, 1);
    }

    public UnicodeSet(String pattern, boolean ignoreWhitespace) {
        this();
        this.applyPattern(pattern, null, null, ignoreWhitespace ? 1 : 0);
    }

    public UnicodeSet(String pattern, int options) {
        this();
        this.applyPattern(pattern, null, null, options);
    }

    public UnicodeSet(String pattern, ParsePosition pos, SymbolTable symbols) {
        this();
        this.applyPattern(pattern, pos, symbols, 1);
    }

    public UnicodeSet(String pattern, ParsePosition pos, SymbolTable symbols, int options) {
        this();
        this.applyPattern(pattern, pos, symbols, options);
    }

    public Object clone() {
        if (this.isFrozen()) {
            return this;
        }
        return new UnicodeSet(this);
    }

    public UnicodeSet set(int start, int end) {
        this.checkFrozen();
        this.clear();
        this.complement(start, end);
        return this;
    }

    public UnicodeSet set(UnicodeSet other) {
        this.checkFrozen();
        this.list = Arrays.copyOf(other.list, other.len);
        this.len = other.len;
        this.pat = other.pat;
        this.strings = other.hasStrings() ? new TreeSet<String>(other.strings) : EMPTY_STRINGS;
        return this;
    }

    public final UnicodeSet applyPattern(String pattern) {
        this.checkFrozen();
        return this.applyPattern(pattern, null, null, 1);
    }

    public UnicodeSet applyPattern(String pattern, boolean ignoreWhitespace) {
        this.checkFrozen();
        return this.applyPattern(pattern, null, null, ignoreWhitespace ? 1 : 0);
    }

    public UnicodeSet applyPattern(String pattern, int options) {
        this.checkFrozen();
        return this.applyPattern(pattern, null, null, options);
    }

    public static boolean resemblesPattern(String pattern, int pos) {
        return pos + 1 < pattern.length() && pattern.charAt(pos) == '[' || UnicodeSet.resemblesPropertyPattern(pattern, pos);
    }

    private static void appendCodePoint(Appendable app, int c) {
        assert (0 <= c && c <= 0x10FFFF);
        try {
            if (c <= 65535) {
                app.append((char)c);
            } else {
                app.append(UTF16.getLeadSurrogate(c)).append(UTF16.getTrailSurrogate(c));
            }
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static void append(Appendable app, CharSequence s) {
        try {
            app.append(s);
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static <T extends Appendable> T _appendToPat(T buf, String s, boolean escapeUnprintable) {
        int cp;
        for (int i = 0; i < s.length(); i += Character.charCount(cp)) {
            cp = s.codePointAt(i);
            UnicodeSet._appendToPat(buf, cp, escapeUnprintable);
        }
        return buf;
    }

    private static <T extends Appendable> T _appendToPat(T buf, int c, boolean escapeUnprintable) {
        try {
            if (escapeUnprintable ? Utility.isUnprintable(c) : Utility.shouldAlwaysBeEscaped(c)) {
                return Utility.escape(buf, c);
            }
            switch (c) {
                case 36: 
                case 38: 
                case 45: 
                case 58: 
                case 91: 
                case 92: 
                case 93: 
                case 94: 
                case 123: 
                case 125: {
                    buf.append('\\');
                    break;
                }
                default: {
                    if (!PatternProps.isWhiteSpace(c)) break;
                    buf.append('\\');
                }
            }
            UnicodeSet.appendCodePoint(buf, c);
            return buf;
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    private static <T extends Appendable> T _appendToPat(T result, int start, int end, boolean escapeUnprintable) {
        UnicodeSet._appendToPat(result, start, escapeUnprintable);
        if (start != end) {
            if (start + 1 != end || start == 56319) {
                try {
                    result.append('-');
                }
                catch (IOException e) {
                    throw new ICUUncheckedIOException(e);
                }
            }
            UnicodeSet._appendToPat(result, end, escapeUnprintable);
        }
        return result;
    }

    @Override
    public String toPattern(boolean escapeUnprintable) {
        if (this.pat != null && !escapeUnprintable) {
            return this.pat;
        }
        StringBuilder result = new StringBuilder();
        return this._toPattern(result, escapeUnprintable).toString();
    }

    private <T extends Appendable> T _toPattern(T result, boolean escapeUnprintable) {
        if (this.pat == null) {
            return this.appendNewPattern(result, escapeUnprintable, true);
        }
        try {
            if (!escapeUnprintable) {
                result.append(this.pat);
                return result;
            }
            boolean oddNumberOfBackslashes = false;
            int i = 0;
            while (i < this.pat.length()) {
                int c = this.pat.codePointAt(i);
                i += Character.charCount(c);
                if (Utility.isUnprintable(c)) {
                    Utility.escape(result, c);
                    oddNumberOfBackslashes = false;
                    continue;
                }
                if (!oddNumberOfBackslashes && c == 92) {
                    oddNumberOfBackslashes = true;
                    continue;
                }
                if (oddNumberOfBackslashes) {
                    result.append('\\');
                }
                UnicodeSet.appendCodePoint(result, c);
                oddNumberOfBackslashes = false;
            }
            if (oddNumberOfBackslashes) {
                result.append('\\');
            }
            return result;
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public StringBuffer _generatePattern(StringBuffer result, boolean escapeUnprintable) {
        return this._generatePattern(result, escapeUnprintable, true);
    }

    public StringBuffer _generatePattern(StringBuffer result, boolean escapeUnprintable, boolean includeStrings) {
        return this.appendNewPattern(result, escapeUnprintable, includeStrings);
    }

    private <T extends Appendable> T appendNewPattern(T result, boolean escapeUnprintable, boolean includeStrings) {
        try {
            result.append('[');
            int i = 0;
            int limit = this.len & 0xFFFFFFFE;
            if (this.len >= 4 && this.list[0] == 0 && limit == this.len && !this.hasStrings()) {
                result.append('^');
                i = 1;
                --limit;
            }
            while (i < limit) {
                int start = this.list[i];
                int end = this.list[i + 1] - 1;
                if (55296 > end || end > 56319) {
                    UnicodeSet._appendToPat(result, start, end, escapeUnprintable);
                    i += 2;
                    continue;
                }
                int firstLead = i;
                while ((i += 2) < limit && this.list[i] <= 56319) {
                }
                int firstAfterLead = i;
                while (i < limit && (start = this.list[i]) <= 57343) {
                    UnicodeSet._appendToPat(result, start, this.list[i + 1] - 1, escapeUnprintable);
                    i += 2;
                }
                for (int j = firstLead; j < firstAfterLead; j += 2) {
                    UnicodeSet._appendToPat(result, this.list[j], this.list[j + 1] - 1, escapeUnprintable);
                }
            }
            if (includeStrings && this.hasStrings()) {
                for (String s : this.strings) {
                    result.append('{');
                    UnicodeSet._appendToPat(result, s, escapeUnprintable);
                    result.append('}');
                }
            }
            result.append(']');
            return result;
        }
        catch (IOException e) {
            throw new ICUUncheckedIOException(e);
        }
    }

    public int size() {
        int n = 0;
        int count = this.getRangeCount();
        for (int i = 0; i < count; ++i) {
            n += this.getRangeEnd(i) - this.getRangeStart(i) + 1;
        }
        return n + this.strings.size();
    }

    public boolean isEmpty() {
        return this.len == 1 && !this.hasStrings();
    }

    public boolean hasStrings() {
        return !this.strings.isEmpty();
    }

    @Override
    public boolean matchesIndexValue(int v) {
        for (int i = 0; i < this.getRangeCount(); ++i) {
            int high;
            int low = this.getRangeStart(i);
            if (!((low & 0xFFFFFF00) == ((high = this.getRangeEnd(i)) & 0xFFFFFF00) ? (low & 0xFF) <= v && v <= (high & 0xFF) : (low & 0xFF) <= v || v <= (high & 0xFF))) continue;
            return true;
        }
        if (this.hasStrings()) {
            for (String s : this.strings) {
                int c;
                if (s.isEmpty() || ((c = UTF16.charAt(s, 0)) & 0xFF) != v) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public int matches(Replaceable text, int[] offset, int limit, boolean incremental) {
        if (offset[0] == limit) {
            if (this.contains(65535)) {
                return incremental ? 1 : 2;
            }
            return 0;
        }
        if (this.hasStrings()) {
            boolean forward = offset[0] < limit;
            char firstChar = text.charAt(offset[0]);
            int highWaterLength = 0;
            for (String trial : this.strings) {
                if (trial.isEmpty()) continue;
                char c = trial.charAt(forward ? 0 : trial.length() - 1);
                if (forward && c > firstChar) break;
                if (c != firstChar) continue;
                int length = UnicodeSet.matchRest(text, offset[0], limit, trial);
                if (incremental) {
                    int maxLen;
                    int n = maxLen = forward ? limit - offset[0] : offset[0] - limit;
                    if (length == maxLen) {
                        return 1;
                    }
                }
                if (length != trial.length()) continue;
                if (length > highWaterLength) {
                    highWaterLength = length;
                }
                if (!forward || length >= highWaterLength) continue;
                break;
            }
            if (highWaterLength != 0) {
                offset[0] = offset[0] + (forward ? highWaterLength : -highWaterLength);
                return 2;
            }
        }
        return super.matches(text, offset, limit, incremental);
    }

    private static int matchRest(Replaceable text, int start, int limit, String s) {
        int maxLen;
        int slen = s.length();
        if (start < limit) {
            maxLen = limit - start;
            if (maxLen > slen) {
                maxLen = slen;
            }
            for (int i = 1; i < maxLen; ++i) {
                if (text.charAt(start + i) == s.charAt(i)) continue;
                return 0;
            }
        } else {
            maxLen = start - limit;
            if (maxLen > slen) {
                maxLen = slen;
            }
            --slen;
            for (int i = 1; i < maxLen; ++i) {
                if (text.charAt(start - i) == s.charAt(slen - i)) continue;
                return 0;
            }
        }
        return maxLen;
    }

    @Deprecated
    public int matchesAt(CharSequence text, int offset) {
        int cp;
        int lastLen;
        block4: {
            lastLen = -1;
            if (this.hasStrings()) {
                int tempLen;
                char firstChar = text.charAt(offset);
                String trial = null;
                Iterator it = this.strings.iterator();
                while (it.hasNext()) {
                    trial = (String)it.next();
                    char firstStringChar = trial.charAt(0);
                    if (firstStringChar < firstChar || firstStringChar <= firstChar) continue;
                    break block4;
                }
                while (lastLen <= (tempLen = UnicodeSet.matchesAt(text, offset, trial))) {
                    lastLen = tempLen;
                    if (!it.hasNext()) break;
                    trial = (String)it.next();
                }
            }
        }
        if (lastLen < 2 && this.contains(cp = UTF16.charAt(text, offset))) {
            lastLen = UTF16.getCharCount(cp);
        }
        return offset + lastLen;
    }

    private static int matchesAt(CharSequence text, int offsetInText, CharSequence substring) {
        int len = substring.length();
        int textLength = text.length();
        if (textLength + offsetInText > len) {
            return -1;
        }
        int i = 0;
        int j = offsetInText;
        while (i < len) {
            char tc;
            char pc = substring.charAt(i);
            if (pc != (tc = text.charAt(j))) {
                return -1;
            }
            ++i;
            ++j;
        }
        return i;
    }

    @Override
    public void addMatchSetTo(UnicodeSet toUnionTo) {
        toUnionTo.addAll(this);
    }

    public int indexOf(int c) {
        if (c < 0 || c > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        int i = 0;
        int n = 0;
        int start;
        while (c >= (start = this.list[i++])) {
            int limit;
            if (c < (limit = this.list[i++])) {
                return n + c - start;
            }
            n += limit - start;
        }
        return -1;
    }

    public int charAt(int index) {
        if (index >= 0) {
            int len2 = this.len & 0xFFFFFFFE;
            int i = 0;
            while (i < len2) {
                int start;
                int count;
                if (index < (count = this.list[i++] - (start = this.list[i++]))) {
                    return start + index;
                }
                index -= count;
            }
        }
        return -1;
    }

    public UnicodeSet add(int start, int end) {
        this.checkFrozen();
        return this.add_unchecked(start, end);
    }

    public UnicodeSet addAll(int start, int end) {
        this.checkFrozen();
        return this.add_unchecked(start, end);
    }

    private UnicodeSet add_unchecked(int start, int end) {
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start < end) {
            int limit = end + 1;
            if ((this.len & 1) != 0) {
                int lastLimit;
                int n = lastLimit = this.len == 1 ? -2 : this.list[this.len - 2];
                if (lastLimit <= start) {
                    this.checkFrozen();
                    if (lastLimit == start) {
                        this.list[this.len - 2] = limit;
                        if (limit == 0x110000) {
                            --this.len;
                        }
                    } else {
                        this.list[this.len - 1] = start;
                        if (limit < 0x110000) {
                            this.ensureCapacity(this.len + 2);
                            this.list[this.len++] = limit;
                            this.list[this.len++] = 0x110000;
                        } else {
                            this.ensureCapacity(this.len + 1);
                            this.list[this.len++] = 0x110000;
                        }
                    }
                    this.pat = null;
                    return this;
                }
            }
            this.add(this.range(start, end), 2, 0);
        } else if (start == end) {
            this.add(start);
        }
        return this;
    }

    public final UnicodeSet add(int c) {
        this.checkFrozen();
        return this.add_unchecked(c);
    }

    private final UnicodeSet add_unchecked(int c) {
        if (c < 0 || c > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        int i = this.findCodePoint(c);
        if ((i & 1) != 0) {
            return this;
        }
        if (c == this.list[i] - 1) {
            this.list[i] = c;
            if (c == 0x10FFFF) {
                this.ensureCapacity(this.len + 1);
                this.list[this.len++] = 0x110000;
            }
            if (i > 0 && c == this.list[i - 1]) {
                System.arraycopy(this.list, i + 1, this.list, i - 1, this.len - i - 1);
                this.len -= 2;
            }
        } else if (i > 0 && c == this.list[i - 1]) {
            int n = i - 1;
            this.list[n] = this.list[n] + 1;
        } else {
            if (this.len + 2 > this.list.length) {
                int[] temp = new int[this.nextCapacity(this.len + 2)];
                if (i != 0) {
                    System.arraycopy(this.list, 0, temp, 0, i);
                }
                System.arraycopy(this.list, i, temp, i + 2, this.len - i);
                this.list = temp;
            } else {
                System.arraycopy(this.list, i, this.list, i + 2, this.len - i);
            }
            this.list[i] = c;
            this.list[i + 1] = c + 1;
            this.len += 2;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet add(CharSequence s) {
        this.checkFrozen();
        int cp = UnicodeSet.getSingleCP(s);
        if (cp < 0) {
            String str = s.toString();
            if (!this.strings.contains(str)) {
                this.addString(str);
                this.pat = null;
            }
        } else {
            this.add_unchecked(cp, cp);
        }
        return this;
    }

    private void addString(CharSequence s) {
        if (this.strings == EMPTY_STRINGS) {
            this.strings = new TreeSet<String>();
        }
        this.strings.add(s.toString());
    }

    private static int getSingleCP(CharSequence s) {
        int cp;
        if (s.length() == 1) {
            return s.charAt(0);
        }
        if (s.length() == 2 && (cp = Character.codePointAt(s, 0)) > 65535) {
            return cp;
        }
        return -1;
    }

    public final UnicodeSet addAll(CharSequence s) {
        int cp;
        this.checkFrozen();
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(s, i);
            this.add_unchecked(cp, cp);
        }
        return this;
    }

    public final UnicodeSet retainAll(CharSequence s) {
        return this.retainAll(UnicodeSet.fromAll(s));
    }

    public final UnicodeSet complementAll(CharSequence s) {
        return this.complementAll(UnicodeSet.fromAll(s));
    }

    public final UnicodeSet removeAll(CharSequence s) {
        return this.removeAll(UnicodeSet.fromAll(s));
    }

    public final UnicodeSet removeAllStrings() {
        this.checkFrozen();
        if (this.hasStrings()) {
            this.strings.clear();
            this.pat = null;
        }
        return this;
    }

    public static UnicodeSet from(CharSequence s) {
        return new UnicodeSet().add(s);
    }

    public static UnicodeSet fromAll(CharSequence s) {
        return new UnicodeSet().addAll(s);
    }

    public UnicodeSet retain(int start, int end) {
        this.checkFrozen();
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            this.retain(this.range(start, end), 2, 0);
        } else {
            this.clear();
        }
        return this;
    }

    public final UnicodeSet retain(int c) {
        return this.retain(c, c);
    }

    public final UnicodeSet retain(CharSequence cs) {
        int cp = UnicodeSet.getSingleCP(cs);
        if (cp < 0) {
            this.checkFrozen();
            String s = cs.toString();
            boolean isIn = this.strings.contains(s);
            if (isIn && this.getRangeCount() == 0 && this.size() == 1) {
                return this;
            }
            this.clear();
            if (isIn) {
                this.addString(s);
            }
            this.pat = null;
        } else {
            this.retain(cp, cp);
        }
        return this;
    }

    public UnicodeSet remove(int start, int end) {
        this.checkFrozen();
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            this.retain(this.range(start, end), 2, 2);
        }
        return this;
    }

    public final UnicodeSet remove(int c) {
        return this.remove(c, c);
    }

    public final UnicodeSet remove(CharSequence s) {
        int cp = UnicodeSet.getSingleCP(s);
        if (cp < 0) {
            this.checkFrozen();
            String str = s.toString();
            if (this.strings.contains(str)) {
                this.strings.remove(str);
                this.pat = null;
            }
        } else {
            this.remove(cp, cp);
        }
        return this;
    }

    public UnicodeSet complement(int start, int end) {
        this.checkFrozen();
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        if (start <= end) {
            this.xor(this.range(start, end), 2, 0);
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet complement(int c) {
        return this.complement(c, c);
    }

    public UnicodeSet complement() {
        this.checkFrozen();
        if (this.list[0] == 0) {
            System.arraycopy(this.list, 1, this.list, 0, this.len - 1);
            --this.len;
        } else {
            this.ensureCapacity(this.len + 1);
            System.arraycopy(this.list, 0, this.list, 1, this.len);
            this.list[0] = 0;
            ++this.len;
        }
        this.pat = null;
        return this;
    }

    public final UnicodeSet complement(CharSequence s) {
        this.checkFrozen();
        int cp = UnicodeSet.getSingleCP(s);
        if (cp < 0) {
            String s2 = s.toString();
            if (this.strings.contains(s2)) {
                this.strings.remove(s2);
            } else {
                this.addString(s2);
            }
            this.pat = null;
        } else {
            this.complement(cp, cp);
        }
        return this;
    }

    @Override
    public boolean contains(int c) {
        if (c < 0 || c > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(c, 6));
        }
        if (this.bmpSet != null) {
            return this.bmpSet.contains(c);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.contains(c);
        }
        int i = this.findCodePoint(c);
        return (i & 1) != 0;
    }

    private final int findCodePoint(int c) {
        if (c < this.list[0]) {
            return 0;
        }
        if (this.len >= 2 && c >= this.list[this.len - 2]) {
            return this.len - 1;
        }
        int lo = 0;
        int hi = this.len - 1;
        int i;
        while ((i = lo + hi >>> 1) != lo) {
            if (c < this.list[i]) {
                hi = i;
                continue;
            }
            lo = i;
        }
        return hi;
    }

    public boolean contains(int start, int end) {
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        int i = this.findCodePoint(start);
        return (i & 1) != 0 && end < this.list[i];
    }

    public final boolean contains(CharSequence s) {
        int cp = UnicodeSet.getSingleCP(s);
        if (cp < 0) {
            return this.strings.contains(s.toString());
        }
        return this.contains(cp);
    }

    public boolean containsAll(UnicodeSet b) {
        block6: {
            int[] listB = b.list;
            boolean needA = true;
            boolean needB = true;
            int aPtr = 0;
            int bPtr = 0;
            int aLen = this.len - 1;
            int bLen = b.len - 1;
            int startA = 0;
            int startB = 0;
            int limitA = 0;
            int limitB = 0;
            while (true) {
                if (needA) {
                    if (aPtr >= aLen) {
                        if (!needB || bPtr < bLen) {
                            return false;
                        }
                        break block6;
                    }
                    startA = this.list[aPtr++];
                    limitA = this.list[aPtr++];
                }
                if (needB) {
                    if (bPtr >= bLen) break block6;
                    startB = listB[bPtr++];
                    limitB = listB[bPtr++];
                }
                if (startB >= limitA) {
                    needA = true;
                    needB = false;
                    continue;
                }
                if (startB < startA || limitB > limitA) break;
                needA = false;
                needB = true;
            }
            return false;
        }
        return this.strings.containsAll(b.strings);
    }

    public boolean containsAll(String s) {
        int cp;
        for (int i = 0; i < s.length(); i += UTF16.getCharCount(cp)) {
            cp = UTF16.charAt(s, i);
            if (this.contains(cp)) continue;
            if (!this.hasStrings()) {
                return false;
            }
            return this.containsAll(s, 0);
        }
        return true;
    }

    private boolean containsAll(String s, int i) {
        if (i >= s.length()) {
            return true;
        }
        int cp = UTF16.charAt(s, i);
        if (this.contains(cp) && this.containsAll(s, i + UTF16.getCharCount(cp))) {
            return true;
        }
        for (String setStr : this.strings) {
            if (setStr.isEmpty() || !s.startsWith(setStr, i) || !this.containsAll(s, i + setStr.length())) continue;
            return true;
        }
        return false;
    }

    @Deprecated
    public String getRegexEquivalent() {
        if (!this.hasStrings()) {
            return this.toString();
        }
        StringBuilder result = new StringBuilder("(?:");
        this.appendNewPattern(result, true, false);
        for (String s : this.strings) {
            result.append('|');
            UnicodeSet._appendToPat(result, s, true);
        }
        return result.append(")").toString();
    }

    public boolean containsNone(int start, int end) {
        if (start < 0 || start > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(start, 6));
        }
        if (end < 0 || end > 0x10FFFF) {
            throw new IllegalArgumentException("Invalid code point U+" + Utility.hex(end, 6));
        }
        int i = -1;
        while (start >= this.list[++i]) {
        }
        return (i & 1) == 0 && end < this.list[i];
    }

    public boolean containsNone(UnicodeSet b) {
        block4: {
            int[] listB = b.list;
            boolean needA = true;
            boolean needB = true;
            int aPtr = 0;
            int bPtr = 0;
            int aLen = this.len - 1;
            int bLen = b.len - 1;
            int startA = 0;
            int startB = 0;
            int limitA = 0;
            int limitB = 0;
            while (true) {
                if (needA) {
                    if (aPtr >= aLen) break block4;
                    startA = this.list[aPtr++];
                    limitA = this.list[aPtr++];
                }
                if (needB) {
                    if (bPtr >= bLen) break block4;
                    startB = listB[bPtr++];
                    limitB = listB[bPtr++];
                }
                if (startB >= limitA) {
                    needA = true;
                    needB = false;
                    continue;
                }
                if (startA < limitB) break;
                needA = false;
                needB = true;
            }
            return false;
        }
        return SortedSetRelation.hasRelation(this.strings, 5, b.strings);
    }

    public boolean containsNone(CharSequence s) {
        return this.span(s, SpanCondition.NOT_CONTAINED) == s.length();
    }

    public final boolean containsSome(int start, int end) {
        return !this.containsNone(start, end);
    }

    public final boolean containsSome(UnicodeSet s) {
        return !this.containsNone(s);
    }

    public final boolean containsSome(CharSequence s) {
        return !this.containsNone(s);
    }

    public UnicodeSet addAll(UnicodeSet c) {
        this.checkFrozen();
        this.add(c.list, c.len, 0);
        if (c.hasStrings()) {
            if (this.strings == EMPTY_STRINGS) {
                this.strings = new TreeSet<String>(c.strings);
            } else {
                this.strings.addAll(c.strings);
            }
        }
        return this;
    }

    public UnicodeSet retainAll(UnicodeSet c) {
        this.checkFrozen();
        this.retain(c.list, c.len, 0);
        if (this.hasStrings()) {
            if (!c.hasStrings()) {
                this.strings.clear();
            } else {
                this.strings.retainAll(c.strings);
            }
        }
        return this;
    }

    public UnicodeSet removeAll(UnicodeSet c) {
        this.checkFrozen();
        this.retain(c.list, c.len, 2);
        if (this.hasStrings() && c.hasStrings()) {
            this.strings.removeAll(c.strings);
        }
        return this;
    }

    public UnicodeSet complementAll(UnicodeSet c) {
        this.checkFrozen();
        this.xor(c.list, c.len, 0);
        if (c.hasStrings()) {
            if (this.strings == EMPTY_STRINGS) {
                this.strings = new TreeSet<String>(c.strings);
            } else {
                SortedSetRelation.doOperation(this.strings, 5, c.strings);
            }
        }
        return this;
    }

    public UnicodeSet clear() {
        this.checkFrozen();
        this.list[0] = 0x110000;
        this.len = 1;
        this.pat = null;
        if (this.hasStrings()) {
            this.strings.clear();
        }
        return this;
    }

    public int getRangeCount() {
        return this.len / 2;
    }

    public int getRangeStart(int index) {
        return this.list[index * 2];
    }

    public int getRangeEnd(int index) {
        return this.list[index * 2 + 1] - 1;
    }

    public UnicodeSet compact() {
        this.checkFrozen();
        if (this.len + 7 < this.list.length) {
            this.list = Arrays.copyOf(this.list, this.len);
        }
        this.rangeList = null;
        this.buffer = null;
        if (this.strings != EMPTY_STRINGS && this.strings.isEmpty()) {
            this.strings = EMPTY_STRINGS;
        }
        return this;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        try {
            UnicodeSet that = (UnicodeSet)o;
            if (this.len != that.len) {
                return false;
            }
            for (int i = 0; i < this.len; ++i) {
                if (this.list[i] == that.list[i]) continue;
                return false;
            }
            if (!this.strings.equals(that.strings)) {
                return false;
            }
        }
        catch (Exception e) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        int result = this.len;
        for (int i = 0; i < this.len; ++i) {
            result *= 1000003;
            result += this.list[i];
        }
        return result;
    }

    public String toString() {
        return this.toPattern(true);
    }

    @Deprecated
    public UnicodeSet applyPattern(String pattern, ParsePosition pos, SymbolTable symbols, int options) {
        boolean parsePositionWasNull;
        boolean bl = parsePositionWasNull = pos == null;
        if (parsePositionWasNull) {
            pos = new ParsePosition(0);
        }
        StringBuilder rebuiltPat = new StringBuilder();
        RuleCharacterIterator chars = new RuleCharacterIterator(pattern, symbols, pos);
        this.applyPattern(chars, symbols, rebuiltPat, options, 0);
        if (chars.inVariable()) {
            UnicodeSet.syntaxError(chars, "Extra chars in variable value");
        }
        this.pat = rebuiltPat.toString();
        if (parsePositionWasNull) {
            int i = pos.getIndex();
            if ((options & 1) != 0) {
                i = PatternProps.skipWhiteSpace(pattern, i);
            }
            if (i != pattern.length()) {
                throw new IllegalArgumentException("Parse of \"" + pattern + "\" failed at " + i);
            }
        }
        return this;
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private void applyPattern(RuleCharacterIterator chars, SymbolTable symbols, Appendable rebuiltPat, int options, int depth) {
        if (depth > 100) {
            UnicodeSet.syntaxError(chars, "Pattern nested too deeply");
        }
        int opts = 3;
        if ((options & 1) != 0) {
            opts |= 4;
        }
        StringBuilder patBuf = new StringBuilder();
        StringBuilder buf = null;
        boolean usePat = false;
        UnicodeSet scratch = null;
        RuleCharacterIterator.Position backup = null;
        int lastItem = 0;
        int lastChar = 0;
        int mode = 0;
        char op = '\u0000';
        boolean invert = false;
        this.clear();
        String lastString = null;
        block27: while (mode != 2 && !chars.atEnd()) {
            int setMode;
            UnicodeSet nested;
            boolean literal;
            int c;
            block78: {
                UnicodeMatcher m;
                block80: {
                    block79: {
                        c = 0;
                        literal = false;
                        nested = null;
                        setMode = 0;
                        if (!UnicodeSet.resemblesPropertyPattern(chars, opts)) break block79;
                        setMode = 2;
                        break block78;
                    }
                    backup = chars.getPos(backup);
                    c = chars.next(opts);
                    literal = chars.isEscaped();
                    if (c != 91 || literal) break block80;
                    if (mode == 1) {
                        chars.setPos(backup);
                        setMode = 1;
                        break block78;
                    } else {
                        mode = 1;
                        patBuf.append('[');
                        backup = chars.getPos(backup);
                        c = chars.next(opts);
                        literal = chars.isEscaped();
                        if (c == 94 && !literal) {
                            invert = true;
                            patBuf.append('^');
                            backup = chars.getPos(backup);
                            c = chars.next(opts);
                            literal = chars.isEscaped();
                        }
                        if (c == 45) {
                            literal = true;
                            break block78;
                        } else {
                            chars.setPos(backup);
                            continue;
                        }
                    }
                }
                if (symbols != null && (m = symbols.lookupMatcher(c)) != null) {
                    try {
                        nested = (UnicodeSet)m;
                        setMode = 3;
                    }
                    catch (ClassCastException e) {
                        UnicodeSet.syntaxError(chars, "Syntax error");
                    }
                }
            }
            if (setMode != 0) {
                if (lastItem == 1) {
                    if (op != '\u0000') {
                        UnicodeSet.syntaxError(chars, "Char expected after operator");
                    }
                    this.add_unchecked(lastChar, lastChar);
                    UnicodeSet._appendToPat(patBuf, lastChar, false);
                    lastItem = 0;
                    op = '\u0000';
                }
                if (op == '-' || op == '&') {
                    patBuf.append(op);
                }
                if (nested == null) {
                    if (scratch == null) {
                        scratch = new UnicodeSet();
                    }
                    nested = scratch;
                }
                switch (setMode) {
                    case 1: {
                        nested.applyPattern(chars, symbols, patBuf, options, depth + 1);
                        break;
                    }
                    case 2: {
                        chars.skipIgnored(opts);
                        nested.applyPropertyPattern(chars, patBuf, symbols);
                        break;
                    }
                    case 3: {
                        nested._toPattern(patBuf, false);
                        break;
                    }
                }
                usePat = true;
                if (mode == 0) {
                    this.set(nested);
                    mode = 2;
                    break;
                }
                switch (op) {
                    case '-': {
                        this.removeAll(nested);
                        break;
                    }
                    case '&': {
                        this.retainAll(nested);
                        break;
                    }
                    case '\u0000': {
                        this.addAll(nested);
                        break;
                    }
                }
                op = '\u0000';
                lastItem = 2;
                continue;
            }
            if (mode == 0) {
                UnicodeSet.syntaxError(chars, "Missing '['");
            }
            if (!literal) {
                switch (c) {
                    case 93: {
                        if (lastItem == 1) {
                            this.add_unchecked(lastChar, lastChar);
                            UnicodeSet._appendToPat(patBuf, lastChar, false);
                        }
                        if (op == '-') {
                            this.add_unchecked(op, op);
                            patBuf.append(op);
                        } else if (op == '&') {
                            UnicodeSet.syntaxError(chars, "Trailing '&'");
                        }
                        patBuf.append(']');
                        mode = 2;
                        continue block27;
                    }
                    case 45: {
                        if (op == '\u0000') {
                            if (lastItem != 0) {
                                op = (char)c;
                                continue block27;
                            }
                            if (lastString != null) {
                                op = (char)c;
                                continue block27;
                            }
                            this.add_unchecked(c, c);
                            c = chars.next(opts);
                            literal = chars.isEscaped();
                            if (c == 93 && !literal) {
                                patBuf.append("-]");
                                mode = 2;
                                continue block27;
                            }
                        }
                        UnicodeSet.syntaxError(chars, "'-' not after char, string, or set");
                        break;
                    }
                    case 38: {
                        if (lastItem == 2 && op == '\u0000') {
                            op = (char)c;
                            continue block27;
                        }
                        UnicodeSet.syntaxError(chars, "'&' not after set");
                        break;
                    }
                    case 94: {
                        UnicodeSet.syntaxError(chars, "'^' not after '['");
                        break;
                    }
                    case 123: {
                        if (op != '\u0000' && op != '-') {
                            UnicodeSet.syntaxError(chars, "Missing operand after operator");
                        }
                        if (lastItem == 1) {
                            this.add_unchecked(lastChar, lastChar);
                            UnicodeSet._appendToPat(patBuf, lastChar, false);
                        }
                        lastItem = 0;
                        if (buf == null) {
                            buf = new StringBuilder();
                        } else {
                            buf.setLength(0);
                        }
                        boolean ok = false;
                        while (!chars.atEnd()) {
                            c = chars.next(opts);
                            literal = chars.isEscaped();
                            if (c == 125 && !literal) {
                                ok = true;
                                break;
                            }
                            UnicodeSet.appendCodePoint(buf, c);
                        }
                        if (!ok) {
                            UnicodeSet.syntaxError(chars, "Invalid multicharacter string");
                        }
                        String curString = buf.toString();
                        if (op == '-') {
                            int lastSingle = CharSequences.getSingleCodePoint(lastString == null ? "" : lastString);
                            int curSingle = CharSequences.getSingleCodePoint(curString);
                            if (lastSingle != Integer.MAX_VALUE && curSingle != Integer.MAX_VALUE) {
                                this.add(lastSingle, curSingle);
                            } else {
                                if (this.strings == EMPTY_STRINGS) {
                                    this.strings = new TreeSet<String>();
                                }
                                try {
                                    StringRange.expand(lastString, curString, true, this.strings);
                                }
                                catch (Exception e) {
                                    UnicodeSet.syntaxError(chars, e.getMessage());
                                }
                            }
                            lastString = null;
                            op = '\u0000';
                        } else {
                            this.add(curString);
                            lastString = curString;
                        }
                        patBuf.append('{');
                        UnicodeSet._appendToPat(patBuf, curString, false);
                        patBuf.append('}');
                        continue block27;
                    }
                    case 36: {
                        boolean anchor;
                        backup = chars.getPos(backup);
                        c = chars.next(opts);
                        literal = chars.isEscaped();
                        boolean bl = anchor = c == 93 && !literal;
                        if (symbols == null && !anchor) {
                            c = 36;
                            chars.setPos(backup);
                            break;
                        }
                        if (anchor && op == '\u0000') {
                            if (lastItem == 1) {
                                this.add_unchecked(lastChar, lastChar);
                                UnicodeSet._appendToPat(patBuf, lastChar, false);
                            }
                            this.add_unchecked(65535);
                            usePat = true;
                            patBuf.append('$').append(']');
                            mode = 2;
                            continue block27;
                        }
                        UnicodeSet.syntaxError(chars, "Unquoted '$'");
                        break;
                    }
                }
            }
            switch (lastItem) {
                case 0: {
                    if (op == '-' && lastString != null) {
                        UnicodeSet.syntaxError(chars, "Invalid range");
                    }
                    lastItem = 1;
                    lastChar = c;
                    lastString = null;
                    break;
                }
                case 1: {
                    if (op == '-') {
                        if (lastString != null) {
                            UnicodeSet.syntaxError(chars, "Invalid range");
                        }
                        if (lastChar >= c) {
                            UnicodeSet.syntaxError(chars, "Invalid range");
                        }
                        this.add_unchecked(lastChar, c);
                        UnicodeSet._appendToPat(patBuf, lastChar, false);
                        patBuf.append(op);
                        UnicodeSet._appendToPat(patBuf, c, false);
                        lastItem = 0;
                        op = '\u0000';
                        break;
                    }
                    this.add_unchecked(lastChar, lastChar);
                    UnicodeSet._appendToPat(patBuf, lastChar, false);
                    lastChar = c;
                    break;
                }
                case 2: {
                    if (op != '\u0000') {
                        UnicodeSet.syntaxError(chars, "Set expected after operator");
                    }
                    lastChar = c;
                    lastItem = 1;
                    continue block27;
                }
            }
        }
        if (mode != 2) {
            UnicodeSet.syntaxError(chars, "Missing ']'");
        }
        chars.skipIgnored(opts);
        if ((options & 6) != 0) {
            this.closeOver(options);
        }
        if (invert) {
            this.complement().removeAllStrings();
        }
        if (usePat) {
            UnicodeSet.append(rebuiltPat, patBuf.toString());
            return;
        }
        this.appendNewPattern(rebuiltPat, false, true);
    }

    private static void syntaxError(RuleCharacterIterator chars, String msg) {
        throw new IllegalArgumentException("Error: " + msg + " at \"" + Utility.escape(chars.toString()) + '\"');
    }

    public <T extends Collection<String>> T addAllTo(T target) {
        return UnicodeSet.addAllTo(this, target);
    }

    public String[] addAllTo(String[] target) {
        return UnicodeSet.addAllTo(this, target);
    }

    public static String[] toArray(UnicodeSet set) {
        return UnicodeSet.addAllTo(set, new String[set.size()]);
    }

    public UnicodeSet add(Iterable<?> source) {
        return this.addAll(source);
    }

    public UnicodeSet addAll(Iterable<?> source) {
        this.checkFrozen();
        for (Object o : source) {
            this.add(o.toString());
        }
        return this;
    }

    private int nextCapacity(int minCapacity) {
        if (minCapacity < 25) {
            return minCapacity + 25;
        }
        if (minCapacity <= 2500) {
            return 5 * minCapacity;
        }
        int newCapacity = 2 * minCapacity;
        if (newCapacity > 0x110001) {
            newCapacity = 0x110001;
        }
        return newCapacity;
    }

    private void ensureCapacity(int newLen) {
        if (newLen > 0x110001) {
            newLen = 0x110001;
        }
        if (newLen <= this.list.length) {
            return;
        }
        int newCapacity = this.nextCapacity(newLen);
        int[] temp = new int[newCapacity];
        System.arraycopy(this.list, 0, temp, 0, this.len);
        this.list = temp;
    }

    private void ensureBufferCapacity(int newLen) {
        if (newLen > 0x110001) {
            newLen = 0x110001;
        }
        if (this.buffer != null && newLen <= this.buffer.length) {
            return;
        }
        int newCapacity = this.nextCapacity(newLen);
        this.buffer = new int[newCapacity];
    }

    private int[] range(int start, int end) {
        if (this.rangeList == null) {
            this.rangeList = new int[]{start, end + 1, 0x110000};
        } else {
            this.rangeList[0] = start;
            this.rangeList[1] = end + 1;
        }
        return this.rangeList;
    }

    private UnicodeSet xor(int[] other, int otherLen, int polarity) {
        int b;
        this.ensureBufferCapacity(this.len + otherLen);
        int i = 0;
        int j = 0;
        int k = 0;
        int a = this.list[i++];
        if (polarity == 1 || polarity == 2) {
            b = 0;
            if (other[j] == 0) {
                b = other[++j];
            }
        } else {
            b = other[j++];
        }
        while (true) {
            if (a < b) {
                this.buffer[k++] = a;
                a = this.list[i++];
                continue;
            }
            if (b < a) {
                this.buffer[k++] = b;
                b = other[j++];
                continue;
            }
            if (a == 0x110000) break;
            a = this.list[i++];
            b = other[j++];
        }
        this.buffer[k++] = 0x110000;
        this.len = k;
        int[] temp = this.list;
        this.list = this.buffer;
        this.buffer = temp;
        this.pat = null;
        return this;
    }

    /*
     * Enabled aggressive block sorting
     */
    private UnicodeSet add(int[] other, int otherLen, int polarity) {
        this.ensureBufferCapacity(this.len + otherLen);
        int i = 0;
        int j = 0;
        int k = 0;
        int a = this.list[i++];
        int b = other[j++];
        block6: while (true) {
            switch (polarity) {
                case 0: {
                    if (a < b) {
                        if (k > 0 && a <= this.buffer[k - 1]) {
                            a = UnicodeSet.max(this.list[i], this.buffer[--k]);
                        } else {
                            this.buffer[k++] = a;
                            a = this.list[i];
                        }
                        ++i;
                        polarity ^= 1;
                        break;
                    }
                    if (b < a) {
                        if (k > 0 && b <= this.buffer[k - 1]) {
                            b = UnicodeSet.max(other[j], this.buffer[--k]);
                        } else {
                            this.buffer[k++] = b;
                            b = other[j];
                        }
                        ++j;
                        polarity ^= 2;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    if (k > 0 && a <= this.buffer[k - 1]) {
                        a = UnicodeSet.max(this.list[i], this.buffer[--k]);
                    } else {
                        this.buffer[k++] = a;
                        a = this.list[i];
                    }
                    ++i;
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 3: {
                    if (b <= a) {
                        if (a == 0x110000) break block6;
                        this.buffer[k++] = a;
                    } else {
                        if (b == 0x110000) break block6;
                        this.buffer[k++] = b;
                    }
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 1: {
                    if (a < b) {
                        this.buffer[k++] = a;
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (b < a) {
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 2: {
                    if (b < a) {
                        this.buffer[k++] = b;
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a < b) {
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                }
            }
        }
        this.buffer[k++] = 0x110000;
        this.len = k;
        int[] temp = this.list;
        this.list = this.buffer;
        this.buffer = temp;
        this.pat = null;
        return this;
    }

    /*
     * Enabled aggressive block sorting
     */
    private UnicodeSet retain(int[] other, int otherLen, int polarity) {
        this.ensureBufferCapacity(this.len + otherLen);
        int i = 0;
        int j = 0;
        int k = 0;
        int a = this.list[i++];
        int b = other[j++];
        block6: while (true) {
            switch (polarity) {
                case 0: {
                    if (a < b) {
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (b < a) {
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    this.buffer[k++] = a;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 3: {
                    if (a < b) {
                        this.buffer[k++] = a;
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (b < a) {
                        this.buffer[k++] = b;
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    this.buffer[k++] = a;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 1: {
                    if (a < b) {
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (b < a) {
                        this.buffer[k++] = b;
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                    break;
                }
                case 2: {
                    if (b < a) {
                        b = other[j++];
                        polarity ^= 2;
                        break;
                    }
                    if (a < b) {
                        this.buffer[k++] = a;
                        a = this.list[i++];
                        polarity ^= 1;
                        break;
                    }
                    if (a == 0x110000) break block6;
                    a = this.list[i++];
                    polarity ^= 1;
                    b = other[j++];
                    polarity ^= 2;
                }
            }
        }
        this.buffer[k++] = 0x110000;
        this.len = k;
        int[] temp = this.list;
        this.list = this.buffer;
        this.buffer = temp;
        this.pat = null;
        return this;
    }

    private static final int max(int a, int b) {
        return a > b ? a : b;
    }

    private void applyFilter(Filter filter, UnicodeSet inclusions) {
        this.clear();
        int startHasProperty = -1;
        int limitRange = inclusions.getRangeCount();
        for (int j = 0; j < limitRange; ++j) {
            int start = inclusions.getRangeStart(j);
            int end = inclusions.getRangeEnd(j);
            for (int ch = start; ch <= end; ++ch) {
                if (filter.contains(ch)) {
                    if (startHasProperty >= 0) continue;
                    startHasProperty = ch;
                    continue;
                }
                if (startHasProperty < 0) continue;
                this.add_unchecked(startHasProperty, ch - 1);
                startHasProperty = -1;
            }
        }
        if (startHasProperty >= 0) {
            this.add_unchecked(startHasProperty, 0x10FFFF);
        }
    }

    private static String mungeCharName(String source) {
        source = PatternProps.trimWhiteSpace(source);
        StringBuilder buf = null;
        for (int i = 0; i < source.length(); ++i) {
            int ch = source.charAt(i);
            if (PatternProps.isWhiteSpace(ch)) {
                if (buf == null) {
                    buf = new StringBuilder().append(source, 0, i);
                } else if (buf.charAt(buf.length() - 1) == ' ') continue;
                ch = 32;
            }
            if (buf == null) continue;
            buf.append((char)ch);
        }
        return buf == null ? source : buf.toString();
    }

    public UnicodeSet applyIntPropertyValue(int prop, int value) {
        if (prop == 8192) {
            UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(prop);
            this.applyFilter(new GeneralCategoryMaskFilter(value), inclusions);
        } else if (prop == 28672) {
            UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(prop);
            this.applyFilter(new ScriptExtensionsFilter(value), inclusions);
        } else if (prop == 28673) {
            UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(prop);
            this.applyFilter(new IdentifierTypeFilter(value), inclusions);
        } else if (0 <= prop && prop < 76) {
            if (value == 0 || value == 1) {
                this.set(CharacterProperties.getBinaryPropertySet(prop));
                if (value == 0) {
                    this.complement().removeAllStrings();
                }
            } else {
                this.clear();
            }
        } else if (4096 <= prop && prop < 4123) {
            UnicodeSet inclusions = CharacterPropertiesImpl.getInclusionsForProperty(prop);
            this.applyFilter(new IntPropertyFilter(prop, value), inclusions);
        } else {
            throw new IllegalArgumentException("unsupported property " + prop);
        }
        return this;
    }

    public UnicodeSet applyPropertyAlias(String propertyAlias, String valueAlias) {
        return this.applyPropertyAlias(propertyAlias, valueAlias, null);
    }

    /*
     * Unable to fully structure code
     */
    public UnicodeSet applyPropertyAlias(String propertyAlias, String valueAlias, SymbolTable symbols) {
        block23: {
            block24: {
                block21: {
                    block22: {
                        this.checkFrozen();
                        invert = false;
                        if (symbols != null && symbols instanceof XSymbolTable && ((XSymbolTable)symbols).applyPropertyAlias(propertyAlias, valueAlias, this)) {
                            return this;
                        }
                        if (UnicodeSet.XSYMBOL_TABLE != null && UnicodeSet.XSYMBOL_TABLE.applyPropertyAlias(propertyAlias, valueAlias, this)) {
                            return this;
                        }
                        if (valueAlias.length() <= 0) break block21;
                        p = UCharacter.getPropertyEnum(propertyAlias);
                        if (p == 4101) {
                            p = 8192;
                        }
                        if (!(p >= 0 && p < 76 || p >= 4096 && p < 4123) && (p < 8192 || p >= 8193)) break block22;
                        try {
                            v = UCharacter.getPropertyValueEnum(p, valueAlias);
                        }
                        catch (IllegalArgumentException e) {
                            if (p == 4098 || p == 4112 || p == 4113) {
                                v = Integer.parseInt(PatternProps.trimWhiteSpace(valueAlias));
                                if (v >= 0 && v <= 255) ** GOTO lbl79
                                throw e;
                            }
                            throw e;
                        }
                    }
                    switch (p) {
                        case 12288: {
                            value = Double.parseDouble(PatternProps.trimWhiteSpace(valueAlias));
                            this.applyFilter(new NumericValueFilter(value), CharacterPropertiesImpl.getInclusionsForProperty(p));
                            return this;
                        }
                        case 16389: {
                            buf = UnicodeSet.mungeCharName(valueAlias);
                            ch = UCharacter.getCharFromExtendedName(buf);
                            if (ch == -1) {
                                throw new IllegalArgumentException("Invalid character name");
                            }
                            this.clear();
                            this.add_unchecked(ch);
                            return this;
                        }
                        case 16395: {
                            throw new IllegalArgumentException("Unicode_1_Name (na1) not supported");
                        }
                        case 16384: {
                            version = VersionInfo.getInstance(UnicodeSet.mungeCharName(valueAlias));
                            this.applyFilter(new VersionFilter(version), CharacterPropertiesImpl.getInclusionsForProperty(p));
                            return this;
                        }
                        case 28672: {
                            v = UCharacter.getPropertyValueEnum(4106, valueAlias);
                            break block23;
                        }
                        case 28673: {
                            v = UCharacter.getPropertyValueEnum(p, valueAlias);
                            break block23;
                        }
                        default: {
                            throw new IllegalArgumentException("Unsupported property");
                        }
                    }
                }
                pnames = UPropertyAliases.INSTANCE;
                p = 8192;
                v = pnames.getPropertyValueEnum(p, propertyAlias);
                if (v != -1 || (v = pnames.getPropertyValueEnum(p = 4106, propertyAlias)) != -1) break block23;
                p = pnames.getPropertyEnum(propertyAlias);
                if (p == -1) {
                    p = -1;
                }
                if (p < 0 || p >= 76) break block24;
                v = 1;
                break block23;
            }
            if (p != -1) ** GOTO lbl78
            if (0 == UPropertyAliases.compare("ANY", propertyAlias)) {
                this.set(0, 0x10FFFF);
                return this;
            }
            if (0 == UPropertyAliases.compare("ASCII", propertyAlias)) {
                this.set(0, 127);
                return this;
            }
            if (0 == UPropertyAliases.compare("Assigned", propertyAlias)) {
                p = 8192;
                v = 1;
                invert = true;
            } else {
                throw new IllegalArgumentException("Invalid property alias: " + propertyAlias + "=" + valueAlias);
lbl78:
                // 1 sources

                throw new IllegalArgumentException("Missing property value");
            }
        }
        this.applyIntPropertyValue(p, v);
        if (invert) {
            this.complement().removeAllStrings();
        }
        return this;
    }

    private static boolean resemblesPropertyPattern(String pattern, int pos) {
        if (pos + 5 > pattern.length()) {
            return false;
        }
        return pattern.regionMatches(pos, "[:", 0, 2) || pattern.regionMatches(true, pos, "\\p", 0, 2) || pattern.regionMatches(pos, "\\N", 0, 2);
    }

    private static boolean resemblesPropertyPattern(RuleCharacterIterator chars, int iterOpts) {
        boolean result = false;
        RuleCharacterIterator.Position pos = chars.getPos(null);
        int c = chars.next(iterOpts &= 0xFFFFFFFD);
        if (c == 91 || c == 92) {
            int d = chars.next(iterOpts & 0xFFFFFFFB);
            result = c == 91 ? d == 58 : d == 78 || d == 112 || d == 80;
        }
        chars.setPos(pos);
        return result;
    }

    private UnicodeSet applyPropertyPattern(String pattern, ParsePosition ppos, SymbolTable symbols) {
        String valueName;
        String propName;
        int close;
        int pos = ppos.getIndex();
        if (pos + 5 > pattern.length()) {
            return null;
        }
        boolean posix = false;
        boolean isName = false;
        boolean invert = false;
        if (pattern.regionMatches(pos, "[:", 0, 2)) {
            posix = true;
            if ((pos = PatternProps.skipWhiteSpace(pattern, pos + 2)) < pattern.length() && pattern.charAt(pos) == '^') {
                ++pos;
                invert = true;
            }
        } else if (pattern.regionMatches(true, pos, "\\p", 0, 2) || pattern.regionMatches(pos, "\\N", 0, 2)) {
            char c = pattern.charAt(pos + 1);
            invert = c == 'P';
            isName = c == 'N';
            pos = PatternProps.skipWhiteSpace(pattern, pos + 2);
            if (pos == pattern.length() || pattern.charAt(pos++) != '{') {
                return null;
            }
        } else {
            return null;
        }
        if ((close = pattern.indexOf(posix ? ":]" : "}", pos)) < 0) {
            return null;
        }
        int equals = pattern.indexOf(61, pos);
        if (equals >= 0 && equals < close && !isName) {
            propName = pattern.substring(pos, equals);
            valueName = pattern.substring(equals + 1, close);
        } else {
            propName = pattern.substring(pos, close);
            valueName = "";
            if (isName) {
                valueName = propName;
                propName = "na";
            }
        }
        this.applyPropertyAlias(propName, valueName, symbols);
        if (invert) {
            this.complement().removeAllStrings();
        }
        ppos.setIndex(close + (posix ? 2 : 1));
        return this;
    }

    private void applyPropertyPattern(RuleCharacterIterator chars, Appendable rebuiltPat, SymbolTable symbols) {
        String patStr = chars.getCurrentBuffer();
        int start = chars.getCurrentBufferPos();
        ParsePosition pos = new ParsePosition(start);
        this.applyPropertyPattern(patStr, pos, symbols);
        int length = pos.getIndex() - start;
        if (length == 0) {
            UnicodeSet.syntaxError(chars, "Invalid property pattern");
        }
        chars.jumpahead(length);
        UnicodeSet.append(rebuiltPat, patStr.substring(start, pos.getIndex()));
    }

    private static final void addCaseMapping(UnicodeSet set, int result, StringBuilder full) {
        if (result >= 0) {
            if (result > 31) {
                set.add(result);
            } else {
                set.add(full.toString());
                full.setLength(0);
            }
        }
    }

    UnicodeSet maybeOnlyCaseSensitive(UnicodeSet src) {
        if (src.size() < 30) {
            return src;
        }
        UnicodeSet sensitive = CharacterProperties.getBinaryPropertySet(34);
        if (src.hasStrings() || src.getRangeCount() > sensitive.getRangeCount()) {
            return sensitive.cloneAsThawed().retainAll(src);
        }
        return ((UnicodeSet)src.clone()).retainAll(sensitive);
    }

    private static final boolean scfString(CharSequence s, StringBuilder scf) {
        int c;
        int length = s.length();
        for (int i = 0; i < length; i += Character.charCount(c)) {
            c = Character.codePointAt(s, i);
            int scfChar = UCharacter.foldCase(c, 0);
            if (scfChar == c) continue;
            scf.setLength(0);
            scf.append(s, 0, i);
            while (true) {
                scf.appendCodePoint(scfChar);
                if ((i += Character.charCount(c)) == length) {
                    return true;
                }
                c = Character.codePointAt(s, i);
                scfChar = UCharacter.foldCase(c, 0);
            }
        }
        return false;
    }

    public UnicodeSet closeOver(int attribute) {
        this.checkFrozen();
        switch (attribute & 6) {
            case 0: {
                break;
            }
            case 2: {
                this.closeOverCaseInsensitive(false);
                break;
            }
            case 4: {
                this.closeOverAddCaseMappings();
                break;
            }
            case 6: {
                this.closeOverCaseInsensitive(true);
                break;
            }
        }
        return this;
    }

    private void closeOverCaseInsensitive(boolean simple) {
        UCaseProps csp = UCaseProps.INSTANCE;
        UnicodeSet foldSet = new UnicodeSet(this);
        if (!simple && foldSet.hasStrings()) {
            foldSet.strings.clear();
        }
        UnicodeSet codePoints = this.maybeOnlyCaseSensitive(this);
        int n = codePoints.getRangeCount();
        for (int i = 0; i < n; ++i) {
            int cp;
            int start = codePoints.getRangeStart(i);
            int end = codePoints.getRangeEnd(i);
            if (simple) {
                for (cp = start; cp <= end; ++cp) {
                    csp.addSimpleCaseClosure(cp, foldSet);
                }
                continue;
            }
            for (cp = start; cp <= end; ++cp) {
                csp.addCaseClosure(cp, foldSet);
            }
        }
        if (this.hasStrings()) {
            StringBuilder sb = simple ? new StringBuilder() : null;
            for (String s : this.strings) {
                if (simple) {
                    if (!UnicodeSet.scfString(s, sb)) continue;
                    foldSet.remove(s).add(sb);
                    continue;
                }
                String str = UCharacter.foldCase(s, 0);
                if (csp.addStringCaseClosure(str, foldSet)) continue;
                foldSet.add(str);
            }
        }
        this.set(foldSet);
    }

    private void closeOverAddCaseMappings() {
        UCaseProps csp = UCaseProps.INSTANCE;
        UnicodeSet foldSet = new UnicodeSet(this);
        UnicodeSet codePoints = this.maybeOnlyCaseSensitive(this);
        int n = codePoints.getRangeCount();
        StringBuilder full = new StringBuilder();
        for (int i = 0; i < n; ++i) {
            int start = codePoints.getRangeStart(i);
            int end = codePoints.getRangeEnd(i);
            for (int cp = start; cp <= end; ++cp) {
                int result = csp.toFullLower(cp, null, full, 1);
                UnicodeSet.addCaseMapping(foldSet, result, full);
                result = csp.toFullTitle(cp, null, full, 1);
                UnicodeSet.addCaseMapping(foldSet, result, full);
                result = csp.toFullUpper(cp, null, full, 1);
                UnicodeSet.addCaseMapping(foldSet, result, full);
                result = csp.toFullFolding(cp, full, 0);
                UnicodeSet.addCaseMapping(foldSet, result, full);
            }
        }
        if (this.hasStrings()) {
            ULocale root = ULocale.ROOT;
            BreakIterator bi = BreakIterator.getWordInstance(root);
            for (String str : this.strings) {
                foldSet.add(UCharacter.toLowerCase(root, str));
                foldSet.add(UCharacter.toTitleCase(root, str, bi));
                foldSet.add(UCharacter.toUpperCase(root, str));
                foldSet.add(UCharacter.foldCase(str, 0));
            }
        }
        this.set(foldSet);
    }

    @Override
    public boolean isFrozen() {
        return this.bmpSet != null || this.stringSpan != null;
    }

    @Override
    public UnicodeSet freeze() {
        if (!this.isFrozen()) {
            this.compact();
            if (this.hasStrings()) {
                this.stringSpan = new UnicodeSetStringSpan(this, new ArrayList<String>(this.strings), 127);
            }
            if (this.stringSpan == null || !this.stringSpan.needsStringSpanUTF16()) {
                this.bmpSet = new BMPSet(this.list, this.len);
            }
        }
        return this;
    }

    public int span(CharSequence s, SpanCondition spanCondition) {
        return this.span(s, 0, spanCondition);
    }

    public int span(CharSequence s, int start, SpanCondition spanCondition) {
        int which;
        UnicodeSetStringSpan strSpan;
        int end = s.length();
        if (start < 0) {
            start = 0;
        } else if (start >= end) {
            return end;
        }
        if (this.bmpSet != null) {
            return this.bmpSet.span(s, start, spanCondition, null);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.span(s, start, spanCondition);
        }
        if (this.hasStrings() && (strSpan = new UnicodeSetStringSpan(this, new ArrayList<String>(this.strings), which = spanCondition == SpanCondition.NOT_CONTAINED ? 33 : 34)).needsStringSpanUTF16()) {
            return strSpan.span(s, start, spanCondition);
        }
        return this.spanCodePointsAndCount(s, start, spanCondition, null);
    }

    @Deprecated
    public int spanAndCount(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        if (outCount == null) {
            throw new IllegalArgumentException("outCount must not be null");
        }
        int end = s.length();
        if (start < 0) {
            start = 0;
        } else if (start >= end) {
            return end;
        }
        if (this.stringSpan != null) {
            return this.stringSpan.spanAndCount(s, start, spanCondition, outCount);
        }
        if (this.bmpSet != null) {
            return this.bmpSet.span(s, start, spanCondition, outCount);
        }
        if (this.hasStrings()) {
            int which = spanCondition == SpanCondition.NOT_CONTAINED ? 33 : 34;
            UnicodeSetStringSpan strSpan = new UnicodeSetStringSpan(this, new ArrayList<String>(this.strings), which |= 0x40);
            return strSpan.spanAndCount(s, start, spanCondition, outCount);
        }
        return this.spanCodePointsAndCount(s, start, spanCondition, outCount);
    }

    private int spanCodePointsAndCount(CharSequence s, int start, SpanCondition spanCondition, OutputInt outCount) {
        int c;
        boolean spanContained = spanCondition != SpanCondition.NOT_CONTAINED;
        int next = start;
        int length = s.length();
        int count = 0;
        while (spanContained == this.contains(c = Character.codePointAt(s, next))) {
            ++count;
            if ((next += Character.charCount(c)) < length) continue;
        }
        if (outCount != null) {
            outCount.value = count;
        }
        return next;
    }

    public int spanBack(CharSequence s, SpanCondition spanCondition) {
        return this.spanBack(s, s.length(), spanCondition);
    }

    public int spanBack(CharSequence s, int fromIndex, SpanCondition spanCondition) {
        int c;
        int which;
        UnicodeSetStringSpan strSpan;
        if (fromIndex <= 0) {
            return 0;
        }
        if (fromIndex > s.length()) {
            fromIndex = s.length();
        }
        if (this.bmpSet != null) {
            return this.bmpSet.spanBack(s, fromIndex, spanCondition);
        }
        if (this.stringSpan != null) {
            return this.stringSpan.spanBack(s, fromIndex, spanCondition);
        }
        if (this.hasStrings() && (strSpan = new UnicodeSetStringSpan(this, new ArrayList<String>(this.strings), which = spanCondition == SpanCondition.NOT_CONTAINED ? 17 : 18)).needsStringSpanUTF16()) {
            return strSpan.spanBack(s, fromIndex, spanCondition);
        }
        boolean spanContained = spanCondition != SpanCondition.NOT_CONTAINED;
        int prev = fromIndex;
        while (spanContained == this.contains(c = Character.codePointBefore(s, prev)) && (prev -= Character.charCount(c)) > 0) {
        }
        return prev;
    }

    @Override
    public UnicodeSet cloneAsThawed() {
        UnicodeSet result = new UnicodeSet(this);
        assert (!result.isFrozen());
        return result;
    }

    private void checkFrozen() {
        if (this.isFrozen()) {
            throw new UnsupportedOperationException("Attempt to modify frozen object");
        }
    }

    public Iterable<EntryRange> ranges() {
        return new EntryRangeIterable();
    }

    @Override
    public Iterator<String> iterator() {
        return new UnicodeSetIterator2(this);
    }

    public <T extends CharSequence> boolean containsAll(Iterable<T> collection) {
        for (CharSequence o : collection) {
            if (this.contains(o)) continue;
            return false;
        }
        return true;
    }

    public <T extends CharSequence> boolean containsNone(Iterable<T> collection) {
        for (CharSequence o : collection) {
            if (!this.contains(o)) continue;
            return false;
        }
        return true;
    }

    public final <T extends CharSequence> boolean containsSome(Iterable<T> collection) {
        return !this.containsNone(collection);
    }

    public <T extends CharSequence> UnicodeSet addAll(T ... collection) {
        this.checkFrozen();
        for (T str : collection) {
            this.add((CharSequence)str);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet removeAll(Iterable<T> collection) {
        this.checkFrozen();
        for (CharSequence o : collection) {
            this.remove(o);
        }
        return this;
    }

    public <T extends CharSequence> UnicodeSet retainAll(Iterable<T> collection) {
        this.checkFrozen();
        UnicodeSet toRetain = new UnicodeSet();
        toRetain.addAll(collection);
        this.retainAll(toRetain);
        return this;
    }

    @Override
    public int compareTo(UnicodeSet o) {
        return this.compareTo(o, ComparisonStyle.SHORTER_FIRST);
    }

    public int compareTo(UnicodeSet o, ComparisonStyle style) {
        int diff;
        if (style != ComparisonStyle.LEXICOGRAPHIC && (diff = this.size() - o.size()) != 0) {
            return diff < 0 == (style == ComparisonStyle.SHORTER_FIRST) ? -1 : 1;
        }
        int i = 0;
        while (true) {
            int result;
            if (0 != (result = this.list[i] - o.list[i])) {
                if (this.list[i] == 0x110000) {
                    if (!this.hasStrings()) {
                        return 1;
                    }
                    String item = this.strings.first();
                    return UnicodeSet.compare(item, o.list[i]);
                }
                if (o.list[i] == 0x110000) {
                    if (!o.hasStrings()) {
                        return -1;
                    }
                    String item = o.strings.first();
                    int compareResult = UnicodeSet.compare(item, this.list[i]);
                    return compareResult > 0 ? -1 : (compareResult < 0 ? 1 : 0);
                }
                return (i & 1) == 0 ? result : -result;
            }
            if (this.list[i] == 0x110000) break;
            ++i;
        }
        return UnicodeSet.compare(this.strings, o.strings);
    }

    @Override
    public int compareTo(Iterable<String> other) {
        return UnicodeSet.compare(this, other);
    }

    public static int compare(CharSequence string, int codePoint) {
        return CharSequences.compare(string, codePoint);
    }

    public static int compare(int codePoint, CharSequence string) {
        return -CharSequences.compare(string, codePoint);
    }

    public static <T extends Comparable<T>> int compare(Iterable<T> collection1, Iterable<T> collection2) {
        return UnicodeSet.compare(collection1.iterator(), collection2.iterator());
    }

    @Deprecated
    public static <T extends Comparable<T>> int compare(Iterator<T> first, Iterator<T> other) {
        Comparable item2;
        Comparable item1;
        int result;
        do {
            if (!first.hasNext()) {
                return other.hasNext() ? -1 : 0;
            }
            if (other.hasNext()) continue;
            return 1;
        } while ((result = (item1 = (Comparable)first.next()).compareTo(item2 = (Comparable)other.next())) == 0);
        return result;
    }

    public static <T extends Comparable<T>> int compare(Collection<T> collection1, Collection<T> collection2, ComparisonStyle style) {
        int diff;
        if (style != ComparisonStyle.LEXICOGRAPHIC && (diff = collection1.size() - collection2.size()) != 0) {
            return diff < 0 == (style == ComparisonStyle.SHORTER_FIRST) ? -1 : 1;
        }
        return UnicodeSet.compare(collection1, collection2);
    }

    public static <T, U extends Collection<T>> U addAllTo(Iterable<T> source, U target) {
        for (T item : source) {
            target.add(item);
        }
        return target;
    }

    public static <T> T[] addAllTo(Iterable<T> source, T[] target) {
        int i = 0;
        for (T item : source) {
            target[i++] = item;
        }
        return target;
    }

    public Collection<String> strings() {
        if (this.hasStrings()) {
            return Collections.unmodifiableSortedSet(this.strings);
        }
        return EMPTY_STRINGS;
    }

    @Deprecated
    public static int getSingleCodePoint(CharSequence s) {
        return CharSequences.getSingleCodePoint(s);
    }

    @Deprecated
    public UnicodeSet addBridges(UnicodeSet dontCare) {
        UnicodeSet notInInput = new UnicodeSet(this).complement().removeAllStrings();
        UnicodeSetIterator it = new UnicodeSetIterator(notInInput);
        while (it.nextRange()) {
            if (it.codepoint == 0 || it.codepointEnd == 0x10FFFF || !dontCare.contains(it.codepoint, it.codepointEnd)) continue;
            this.add(it.codepoint, it.codepointEnd);
        }
        return this;
    }

    @Deprecated
    public int findIn(CharSequence value, int fromIndex, boolean findNot) {
        int cp;
        while (fromIndex < value.length() && this.contains(cp = UTF16.charAt(value, fromIndex)) == findNot) {
            fromIndex += UTF16.getCharCount(cp);
        }
        return fromIndex;
    }

    @Deprecated
    public int findLastIn(CharSequence value, int fromIndex, boolean findNot) {
        int cp;
        --fromIndex;
        while (fromIndex >= 0 && this.contains(cp = UTF16.charAt(value, fromIndex)) == findNot) {
            fromIndex -= UTF16.getCharCount(cp);
        }
        return fromIndex < 0 ? -1 : fromIndex;
    }

    @Deprecated
    public String stripFrom(CharSequence source, boolean matches) {
        StringBuilder result = new StringBuilder();
        int pos = 0;
        while (pos < source.length()) {
            int inside = this.findIn(source, pos, !matches);
            result.append(source.subSequence(pos, inside));
            pos = this.findIn(source, inside, matches);
        }
        return result.toString();
    }

    @Deprecated
    public static XSymbolTable getDefaultXSymbolTable() {
        return XSYMBOL_TABLE;
    }

    @Deprecated
    public static void setDefaultXSymbolTable(XSymbolTable xSymbolTable) {
        CharacterPropertiesImpl.clear();
        XSYMBOL_TABLE = xSymbolTable;
    }

    public Stream<EntryRange> rangeStream() {
        return StreamSupport.stream(this.ranges().spliterator(), false);
    }

    public Stream<String> stringStream() {
        return this.strings().stream();
    }

    public IntStream codePointStream() {
        return StreamSupport.intStream(new CodePointSpliterator(this), false);
    }

    public Stream<String> stream() {
        return StreamSupport.stream(this.spliterator(), false);
    }

    public Iterable<Integer> codePoints() {
        return new CodePointIterable(this);
    }

    private static class CodePointIteratorInt {
        private final int[] list;
        private final int lastRange;
        private int currentRange = 0;
        private int rangeStart;
        private int rangeLimit;

        public CodePointIteratorInt(UnicodeSet unicodeSet) {
            this.list = unicodeSet.list;
            this.lastRange = unicodeSet.len - 1;
            this.currentRange = 0;
            this.rangeStart = this.list[this.currentRange++];
            this.rangeLimit = this.lastRange > 0 ? this.list[this.currentRange++] : this.rangeStart;
        }

        public boolean hasNext() {
            return this.rangeStart < this.rangeLimit || this.currentRange < this.lastRange;
        }

        public int next() {
            if (this.rangeStart >= this.rangeLimit) {
                if (this.currentRange >= this.lastRange) {
                    throw new NoSuchElementException();
                }
                this.rangeStart = this.list[this.currentRange++];
                this.rangeLimit = this.list[this.currentRange++];
            }
            return this.rangeStart++;
        }
    }

    private static class CodePointSpliterator
    implements Spliterator.OfInt {
        private static final int CHARACTERISTICS = 337;
        private final UnicodeSet unicodeSet;
        private final CodePointIteratorInt cpi;

        CodePointSpliterator(UnicodeSet unicodeSet) {
            this.unicodeSet = unicodeSet;
            this.cpi = new CodePointIteratorInt(unicodeSet);
        }

        @Override
        public long estimateSize() {
            return this.unicodeSet.size() - this.unicodeSet.strings.size();
        }

        @Override
        public int characteristics() {
            return this.unicodeSet.isFrozen() ? 1361 : 337;
        }

        @Override
        public Spliterator.OfInt trySplit() {
            return null;
        }

        @Override
        public boolean tryAdvance(IntConsumer action) {
            if (action == null) {
                throw new NullPointerException();
            }
            if (this.cpi.hasNext()) {
                action.accept(this.cpi.next());
                return true;
            }
            return false;
        }
    }

    private class CodePointIterator
    implements Iterator<Integer> {
        private final CodePointIteratorInt cpi;

        CodePointIterator(UnicodeSet unicodeSet2) {
            this.cpi = new CodePointIteratorInt(unicodeSet2);
        }

        @Override
        public boolean hasNext() {
            return this.cpi.hasNext();
        }

        @Override
        public Integer next() {
            return this.cpi.next();
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class CodePointIterable
    implements Iterable<Integer> {
        private final UnicodeSet unicodeSet;

        CodePointIterable(UnicodeSet unicodeSet2) {
            this.unicodeSet = unicodeSet2;
        }

        @Override
        public Iterator<Integer> iterator() {
            return new CodePointIterator(this.unicodeSet);
        }
    }

    public static enum SpanCondition {
        NOT_CONTAINED,
        CONTAINED,
        SIMPLE,
        CONDITION_COUNT;

    }

    public static enum ComparisonStyle {
        SHORTER_FIRST,
        LEXICOGRAPHIC,
        LONGER_FIRST;

    }

    private static class UnicodeSetIterator2
    implements Iterator<String> {
        private int[] sourceList;
        private int len;
        private int item;
        private int current;
        private int limit;
        private SortedSet<String> sourceStrings;
        private Iterator<String> stringIterator;
        private char[] buffer;

        UnicodeSetIterator2(UnicodeSet source) {
            this.len = source.len - 1;
            if (this.len > 0) {
                this.sourceStrings = source.strings;
                this.sourceList = source.list;
                this.current = this.sourceList[this.item++];
                this.limit = this.sourceList[this.item++];
            } else {
                this.stringIterator = source.strings.iterator();
                this.sourceList = null;
            }
        }

        @Override
        public boolean hasNext() {
            return this.sourceList != null || this.stringIterator.hasNext();
        }

        @Override
        public String next() {
            if (this.sourceList == null) {
                return this.stringIterator.next();
            }
            int codepoint = this.current++;
            if (this.current >= this.limit) {
                if (this.item >= this.len) {
                    this.stringIterator = this.sourceStrings.iterator();
                    this.sourceList = null;
                } else {
                    this.current = this.sourceList[this.item++];
                    this.limit = this.sourceList[this.item++];
                }
            }
            if (codepoint <= 65535) {
                return String.valueOf((char)codepoint);
            }
            if (this.buffer == null) {
                this.buffer = new char[2];
            }
            int offset = codepoint - 65536;
            this.buffer[0] = (char)((offset >>> 10) + 55296);
            this.buffer[1] = (char)((offset & 0x3FF) + 56320);
            return String.valueOf(this.buffer);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class EntryRangeIterator
    implements Iterator<EntryRange> {
        int pos;
        EntryRange result = new EntryRange();

        private EntryRangeIterator() {
        }

        @Override
        public boolean hasNext() {
            return this.pos < UnicodeSet.this.len - 1;
        }

        @Override
        public EntryRange next() {
            if (this.pos >= UnicodeSet.this.len - 1) {
                throw new NoSuchElementException();
            }
            this.result.codepoint = UnicodeSet.this.list[this.pos++];
            this.result.codepointEnd = UnicodeSet.this.list[this.pos++] - 1;
            return this.result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private class EntryRangeIterable
    implements Iterable<EntryRange> {
        private EntryRangeIterable() {
        }

        @Override
        public Iterator<EntryRange> iterator() {
            return new EntryRangeIterator();
        }
    }

    public static class EntryRange {
        public int codepoint;
        public int codepointEnd;

        EntryRange() {
        }

        public String toString() {
            StringBuilder b = new StringBuilder();
            return (this.codepoint == this.codepointEnd ? (StringBuilder)UnicodeSet._appendToPat(b, this.codepoint, false) : (StringBuilder)UnicodeSet._appendToPat(((StringBuilder)UnicodeSet._appendToPat(b, this.codepoint, false)).append('-'), this.codepointEnd, false)).toString();
        }
    }

    public static abstract class XSymbolTable
    implements SymbolTable {
        @Override
        public UnicodeMatcher lookupMatcher(int i) {
            return null;
        }

        public boolean applyPropertyAlias(String propertyName, String propertyValue, UnicodeSet result) {
            return false;
        }

        @Override
        public char[] lookup(String s) {
            return null;
        }

        @Override
        public String parseReference(String text, ParsePosition pos, int limit) {
            return null;
        }
    }

    private static final class VersionFilter
    implements Filter {
        VersionInfo version;

        VersionFilter(VersionInfo version) {
            this.version = version;
        }

        @Override
        public boolean contains(int ch) {
            VersionInfo v = UCharacter.getAge(ch);
            return !Utility.sameObjects(v, NO_VERSION) && v.compareTo(this.version) <= 0;
        }
    }

    private static final class IdentifierTypeFilter
    implements Filter {
        int idType;

        IdentifierTypeFilter(int idType) {
            this.idType = idType;
        }

        @Override
        public boolean contains(int c) {
            return UCharacterProperty.INSTANCE.hasIDType(c, this.idType);
        }
    }

    private static final class ScriptExtensionsFilter
    implements Filter {
        int script;

        ScriptExtensionsFilter(int script) {
            this.script = script;
        }

        @Override
        public boolean contains(int c) {
            return UScript.hasScript(c, this.script);
        }
    }

    private static final class IntPropertyFilter
    implements Filter {
        int prop;
        int value;

        IntPropertyFilter(int prop, int value) {
            this.prop = prop;
            this.value = value;
        }

        @Override
        public boolean contains(int ch) {
            return UCharacter.getIntPropertyValue(ch, this.prop) == this.value;
        }
    }

    private static final class GeneralCategoryMaskFilter
    implements Filter {
        int mask;

        GeneralCategoryMaskFilter(int mask) {
            this.mask = mask;
        }

        @Override
        public boolean contains(int ch) {
            return (1 << UCharacter.getType(ch) & this.mask) != 0;
        }
    }

    private static final class NumericValueFilter
    implements Filter {
        double value;

        NumericValueFilter(double value) {
            this.value = value;
        }

        @Override
        public boolean contains(int ch) {
            return UCharacter.getUnicodeNumericValue(ch) == this.value;
        }
    }

    private static interface Filter {
        public boolean contains(int var1);
    }
}

