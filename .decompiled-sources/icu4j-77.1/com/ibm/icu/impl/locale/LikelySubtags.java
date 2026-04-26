/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.locale;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.impl.locale.LSR;
import com.ibm.icu.util.BytesTrie;
import com.ibm.icu.util.Region;
import com.ibm.icu.util.ULocale;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.TreeMap;

public final class LikelySubtags {
    private static final String PSEUDO_ACCENTS_PREFIX = "'";
    private static final String PSEUDO_BIDI_PREFIX = "+";
    private static final String PSEUDO_CRACKED_PREFIX = ",";
    public static final int SKIP_SCRIPT = 1;
    private static final boolean DEBUG_OUTPUT = false;
    public static final LikelySubtags INSTANCE = new LikelySubtags(Data.load());
    private final Map<String, String> languageAliases;
    private final Map<String, String> regionAliases;
    private final BytesTrie trie;
    private final long trieUndState;
    private final long trieUndZzzzState;
    private final int defaultLsrIndex;
    private final long[] trieFirstLetterStates = new long[26];
    private final LSR[] lsrs;

    private LikelySubtags(Data data) {
        this.languageAliases = data.languageAliases;
        this.regionAliases = data.regionAliases;
        this.trie = new BytesTrie(data.trie, 0);
        this.lsrs = data.lsrs;
        BytesTrie.Result result = this.trie.next(42);
        assert (result.hasNext());
        this.trieUndState = this.trie.getState64();
        result = this.trie.next(42);
        assert (result.hasNext());
        this.trieUndZzzzState = this.trie.getState64();
        result = this.trie.next(42);
        assert (result.hasValue());
        this.defaultLsrIndex = this.trie.getValue();
        this.trie.reset();
        for (int c = 97; c <= 122; c = (int)((char)(c + 1))) {
            result = this.trie.next(c);
            if (result == BytesTrie.Result.NO_VALUE) {
                this.trieFirstLetterStates[c - 97] = this.trie.getState64();
            }
            this.trie.reset();
        }
    }

    public ULocale canonicalize(ULocale locale) {
        String lang = locale.getLanguage();
        String lang2 = this.languageAliases.get(lang);
        String region = locale.getCountry();
        String region2 = this.regionAliases.get(region);
        if (lang2 != null || region2 != null) {
            return new ULocale(lang2 == null ? lang : lang2, locale.getScript(), region2 == null ? region : region2);
        }
        return locale;
    }

    private static String getCanonical(Map<String, String> aliases, String alias) {
        String canonical = aliases.get(alias);
        return canonical == null ? alias : canonical;
    }

    public LSR makeMaximizedLsrFrom(ULocale locale, boolean returnInputIfUnmatch) {
        String name = locale.getName();
        if (name.startsWith("@x=")) {
            String tag = locale.toLanguageTag();
            assert (tag.startsWith("und-x-"));
            return new LSR(tag, "", "", 7);
        }
        LSR max = this.makeMaximizedLsr(locale.getLanguage(), locale.getScript(), locale.getCountry(), locale.getVariant(), returnInputIfUnmatch);
        if (max.language.isEmpty() && max.script.isEmpty() && max.region.isEmpty()) {
            return new LSR(locale.getLanguage(), locale.getScript(), locale.getCountry(), 7);
        }
        return max;
    }

    public LSR makeMaximizedLsrFrom(Locale locale) {
        String tag = locale.toLanguageTag();
        if (tag.startsWith("x-") || tag.startsWith("und-x-")) {
            return new LSR(tag, "", "", 7);
        }
        return this.makeMaximizedLsr(locale.getLanguage(), locale.getScript(), locale.getCountry(), locale.getVariant(), false);
    }

