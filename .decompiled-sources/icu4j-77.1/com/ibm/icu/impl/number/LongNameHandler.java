/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.CurrencyData;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.PatternProps;
import com.ibm.icu.impl.SimpleFormatterImpl;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.LongNameMultiplexer;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.number.Modifier;
import com.ibm.icu.impl.number.ModifierStore;
import com.ibm.icu.impl.number.RoundingUtils;
import com.ibm.icu.impl.number.SimpleModifier;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.SingleUnitImpl;
import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.ICUException;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import com.ibm.icu.util.UResourceBundle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;

public class LongNameHandler
implements MicroPropsGenerator,
ModifierStore,
LongNameMultiplexer.ParentlessMicroPropsGenerator {
    private static int i = 0;
    private static final int DNAM_INDEX = StandardPlural.COUNT + i++;
    private static final int PER_INDEX = StandardPlural.COUNT + i++;
    private static final int GENDER_INDEX = StandardPlural.COUNT + i++;
    private static final int CONSTANT_DENOMINATOR_INDEX = StandardPlural.COUNT + i++;
    static final int ARRAY_LENGTH = StandardPlural.COUNT + i++;
    private final Map<StandardPlural, SimpleModifier> modifiers;
    private final PluralRules rules;
    private final MicroPropsGenerator parent;
    private String gender = "";

    private static int getIndex(String pluralKeyword) {
        if (pluralKeyword.equals("dnam")) {
            return DNAM_INDEX;
        }
        if (pluralKeyword.equals("per")) {
            return PER_INDEX;
        }
        if (pluralKeyword.equals("gender")) {
            return GENDER_INDEX;
        }
        return StandardPlural.fromString(pluralKeyword).ordinal();
    }

    static String getWithPlural(String[] strings, StandardPlural plural) {
        String result = strings[plural.ordinal()];
        if (result == null) {
            result = strings[StandardPlural.OTHER.ordinal()];
        }
        if (result == null) {
            throw new ICUException("Could not find data in 'other' plural variant");
        }
        return result;
    }

    private static ExtractCorePatternResult extractCorePattern(String pattern) {
        ExtractCorePatternResult result = new ExtractCorePatternResult();
        result.joinerChar = '\u0000';
        int len = pattern.length();
        if (pattern.startsWith("{0}")) {
            result.placeholderPosition = PlaceholderPosition.BEGINNING;
            if (len > 3 && Character.isSpaceChar(pattern.charAt(3))) {
                result.joinerChar = pattern.charAt(3);
                result.coreUnit = pattern.substring(4);
            } else {
                result.coreUnit = pattern.substring(3);
            }
        } else if (pattern.endsWith("{0}")) {
            result.placeholderPosition = PlaceholderPosition.END;
            if (Character.isSpaceChar(pattern.charAt(len - 4))) {
                result.coreUnit = pattern.substring(0, len - 4);
                result.joinerChar = pattern.charAt(len - 4);
            } else {
                result.coreUnit = pattern.substring(0, len - 3);
            }
        } else if (pattern.indexOf("{0}", 1) == -1) {
            result.placeholderPosition = PlaceholderPosition.NONE;
            result.coreUnit = pattern;
        } else {
            result.placeholderPosition = PlaceholderPosition.MIDDLE;
            result.coreUnit = pattern;
        }
        return result;
    }

    private static String getGenderForBuiltin(ULocale locale, MeasureUnit builtinUnit) {
        ICUResourceBundle unitsBundle = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/unit", locale);
        StringBuilder key = new StringBuilder();
        key.append("units/");
        key.append(builtinUnit.getType());
        key.append("/");
        if (builtinUnit.getSubtype() != null && builtinUnit.getSubtype().endsWith("-person")) {
            key.append(builtinUnit.getSubtype(), 0, builtinUnit.getSubtype().length() - 7);
        } else {
            key.append(builtinUnit.getSubtype());
        }
        key.append("/gender");
        try {
            return unitsBundle.getWithFallback(key.toString()).getString();
        }
        catch (MissingResourceException e) {
            return "";
        }
    }

    static void getInflectedMeasureData(String subKey, ULocale locale, NumberFormatter.UnitWidth width, String gender, String caseVariant, String[] outArray) {
        InflectedPluralSink sink = new InflectedPluralSink(gender, caseVariant, outArray);
        ICUResourceBundle unitsBundle = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/unit", locale);
        StringBuilder key = new StringBuilder();
        key.append("units");
        if (width == NumberFormatter.UnitWidth.NARROW) {
            key.append("Narrow");
        } else if (width == NumberFormatter.UnitWidth.SHORT) {
            key.append("Short");
        }
        key.append("/");
        key.append(subKey);
        try {
            unitsBundle.getAllItemsWithFallback(key.toString(), sink);
            if (width == NumberFormatter.UnitWidth.SHORT) {
                return;
            }
        }
        catch (MissingResourceException missingResourceException) {
            // empty catch block
        }
        unitsBundle.getAllItemsWithFallback(key.toString(), sink);
    }

    static void getMeasureData(ULocale locale, MeasureUnit unit, NumberFormatter.UnitWidth width, String unitDisplayCase, String[] outArray) {
        PluralTableSink sink = new PluralTableSink(outArray);
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/unit", locale);
        StringBuilder subKey = new StringBuilder();
        subKey.append("/");
        subKey.append(unit.getType());
        subKey.append("/");
        String unitSubType = unit.getSubtype();
        ICUResourceBundle metadataResource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "metadata");
        AliasSink aliasSink = new AliasSink();
        metadataResource.getAllItemsWithFallbackNoFail("alias/unit/" + unitSubType, aliasSink);
        if (aliasSink.replacement != null) {
            unitSubType = aliasSink.replacement;
        }
        if (unitSubType != null && unitSubType.endsWith("-person")) {
            subKey.append(unitSubType, 0, unitSubType.length() - 7);
        } else {
            subKey.append(unitSubType);
        }
        if (width != NumberFormatter.UnitWidth.FULL_NAME) {
            StringBuilder genderKey = new StringBuilder();
            genderKey.append("units");
            genderKey.append((CharSequence)subKey);
            genderKey.append("/gender");
            try {
                outArray[LongNameHandler.GENDER_INDEX] = resource.getWithFallback(genderKey.toString()).getString();
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }
        StringBuilder key = new StringBuilder();
        key.append("units");
        if (width == NumberFormatter.UnitWidth.NARROW) {
            key.append("Narrow");
        } else if (width == NumberFormatter.UnitWidth.SHORT) {
            key.append("Short");
        }
        key.append((CharSequence)subKey);
        if (width == NumberFormatter.UnitWidth.FULL_NAME && unitDisplayCase != null && !unitDisplayCase.isEmpty()) {
            StringBuilder caseKey = new StringBuilder();
            caseKey.append((CharSequence)key);
            caseKey.append("/case/");
            caseKey.append(unitDisplayCase);
            try {
                resource.getAllItemsWithFallback(caseKey.toString(), sink);
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }
        try {
            resource.getAllItemsWithFallback(key.toString(), sink);
        }
        catch (MissingResourceException e) {
            throw new IllegalArgumentException("No data for unit " + unit + ", width " + (Object)((Object)width), e);
        }
    }

    private static void getCurrencyLongNameData(ULocale locale, Currency currency, String[] outArray) {
        Map<String, String> data = CurrencyData.provider.getInstance(locale, true).getUnitPatterns();
        PluralRules prules = PluralRules.forLocale(locale);
        Set<String> keywords = prules.getKeywords();
        String otherPattern = data.get("other");
        if (keywords != null && otherPattern != null) {
            for (String string : keywords) {
                if (string.equals("other") || data.containsKey(string)) continue;
                data.put(string, otherPattern);
            }
        }
        for (Map.Entry entry : data.entrySet()) {
            String pluralKeyword = (String)entry.getKey();
            int index = LongNameHandler.getIndex(pluralKeyword);
            String longName = currency.getName(locale, 2, pluralKeyword, null);
            String simpleFormat = (String)entry.getValue();
            outArray[index] = simpleFormat = simpleFormat.replace("{1}", longName);
        }
    }

    private static String getCompoundValue(String compoundKey, ULocale locale, NumberFormatter.UnitWidth width) {
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/unit", locale);
        StringBuilder key = new StringBuilder();
        key.append("units");
        if (width == NumberFormatter.UnitWidth.NARROW) {
            key.append("Narrow");
        } else if (width == NumberFormatter.UnitWidth.SHORT) {
            key.append("Short");
        }
        key.append("/compound/");
        key.append(compoundKey);
        try {
            return resource.getStringWithFallback(key.toString());
        }
        catch (MissingResourceException e) {
            if (width == NumberFormatter.UnitWidth.SHORT) {
                return "";
            }
            try {
                return resource.getStringWithFallback(key.toString());
            }
            catch (MissingResourceException e2) {
                return "";
            }
        }
    }

    private static String getDeriveCompoundRule(ULocale locale, String feature, String structure) {
        ICUResourceBundle stackBundle;
        ICUResourceBundle derivationsBundle = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "grammaticalFeatures");
        derivationsBundle = (ICUResourceBundle)derivationsBundle.get("grammaticalData");
        derivationsBundle = (ICUResourceBundle)derivationsBundle.get("derivations");
        try {
            stackBundle = (ICUResourceBundle)derivationsBundle.get(locale.getLanguage());
        }
        catch (MissingResourceException e) {
            stackBundle = (ICUResourceBundle)derivationsBundle.get("root");
        }
        stackBundle = (ICUResourceBundle)stackBundle.get("compound");
        stackBundle = (ICUResourceBundle)stackBundle.get(feature);
        return stackBundle.getString(structure);
    }

    private static String getDerivedGender(ULocale locale, String structure, String[] data0, String[] data1) {
        String val = LongNameHandler.getDeriveCompoundRule(locale, "gender", structure);
        if (val.length() == 1) {
            switch (val.charAt(0)) {
                case '0': {
                    return data0[GENDER_INDEX];
                }
                case '1': {
                    if (data1 == null) {
                        return null;
                    }
                    return data1[GENDER_INDEX];
                }
            }
        }
        return val;
    }

    private static String calculateGenderForUnit(ULocale locale, MeasureUnit unit) {
        String prefixRule;
        String powerRule;
        MeasureUnitImpl mui = unit.getCopyOfMeasureUnitImpl();
        ArrayList<SingleUnitImpl> singleUnits = mui.getSingleUnits();
        int singleUnitIndex = 0;
        if (mui.getComplexity() == MeasureUnit.Complexity.COMPOUND) {
            int endSlice;
            int startSlice = 0;
            assert (endSlice > 0) : "COMPOUND units have more than one single unit";
            if (singleUnits.get(endSlice).getDimensionality() < 0) {
                String perRule = LongNameHandler.getDeriveCompoundRule(locale, "gender", "per");
                if (perRule.length() != 1) {
                    return perRule;
                }
                if (perRule.charAt(0) == '1') {
                    while (singleUnits.get(startSlice).getDimensionality() >= 0) {
                        ++startSlice;
                    }
                } else {
                    for (endSlice = singleUnits.size() - 1; endSlice >= 0 && singleUnits.get(endSlice).getDimensionality() < 0; --endSlice) {
                    }
                    if (endSlice < 0) {
                        return "";
                    }
                }
            }
            if (endSlice > startSlice) {
                String timesRule = LongNameHandler.getDeriveCompoundRule(locale, "gender", "times");
                if (timesRule.length() != 1) {
                    return timesRule;
                }
                if (timesRule.charAt(0) == '0') {
                    endSlice = startSlice;
                } else {
                    startSlice = endSlice;
                }
            }
            assert (startSlice == endSlice);
            singleUnitIndex = startSlice;
        } else {
            if (mui.getComplexity() == MeasureUnit.Complexity.MIXED) {
                throw new ICUException("calculateGenderForUnit does not support MIXED units");
            }
            assert (mui.getComplexity() == MeasureUnit.Complexity.SINGLE);
            assert (singleUnits.size() == 1);
        }
        SingleUnitImpl singleUnit = singleUnits.get(singleUnitIndex);
        if (Math.abs(singleUnit.getDimensionality()) != 1 && (powerRule = LongNameHandler.getDeriveCompoundRule(locale, "gender", "power")).length() != 1) {
            return powerRule;
        }
        if (Math.abs(singleUnit.getDimensionality()) != 1 && (prefixRule = LongNameHandler.getDeriveCompoundRule(locale, "gender", "prefix")).length() != 1) {
            return prefixRule;
        }
        return LongNameHandler.getGenderForBuiltin(locale, MeasureUnit.forIdentifier(singleUnit.getSimpleUnitID()));
    }

    private static void maybeCalculateGender(ULocale locale, MeasureUnit unit, String[] outArray) {
        if (outArray[GENDER_INDEX] == null) {
            String meterGender = LongNameHandler.getGenderForBuiltin(locale, MeasureUnit.METER);
            if (meterGender.isEmpty()) {
                return;
            }
            outArray[LongNameHandler.GENDER_INDEX] = LongNameHandler.calculateGenderForUnit(locale, unit);
        }
    }

    private LongNameHandler(Map<StandardPlural, SimpleModifier> modifiers, PluralRules rules, MicroPropsGenerator parent) {
        this.modifiers = modifiers;
        this.rules = rules;
        this.parent = parent;
    }

    public static String getUnitDisplayName(ULocale locale, MeasureUnit unit, NumberFormatter.UnitWidth width) {
        String[] measureData = new String[ARRAY_LENGTH];
        LongNameHandler.getMeasureData(locale, unit, width, "", measureData);
        return measureData[DNAM_INDEX];
    }

    public static LongNameHandler forCurrencyLongNames(ULocale locale, Currency currency, PluralRules rules, MicroPropsGenerator parent) {
        String[] simpleFormats = new String[ARRAY_LENGTH];
        LongNameHandler.getCurrencyLongNameData(locale, currency, simpleFormats);
        EnumMap<StandardPlural, SimpleModifier> modifiers = new EnumMap<StandardPlural, SimpleModifier>(StandardPlural.class);
        LongNameHandler result = new LongNameHandler(modifiers, rules, parent);
        result.simpleFormatsToModifiers(simpleFormats, NumberFormat.Field.CURRENCY);
        return result;
    }

    public static LongNameHandler forMeasureUnit(ULocale locale, MeasureUnit unit, NumberFormatter.UnitWidth width, String unitDisplayCase, PluralRules rules, MicroPropsGenerator parent) {
        if (unit.getType() != null) {
            String[] simpleFormats = new String[ARRAY_LENGTH];
            LongNameHandler.getMeasureData(locale, unit, width, unitDisplayCase, simpleFormats);
            LongNameHandler.maybeCalculateGender(locale, unit, simpleFormats);
            EnumMap<StandardPlural, SimpleModifier> modifiers = new EnumMap<StandardPlural, SimpleModifier>(StandardPlural.class);
            LongNameHandler result = new LongNameHandler(modifiers, rules, parent);
            result.simpleFormatsToModifiers(simpleFormats, NumberFormat.Field.MEASURE_UNIT);
            if (simpleFormats[GENDER_INDEX] != null) {
                result.gender = simpleFormats[GENDER_INDEX];
            }
            return result;
        }
        assert (unit.getComplexity() != MeasureUnit.Complexity.MIXED) : "Mixed units not supported by LongNameHandler: use MixedUnitLongNameHandler";
        return LongNameHandler.forArbitraryUnit(locale, unit, width, unitDisplayCase, rules, parent);
    }

    private static LongNameHandler forArbitraryUnit(ULocale loc, MeasureUnit unit, NumberFormatter.UnitWidth width, String unitDisplayCase, PluralRules rules, MicroPropsGenerator parent) {
        MeasureUnitImpl fullUnit = unit.getCopyOfMeasureUnitImpl();
        unit = null;
        MeasureUnit perUnit = null;
        if (fullUnit.getConstantDenominator() != 0L) {
            MeasureUnitImpl perUnitImpl = new MeasureUnitImpl();
            perUnitImpl.setConstantDenominator(fullUnit.getConstantDenominator());
            perUnit = perUnitImpl.build();
        }
        for (SingleUnitImpl subUnit : fullUnit.getSingleUnits()) {
            if (subUnit.getDimensionality() > 0) {
                if (unit == null) {
                    unit = subUnit.build();
                    continue;
                }
                unit = unit.product(subUnit.build());
                continue;
            }
            subUnit.setDimensionality(subUnit.getDimensionality() * -1);
            if (perUnit == null) {
                perUnit = subUnit.build();
                continue;
            }
            perUnit = perUnit.product(subUnit.build());
        }
        MeasureUnitImpl unitImpl = unit == null ? null : unit.getCopyOfMeasureUnitImpl();
        MeasureUnitImpl perUnitImpl = perUnit == null ? null : perUnit.getCopyOfMeasureUnitImpl();
        DerivedComponents derivedPerCases = new DerivedComponents(loc, "case", "per");
        String[] numeratorUnitData = new String[ARRAY_LENGTH];
        LongNameHandler.processPatternTimes(unitImpl, loc, width, derivedPerCases.value0(unitDisplayCase), numeratorUnitData);
        String[] denominatorUnitData = new String[ARRAY_LENGTH];
        LongNameHandler.processPatternTimes(perUnitImpl, loc, width, derivedPerCases.value1(unitDisplayCase), denominatorUnitData);
        String perUnitPattern = null;
        if (denominatorUnitData[PER_INDEX] != null) {
            perUnitPattern = denominatorUnitData[PER_INDEX];
        } else {
            StringBuilder sb = new StringBuilder();
            String rawPerUnitFormat = LongNameHandler.getCompoundValue("per", loc, width);
            String perPatternFormatter = SimpleFormatterImpl.compileToStringMinMaxArguments(rawPerUnitFormat, sb, 2, 2);
            String rawDenominatorFormat = LongNameHandler.getWithPlural(denominatorUnitData, StandardPlural.ONE);
            String denominatorFormatter = SimpleFormatterImpl.compileToStringMinMaxArguments(rawDenominatorFormat, sb, 0, 1);
            String denominatorString = PatternProps.trimSpaceChar(SimpleFormatterImpl.getTextWithNoArguments(denominatorFormatter));
            perUnitPattern = SimpleFormatterImpl.formatCompiledPattern(perPatternFormatter, "{0}", denominatorString);
        }
        EnumMap<StandardPlural, SimpleModifier> modifiers = new EnumMap<StandardPlural, SimpleModifier>(StandardPlural.class);
        LongNameHandler result = new LongNameHandler(modifiers, rules, parent);
        if (perUnitPattern.length() == 0) {
            result.simpleFormatsToModifiers(numeratorUnitData, NumberFormat.Field.MEASURE_UNIT);
        } else {
            result.multiSimpleFormatsToModifiers(numeratorUnitData, perUnitPattern, NumberFormat.Field.MEASURE_UNIT);
        }
        result.gender = LongNameHandler.getDerivedGender(loc, "per", numeratorUnitData, denominatorUnitData);
        return result;
    }

    private static void processPatternTimes(MeasureUnitImpl productUnit, ULocale loc, NumberFormatter.UnitWidth width, String caseVariant, String[] outArray) {
        assert (outArray[StandardPlural.OTHER.ordinal()] == null) : "outArray must have only null values!";
        assert (outArray[PER_INDEX] == null) : "outArray must have only null values!";
        if (productUnit == null) {
            outArray[StandardPlural.OTHER.ordinal()] = "";
            outArray[LongNameHandler.PER_INDEX] = "";
            return;
        }
        if (productUnit.getComplexity() == MeasureUnit.Complexity.MIXED) {
            throw new UnsupportedOperationException("Mixed units not supported by LongNameHandler");
        }
        if (productUnit.getIdentifier() == null) {
            productUnit.serialize();
        }
        if (productUnit.getIdentifier().length() == 0) {
            return;
        }
        MeasureUnit simpleUnit = MeasureUnit.findBySubType(productUnit.getIdentifier());
        if (simpleUnit != null) {
            LongNameHandler.getMeasureData(loc, simpleUnit, width, caseVariant, outArray);
            LongNameHandler.maybeCalculateGender(loc, simpleUnit, outArray);
            return;
        }
        String timesPattern = LongNameHandler.getCompoundValue("times", loc, width);
        StringBuilder sb = new StringBuilder();
        String timesPatternFormatter = SimpleFormatterImpl.compileToStringMinMaxArguments(timesPattern, sb, 2, 2);
        PlaceholderPosition[] globalPlaceholder = new PlaceholderPosition[ARRAY_LENGTH];
        char globalJoinerChar = '\u0000';
        for (StandardPlural plural : StandardPlural.values()) {
            int pluralIndex = plural.ordinal();
            outArray[pluralIndex] = plural == StandardPlural.OTHER ? "" : null;
            globalPlaceholder[pluralIndex] = null;
        }
        String pluralCategory = null;
        DerivedComponents derivedTimesPlurals = new DerivedComponents(loc, "plural", "times");
        DerivedComponents derivedTimesCases = new DerivedComponents(loc, "case", "times");
        DerivedComponents derivedPowerCases = new DerivedComponents(loc, "case", "power");
        ArrayList<SingleUnitImpl> singleUnits = productUnit.getSingleUnits();
        for (int singleUnitIndex = 0; singleUnitIndex < singleUnits.size(); ++singleUnitIndex) {
            String singleCaseVariant;
            String singlePluralCategory;
            SingleUnitImpl singleUnit = singleUnits.get(singleUnitIndex);
            if (singleUnitIndex < singleUnits.size() - 1) {
                singlePluralCategory = derivedTimesPlurals.value0(pluralCategory);
                singleCaseVariant = derivedTimesCases.value0(caseVariant);
                pluralCategory = derivedTimesPlurals.value1(pluralCategory);
                caseVariant = derivedTimesCases.value1(caseVariant);
            } else {
                singlePluralCategory = derivedTimesPlurals.value1(pluralCategory);
                singleCaseVariant = derivedTimesCases.value1(caseVariant);
            }
            simpleUnit = MeasureUnit.findBySubType(singleUnit.getSimpleUnitID());
            if (simpleUnit == null) {
                throw new UnsupportedOperationException("Unsupported sinlgeUnit: " + singleUnit.getSimpleUnitID());
            }
            String gender = LongNameHandler.getGenderForBuiltin(loc, simpleUnit);
            assert (singleUnit.getDimensionality() > 0);
            int dimensionality = singleUnit.getDimensionality();
            String[] dimensionalityPrefixPatterns = new String[ARRAY_LENGTH];
            if (dimensionality != 1) {
                StringBuilder dimensionalityKey = new StringBuilder("compound/power");
                dimensionalityKey.append(dimensionality);
                try {
                    LongNameHandler.getInflectedMeasureData(dimensionalityKey.toString(), loc, width, gender, singleCaseVariant, dimensionalityPrefixPatterns);
                }
                catch (MissingResourceException e) {
                    if (dimensionality > 3) {
                        throw new UnsupportedOperationException("powerN not supported for N > 3: " + productUnit.getIdentifier());
                    }
                    throw e;
                }
                singleCaseVariant = derivedPowerCases.value0(singleCaseVariant);
                singleUnit.setDimensionality(1);
            }
            MeasureUnit.MeasurePrefix prefix = singleUnit.getPrefix();
            String prefixPattern = "";
            if (prefix != MeasureUnit.MeasurePrefix.ONE) {
                StringBuilder prefixKey = new StringBuilder();
                prefixKey.append(prefix.getBase());
                prefixKey.append('p');
                prefixKey.append(prefix.getPower());
                prefixPattern = LongNameHandler.getCompoundValue(prefixKey.toString(), loc, width);
                singleUnit.setPrefix(MeasureUnit.MeasurePrefix.ONE);
            }
            String[] singleUnitArray = new String[ARRAY_LENGTH];
            assert (singleUnit.build().getIdentifier().equals(singleUnit.getSimpleUnitID())) : "Should be equal: singleUnit.build().getIdentifier() produced " + singleUnit.build().getIdentifier() + ", singleUnit.getSimpleUnitID() produced " + singleUnit.getSimpleUnitID();
            LongNameHandler.getMeasureData(loc, singleUnit.build(), width, singleCaseVariant, singleUnitArray);
            if (singleUnitArray[GENDER_INDEX] != null) {
                String timesGenderRule;
                assert (!singleUnitArray[GENDER_INDEX].isEmpty());
                if (prefix != MeasureUnit.MeasurePrefix.ONE) {
                    singleUnitArray[LongNameHandler.GENDER_INDEX] = LongNameHandler.getDerivedGender(loc, "prefix", singleUnitArray, null);
                }
                if (dimensionality != 1) {
                    singleUnitArray[LongNameHandler.GENDER_INDEX] = LongNameHandler.getDerivedGender(loc, "power", singleUnitArray, null);
                }
                if ((timesGenderRule = LongNameHandler.getDeriveCompoundRule(loc, "gender", "times")).length() == 1) {
                    switch (timesGenderRule.charAt(0)) {
                        case '0': {
                            if (singleUnitIndex != 0) break;
                            assert (outArray[GENDER_INDEX] == null);
                            outArray[LongNameHandler.GENDER_INDEX] = singleUnitArray[GENDER_INDEX];
                            break;
                        }
                        case '1': {
                            if (singleUnitIndex != singleUnits.size() - 1) break;
                            assert (outArray[GENDER_INDEX] == null);
                            outArray[LongNameHandler.GENDER_INDEX] = singleUnitArray[GENDER_INDEX];
                        }
                    }
                } else if (outArray[GENDER_INDEX] == null) {
                    outArray[LongNameHandler.GENDER_INDEX] = timesGenderRule;
                }
            }
            for (StandardPlural plural_ : StandardPlural.values()) {
                StandardPlural plural = plural_;
                int pluralIndex = plural.ordinal();
                if (outArray[pluralIndex] == null) {
                    if (singleUnitArray[pluralIndex] == null) continue;
                    outArray[pluralIndex] = LongNameHandler.getWithPlural(outArray, plural);
                }
                if (singlePluralCategory != null) {
                    plural = StandardPlural.fromString(singlePluralCategory);
                }
                ExtractCorePatternResult r = LongNameHandler.extractCorePattern(LongNameHandler.getWithPlural(singleUnitArray, plural));
                if (r.placeholderPosition == PlaceholderPosition.MIDDLE) {
                    throw new UnsupportedOperationException();
                }
                if (globalPlaceholder[pluralIndex] == null) {
                    globalPlaceholder[pluralIndex] = r.placeholderPosition;
                    globalJoinerChar = r.joinerChar;
                } else assert (globalPlaceholder[pluralIndex] == r.placeholderPosition);
                if (prefix != MeasureUnit.MeasurePrefix.ONE) {
                    String prefixCompiled = SimpleFormatterImpl.compileToStringMinMaxArguments(prefixPattern, sb, 1, 1);
                    if (width == NumberFormatter.UnitWidth.FULL_NAME) {
                        r.coreUnit = UCharacter.toLowerCase(loc, r.coreUnit);
                    }
                    r.coreUnit = SimpleFormatterImpl.formatCompiledPattern(prefixCompiled, r.coreUnit);
                }
                if (dimensionality != 1) {
                    String dimensionalityCompiled = SimpleFormatterImpl.compileToStringMinMaxArguments(LongNameHandler.getWithPlural(dimensionalityPrefixPatterns, plural), sb, 1, 1);
                    if (width == NumberFormatter.UnitWidth.FULL_NAME) {
                        r.coreUnit = UCharacter.toLowerCase(loc, r.coreUnit);
                    }
                    r.coreUnit = SimpleFormatterImpl.formatCompiledPattern(dimensionalityCompiled, r.coreUnit);
                }
                outArray[pluralIndex] = outArray[pluralIndex].length() == 0 ? r.coreUnit : SimpleFormatterImpl.formatCompiledPattern(timesPatternFormatter, outArray[pluralIndex], r.coreUnit);
            }
        }
        if (productUnit.getConstantDenominator() != 0L) {
            outArray[LongNameHandler.CONSTANT_DENOMINATOR_INDEX] = String.valueOf(productUnit.getConstantDenominator());
            Integer pluralIndex = null;
            for (StandardPlural plural_ : StandardPlural.values()) {
                if (outArray[plural_.ordinal()] == null) continue;
                pluralIndex = plural_.ordinal();
                break;
            }
            assert (pluralIndex != null) : "No plural form found for constant denominator";
            outArray[pluralIndex.intValue()] = outArray[pluralIndex].length() == 0 ? outArray[CONSTANT_DENOMINATOR_INDEX] : SimpleFormatterImpl.formatCompiledPattern(timesPatternFormatter, outArray[CONSTANT_DENOMINATOR_INDEX], outArray[pluralIndex]);
        }
        for (StandardPlural plural : StandardPlural.values()) {
            int pluralIndex = plural.ordinal();
            if (globalPlaceholder[pluralIndex] == PlaceholderPosition.BEGINNING) {
                StringBuilder tmp = new StringBuilder();
                tmp.append("{0}");
                if (globalJoinerChar != '\u0000') {
                    tmp.append(globalJoinerChar);
                }
                tmp.append(outArray[pluralIndex]);
                outArray[pluralIndex] = tmp.toString();
                continue;
            }
            if (globalPlaceholder[pluralIndex] != PlaceholderPosition.END) continue;
            if (globalJoinerChar != '\u0000') {
                outArray[pluralIndex] = outArray[pluralIndex] + globalJoinerChar;
            }
            outArray[pluralIndex] = outArray[pluralIndex] + "{0}";
        }
    }

    private void simpleFormatsToModifiers(String[] simpleFormats, NumberFormat.Field field) {
        StringBuilder sb = new StringBuilder();
        for (StandardPlural plural : StandardPlural.VALUES) {
            String simpleFormat = LongNameHandler.getWithPlural(simpleFormats, plural);
            String compiled = SimpleFormatterImpl.compileToStringMinMaxArguments(simpleFormat, sb, 0, 1);
            Modifier.Parameters parameters = new Modifier.Parameters();
            parameters.obj = this;
            parameters.signum = null;
            parameters.plural = plural;
            this.modifiers.put(plural, new SimpleModifier(compiled, field, false, parameters));
        }
    }

    private void multiSimpleFormatsToModifiers(String[] leadFormats, String trailFormat, NumberFormat.Field field) {
        StringBuilder sb = new StringBuilder();
        String trailCompiled = SimpleFormatterImpl.compileToStringMinMaxArguments(trailFormat, sb, 1, 1);
        for (StandardPlural plural : StandardPlural.VALUES) {
            String leadFormat = LongNameHandler.getWithPlural(leadFormats, plural);
            String compoundFormat = leadFormat.length() == 0 ? trailFormat : SimpleFormatterImpl.formatCompiledPattern(trailCompiled, leadFormat);
            String compoundCompiled = SimpleFormatterImpl.compileToStringMinMaxArguments(compoundFormat, sb, 0, 1);
            Modifier.Parameters parameters = new Modifier.Parameters();
            parameters.obj = this;
            parameters.signum = null;
            parameters.plural = plural;
            this.modifiers.put(plural, new SimpleModifier(compoundCompiled, field, false, parameters));
        }
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps micros = this.parent.processQuantity(quantity);
        StandardPlural pluralForm = RoundingUtils.getPluralSafe(micros.rounder, this.rules, quantity);
        micros.modOuter = this.modifiers.get((Object)pluralForm);
        micros.gender = this.gender;
        return micros;
    }

    @Override
    public MicroProps processQuantityWithMicros(DecimalQuantity quantity, MicroProps micros) {
        StandardPlural pluralForm = RoundingUtils.getPluralSafe(micros.rounder, this.rules, quantity);
        micros.modOuter = this.modifiers.get((Object)pluralForm);
        return micros;
    }

    @Override
    public Modifier getModifier(Modifier.Signum signum, StandardPlural plural) {
        return this.modifiers.get((Object)plural);
    }

    private static class DerivedComponents {
        private String value0 = "";
        private String value1 = "";

        DerivedComponents(ULocale locale, String feature, String structure) {
            try {
                ICUResourceBundle stackBundle;
                ICUResourceBundle derivationsBundle = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "grammaticalFeatures");
                derivationsBundle = (ICUResourceBundle)derivationsBundle.get("grammaticalData");
                derivationsBundle = (ICUResourceBundle)derivationsBundle.get("derivations");
                try {
                    stackBundle = (ICUResourceBundle)derivationsBundle.get(locale.getLanguage());
                }
                catch (MissingResourceException e) {
                    stackBundle = (ICUResourceBundle)derivationsBundle.get("root");
                }
                stackBundle = (ICUResourceBundle)stackBundle.get("component");
                stackBundle = (ICUResourceBundle)stackBundle.get(feature);
                stackBundle = (ICUResourceBundle)stackBundle.get(structure);
                String value = stackBundle.getString(0);
                this.value0 = value.compareTo("compound") == 0 ? null : value;
                value = stackBundle.getString(1);
                this.value1 = value.compareTo("compound") == 0 ? null : value;
            }
            catch (MissingResourceException missingResourceException) {
                // empty catch block
            }
        }

        String value0(String compoundValue) {
            return this.value0 != null ? this.value0 : compoundValue;
        }

        String value1(String compoundValue) {
            return this.value1 != null ? this.value1 : compoundValue;
        }
    }

    private static final class AliasSink
    extends UResource.Sink {
        String replacement;

        private AliasSink() {
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table aliasTable = value.getTable();
            int i = 0;
            while (aliasTable.getKeyAndValue(i, key, value)) {
                String keyString = key.toString();
                if (keyString.equals("replacement")) {
                    this.replacement = value.toString();
                }
                ++i;
            }
        }
    }

    private static final class PluralTableSink
    extends UResource.Sink {
        String[] outArray;

        public PluralTableSink(String[] outArray) {
            this.outArray = outArray;
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table pluralsTable = value.getTable();
            int i = 0;
            while (pluralsTable.getKeyAndValue(i, key, value)) {
                int index;
                String keyString = key.toString();
                if (!keyString.equals("case") && this.outArray[index = LongNameHandler.getIndex(keyString)] == null) {
                    String formatString;
                    this.outArray[index] = formatString = value.getString();
                }
                ++i;
            }
        }
    }

    private static final class InflectedPluralSink
    extends UResource.Sink {
        String gender;
        String caseVariant;
        String[] outArray;

        public InflectedPluralSink(String gender, String caseVariant, String[] outArray) {
            this.gender = gender;
            this.caseVariant = caseVariant;
            this.outArray = outArray;
            for (int i = 0; i < ARRAY_LENGTH; ++i) {
                outArray[i] = null;
            }
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table pluralsTable = value.getTable();
            int i = 0;
            while (pluralsTable.getKeyAndValue(i, key, value)) {
                UResource.Table genderTable;
                String keyString = key.toString();
                int pluralIndex = LongNameHandler.getIndex(keyString);
                if (this.outArray[pluralIndex] == null && this.loadForPluralForm(genderTable = value.getTable(), value)) {
                    this.outArray[pluralIndex] = value.getString();
                }
                ++i;
            }
        }

        private boolean loadForPluralForm(UResource.Table genderTable, UResource.Value value) {
            if (this.gender != null && !this.gender.isEmpty()) {
                if (this.loadForGender(genderTable, this.gender, value)) {
                    return true;
                }
                if (this.gender != "neuter" && this.loadForGender(genderTable, "neuter", value)) {
                    return true;
                }
            }
            return this.loadForGender(genderTable, "_", value);
        }

        private boolean loadForGender(UResource.Table genderTable, String genderVal, UResource.Value value) {
            if (!genderTable.findValue(genderVal, value)) {
                return false;
            }
            UResource.Table caseTable = value.getTable();
            if (this.caseVariant != null && !this.caseVariant.isEmpty()) {
                if (this.loadForCase(caseTable, this.caseVariant, value)) {
                    return true;
                }
                if (this.caseVariant != "nominative" && this.loadForCase(caseTable, "nominative", value)) {
                    return true;
                }
            }
            return this.loadForCase(caseTable, "_", value);
        }

        private boolean loadForCase(UResource.Table caseTable, String caseValue, UResource.Value value) {
            return caseTable.findValue(caseValue, value);
        }
    }

    private static class ExtractCorePatternResult {
        String coreUnit;
        PlaceholderPosition placeholderPosition;
        char joinerChar;

        private ExtractCorePatternResult() {
        }
    }

    private static enum PlaceholderPosition {
        NONE,
        BEGINNING,
        MIDDLE,
        END;

    }
}

