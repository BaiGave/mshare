/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.impl.FormattedStringBuilder;
import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.CompactData;
import com.ibm.icu.impl.number.ConstantAffixModifier;
import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.DecimalQuantity_DualStorageBCD;
import com.ibm.icu.impl.number.Grouper;
import com.ibm.icu.impl.number.LongNameHandler;
import com.ibm.icu.impl.number.LongNameMultiplexer;
import com.ibm.icu.impl.number.MacroProps;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.number.MixedUnitLongNameHandler;
import com.ibm.icu.impl.number.MultiplierFormatHandler;
import com.ibm.icu.impl.number.MutablePatternModifier;
import com.ibm.icu.impl.number.Padder;
import com.ibm.icu.impl.number.PatternStringParser;
import com.ibm.icu.impl.number.RoundingUtils;
import com.ibm.icu.impl.number.UnitConversionHandler;
import com.ibm.icu.impl.number.UsagePrefsHandler;
import com.ibm.icu.number.CompactNotation;
import com.ibm.icu.number.IntegerWidth;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.number.Precision;
import com.ibm.icu.number.ScientificNotation;
import com.ibm.icu.text.DecimalFormatSymbols;
import com.ibm.icu.text.NumberFormat;
import com.ibm.icu.text.NumberingSystem;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.MeasureUnit;

class NumberFormatterImpl {
    private static final Currency DEFAULT_CURRENCY = Currency.getInstance("XXX");
    final MicroProps micros = new MicroProps(true);
    final MicroPropsGenerator microPropsGenerator;

    public NumberFormatterImpl(MacroProps macros) {
        this.microPropsGenerator = NumberFormatterImpl.macrosToMicroGenerator(macros, this.micros, true);
    }

    public static MicroProps formatStatic(MacroProps macros, DecimalQuantity inValue, FormattedStringBuilder outString) {
        MicroProps result = NumberFormatterImpl.preProcessUnsafe(macros, inValue);
        int length = NumberFormatterImpl.writeNumber(result, inValue, outString, 0);
        NumberFormatterImpl.writeAffixes(result, outString, 0, length);
        return result;
    }

    public static int getPrefixSuffixStatic(MacroProps macros, byte signum, StandardPlural plural, FormattedStringBuilder output) {
        MicroProps micros = new MicroProps(false);
        MicroPropsGenerator microPropsGenerator = NumberFormatterImpl.macrosToMicroGenerator(macros, micros, false);
        return NumberFormatterImpl.getPrefixSuffixImpl(microPropsGenerator, signum, output);
    }

    public MicroProps format(DecimalQuantity inValue, FormattedStringBuilder outString) {
        MicroProps result = this.preProcess(inValue);
        int length = NumberFormatterImpl.writeNumber(result, inValue, outString, 0);
        NumberFormatterImpl.writeAffixes(result, outString, 0, length);
        return result;
    }

    public MicroProps preProcess(DecimalQuantity inValue) {
        MicroProps micros = this.microPropsGenerator.processQuantity(inValue);
        if (micros.integerWidth.maxInt == -1) {
            inValue.setMinInteger(micros.integerWidth.minInt);
        } else {
            inValue.setMinInteger(micros.integerWidth.minInt);
            inValue.applyMaxInteger(micros.integerWidth.maxInt);
        }
        return micros;
    }

    private static MicroProps preProcessUnsafe(MacroProps macros, DecimalQuantity inValue) {
        MicroProps micros = new MicroProps(false);
        MicroPropsGenerator microPropsGenerator = NumberFormatterImpl.macrosToMicroGenerator(macros, micros, false);
        micros = microPropsGenerator.processQuantity(inValue);
        if (micros.integerWidth.maxInt == -1) {
            inValue.setMinInteger(micros.integerWidth.minInt);
        } else {
            inValue.setMinInteger(micros.integerWidth.minInt);
            inValue.applyMaxInteger(micros.integerWidth.maxInt);
        }
        return micros;
    }

    public int getPrefixSuffix(byte signum, StandardPlural plural, FormattedStringBuilder output) {
        return NumberFormatterImpl.getPrefixSuffixImpl(this.microPropsGenerator, signum, output);
    }

    private static int getPrefixSuffixImpl(MicroPropsGenerator generator, byte signum, FormattedStringBuilder output) {
        DecimalQuantity_DualStorageBCD quantity = new DecimalQuantity_DualStorageBCD(0);
        if (signum < 0) {
            quantity.negate();
        }
        MicroProps micros = generator.processQuantity(quantity);
        micros.modMiddle.apply(output, 0, 0);
        return micros.modMiddle.getPrefixLength();
    }

