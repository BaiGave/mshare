/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number.range;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import com.ibm.icu.util.UResourceTypeMismatchException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StandardPluralRanges {
    StandardPlural[] flatTriples;
    int numTriples = 0;
    private static volatile Map<String, String> languageToSet;
    public static final StandardPluralRanges DEFAULT;

    private static Map<String, String> getLanguageToSet() {
        Map<String, String> candidate = languageToSet;
        if (candidate == null) {
            HashMap<String, String> map = new HashMap<String, String>();
            PluralRangeSetsDataSink sink = new PluralRangeSetsDataSink(map);
            ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "pluralRanges");
            resource.getAllItemsWithFallback("locales", sink);
            candidate = Collections.unmodifiableMap(map);
        }
        if (languageToSet == null) {
            languageToSet = candidate;
        }
        return languageToSet;
    }

    private static void getPluralRangesData(String set, StandardPluralRanges out) {
        StringBuilder sb = new StringBuilder();
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "pluralRanges");
        sb.setLength(0);
        sb.append("rules/");
        sb.append(set);
        String key = sb.toString();
        PluralRangesDataSink sink = new PluralRangesDataSink(out);
        resource.getAllItemsWithFallback(key, sink);
    }

    public static StandardPluralRanges forLocale(ULocale locale) {
        return StandardPluralRanges.forSet(StandardPluralRanges.getSetForLocale(locale));
    }

    public static StandardPluralRanges forSet(String set) {
        StandardPluralRanges result = new StandardPluralRanges();
        if (set == null) {
            return DEFAULT;
        }
        StandardPluralRanges.getPluralRangesData(set, result);
        return result;
    }

    public static String getSetForLocale(ULocale locale) {
        return StandardPluralRanges.getLanguageToSet().get(locale.getLanguage());
    }

    private StandardPluralRanges() {
    }

    private void addPluralRange(StandardPlural first, StandardPlural second, StandardPlural result) {
        this.flatTriples[3 * this.numTriples] = first;
        this.flatTriples[3 * this.numTriples + 1] = second;
        this.flatTriples[3 * this.numTriples + 2] = result;
        ++this.numTriples;
    }

    private void setCapacity(int length) {
        this.flatTriples = new StandardPlural[length * 3];
    }

    public StandardPlural resolve(StandardPlural first, StandardPlural second) {
        for (int i = 0; i < this.numTriples; ++i) {
            if (first != this.flatTriples[3 * i] || second != this.flatTriples[3 * i + 1]) continue;
            return this.flatTriples[3 * i + 2];
        }
        return StandardPlural.OTHER;
    }

    static {
        DEFAULT = new StandardPluralRanges();
    }

    private static final class PluralRangesDataSink
    extends UResource.Sink {
        StandardPluralRanges output;

        PluralRangesDataSink(StandardPluralRanges output) {
            this.output = output;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Array entriesArray = value.getArray();
            this.output.setCapacity(entriesArray.getSize());
            int i = 0;
            while (entriesArray.getValue(i, value)) {
                UResource.Array pluralFormsArray = value.getArray();
                if (pluralFormsArray.getSize() != 3) {
                    throw new UResourceTypeMismatchException("Expected 3 elements in pluralRanges.txt array");
                }
                pluralFormsArray.getValue(0, value);
                StandardPlural first = StandardPlural.fromString(value.getString());
                pluralFormsArray.getValue(1, value);
                StandardPlural second = StandardPlural.fromString(value.getString());
                pluralFormsArray.getValue(2, value);
                StandardPlural result = StandardPlural.fromString(value.getString());
                this.output.addPluralRange(first, second, result);
                ++i;
            }
        }
    }

    private static final class PluralRangeSetsDataSink
    extends UResource.Sink {
        Map<String, String> output;

        PluralRangeSetsDataSink(Map<String, String> output) {
            this.output = output;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table table = value.getTable();
            int i = 0;
            while (table.getKeyAndValue(i, key, value)) {
                assert (key.toString().equals(new ULocale(key.toString()).getLanguage()));
                this.output.put(key.toString(), value.toString());
                ++i;
            }
        }
    }
}