    private LSR makeMaximizedLsr(String language, String script, String region, String variant, boolean returnInputIfUnmatch) {
        if (!returnInputIfUnmatch) {
            if (region.length() == 2 && region.charAt(0) == 'X') {
                switch (region.charAt(1)) {
                    case 'A': {
                        return new LSR(PSEUDO_ACCENTS_PREFIX + language, PSEUDO_ACCENTS_PREFIX + script, region, 7);
                    }
                    case 'B': {
                        return new LSR(PSEUDO_BIDI_PREFIX + language, PSEUDO_BIDI_PREFIX + script, region, 7);
                    }
                    case 'C': {
                        return new LSR(PSEUDO_CRACKED_PREFIX + language, PSEUDO_CRACKED_PREFIX + script, region, 7);
                    }
                }
            }
            if (variant.startsWith("PS")) {
                int lsrFlags = region.isEmpty() ? 6 : 7;
                switch (variant) {
                    case "PSACCENT": {
                        return new LSR(PSEUDO_ACCENTS_PREFIX + language, PSEUDO_ACCENTS_PREFIX + script, region.isEmpty() ? "XA" : region, lsrFlags);
                    }
                    case "PSBIDI": {
                        return new LSR(PSEUDO_BIDI_PREFIX + language, PSEUDO_BIDI_PREFIX + script, region.isEmpty() ? "XB" : region, lsrFlags);
                    }
                    case "PSCRACK": {
                        return new LSR(PSEUDO_CRACKED_PREFIX + language, PSEUDO_CRACKED_PREFIX + script, region.isEmpty() ? "XC" : region, lsrFlags);
                    }
                }
            }
        }
        language = LikelySubtags.getCanonical(this.languageAliases, language);
        region = LikelySubtags.getCanonical(this.regionAliases, region);
        return this.maximize(language, script, region, returnInputIfUnmatch);
    }

    private boolean isMacroregion(String region) {
        Region.RegionType type = Region.getInstance(region).getType();
        return type == Region.RegionType.WORLD || type == Region.RegionType.CONTINENT || type == Region.RegionType.SUBCONTINENT;
    }

    private LSR maximize(String language, String script, String region, boolean returnInputIfUnmatch) {
        long state;
        int c0;
        if (language.equals("und")) {
            language = "";
        }
        if (script.equals("Zzzz")) {
            script = "";
        }
        if (region.equals("ZZ")) {
            region = "";
        }
        if (!(script.isEmpty() || region.isEmpty() || language.isEmpty())) {
            return new LSR(language, script, region, 7);
        }
        boolean retainLanguage = false;
        boolean retainScript = false;
        boolean retainRegion = false;
        BytesTrie iter = new BytesTrie(this.trie);
        int value = language.length() >= 2 && 0 <= (c0 = language.charAt(0) - 97) && c0 <= 25 && (state = this.trieFirstLetterStates[c0]) != 0L ? LikelySubtags.trieNext(iter.resetToState64(state), language, 1) : LikelySubtags.trieNext(iter, language, 0);
        boolean matchLanguage = value >= 0;
        boolean matchScript = false;
        if (value >= 0) {
            retainLanguage = !language.isEmpty();
            state = iter.getState64();
        } else {
            retainLanguage = true;
            iter.resetToState64(this.trieUndState);
            state = 0L;
        }
        if (value >= 0 && !script.isEmpty()) {
            matchScript = true;
        }
        if (value > 0) {
            if (value == 1) {
                value = 0;
            }
            retainScript = !script.isEmpty();
        } else {
            value = LikelySubtags.trieNext(iter, script, 0);
            if (value >= 0) {
                retainScript = !script.isEmpty();
                state = iter.getState64();
            } else {
                retainScript = true;
                if (state == 0L) {
                    iter.resetToState64(this.trieUndZzzzState);
                } else {
                    iter.resetToState64(state);
                    value = LikelySubtags.trieNext(iter, "", 0);
                    assert (value >= 0);
                    state = iter.getState64();
                }
            }
        }
        boolean matchRegion = false;
        if (value > 0) {
            retainRegion = !region.isEmpty();
        } else {
            value = LikelySubtags.trieNext(iter, region, 0);
            if (value >= 0) {
                if (!region.isEmpty() && !this.isMacroregion(region)) {
                    retainRegion = true;
                    matchRegion = true;
                }
            } else {
                retainRegion = true;
                if (state == 0L) {
                    value = this.defaultLsrIndex;
                } else {
                    iter.resetToState64(state);
                    value = LikelySubtags.trieNext(iter, "", 0);
                    assert (value > 0);
                }
            }
        }
        LSR result = this.lsrs[value];
        if (!(!returnInputIfUnmatch || matchLanguage || matchScript || matchRegion && language.isEmpty())) {
            return new LSR("", "", "", 7);
        }
        if (language.isEmpty()) {
            language = "und";
        }
        if (!(retainLanguage || retainScript || retainRegion)) {
            assert (result.flags == 0);
            return result;
        }
        if (!retainLanguage) {
            language = result.language;
        }
        if (!retainScript) {
            script = result.script;
        }
        if (!retainRegion) {
            region = result.region;
        }
        int retainMask = (retainLanguage ? 4 : 0) + (retainScript ? 2 : 0) + (retainRegion ? 1 : 0);
        return new LSR(language, script, region, retainMask);
    }

