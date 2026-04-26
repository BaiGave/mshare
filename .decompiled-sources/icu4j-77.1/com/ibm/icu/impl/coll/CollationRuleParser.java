/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.coll;

import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.impl.coll.CollationData;
import com.ibm.icu.impl.coll.CollationSettings;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UTF16;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;
import java.text.ParseException;
import java.util.ArrayList;

public final class CollationRuleParser {
    static final Position[] POSITION_VALUES = Position.values();
    static final char POS_LEAD = '\ufffe';
    static final char POS_BASE = '\u2800';
    private static final int UCOL_DEFAULT = -1;
    private static final int UCOL_OFF = 0;
    private static final int UCOL_ON = 1;
    private static final int STRENGTH_MASK = 15;
    private static final int STARRED_FLAG = 16;
    private static final int OFFSET_SHIFT = 8;
    private static final String BEFORE = "[before";
    private final StringBuilder rawBuilder = new StringBuilder();
    private static final String[] positions = new String[]{"first tertiary ignorable", "last tertiary ignorable", "first secondary ignorable", "last secondary ignorable", "first primary ignorable", "last primary ignorable", "first variable", "last variable", "first regular", "last regular", "first implicit", "last implicit", "first trailing", "last trailing"};
    private static final String[] gSpecialReorderCodes = new String[]{"space", "punct", "symbol", "currency", "digit"};
    private static final int U_PARSE_CONTEXT_LEN = 16;
    private Normalizer2 nfd = Normalizer2.getNFDInstance();
    private Normalizer2 nfc = Normalizer2.getNFCInstance();
    private String rules;
    private final CollationData baseData;
    private CollationSettings settings;
    private Sink sink;
    private Importer importer;
    private int ruleIndex;

    CollationRuleParser(CollationData base) {
        this.baseData = base;
    }

    void setSink(Sink sinkAlias) {
        this.sink = sinkAlias;
    }

    void setImporter(Importer importerAlias) {
        this.importer = importerAlias;
    }

    void parse(String ruleString, CollationSettings outSettings) throws ParseException {
        this.settings = outSettings;
        this.parse(ruleString);
    }

    private void parse(String ruleString) throws ParseException {
        this.rules = ruleString;
        this.ruleIndex = 0;
        block7: while (this.ruleIndex < this.rules.length()) {
            char c = this.rules.charAt(this.ruleIndex);
            if (PatternProps.isWhiteSpace(c)) {
                ++this.ruleIndex;
                continue;
            }
            switch (c) {
                case '&': {
                    this.parseRuleChain();
                    continue block7;
                }
                case '[': {
                    this.parseSetting();
                    continue block7;
                }
                case '#': {
                    this.ruleIndex = this.skipComment(this.ruleIndex + 1);
                    continue block7;
                }
                case '@': {
                    this.settings.setFlag(2048, true);
                    ++this.ruleIndex;
                    continue block7;
                }
                case '!': {
                    ++this.ruleIndex;
                    continue block7;
                }
            }
            this.setParseError("expected a reset or setting or comment");
        }
    }

    private void parseRuleChain() throws ParseException {
        int resetStrength = this.parseResetAndPosition();
        boolean isFirstRelation = true;
        while (true) {
            int result;
            if ((result = this.parseRelationOperator()) < 0) {
                if (this.ruleIndex < this.rules.length() && this.rules.charAt(this.ruleIndex) == '#') {
                    this.ruleIndex = this.skipComment(this.ruleIndex + 1);
                    continue;
                }
                if (isFirstRelation) {
                    this.setParseError("reset not followed by a relation");
                }
                return;
            }
            int strength = result & 0xF;
            if (resetStrength < 15) {
                if (isFirstRelation) {
                    if (strength != resetStrength) {
                        this.setParseError("reset-before strength differs from its first relation");
                        return;
                    }
                } else if (strength < resetStrength) {
                    this.setParseError("reset-before strength followed by a stronger relation");
                    return;
                }
            }
            int i = this.ruleIndex + (result >> 8);
            if ((result & 0x10) == 0) {
                this.parseRelationStrings(strength, i);
            } else {
                this.parseStarredCharacters(strength, i);
            }
            isFirstRelation = false;
        }
    }

