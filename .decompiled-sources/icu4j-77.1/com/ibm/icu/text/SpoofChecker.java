/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.text;

import com.ibm.icu.impl.ICUBinary;
import com.ibm.icu.impl.Utility;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UScript;
import com.ibm.icu.text.Bidi;
import com.ibm.icu.text.Normalizer2;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.ULocale;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpoofChecker {
    public static final UnicodeSet INCLUSION = new UnicodeSet().applyIntPropertyValue(28673, UCharacter.IdentifierType.INCLUSION.ordinal()).freeze();
    public static final UnicodeSet RECOMMENDED = new UnicodeSet().applyIntPropertyValue(28673, UCharacter.IdentifierType.RECOMMENDED.ordinal()).freeze();
    public static final int SINGLE_SCRIPT_CONFUSABLE = 1;
    public static final int MIXED_SCRIPT_CONFUSABLE = 2;
    public static final int WHOLE_SCRIPT_CONFUSABLE = 4;
    public static final int CONFUSABLE = 7;
    @Deprecated
    public static final int ANY_CASE = 8;
    public static final int RESTRICTION_LEVEL = 16;
    @Deprecated
    public static final int SINGLE_SCRIPT = 16;
    public static final int INVISIBLE = 32;
    public static final int CHAR_LIMIT = 64;
    public static final int MIXED_NUMBERS = 128;
    public static final int HIDDEN_OVERLAY = 256;
    public static final int ALL_CHECKS = -1;
    static final UnicodeSet ASCII = new UnicodeSet(0, 127).freeze();
    private int fChecks;
    private SpoofData fSpoofData;
    private Set<ULocale> fAllowedLocales;
    private UnicodeSet fAllowedCharsSet;
    private RestrictionLevel fRestrictionLevel;
    private static Normalizer2 nfdNormalizer = Normalizer2.getNFDInstance();

    private SpoofChecker() {
    }

    @Deprecated
    public RestrictionLevel getRestrictionLevel() {
        return this.fRestrictionLevel;
    }

    public int getChecks() {
        return this.fChecks;
    }

    public Set<ULocale> getAllowedLocales() {
        return Collections.unmodifiableSet(this.fAllowedLocales);
    }

    public Set<Locale> getAllowedJavaLocales() {
        HashSet<Locale> locales = new HashSet<Locale>(this.fAllowedLocales.size());
        for (ULocale uloc : this.fAllowedLocales) {
            locales.add(uloc.toLocale());
        }
        return locales;
    }

    public UnicodeSet getAllowedChars() {
        return this.fAllowedCharsSet;
    }

    public boolean failsChecks(String text, CheckResult checkResult) {
        int index;
        int length = text.length();
        int result = 0;
        if (checkResult != null) {
            checkResult.position = 0;
            checkResult.numerics = null;
            checkResult.restrictionLevel = null;
        }
        if (0 != (this.fChecks & 0x10)) {
            RestrictionLevel textRestrictionLevel = this.getRestrictionLevel(text);
            if (textRestrictionLevel.compareTo(this.fRestrictionLevel) > 0) {
                result |= 0x10;
            }
            if (checkResult != null) {
                checkResult.restrictionLevel = textRestrictionLevel;
            }
        }
        if (0 != (this.fChecks & 0x80)) {
            UnicodeSet numerics = new UnicodeSet();
            this.getNumerics(text, numerics);
            if (numerics.size() > 1) {
                result |= 0x80;
            }
            if (checkResult != null) {
                checkResult.numerics = numerics;
            }
        }
        if (0 != (this.fChecks & 0x100) && (index = this.findHiddenOverlay(text)) != -1) {
            result |= 0x100;
        }
        if (0 != (this.fChecks & 0x40)) {
            int i = 0;
            while (i < length) {
                int c = Character.codePointAt(text, i);
                i = Character.offsetByCodePoints(text, i, 1);
                if (this.fAllowedCharsSet.contains(c)) continue;
                result |= 0x40;
                break;
            }
        }
        if (0 != (this.fChecks & 0x20)) {
            String nfdText = nfdNormalizer.normalize(text);
            int firstNonspacingMark = 0;
            boolean haveMultipleMarks = false;
            UnicodeSet marksSeenSoFar = new UnicodeSet();
            int i = 0;
            while (i < length) {
                int c = Character.codePointAt(nfdText, i);
                i = Character.offsetByCodePoints(nfdText, i, 1);
                if (Character.getType(c) != 6) {
                    firstNonspacingMark = 0;
                    if (!haveMultipleMarks) continue;
                    marksSeenSoFar.clear();
                    haveMultipleMarks = false;
                    continue;
                }
                if (firstNonspacingMark == 0) {
                    firstNonspacingMark = c;
                    continue;
                }
                if (!haveMultipleMarks) {
                    marksSeenSoFar.add(firstNonspacingMark);
                    haveMultipleMarks = true;
                }
                if (marksSeenSoFar.contains(c)) {
                    result |= 0x20;
                    break;
                }
                marksSeenSoFar.add(c);
            }
        }
        if (checkResult != null) {
            checkResult.checks = result;
        }
        return 0 != result;
    }

    public boolean failsChecks(String text) {
        return this.failsChecks(text, null);
    }

    public int areConfusable(String s1, String s2) {
        String s2Skeleton;
        if ((this.fChecks & 7) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        }
        String s1Skeleton = this.getSkeleton(s1);
        if (!s1Skeleton.equals(s2Skeleton = this.getSkeleton(s2))) {
            return 0;
        }
        ScriptSet s1RSS = new ScriptSet();
        this.getResolvedScriptSet(s1, s1RSS);
        ScriptSet s2RSS = new ScriptSet();
        this.getResolvedScriptSet(s2, s2RSS);
        int result = 0;
        if (s1RSS.intersects(s2RSS)) {
            result |= 1;
        } else {
            result |= 2;
            if (!s1RSS.isEmpty() && !s2RSS.isEmpty()) {
                result |= 4;
            }
        }
        return result & this.fChecks;
    }

    public int areConfusable(int direction, CharSequence s1, CharSequence s2) {
        String s2Skeleton;
        if ((this.fChecks & 7) == 0) {
            throw new IllegalArgumentException("No confusable checks are enabled.");
        }
        String s1Skeleton = this.getBidiSkeleton(direction, s1);
        if (!s1Skeleton.equals(s2Skeleton = this.getBidiSkeleton(direction, s2))) {
            return 0;
        }
        ScriptSet s1RSS = new ScriptSet();
        this.getResolvedScriptSet(s1, s1RSS);
        ScriptSet s2RSS = new ScriptSet();
        this.getResolvedScriptSet(s2, s2RSS);
        int result = 0;
        if (s1RSS.intersects(s2RSS)) {
            result |= 1;
        } else {
            result |= 2;
            if (!s1RSS.isEmpty() && !s2RSS.isEmpty()) {
                result |= 4;
            }
        }
        return result &= this.fChecks;
    }

    public String getBidiSkeleton(int direction, CharSequence str) {
        if (direction != 0 && direction != 1) {
            throw new IllegalArgumentException("direction should be DIRECTION_LEFT_TO_RIGHT or DIRECTION_RIGHT_TO_LEFT");
        }
        Bidi bidi = new Bidi(str.toString(), direction);
        return this.getSkeleton(bidi.writeReordered(3));
    }

    public String getSkeleton(CharSequence str) {
        int c;
        String nfdId = nfdNormalizer.normalize(str);
        int normalizedLen = nfdId.length();
        StringBuilder skelSB = new StringBuilder();
        for (int inputIndex = 0; inputIndex < normalizedLen; inputIndex += Character.charCount(c)) {
            c = Character.codePointAt(nfdId, inputIndex);
            if (UCharacter.hasBinaryProperty(c, 5)) continue;
            this.fSpoofData.confusableLookup(c, skelSB);
        }
        String skelStr = skelSB.toString();
        skelStr = nfdNormalizer.normalize(skelStr);
        return skelStr;
    }

    @Deprecated
    public String getSkeleton(int type, String id) {
        return this.getSkeleton(id);
    }

    public boolean equals(Object other) {
        if (!(other instanceof SpoofChecker)) {
            return false;
        }
        SpoofChecker otherSC = (SpoofChecker)other;
        if (this.fSpoofData != otherSC.fSpoofData && this.fSpoofData != null && !this.fSpoofData.equals(otherSC.fSpoofData)) {
            return false;
        }
        if (this.fChecks != otherSC.fChecks) {
            return false;
        }
        if (this.fAllowedLocales != otherSC.fAllowedLocales && this.fAllowedLocales != null && !this.fAllowedLocales.equals(otherSC.fAllowedLocales)) {
            return false;
        }
        if (this.fAllowedCharsSet != otherSC.fAllowedCharsSet && this.fAllowedCharsSet != null && !this.fAllowedCharsSet.equals(otherSC.fAllowedCharsSet)) {
            return false;
        }
        return this.fRestrictionLevel == otherSC.fRestrictionLevel;
    }

    public int hashCode() {
        return this.fChecks ^ this.fSpoofData.hashCode() ^ this.fAllowedLocales.hashCode() ^ this.fAllowedCharsSet.hashCode() ^ this.fRestrictionLevel.ordinal();
    }

    private static void getAugmentedScriptSet(int codePoint, ScriptSet result) {
        result.clear();
        UScript.getScriptExtensions(codePoint, result);
        if (result.get(17)) {
            result.set(172);
            result.set(105);
            result.set(119);
        }
        if (result.get(20)) {
            result.set(105);
        }
        if (result.get(22)) {
            result.set(105);
        }
        if (result.get(18)) {
            result.set(119);
        }
        if (result.get(5)) {
            result.set(172);
        }
        if (result.get(0) || result.get(1)) {
            result.setAll();
        }
    }

    private void getResolvedScriptSet(CharSequence input, ScriptSet result) {
        this.getResolvedScriptSetWithout(input, 208, result);
    }

    private void getResolvedScriptSetWithout(CharSequence input, int script, ScriptSet result) {
        int codePoint;
        result.setAll();
        ScriptSet temp = new ScriptSet();
        for (int utf16Offset = 0; utf16Offset < input.length(); utf16Offset += Character.charCount(codePoint)) {
            codePoint = Character.codePointAt(input, utf16Offset);
            SpoofChecker.getAugmentedScriptSet(codePoint, temp);
            if (script != 208 && temp.get(script)) continue;
            result.and(temp);
        }
    }

    private void getNumerics(String input, UnicodeSet result) {
        int codePoint;
        result.clear();
        for (int utf16Offset = 0; utf16Offset < input.length(); utf16Offset += Character.charCount(codePoint)) {
            codePoint = Character.codePointAt(input, utf16Offset);
            if (UCharacter.getType(codePoint) != 9) continue;
            result.add(codePoint - UCharacter.getNumericValue(codePoint));
        }
    }

    private RestrictionLevel getRestrictionLevel(String input) {
        if (!this.fAllowedCharsSet.containsAll(input)) {
            return RestrictionLevel.UNRESTRICTIVE;
        }
        if (ASCII.containsAll(input)) {
            return RestrictionLevel.ASCII;
        }
        ScriptSet resolvedScriptSet = new ScriptSet();
        this.getResolvedScriptSet(input, resolvedScriptSet);
        if (!resolvedScriptSet.isEmpty()) {
            return RestrictionLevel.SINGLE_SCRIPT_RESTRICTIVE;
        }
        ScriptSet resolvedNoLatn = new ScriptSet();
        this.getResolvedScriptSetWithout(input, 25, resolvedNoLatn);
        if (resolvedNoLatn.get(172) || resolvedNoLatn.get(105) || resolvedNoLatn.get(119)) {
            return RestrictionLevel.HIGHLY_RESTRICTIVE;
        }
        if (!(resolvedNoLatn.isEmpty() || resolvedNoLatn.get(8) || resolvedNoLatn.get(14) || resolvedNoLatn.get(6))) {
            return RestrictionLevel.MODERATELY_RESTRICTIVE;
        }
        return RestrictionLevel.MINIMALLY_RESTRICTIVE;
    }

    int findHiddenOverlay(String input) {
        int cp;
        boolean sawLeadCharacter = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i += UCharacter.charCount(cp)) {
            cp = input.codePointAt(i);
            if (sawLeadCharacter && cp == 775) {
                return i;
            }
            int combiningClass = UCharacter.getCombiningClass(cp);
            assert (UCharacter.getCombiningClass(775) == 230);
            if (combiningClass != 0 && combiningClass != 230) continue;
            sawLeadCharacter = this.isIllegalCombiningDotLeadCharacter(cp, sb);
        }
        return -1;
    }

    boolean isIllegalCombiningDotLeadCharacterNoLookup(int cp) {
        return cp == 105 || cp == 106 || cp == 305 || cp == 567 || cp == 108 || UCharacter.hasBinaryProperty(cp, 27);
    }

    boolean isIllegalCombiningDotLeadCharacter(int cp, StringBuilder sb) {
        if (this.isIllegalCombiningDotLeadCharacterNoLookup(cp)) {
            return true;
        }
        sb.setLength(0);
        this.fSpoofData.confusableLookup(cp, sb);
        int finalCp = UCharacter.codePointBefore(sb, sb.length());
        return finalCp != cp && this.isIllegalCombiningDotLeadCharacterNoLookup(finalCp);
    }

    static class ScriptSet
    extends BitSet {
        private static final long serialVersionUID = 1L;

        ScriptSet() {
        }

        public void and(int script) {
            this.clear(0, script);
            this.clear(script + 1, 208);
        }

        public void setAll() {
            this.set(0, 208);
        }

        public boolean isFull() {
            return this.cardinality() == 208;
        }

        public void appendStringTo(StringBuilder sb) {
            sb.append("{ ");
            if (this.isEmpty()) {
                sb.append("- ");
            } else if (this.isFull()) {
                sb.append("* ");
            } else {
                for (int script = 0; script < 208; ++script) {
                    if (!this.get(script)) continue;
                    sb.append(UScript.getShortName(script));
                    sb.append(" ");
                }
            }
            sb.append("}");
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("<ScriptSet ");
            this.appendStringTo(sb);
            sb.append(">");
            return sb.toString();
        }
    }

    private static class SpoofData {
        int[] fCFUKeys;
        short[] fCFUValues;
        String fCFUStrings;
        private static final int DATA_FORMAT = 1130788128;
        private static final IsAcceptable IS_ACCEPTABLE = new IsAcceptable();

        public static SpoofData getDefault() {
            if (DefaultData.EXCEPTION != null) {
                throw new MissingResourceException("Could not load default confusables data: " + DefaultData.EXCEPTION.getMessage(), "SpoofChecker", "");
            }
            return DefaultData.INSTANCE;
        }

        private SpoofData() {
        }

        private SpoofData(ByteBuffer bytes) throws IOException {
            ICUBinary.readHeader(bytes, 1130788128, IS_ACCEPTABLE);
            bytes.mark();
            this.readData(bytes);
        }

        public boolean equals(Object other) {
            if (!(other instanceof SpoofData)) {
                return false;
            }
            SpoofData otherData = (SpoofData)other;
            if (!Arrays.equals(this.fCFUKeys, otherData.fCFUKeys)) {
                return false;
            }
            if (!Arrays.equals(this.fCFUValues, otherData.fCFUValues)) {
                return false;
            }
            return Utility.sameObjects(this.fCFUStrings, otherData.fCFUStrings) || this.fCFUStrings == null || this.fCFUStrings.equals(otherData.fCFUStrings);
        }

        public int hashCode() {
            return Arrays.hashCode(this.fCFUKeys) ^ Arrays.hashCode(this.fCFUValues) ^ this.fCFUStrings.hashCode();
        }

        private void readData(ByteBuffer bytes) throws IOException {
            int magic = bytes.getInt();
            if (magic != 944111087) {
                throw new IllegalArgumentException("Bad Spoof Check Data.");
            }
            int dataFormatVersion = bytes.getInt();
            int dataLength = bytes.getInt();
            int CFUKeysOffset = bytes.getInt();
            int CFUKeysSize = bytes.getInt();
            int CFUValuesOffset = bytes.getInt();
            int CFUValuesSize = bytes.getInt();
            int CFUStringTableOffset = bytes.getInt();
            int CFUStringTableSize = bytes.getInt();
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUKeysOffset);
            this.fCFUKeys = ICUBinary.getInts(bytes, CFUKeysSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUValuesOffset);
            this.fCFUValues = ICUBinary.getShorts(bytes, CFUValuesSize, 0);
            bytes.reset();
            ICUBinary.skipBytes(bytes, CFUStringTableOffset);
            this.fCFUStrings = ICUBinary.getString(bytes, CFUStringTableSize, 0);
        }

        public void confusableLookup(int inChar, StringBuilder dest) {
            int lo = 0;
            int hi = this.length();
            do {
                int mid;
                if (this.codePointAt(mid = (lo + hi) / 2) > inChar) {
                    hi = mid;
                    continue;
                }
                if (this.codePointAt(mid) < inChar) {
                    lo = mid;
                    continue;
                }
                lo = mid;
                break;
            } while (hi - lo > 1);
            if (this.codePointAt(lo) != inChar) {
                dest.appendCodePoint(inChar);
                return;
            }
            this.appendValueTo(lo, dest);
        }

        public int length() {
            return this.fCFUKeys.length;
        }

        public int codePointAt(int index) {
            return ConfusableDataUtils.keyToCodePoint(this.fCFUKeys[index]);
        }

        public void appendValueTo(int index, StringBuilder dest) {
            int stringLength = ConfusableDataUtils.keyToLength(this.fCFUKeys[index]);
            short value = this.fCFUValues[index];
            if (stringLength == 1) {
                dest.append((char)value);
            } else {
                dest.append(this.fCFUStrings, (int)value, value + stringLength);
            }
        }

        private static final class DefaultData {
            private static SpoofData INSTANCE = null;
            private static IOException EXCEPTION = null;

            private DefaultData() {
            }

            static {
                try {
                    INSTANCE = new SpoofData(ICUBinary.getRequiredData("confusables.cfu"));
                }
                catch (IOException e) {
                    EXCEPTION = e;
                }
            }
        }

        private static final class IsAcceptable
        implements ICUBinary.Authenticate {
            private IsAcceptable() {
            }

            @Override
            public boolean isDataVersionAcceptable(byte[] version) {
                return version[0] == 2 || version[1] != 0 || version[2] != 0 || version[3] != 0;
            }
        }
    }

    private static final class ConfusableDataUtils {
        public static final int FORMAT_VERSION = 2;

        private ConfusableDataUtils() {
        }

        public static final int keyToCodePoint(int key) {
            return key & 0xFFFFFF;
        }

        public static final int keyToLength(int key) {
            return ((key & 0xFF000000) >> 24) + 1;
        }

        public static final int codePointAndLengthToKey(int codePoint, int length) {
            assert ((codePoint & 0xFFFFFF) == codePoint);
            assert (length <= 256);
            return codePoint | length - 1 << 24;
        }
    }

    public static class CheckResult {
        public int checks = 0;
        @Deprecated
        public int position = 0;
        public UnicodeSet numerics;
        public RestrictionLevel restrictionLevel;

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("checks:");
            if (this.checks == 0) {
                sb.append(" none");
            } else if (this.checks == -1) {
                sb.append(" all");
            } else {
                if ((this.checks & 1) != 0) {
                    sb.append(" SINGLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 2) != 0) {
                    sb.append(" MIXED_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 4) != 0) {
                    sb.append(" WHOLE_SCRIPT_CONFUSABLE");
                }
                if ((this.checks & 8) != 0) {
                    sb.append(" ANY_CASE");
                }
                if ((this.checks & 0x10) != 0) {
                    sb.append(" RESTRICTION_LEVEL");
                }
                if ((this.checks & 0x20) != 0) {
                    sb.append(" INVISIBLE");
                }
                if ((this.checks & 0x40) != 0) {
                    sb.append(" CHAR_LIMIT");
                }
                if ((this.checks & 0x80) != 0) {
                    sb.append(" MIXED_NUMBERS");
                }
            }
            sb.append(", numerics: ").append(this.numerics.toPattern(false));
            sb.append(", position: ").append(this.position);
            sb.append(", restrictionLevel: ").append((Object)this.restrictionLevel);
            return sb.toString();
        }
    }

    public static class Builder {
        int fChecks;
        SpoofData fSpoofData;
        final UnicodeSet fAllowedCharsSet = new UnicodeSet(0, 0x10FFFF);
        final Set<ULocale> fAllowedLocales = new LinkedHashSet<ULocale>();
        private RestrictionLevel fRestrictionLevel;

        public Builder() {
            this.fChecks = -1;
            this.fSpoofData = null;
            this.fRestrictionLevel = RestrictionLevel.HIGHLY_RESTRICTIVE;
        }

        public Builder(SpoofChecker src) {
            this.fChecks = src.fChecks;
            this.fSpoofData = src.fSpoofData;
            this.fAllowedCharsSet.set(src.fAllowedCharsSet);
            this.fAllowedLocales.addAll(src.fAllowedLocales);
            this.fRestrictionLevel = src.fRestrictionLevel;
        }

        public SpoofChecker build() {
            if (this.fSpoofData == null) {
                this.fSpoofData = SpoofData.getDefault();
            }
            SpoofChecker result = new SpoofChecker();
            result.fChecks = this.fChecks;
            result.fSpoofData = this.fSpoofData;
            result.fAllowedCharsSet = (UnicodeSet)this.fAllowedCharsSet.clone();
            result.fAllowedCharsSet.freeze();
            result.fAllowedLocales = new HashSet<ULocale>(this.fAllowedLocales);
            result.fRestrictionLevel = this.fRestrictionLevel;
            return result;
        }

        public Builder setData(Reader confusables) throws ParseException, IOException {
            this.fSpoofData = new SpoofData();
            ConfusabledataBuilder.buildConfusableData(confusables, this.fSpoofData);
            return this;
        }

        @Deprecated
        public Builder setData(Reader confusables, Reader confusablesWholeScript) throws ParseException, IOException {
            this.setData(confusables);
            return this;
        }

        public Builder setChecks(int checks) {
            if (0 != (checks & 0)) {
                throw new IllegalArgumentException("Bad Spoof Checks value.");
            }
            this.fChecks = checks & 0xFFFFFFFF;
            return this;
        }

        public Builder setAllowedLocales(Set<ULocale> locales) {
            this.fAllowedCharsSet.clear();
            for (ULocale locale : locales) {
                this.addScriptChars(locale, this.fAllowedCharsSet);
            }
            this.fAllowedLocales.clear();
            if (locales.size() == 0) {
                this.fAllowedCharsSet.add(0, 0x10FFFF);
                this.fChecks &= 0xFFFFFFBF;
                return this;
            }
            UnicodeSet tempSet = new UnicodeSet();
            tempSet.applyIntPropertyValue(4106, 0);
            this.fAllowedCharsSet.addAll(tempSet);
            tempSet.applyIntPropertyValue(4106, 1);
            this.fAllowedCharsSet.addAll(tempSet);
            this.fAllowedLocales.clear();
            this.fAllowedLocales.addAll(locales);
            this.fChecks |= 0x40;
            return this;
        }

        public Builder setAllowedJavaLocales(Set<Locale> locales) {
            HashSet<ULocale> ulocales = new HashSet<ULocale>(locales.size());
            for (Locale locale : locales) {
                ulocales.add(ULocale.forLocale(locale));
            }
            return this.setAllowedLocales(ulocales);
        }

        private void addScriptChars(ULocale locale, UnicodeSet allowedChars) {
            int[] scripts = UScript.getCode(locale);
            if (scripts != null) {
                UnicodeSet tmpSet = new UnicodeSet();
                for (int i = 0; i < scripts.length; ++i) {
                    tmpSet.applyIntPropertyValue(4106, scripts[i]);
                    allowedChars.addAll(tmpSet);
                }
            }
        }

        public Builder setAllowedChars(UnicodeSet chars) {
            this.fAllowedCharsSet.set(chars);
            this.fAllowedLocales.clear();
            this.fChecks |= 0x40;
            return this;
        }

        public Builder setRestrictionLevel(RestrictionLevel restrictionLevel) {
            this.fRestrictionLevel = restrictionLevel;
            this.fChecks |= 0x90;
            return this;
        }

        private static class ConfusabledataBuilder {
            private Hashtable<Integer, SPUString> fTable = new Hashtable();
            private UnicodeSet fKeySet = new UnicodeSet();
            private StringBuffer fStringTable;
            private ArrayList<Integer> fKeyVec = new ArrayList();
            private ArrayList<Integer> fValueVec = new ArrayList();
            private SPUStringPool stringPool = new SPUStringPool();
            private Pattern fParseLine;
            private Pattern fParseHexNum;
            private int fLineNum;

            ConfusabledataBuilder() {
            }

            void build(Reader confusables, SpoofData dest) throws ParseException, IOException {
                int i;
                String line;
                StringBuffer fInput = new StringBuffer();
                LineNumberReader lnr = new LineNumberReader(confusables);
                while ((line = lnr.readLine()) != null) {
                    fInput.append(line);
                    fInput.append('\n');
                }
                this.fParseLine = Pattern.compile("(?m)^[ \\t]*([0-9A-Fa-f]+)[ \\t]+;[ \\t]*([0-9A-Fa-f]+(?:[ \\t]+[0-9A-Fa-f]+)*)[ \\t]*;\\s*(?:(SL)|(SA)|(ML)|(MA))[ \\t]*(?:#.*?)?$|^([ \\t]*(?:#.*?)?)$|^(.*?)$");
                this.fParseHexNum = Pattern.compile("\\s*([0-9A-F]+)");
                if (fInput.charAt(0) == '\ufeff') {
                    fInput.setCharAt(0, ' ');
                }
                Matcher matcher = this.fParseLine.matcher(fInput);
                while (matcher.find()) {
                    ++this.fLineNum;
                    if (matcher.start(7) >= 0) continue;
                    if (matcher.start(8) >= 0) {
                        throw new ParseException("Confusables, line " + this.fLineNum + ": Unrecognized Line: " + matcher.group(8), matcher.start(8));
                    }
                    int keyChar = Integer.parseInt(matcher.group(1), 16);
                    if (keyChar > 0x10FFFF) {
                        throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + matcher.group(1), matcher.start(1));
                    }
                    Matcher m = this.fParseHexNum.matcher(matcher.group(2));
                    StringBuilder mapString = new StringBuilder();
                    while (m.find()) {
                        int c = Integer.parseInt(m.group(1), 16);
                        if (c > 0x10FFFF) {
                            throw new ParseException("Confusables, line " + this.fLineNum + ": Bad code point: " + Integer.toString(c, 16), matcher.start(2));
                        }
                        mapString.appendCodePoint(c);
                    }
                    assert (mapString.length() >= 1);
                    SPUString smapString = this.stringPool.addString(mapString.toString());
                    this.fTable.put(keyChar, smapString);
                    this.fKeySet.add(keyChar);
                }
                this.stringPool.sort();
                this.fStringTable = new StringBuffer();
                int poolSize = this.stringPool.size();
                for (i = 0; i < poolSize; ++i) {
                    SPUString s = this.stringPool.getByIndex(i);
                    int strLen = s.fStr.length();
                    int strIndex = this.fStringTable.length();
                    if (strLen == 1) {
                        s.fCharOrStrTableIndex = s.fStr.charAt(0);
                        continue;
                    }
                    s.fCharOrStrTableIndex = strIndex;
                    this.fStringTable.append(s.fStr);
                }
                for (String keyCharStr : this.fKeySet) {
                    int keyChar = keyCharStr.codePointAt(0);
                    SPUString targetMapping = this.fTable.get(keyChar);
                    assert (targetMapping != null);
                    if (targetMapping.fStr.length() > 256) {
                        throw new IllegalArgumentException("Confusable prototypes cannot be longer than 256 entries.");
                    }
                    int key = ConfusableDataUtils.codePointAndLengthToKey(keyChar, targetMapping.fStr.length());
                    int value = targetMapping.fCharOrStrTableIndex;
                    this.fKeyVec.add(key);
                    this.fValueVec.add(value);
                }
                int numKeys = this.fKeyVec.size();
                dest.fCFUKeys = new int[numKeys];
                int previousCodePoint = 0;
                for (i = 0; i < numKeys; ++i) {
                    int key = this.fKeyVec.get(i);
                    int codePoint = ConfusableDataUtils.keyToCodePoint(key);
                    assert (codePoint > previousCodePoint);
                    dest.fCFUKeys[i] = key;
                    previousCodePoint = codePoint;
                }
                int numValues = this.fValueVec.size();
                assert (numKeys == numValues);
                dest.fCFUValues = new short[numValues];
                i = 0;
                for (int value : this.fValueVec) {
                    assert (value < 65535);
                    dest.fCFUValues[i++] = (short)value;
                }
                dest.fCFUStrings = this.fStringTable.toString();
            }

            public static void buildConfusableData(Reader confusables, SpoofData dest) throws IOException, ParseException {
                ConfusabledataBuilder builder = new ConfusabledataBuilder();
                builder.build(confusables, dest);
            }

            private static class SPUStringPool {
                private Vector<SPUString> fVec = new Vector();
                private Hashtable<String, SPUString> fHash = new Hashtable();

                public int size() {
                    return this.fVec.size();
                }

                public SPUString getByIndex(int index) {
                    SPUString retString = this.fVec.elementAt(index);
                    return retString;
                }

                public SPUString addString(String src) {
                    SPUString hashedString = this.fHash.get(src);
                    if (hashedString == null) {
                        hashedString = new SPUString(src);
                        this.fHash.put(src, hashedString);
                        this.fVec.addElement(hashedString);
                    }
                    return hashedString;
                }

                public void sort() {
                    Collections.sort(this.fVec, SPUStringComparator.INSTANCE);
                }
            }

            private static class SPUStringComparator
            implements Comparator<SPUString> {
                static final SPUStringComparator INSTANCE = new SPUStringComparator();

                private SPUStringComparator() {
                }

                @Override
                public int compare(SPUString sL, SPUString sR) {
                    int lenR;
                    int lenL = sL.fStr.length();
                    if (lenL < (lenR = sR.fStr.length())) {
                        return -1;
                    }
                    if (lenL > lenR) {
                        return 1;
                    }
                    return sL.fStr.compareTo(sR.fStr);
                }
            }

            private static class SPUString {
                String fStr;
                int fCharOrStrTableIndex;

                SPUString(String s) {
                    this.fStr = s;
                    this.fCharOrStrTableIndex = 0;
                }
            }
        }
    }

    public static enum RestrictionLevel {
        ASCII,
        SINGLE_SCRIPT_RESTRICTIVE,
        HIGHLY_RESTRICTIVE,
        MODERATELY_RESTRICTIVE,
        MINIMALLY_RESTRICTIVE,
        UNRESTRICTIVE;

    }
}