    int compareLikely(LSR lsr, LSR other, int likelyInfo) {
        if (!lsr.language.equals(other.language)) {
            return -4;
        }
        if (!lsr.script.equals(other.script)) {
            int index;
            if (likelyInfo >= 0 && (likelyInfo & 2) == 0) {
                index = likelyInfo >> 2;
            } else {
                index = this.getLikelyIndex(lsr.language, "");
                likelyInfo = index << 2;
            }
            LSR likely = this.lsrs[index];
            if (lsr.script.equals(likely.script)) {
                return likelyInfo | 1;
            }
            return likelyInfo & 0xFFFFFFFE;
        }
        if (!lsr.region.equals(other.region)) {
            int index;
            if (likelyInfo >= 0 && (likelyInfo & 2) != 0) {
                index = likelyInfo >> 2;
            } else {
                index = this.getLikelyIndex(lsr.language, lsr.region);
                likelyInfo = index << 2 | 2;
            }
            LSR likely = this.lsrs[index];
            if (lsr.region.equals(likely.region)) {
                return likelyInfo | 1;
            }
            return likelyInfo & 0xFFFFFFFE;
        }
        return likelyInfo & 0xFFFFFFFE;
    }

    private int getLikelyIndex(String language, String script) {
        long state;
        int c0;
        if (language.equals("und")) {
            language = "";
        }
        if (script.equals("Zzzz")) {
            script = "";
        }
        BytesTrie iter = new BytesTrie(this.trie);
        int value = language.length() >= 2 && 0 <= (c0 = language.charAt(0) - 97) && c0 <= 25 && (state = this.trieFirstLetterStates[c0]) != 0L ? LikelySubtags.trieNext(iter.resetToState64(state), language, 1) : LikelySubtags.trieNext(iter, language, 0);
        if (value >= 0) {
            state = iter.getState64();
        } else {
            iter.resetToState64(this.trieUndState);
            state = 0L;
        }
        if (value > 0) {
            if (value == 1) {
                value = 0;
            }
        } else {
            value = LikelySubtags.trieNext(iter, script, 0);
            if (value >= 0) {
                state = iter.getState64();
            } else if (state == 0L) {
                iter.resetToState64(this.trieUndZzzzState);
            } else {
                iter.resetToState64(state);
                value = LikelySubtags.trieNext(iter, "", 0);
                assert (value >= 0);
                state = iter.getState64();
            }
        }
        if (value <= 0) {
            value = LikelySubtags.trieNext(iter, "", 0);
            assert (value > 0);
        }
        return value;
    }

    private static final int trieNext(BytesTrie iter, String s, int i) {
        BytesTrie.Result result;
        if (s.isEmpty()) {
            result = iter.next(42);
        } else {
            int end = s.length() - 1;
            while (true) {
                char c = s.charAt(i);
                if (i >= end) {
                    result = iter.next(c | 0x80);
                    break;
                }
                if (!iter.next(c).hasNext()) {
                    return -1;
                }
                ++i;
            }
        }
        switch (result) {
            case NO_MATCH: {
                return -1;
            }
            case NO_VALUE: {
                return 0;
            }
            case INTERMEDIATE_VALUE: {
                assert (iter.getValue() == 1);
                return 1;
            }
            case FINAL_VALUE: {
                return iter.getValue();
            }
        }
        return -1;
    }