    public MicroProps getRawMicroProps() {
        return this.micros;
    }

    private static boolean unitIsCurrency(MeasureUnit unit) {
        return unit != null && "currency".equals(unit.getType());
    }

    private static boolean unitIsBaseUnit(MeasureUnit unit) {
        return unit == null;
    }

    private static boolean unitIsPercent(MeasureUnit unit) {
        return unit != null && "percent".equals(unit.getSubtype());
    }

    private static boolean unitIsPermille(MeasureUnit unit) {
        return unit != null && "permille".equals(unit.getSubtype());
    }

    private static MicroPropsGenerator macrosToMicroGenerator(MacroProps macros, MicroProps micros, boolean safe) {
        MicroPropsGenerator chain = micros;
        boolean isCurrency = NumberFormatterImpl.unitIsCurrency(macros.unit);
        boolean isBaseUnit = NumberFormatterImpl.unitIsBaseUnit(macros.unit);
        boolean isPercent = NumberFormatterImpl.unitIsPercent(macros.unit);
        boolean isPermille = NumberFormatterImpl.unitIsPermille(macros.unit);
        boolean isCompactNotation = macros.notation instanceof CompactNotation;
        boolean isAccounting = macros.sign == NumberFormatter.SignDisplay.ACCOUNTING || macros.sign == NumberFormatter.SignDisplay.ACCOUNTING_ALWAYS || macros.sign == NumberFormatter.SignDisplay.ACCOUNTING_EXCEPT_ZERO || macros.sign == NumberFormatter.SignDisplay.ACCOUNTING_NEGATIVE;
        Currency currency = isCurrency ? (Currency)macros.unit : DEFAULT_CURRENCY;
        NumberFormatter.UnitWidth unitWidth = NumberFormatter.UnitWidth.SHORT;
        if (macros.unitWidth != null) {
            unitWidth = macros.unitWidth;
        }
        boolean isCldrUnit = !isCurrency && !isBaseUnit && (unitWidth == NumberFormatter.UnitWidth.FULL_NAME || !isPercent && !isPermille || isCompactNotation);
        boolean isMixedUnit = isCldrUnit && macros.unit.getType() == null && macros.unit.getComplexity() == MeasureUnit.Complexity.MIXED;
        PluralRules rules = macros.rules;
        NumberingSystem ns = macros.symbols instanceof NumberingSystem ? (NumberingSystem)macros.symbols : NumberingSystem.getInstance(macros.loc);
        micros.nsName = ns.getName();
        micros.gender = "";
        if (macros.symbols instanceof DecimalFormatSymbols) {
            micros.symbols = (DecimalFormatSymbols)macros.symbols;
        } else {
            micros.symbols = DecimalFormatSymbols.forNumberingSystem(macros.loc, ns);
            if (isCurrency) {
                micros.symbols.setCurrency(currency);
            }
        }
        String pattern = null;
        if (isCurrency && micros.symbols.getCurrencyPattern() != null) {
            pattern = micros.symbols.getCurrencyPattern();
        }
        if (pattern == null) {
            int patternStyle = isCldrUnit ? 0 : (isPercent || isPermille ? 2 : (!isCurrency || unitWidth == NumberFormatter.UnitWidth.FULL_NAME ? 0 : (isAccounting ? 7 : 1)));
            pattern = NumberFormat.getPatternForStyleAndNumberingSystem(macros.loc, micros.nsName, patternStyle);
        }
        PatternStringParser.ParsedPatternInfo patternInfo = PatternStringParser.parseToPatternInfo(pattern);
        UsagePrefsHandler usagePrefsHandler = null;
        if (macros.usage != null) {
            if (!isCldrUnit) {
                throw new IllegalIcuArgumentException("We only support \"usage\" when the input unit is specified, and is a CLDR Unit.");
            }
            usagePrefsHandler = new UsagePrefsHandler(macros.loc, macros.unit, macros.usage, chain);
            chain = usagePrefsHandler;
        } else if (isMixedUnit) {
            chain = new UnitConversionHandler(macros.unit, chain);
        }
        if (macros.scale != null) {
            chain = new MultiplierFormatHandler(macros.scale, chain);
        }
        micros.rounder = macros.precision != null ? macros.precision : (isCompactNotation ? Precision.COMPACT_STRATEGY : (isCurrency ? Precision.MONETARY_STANDARD : (macros.usage != null ? Precision.BOGUS_PRECISION : Precision.DEFAULT_MAX_FRAC_6)));
        if (macros.roundingMode != null) {
            micros.rounder = micros.rounder.withMode(RoundingUtils.mathContextUnlimited(macros.roundingMode));
        }
        micros.rounder = micros.rounder.withLocaleData(currency);
        micros.grouping = macros.grouping instanceof Grouper ? (Grouper)macros.grouping : (macros.grouping instanceof NumberFormatter.GroupingStrategy ? Grouper.forStrategy((NumberFormatter.GroupingStrategy)((Object)macros.grouping)) : (isCompactNotation ? Grouper.forStrategy(NumberFormatter.GroupingStrategy.MIN2) : Grouper.forStrategy(NumberFormatter.GroupingStrategy.AUTO)));
        micros.grouping = micros.grouping.withLocaleData(macros.loc, patternInfo);
        micros.padding = macros.padder != null ? macros.padder : Padder.NONE;
        micros.integerWidth = macros.integerWidth != null ? macros.integerWidth : IntegerWidth.DEFAULT;
        micros.sign = macros.sign != null ? macros.sign : NumberFormatter.SignDisplay.AUTO;
        micros.decimal = macros.decimal != null ? macros.decimal : NumberFormatter.DecimalSeparatorDisplay.AUTO;
        micros.useCurrency = isCurrency;
        if (macros.notation instanceof ScientificNotation) {
            chain = ((ScientificNotation)macros.notation).withLocaleData(micros.symbols, safe, chain);
        } else {
            micros.modInner = ConstantAffixModifier.EMPTY;
        }
        MutablePatternModifier patternMod = new MutablePatternModifier(false);
        PatternStringParser.ParsedPatternInfo affixProvider = macros.affixProvider != null && (!isCompactNotation || isCurrency == macros.affixProvider.hasCurrencySign()) ? macros.affixProvider : patternInfo;
        patternMod.setPatternInfo(affixProvider, null);
        boolean approximately = macros.approximately != null ? macros.approximately : false;
        patternMod.setPatternAttributes(micros.sign, isPermille, approximately);
        if (patternMod.needsPlurals()) {
            if (rules == null) {
                rules = PluralRules.forLocale(macros.loc);
            }
            patternMod.setSymbols(micros.symbols, currency, unitWidth, rules);
        } else {
            patternMod.setSymbols(micros.symbols, currency, unitWidth, null);
        }
        MutablePatternModifier.ImmutablePatternModifier immPatternMod = null;
        if (safe) {
            immPatternMod = patternMod.createImmutable();
        }
        if (affixProvider.currencyAsDecimal()) {
            micros.currencyAsDecimal = patternMod.getCurrencySymbolForUnitWidth();
        }
        if (isCldrUnit) {
            PluralRules pluralRules;
            String unitDisplayCase = null;
            if (macros.unitDisplayCase != null) {
                unitDisplayCase = macros.unitDisplayCase;
            }
            if (rules == null) {
                rules = PluralRules.forLocale(macros.loc);
            }
            PluralRules pluralRules2 = pluralRules = macros.rules != null ? macros.rules : PluralRules.forLocale(macros.loc);
            if (macros.usage != null) {
                assert (usagePrefsHandler != null);
                chain = LongNameMultiplexer.forMeasureUnits(macros.loc, usagePrefsHandler.getOutputUnits(), unitWidth, unitDisplayCase, pluralRules, chain);
            } else if (isMixedUnit) {
                chain = MixedUnitLongNameHandler.forMeasureUnit(macros.loc, macros.unit, unitWidth, unitDisplayCase, pluralRules, chain);
            } else {
                MeasureUnit unit = macros.unit;
                if (macros.perUnit != null && (unit = unit.product(macros.perUnit.reciprocal())).getType() == null && (macros.unit.getType() == null || macros.perUnit.getType() == null)) {
                    throw new UnsupportedOperationException("perUnit() can only be used if unit and perUnit are both built-ins, or the combination is a built-in");
                }
                chain = LongNameHandler.forMeasureUnit(macros.loc, unit, unitWidth, unitDisplayCase, pluralRules, chain);
            }
        } else if (isCurrency && unitWidth == NumberFormatter.UnitWidth.FULL_NAME) {
            if (rules == null) {
                rules = PluralRules.forLocale(macros.loc);
            }
            chain = LongNameHandler.forCurrencyLongNames(macros.loc, currency, rules, chain);
        } else {
            micros.modOuter = ConstantAffixModifier.EMPTY;
        }
        if (isCompactNotation) {
            if (rules == null) {
                rules = PluralRules.forLocale(macros.loc);
            }
            CompactData.CompactType compactType = macros.unit instanceof Currency && macros.unitWidth != NumberFormatter.UnitWidth.FULL_NAME ? CompactData.CompactType.CURRENCY : CompactData.CompactType.DECIMAL;
            chain = ((CompactNotation)macros.notation).withLocaleData(macros.loc, micros.nsName, compactType, rules, patternMod, safe, chain);
        }
        chain = safe ? immPatternMod.addToChain(chain) : patternMod.addToChain(chain);
        return chain;
    }