    private int parseResetAndPosition() throws ParseException {
        int resetStrength;
        char c;
        int j;
        int i = this.skipWhiteSpace(this.ruleIndex + 1);
        if (this.rules.regionMatches(i, BEFORE, 0, BEFORE.length()) && (j = i + BEFORE.length()) < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(j)) && (j = this.skipWhiteSpace(j + 1)) + 1 < this.rules.length() && '1' <= (c = this.rules.charAt(j)) && c <= '3' && this.rules.charAt(j + 1) == ']') {
            resetStrength = 0 + (c - 49);
            i = this.skipWhiteSpace(j + 2);
        } else {
            resetStrength = 15;
        }
        if (i >= this.rules.length()) {
            this.setParseError("reset without position");
            return -1;
        }
        i = this.rules.charAt(i) == '[' ? this.parseSpecialPosition(i, this.rawBuilder) : this.parseTailoringString(i, this.rawBuilder);
        try {
            this.sink.addReset(resetStrength, this.rawBuilder);
        }
        catch (Exception e) {
            this.setParseError("adding reset failed", e);
            return -1;
        }
        this.ruleIndex = i;
        return resetStrength;
    }

    private int parseRelationOperator() {
        int strength;
        this.ruleIndex = this.skipWhiteSpace(this.ruleIndex);
        if (this.ruleIndex >= this.rules.length()) {
            return -1;
        }
        int i = this.ruleIndex;
        char c = this.rules.charAt(i++);
        switch (c) {
            case '<': {
                if (i < this.rules.length() && this.rules.charAt(i) == '<') {
                    if (++i < this.rules.length() && this.rules.charAt(i) == '<') {
                        if (++i < this.rules.length() && this.rules.charAt(i) == '<') {
                            ++i;
                            strength = 3;
                        } else {
                            strength = 2;
                        }
                    } else {
                        strength = 1;
                    }
                } else {
                    strength = 0;
                }
                if (i >= this.rules.length() || this.rules.charAt(i) != '*') break;
                ++i;
                strength |= 0x10;
                break;
            }
            case ';': {
                strength = 1;
                break;
            }
            case ',': {
                strength = 2;
                break;
            }
            case '=': {
                strength = 15;
                if (i >= this.rules.length() || this.rules.charAt(i) != '*') break;
                ++i;
                strength |= 0x10;
                break;
            }
            default: {
                return -1;
            }
        }
        return i - this.ruleIndex << 8 | strength;
    }

    private void parseRelationStrings(int strength, int i) throws ParseException {
        char next;
        String prefix = "";
        CharSequence extension = "";
        char c = next = (i = this.parseTailoringString(i, this.rawBuilder)) < this.rules.length() ? this.rules.charAt(i) : (char)'\u0000';
        if (next == '|') {
            prefix = this.rawBuilder.toString();
            char c2 = next = (i = this.parseTailoringString(i + 1, this.rawBuilder)) < this.rules.length() ? this.rules.charAt(i) : (char)'\u0000';
        }
        if (next == '/') {
            StringBuilder extBuilder = new StringBuilder();
            i = this.parseTailoringString(i + 1, extBuilder);
            extension = extBuilder;
        }
        if (prefix.length() != 0) {
            int prefix0 = prefix.codePointAt(0);
            int c3 = this.rawBuilder.codePointAt(0);
            if (!this.nfc.hasBoundaryBefore(prefix0) || !this.nfc.hasBoundaryBefore(c3)) {
                this.setParseError("in 'prefix|str', prefix and str must each start with an NFC boundary");
                return;
            }
        }
        try {
            this.sink.addRelation(strength, prefix, this.rawBuilder, extension);
        }
        catch (Exception e) {
            this.setParseError("adding relation failed", e);
            return;
        }
        this.ruleIndex = i;
    }

    private void parseStarredCharacters(int strength, int i) throws ParseException {
        String empty = "";
        i = this.parseString(this.skipWhiteSpace(i), this.rawBuilder);
        if (this.rawBuilder.length() == 0) {
            this.setParseError("missing starred-relation string");
            return;
        }
        int prev = -1;
        int j = 0;
        while (true) {
            int c;
            if (j < this.rawBuilder.length()) {
                c = this.rawBuilder.codePointAt(j);
                if (!this.nfd.isInert(c)) {
                    this.setParseError("starred-relation string is not all NFD-inert");
                    return;
                }
                try {
                    this.sink.addRelation(strength, empty, UTF16.valueOf(c), empty);
                }
                catch (Exception e) {
                    this.setParseError("adding relation failed", e);
                    return;
                }
                j += Character.charCount(c);
                prev = c;
                continue;
            }
            if (i >= this.rules.length() || this.rules.charAt(i) != '-') break;
            if (prev < 0) {
                this.setParseError("range without start in starred-relation string");
                return;
            }
            i = this.parseString(i + 1, this.rawBuilder);
            if (this.rawBuilder.length() == 0) {
                this.setParseError("range without end in starred-relation string");
                return;
            }
            c = this.rawBuilder.codePointAt(0);
            if (c < prev) {
                this.setParseError("range start greater than end in starred-relation string");
                return;
            }
            while (++prev <= c) {
                if (!this.nfd.isInert(prev)) {
                    this.setParseError("starred-relation string range is not all NFD-inert");
                    return;
                }
                if (CollationRuleParser.isSurrogate(prev)) {
                    this.setParseError("starred-relation string range contains a surrogate");
                    return;
                }
                if (65533 <= prev && prev <= 65535) {
                    this.setParseError("starred-relation string range contains U+FFFD, U+FFFE or U+FFFF");
                    return;
                }
                try {
                    this.sink.addRelation(strength, empty, UTF16.valueOf(prev), empty);
                }
                catch (Exception e) {
                    this.setParseError("adding relation failed", e);
                    return;
                }
            }
            prev = -1;
            j = Character.charCount(c);
        }
        this.ruleIndex = this.skipWhiteSpace(i);
    }

    private int parseTailoringString(int i, StringBuilder raw) throws ParseException {
        i = this.parseString(this.skipWhiteSpace(i), raw);
        if (raw.length() == 0) {
            this.setParseError("missing relation string");
        }
        return this.skipWhiteSpace(i);
    }

    private int parseString(int i, StringBuilder raw) throws ParseException {
        int c;
        raw.setLength(0);
        block0: while (i < this.rules.length()) {
            char c2;
            if (CollationRuleParser.isSyntaxChar(c2 = this.rules.charAt(i++))) {
                if (c2 == '\'') {
                    if (i < this.rules.length() && this.rules.charAt(i) == '\'') {
                        raw.append('\'');
                        ++i;
                        continue;
                    }
                    while (true) {
                        if (i == this.rules.length()) {
                            this.setParseError("quoted literal text missing terminating apostrophe");
                            return i;
                        }
                        if ((c2 = this.rules.charAt(i++)) == '\'') {
                            if (i >= this.rules.length() || this.rules.charAt(i) != '\'') continue block0;
                            ++i;
                        }
                        raw.append(c2);
                    }
                }
                if (c2 == '\\') {
                    if (i == this.rules.length()) {
                        this.setParseError("backslash escape at the end of the rule string");
                        return i;
                    }
                    int cp = this.rules.codePointAt(i);
                    raw.appendCodePoint(cp);
                    i += Character.charCount(cp);
                    continue;
                }
                --i;
                break;
            }
            if (PatternProps.isWhiteSpace(c2)) {
                --i;
                break;
            }
            raw.append(c2);
        }
        for (int j = 0; j < raw.length(); j += Character.charCount(c)) {
            c = raw.codePointAt(j);
            if (CollationRuleParser.isSurrogate(c)) {
                this.setParseError("string contains an unpaired surrogate");
                return i;
            }
            if (65533 > c || c > 65535) continue;
            this.setParseError("string contains U+FFFD, U+FFFE or U+FFFF");
            return i;
        }
        return i;
    }

    private static final boolean isSurrogate(int c) {
        return (c & 0xFFFFF800) == 55296;
    }

    private int parseSpecialPosition(int i, StringBuilder str) throws ParseException {
        int j = this.readWords(i + 1, this.rawBuilder);
        if (j > i && this.rules.charAt(j) == ']' && this.rawBuilder.length() != 0) {
            ++j;
            String raw = this.rawBuilder.toString();
            str.setLength(0);
            for (int pos = 0; pos < positions.length; ++pos) {
                if (!raw.equals(positions[pos])) continue;
                str.append('\ufffe').append((char)(10240 + pos));
                return j;
            }
            if (raw.equals("top")) {
                str.append('\ufffe').append((char)(10240 + Position.LAST_REGULAR.ordinal()));
                return j;
            }
            if (raw.equals("variable top")) {
                str.append('\ufffe').append((char)(10240 + Position.LAST_VARIABLE.ordinal()));
                return j;
            }
        }
        this.setParseError("not a valid special reset position");
        return i;
    }

    private void parseSetting() throws ParseException {
        int i = this.ruleIndex + 1;
        int j = this.readWords(i, this.rawBuilder);
        if (j <= i || this.rawBuilder.length() == 0) {
            this.setParseError("expected a setting/option at '['");
        }
        String raw = this.rawBuilder.toString();
        if (this.rules.charAt(j) == ']') {
            String v;
            ++j;
            if (raw.startsWith("reorder") && (raw.length() == 7 || raw.charAt(7) == ' ')) {
                this.parseReordering(raw);
                this.ruleIndex = j;
                return;
            }
            if (raw.equals("backwards 2")) {
                this.settings.setFlag(2048, true);
                this.ruleIndex = j;
                return;
            }
            int valueIndex = raw.lastIndexOf(32);
            if (valueIndex >= 0) {
                v = raw.substring(valueIndex + 1);
                raw = raw.substring(0, valueIndex);
            } else {
                v = "";
            }
            if (raw.equals("strength") && v.length() == 1) {
                int value = -1;
                char c = v.charAt(0);
                if ('1' <= c && c <= '4') {
                    value = 0 + (c - 49);
                } else if (c == 'I') {
                    value = 15;
                }
                if (value != -1) {
                    this.settings.setStrength(value);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("alternate")) {
                int value = -1;
                if (v.equals("non-ignorable")) {
                    value = 0;
                } else if (v.equals("shifted")) {
                    value = 1;
                }
                if (value != -1) {
                    this.settings.setAlternateHandlingShifted(value > 0);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("maxVariable")) {
                int value = -1;
                if (v.equals("space")) {
                    value = 0;
                } else if (v.equals("punct")) {
                    value = 1;
                } else if (v.equals("symbol")) {
                    value = 2;
                } else if (v.equals("currency")) {
                    value = 3;
                }
                if (value != -1) {
                    this.settings.setMaxVariable(value, 0);
                    this.settings.variableTop = this.baseData.getLastPrimaryForGroup(4096 + value);
                    assert (this.settings.variableTop != 0L);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("caseFirst")) {
                int value = -1;
                if (v.equals("off")) {
                    value = 0;
                } else if (v.equals("lower")) {
                    value = 512;
                } else if (v.equals("upper")) {
                    value = 768;
                }
                if (value != -1) {
                    this.settings.setCaseFirst(value);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("caseLevel")) {
                int value = CollationRuleParser.getOnOffValue(v);
                if (value != -1) {
                    this.settings.setFlag(1024, value > 0);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("normalization")) {
                int value = CollationRuleParser.getOnOffValue(v);
                if (value != -1) {
                    this.settings.setFlag(1, value > 0);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("numericOrdering")) {
                int value = CollationRuleParser.getOnOffValue(v);
                if (value != -1) {
                    this.settings.setFlag(2, value > 0);
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("hiraganaQ")) {
                int value = CollationRuleParser.getOnOffValue(v);
                if (value != -1) {
                    if (value == 1) {
                        this.setParseError("[hiraganaQ on] is not supported");
                    }
                    this.ruleIndex = j;
                    return;
                }
            } else if (raw.equals("import")) {
                ULocale localeID;
                try {
                    localeID = new ULocale.Builder().setLanguageTag(v).build();
                }
                catch (Exception e) {
                    this.setParseError("expected language tag in [import langTag]", e);
                    return;
                }
                String baseID = localeID.getBaseName();
                String collationType = localeID.getKeywordValue("collation");
                if (this.importer == null) {
                    this.setParseError("[import langTag] is not supported");
                } else {
                    String importedRules;
                    try {
                        importedRules = this.importer.getRules(baseID, collationType != null ? collationType : "standard");
                    }
                    catch (Exception e) {
                        this.setParseError("[import langTag] failed", e);
                        return;
                    }
                    String outerRules = this.rules;
                    int outerRuleIndex = this.ruleIndex;
                    try {
                        this.parse(importedRules);
                    }
                    catch (Exception e) {
                        this.ruleIndex = outerRuleIndex;
                        this.setParseError("parsing imported rules failed", e);
                    }
                    this.rules = outerRules;
                    this.ruleIndex = j;
                }
                return;
            }
        } else if (this.rules.charAt(j) == '[') {
            UnicodeSet set = new UnicodeSet();
            j = this.parseUnicodeSet(j, set);
            if (raw.equals("optimize")) {
                try {
                    this.sink.optimize(set);
                }
                catch (Exception e) {
                    this.setParseError("[optimize set] failed", e);
                }
                this.ruleIndex = j;
                return;
            }
            if (raw.equals("suppressContractions")) {
                try {
                    this.sink.suppressContractions(set);
                }
                catch (Exception e) {
                    this.setParseError("[suppressContractions set] failed", e);
                }
                this.ruleIndex = j;
                return;
            }
        }
        this.setParseError("not a valid setting/option");
    }

    private void parseReordering(CharSequence raw) throws ParseException {
        int i = 7;
        if (i == raw.length()) {
            this.settings.resetReordering();
            return;
        }
        ArrayList<Integer> reorderCodes = new ArrayList<Integer>();
        while (i < raw.length()) {
            int limit;
            for (limit = ++i; limit < raw.length() && raw.charAt(limit) != ' '; ++limit) {
            }
            String word = raw.subSequence(i, limit).toString();
            int code = CollationRuleParser.getReorderCode(word);
            if (code < 0) {
                this.setParseError("unknown script or reorder code");
                return;
            }
            reorderCodes.add(code);
            i = limit;
        }
        if (reorderCodes.isEmpty()) {
            this.settings.resetReordering();
        } else {
            int[] codes = new int[reorderCodes.size()];
            int j = 0;
            for (Integer code : reorderCodes) {
                codes[j++] = code;
            }
            this.settings.setReordering(this.baseData, codes);
        }
    }

    public static int getReorderCode(String word) {
        for (int i = 0; i < gSpecialReorderCodes.length; ++i) {
            if (!word.equalsIgnoreCase(gSpecialReorderCodes[i])) continue;
            return 4096 + i;
        }
        try {
            int script = UCharacter.getPropertyValueEnum(4106, word);
            if (script >= 0) {
                return script;
            }
        }
        catch (IllegalIcuArgumentException illegalIcuArgumentException) {
            // empty catch block
        }
        if (word.equalsIgnoreCase("others")) {
            return 103;
        }
        return -1;
    }

    private static int getOnOffValue(String s) {
        if (s.equals("on")) {
            return 1;
        }
        if (s.equals("off")) {
            return 0;
        }
        return -1;
    }

    private int parseUnicodeSet(int i, UnicodeSet set) throws ParseException {
        int level = 0;
        int j = i;
        while (true) {
            char c;
            if (j == this.rules.length()) {
                this.setParseError("unbalanced UnicodeSet pattern brackets");
                return j;
            }
            if ((c = this.rules.charAt(j++)) == '[') {
                ++level;
                continue;
            }
            if (c == ']' && --level == 0) break;
        }
        try {
            set.applyPattern(this.rules.substring(i, j));
        }
        catch (Exception e) {
            this.setParseError("not a valid UnicodeSet pattern: " + e.getMessage());
        }
        j = this.skipWhiteSpace(j);
        if (j == this.rules.length() || this.rules.charAt(j) != ']') {
            this.setParseError("missing option-terminating ']' after UnicodeSet pattern");
            return j;
        }
        return ++j;
    }

    private int readWords(int i, StringBuilder raw) {
        raw.setLength(0);
        i = this.skipWhiteSpace(i);
        while (i < this.rules.length()) {
            char c = this.rules.charAt(i);
            if (CollationRuleParser.isSyntaxChar(c) && c != '-' && c != '_') {
                if (raw.length() == 0) {
                    return i;
                }
                int lastIndex = raw.length() - 1;
                if (raw.charAt(lastIndex) == ' ') {
                    raw.setLength(lastIndex);
                }
                return i;
            }
            if (PatternProps.isWhiteSpace(c)) {
                raw.append(' ');
                i = this.skipWhiteSpace(i + 1);
                continue;
            }
            raw.append(c);
            ++i;
        }
        return 0;
    }

    private int skipComment(int i) {
        char c;
        while (i < this.rules.length() && (c = this.rules.charAt(i++)) != '\n' && c != '\f' && c != '\r' && c != '\u0085' && c != '\u2028' && c != '\u2029') {
        }
        return i;
    }

    private void setParseError(String reason) throws ParseException {
        throw this.makeParseException(reason);
    }

    private void setParseError(String reason, Exception e) throws ParseException {
        ParseException newExc = this.makeParseException(reason + ": " + e.getMessage());
        newExc.initCause(e);
        throw newExc;
    }

    private ParseException makeParseException(String reason) {
        return new ParseException(this.appendErrorContext(reason), this.ruleIndex);
    }

    private String appendErrorContext(String reason) {
        StringBuilder msg = new StringBuilder(reason);
        msg.append(" at index ").append(this.ruleIndex);
        msg.append(" near \"");
        int start = this.ruleIndex - 15;
        if (start < 0) {
            start = 0;
        } else if (start > 0 && Character.isLowSurrogate(this.rules.charAt(start))) {
            ++start;
        }
        msg.append(this.rules, start, this.ruleIndex);
        msg.append('!');
        int length = this.rules.length() - this.ruleIndex;
        if (length >= 16 && Character.isHighSurrogate(this.rules.charAt(this.ruleIndex + (length = 15) - 1))) {
            --length;
        }
        msg.append(this.rules, this.ruleIndex, this.ruleIndex + length);
        return msg.append('\"').toString();
    }

    private static boolean isSyntaxChar(int c) {
        return 33 <= c && c <= 126 && (c <= 47 || 58 <= c && c <= 64 || 91 <= c && c <= 96 || 123 <= c);
    }

    private int skipWhiteSpace(int i) {
        while (i < this.rules.length() && PatternProps.isWhiteSpace(this.rules.charAt(i))) {
            ++i;
        }
        return i;
    }

    static interface Importer {
        public String getRules(String var1, String var2);
    }

    static abstract class Sink {
        Sink() {
        }

        abstract void addReset(int var1, CharSequence var2);

        abstract void addRelation(int var1, CharSequence var2, CharSequence var3, CharSequence var4);

        void suppressContractions(UnicodeSet set) {
        }

        void optimize(UnicodeSet set) {
        }
    }

    static enum Position {
        FIRST_TERTIARY_IGNORABLE,
        LAST_TERTIARY_IGNORABLE,
        FIRST_SECONDARY_IGNORABLE,
        LAST_SECONDARY_IGNORABLE,
        FIRST_PRIMARY_IGNORABLE,
        LAST_PRIMARY_IGNORABLE,
        FIRST_VARIABLE,
        LAST_VARIABLE,
        FIRST_REGULAR,
        LAST_REGULAR,
        FIRST_IMPLICIT,
        LAST_IMPLICIT,
        FIRST_TRAILING,
        LAST_TRAILING;

    }
}