    public LSR minimizeSubtags(String languageIn, String scriptIn, String regionIn, ULocale.Minimize fieldToFavor) {
        LSR max = this.maximize(languageIn, scriptIn, regionIn, true);
        if (max.language.isEmpty() && max.region.isEmpty() && max.script.isEmpty()) {
            return new LSR(languageIn, scriptIn, regionIn, 7);
        }
        LSR test = this.maximize(max.language, "", "", true);
        if (test.isEquivalentTo(max)) {
            return new LSR(max.language, "", "", 0);
        }
        if (ULocale.Minimize.FAVOR_REGION == fieldToFavor) {
            test = this.maximize(max.language, "", max.region, true);
            if (test.isEquivalentTo(max)) {
                return new LSR(max.language, "", max.region, 0);
            }
            test = this.maximize(max.language, max.script, "", true);
            if (test.isEquivalentTo(max)) {
                return new LSR(max.language, max.script, "", 0);
            }
        } else {
            test = this.maximize(max.language, max.script, "", true);
            if (test.isEquivalentTo(max)) {
                return new LSR(max.language, max.script, "", 0);
            }
            test = this.maximize(max.language, "", max.region, true);
            if (test.isEquivalentTo(max)) {
                return new LSR(max.language, "", max.region, 0);
            }
        }
        return new LSR(max.language, max.script, max.region, 0);
    }

    private Map<String, LSR> getTable() {
        TreeMap<String, LSR> map = new TreeMap<String, LSR>();
        StringBuilder sb = new StringBuilder();
        for (BytesTrie.Entry entry : this.trie) {
            sb.setLength(0);
            int length = entry.bytesLength();
            int i = 0;
            while (i < length) {
                byte b;
                if ((b = entry.byteAt(i++)) == 42) {
                    sb.append("*-");
                    continue;
                }
                if (b >= 0) {
                    sb.append((char)b);
                    continue;
                }
                sb.append((char)(b & 0x7F)).append('-');
            }
            assert (sb.length() > 0 && sb.charAt(sb.length() - 1) == '-');
            sb.setLength(sb.length() - 1);
            map.put(sb.toString(), this.lsrs[entry.value]);
        }
        return map;
    }

    public String toString() {
        return this.getTable().toString();
    }

    public static final class Data {
        public final Map<String, String> languageAliases;
        public final Map<String, String> regionAliases;
        public final byte[] trie;
        public final LSR[] lsrs;

        public Data(Map<String, String> languageAliases, Map<String, String> regionAliases, byte[] trie, LSR[] lsrs) {
            this.languageAliases = languageAliases;
            this.regionAliases = regionAliases;
            this.trie = trie;
            this.lsrs = lsrs;
        }

        private static UResource.Value getValue(UResource.Table table, String key, UResource.Value value) {
            if (!table.findValue(key, value)) {
                throw new MissingResourceException("langInfo.res missing data", "", "likely/" + key);
            }
            return value;
        }

        public static Data load() throws MissingResourceException {
            Map<String, String> regionAliases;
            Map<String, String> languageAliases;
            ICUResourceBundle langInfo = ICUResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "langInfo", ICUResourceBundle.ICU_DATA_CLASS_LOADER, ICUResourceBundle.OpenType.DIRECT);
            UResource.Value value = langInfo.getValueWithFallback("likely");
            UResource.Table likelyTable = value.getTable();
            if (likelyTable.findValue("languageAliases", value)) {
                String[] pairs = value.getStringArray();
                languageAliases = new HashMap(pairs.length / 2);
                for (int i = 0; i < pairs.length; i += 2) {
                    languageAliases.put(pairs[i], pairs[i + 1]);
                }
            } else {
                languageAliases = Collections.emptyMap();
            }
            if (likelyTable.findValue("regionAliases", value)) {
                String[] pairs = value.getStringArray();
                regionAliases = new HashMap(pairs.length / 2);
                for (int i = 0; i < pairs.length; i += 2) {
                    regionAliases.put(pairs[i], pairs[i + 1]);
                }
            } else {
                regionAliases = Collections.emptyMap();
            }
            ByteBuffer buffer = Data.getValue(likelyTable, "trie", value).getBinary();
            byte[] trie = new byte[buffer.remaining()];
            buffer.get(trie);
            String[] m49 = Data.getValue(likelyTable, "m49", value).getStringArray();
            LSR[] lsrs = LSR.decodeInts(Data.getValue(likelyTable, "lsrnum", value).getIntVector(), m49);
            return new Data(languageAliases, regionAliases, trie, lsrs);
        }

        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || !this.getClass().equals(other.getClass())) {
                return false;
            }
            Data od = (Data)other;
            return this.languageAliases.equals(od.languageAliases) && this.regionAliases.equals(od.regionAliases) && Arrays.equals(this.trie, od.trie) && Arrays.equals(this.lsrs, od.lsrs);
        }

        public int hashCode() {
            return 1;
        }
    }
}