    public static int writeAffixes(MicroProps micros, FormattedStringBuilder string, int start, int end) {
        int length = micros.modInner.apply(string, start, end);
        if (micros.padding.isValid()) {
            micros.padding.padAndApply(micros.modMiddle, micros.modOuter, string, start, end + length);
        } else {
            length += micros.modMiddle.apply(string, start, end + length);
            length += micros.modOuter.apply(string, start, end + length);
        }
        return length;
    }

    public static int writeNumber(MicroProps micros, DecimalQuantity quantity, FormattedStringBuilder string, int index) {
        int length = 0;
        if (quantity.isInfinite()) {
            length += string.insert(length + index, micros.symbols.getInfinity(), NumberFormat.Field.INTEGER);
        } else if (quantity.isNaN()) {
            length += string.insert(length + index, micros.symbols.getNaN(), NumberFormat.Field.INTEGER);
        } else {
            length += NumberFormatterImpl.writeIntegerDigits(micros, quantity, string, length + index);
            if (quantity.getLowerDisplayMagnitude() < 0 || micros.decimal == NumberFormatter.DecimalSeparatorDisplay.ALWAYS) {
                length = micros.currencyAsDecimal != null ? (length += string.insert(length + index, micros.currencyAsDecimal, NumberFormat.Field.CURRENCY)) : (micros.useCurrency ? (length += string.insert(length + index, micros.symbols.getMonetaryDecimalSeparatorString(), NumberFormat.Field.DECIMAL_SEPARATOR)) : (length += string.insert(length + index, micros.symbols.getDecimalSeparatorString(), NumberFormat.Field.DECIMAL_SEPARATOR)));
            }
            if ((length += NumberFormatterImpl.writeFractionDigits(micros, quantity, string, length + index)) == 0) {
                length = micros.symbols.getCodePointZero() != -1 ? (length += string.insertCodePoint(index, micros.symbols.getCodePointZero(), NumberFormat.Field.INTEGER)) : (length += string.insert(index, micros.symbols.getDigitStringsLocal()[0], NumberFormat.Field.INTEGER));
            }
        }
        return length;
    }

    private static int writeIntegerDigits(MicroProps micros, DecimalQuantity quantity, FormattedStringBuilder string, int index) {
        int length = 0;
        int integerCount = quantity.getUpperDisplayMagnitude() + 1;
        for (int i = 0; i < integerCount; ++i) {
            if (micros.grouping.groupAtPosition(i, quantity)) {
                length += string.insert(index, micros.useCurrency ? micros.symbols.getMonetaryGroupingSeparatorString() : micros.symbols.getGroupingSeparatorString(), NumberFormat.Field.GROUPING_SEPARATOR);
            }
            byte nextDigit = quantity.getDigit(i);
            if (micros.symbols.getCodePointZero() != -1) {
                length += string.insertCodePoint(index, micros.symbols.getCodePointZero() + nextDigit, NumberFormat.Field.INTEGER);
                continue;
            }
            length += string.insert(index, micros.symbols.getDigitStringsLocal()[nextDigit], NumberFormat.Field.INTEGER);
        }
        return length;
    }

    private static int writeFractionDigits(MicroProps micros, DecimalQuantity quantity, FormattedStringBuilder string, int index) {
        int length = 0;
        int fractionCount = -quantity.getLowerDisplayMagnitude();
        for (int i = 0; i < fractionCount; ++i) {
            byte nextDigit = quantity.getDigit(-i - 1);
            if (micros.symbols.getCodePointZero() != -1) {
                length += string.insertCodePoint(length + index, micros.symbols.getCodePointZero() + nextDigit, NumberFormat.Field.FRACTION);
                continue;
            }
            length += string.insert(length + index, micros.symbols.getDigitStringsLocal()[nextDigit], NumberFormat.Field.FRACTION);
        }
        return length;
    }
}

