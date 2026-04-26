/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.math.BigDecimal;
import com.ibm.icu.message2.Directionality;
import com.ibm.icu.message2.FormattedPlaceholder;
import com.ibm.icu.message2.Formatter;
import com.ibm.icu.message2.FormatterFactory;
import com.ibm.icu.message2.MFDataModel;
import com.ibm.icu.message2.OptUtils;
import com.ibm.icu.message2.PlainStringFormattedValue;
import com.ibm.icu.message2.Selector;
import com.ibm.icu.message2.SelectorFactory;
import com.ibm.icu.number.FormattedNumber;
import com.ibm.icu.number.LocalizedNumberFormatter;
import com.ibm.icu.number.Notation;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.number.Precision;
import com.ibm.icu.number.UnlocalizedNumberFormatter;
import com.ibm.icu.text.FormattedValue;
import com.ibm.icu.text.NumberingSystem;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.CurrencyAmount;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

class NumberFormatterFactory
implements FormatterFactory,
SelectorFactory {
    private final String kind;
    private static final Pattern CURRENCY_ISO_CODE = Pattern.compile("^[A-Z][A-Z][A-Z]$", 2);

    public NumberFormatterFactory(String kind) {
        switch (kind) {
            case "number": 
            case "integer": 
            case "currency": 
            case "math": {
                break;
            }
            default: {
                kind = "number";
            }
        }
        this.kind = kind;
    }

    @Override
    public Formatter createFormatter(Locale locale, Map<String, Object> fixedOptions) {
        return new NumberFormatterImpl(locale, fixedOptions, this.kind);
    }

    @Override
    public Selector createSelector(Locale locale, Map<String, Object> fixedOptions) {
        PluralRules.PluralType pluralType;
        String type;
        switch (type = OptUtils.getString(fixedOptions, "select", "")) {
            case "ordinal": {
                pluralType = PluralRules.PluralType.ORDINAL;
                break;
            }
            default: {
                pluralType = PluralRules.PluralType.CARDINAL;
            }
        }
        PluralRules rules = PluralRules.forLocale(locale, pluralType);
        return new PluralSelectorImpl(locale, rules, fixedOptions, this.kind);
    }

    private static LocalizedNumberFormatter formatterForOptions(Locale locale, Map<String, Object> fixedOptions, String kind) {
        NumberFormatter.GroupingStrategy grp;
        NumberFormatter.SignDisplay signDisplay;
        Integer option;
        String strOption;
        boolean reportErrors = OptUtils.reportErrors(fixedOptions);
        String skeleton = OptUtils.getString(fixedOptions, "icu:skeleton");
        if (skeleton != null) {
            return NumberFormatter.forSkeleton(skeleton).locale(locale);
        }
        UnlocalizedNumberFormatter nf = NumberFormatter.with();
        if ("number".equals(kind)) {
            Notation notation;
            block6 : switch (OptUtils.getString(fixedOptions, "notation", "standard")) {
                case "scientific": {
                    notation = Notation.scientific();
                    break;
                }
                case "engineering": {
                    notation = Notation.engineering();
                    break;
                }
                case "compact": {
                    switch (OptUtils.getString(fixedOptions, "compactDisplay", "short")) {
                        case "long": {
                            notation = Notation.compactLong();
                            break block6;
                        }
                    }
                    notation = Notation.compactShort();
                    break;
                }
                default: {
                    notation = Notation.simple();
                }
            }
            nf = (UnlocalizedNumberFormatter)nf.notation(notation);
            strOption = OptUtils.getString(fixedOptions, "style", "decimal");
            option = OptUtils.getInteger(fixedOptions, reportErrors, "minimumFractionDigits");
            if (option != null) {
                nf = (UnlocalizedNumberFormatter)nf.precision(Precision.minFraction(option));
            }
            if ((option = OptUtils.getInteger(fixedOptions, reportErrors, "maximumFractionDigits")) != null) {
                nf = (UnlocalizedNumberFormatter)nf.precision(Precision.maxFraction(option));
            }
            if ((option = OptUtils.getInteger(fixedOptions, reportErrors, "minimumSignificantDigits")) != null) {
                nf = (UnlocalizedNumberFormatter)nf.precision(Precision.minSignificantDigits(option));
            }
        }
        if (!(strOption = OptUtils.getString(fixedOptions, "numberingSystem", "")).isEmpty()) {
            strOption = strOption.toLowerCase(Locale.US);
            NumberingSystem ns = NumberingSystem.getInstanceByName(strOption);
            nf = (UnlocalizedNumberFormatter)nf.symbols(ns);
        }
        if ((option = OptUtils.getInteger(fixedOptions, reportErrors, "minimumIntegerDigits")) != null) {
            // empty if block
        }
        if ((option = OptUtils.getInteger(fixedOptions, reportErrors, "maximumSignificantDigits")) != null) {
            nf = (UnlocalizedNumberFormatter)nf.precision(Precision.maxSignificantDigits(option));
        }
        switch (strOption = OptUtils.getString(fixedOptions, "signDisplay", "auto")) {
            case "always": {
                signDisplay = NumberFormatter.SignDisplay.ALWAYS;
                break;
            }
            case "exceptZero": {
                signDisplay = NumberFormatter.SignDisplay.EXCEPT_ZERO;
                break;
            }
            case "negative": {
                signDisplay = NumberFormatter.SignDisplay.NEGATIVE;
                break;
            }
            case "never": {
                signDisplay = NumberFormatter.SignDisplay.NEVER;
                break;
            }
            default: {
                signDisplay = NumberFormatter.SignDisplay.AUTO;
            }
        }
        nf = (UnlocalizedNumberFormatter)nf.sign(signDisplay);
        switch (strOption = OptUtils.getString(fixedOptions, "useGrouping", "auto")) {
            case "always": {
                grp = NumberFormatter.GroupingStrategy.ON_ALIGNED;
                break;
            }
            case "never": {
                grp = NumberFormatter.GroupingStrategy.OFF;
                break;
            }
            case "min2": {
                grp = NumberFormatter.GroupingStrategy.MIN2;
                break;
            }
            default: {
                grp = NumberFormatter.GroupingStrategy.AUTO;
            }
        }
        nf = (UnlocalizedNumberFormatter)nf.grouping(grp);
        if (kind.equals("integer")) {
            nf = (UnlocalizedNumberFormatter)nf.precision(Precision.integer());
        }
        if (kind.equals("currency")) {
            NumberFormatter.UnitWidth width;
            strOption = NumberFormatterFactory.getCurrency(fixedOptions);
            if (strOption != null) {
                nf = (UnlocalizedNumberFormatter)nf.unit(Currency.getInstance(strOption));
            }
            switch (strOption = OptUtils.getString(fixedOptions, "currencySign", "standard")) {
                default: 
            }
            switch (strOption = OptUtils.getString(fixedOptions, "currencyDisplay", "symbol")) {
                case "narrowSymbol": {
                    width = NumberFormatter.UnitWidth.NARROW;
                    break;
                }
                case "symbol": {
                    width = NumberFormatter.UnitWidth.SHORT;
                    break;
                }
                case "name": {
                    width = NumberFormatter.UnitWidth.FULL_NAME;
                    break;
                }
                case "code": {
                    width = NumberFormatter.UnitWidth.ISO_CODE;
                    break;
                }
                case "formalSymbol": {
                    width = NumberFormatter.UnitWidth.FORMAL;
                    break;
                }
                case "never": {
                    width = NumberFormatter.UnitWidth.HIDDEN;
                    break;
                }
                default: {
                    width = NumberFormatter.UnitWidth.SHORT;
                }
            }
            nf = (UnlocalizedNumberFormatter)nf.unitWidth(width);
        }
        return nf.locale(locale);
    }

    static String getCurrency(Map<String, Object> options) {
        String value = OptUtils.getString(options, "currency", null);
        if (value != null) {
            if (CURRENCY_ISO_CODE.matcher(value).find()) {
                return value;
            }
            if (OptUtils.reportErrors(options)) {
                throw new IllegalArgumentException("bad-option: the `currency` must be an ISO 4217 code.");
            }
        }
        return null;
    }

    private static class ResolvedMathOptions {
        final Double operand;
        final boolean reportErrors;

        ResolvedMathOptions(Double operand, boolean reportErrors) {
            this.operand = operand;
            this.reportErrors = reportErrors;
        }

        static ResolvedMathOptions of(Map<String, Object> options) {
            boolean reportErrors = OptUtils.reportErrors(options);
            Double operand = null;
            String addOption = OptUtils.getString(options, "add", null);
            String subtractOption = OptUtils.getString(options, "subtract");
            if (addOption == null) {
                if (subtractOption == null) {
                    throw new IllegalArgumentException("bad-option: :math function needs an `add` or `subtract` option.");
                }
                operand = -OptUtils.asNumber(reportErrors, "subtract", subtractOption).doubleValue();
            } else if (subtractOption == null) {
                operand = OptUtils.asNumber(reportErrors, "add", addOption).doubleValue();
            } else {
                throw new IllegalArgumentException("bad-option: :math function can't have both `add` and `subtract` options.");
            }
            return new ResolvedMathOptions(operand, reportErrors);
        }
    }

    private static class PluralSelectorImpl
    implements Selector {
        private static final String NO_MATCH = "\ufffdNO_MATCH\ufffe";
        private final PluralRules rules;
        private final Map<String, Object> fixedOptions;
        private final LocalizedNumberFormatter icuFormatter;
        private final String kind;

        private PluralSelectorImpl(Locale locale, PluralRules rules, Map<String, Object> fixedOptions, String kind) {
            this.rules = rules;
            this.fixedOptions = fixedOptions;
            this.icuFormatter = NumberFormatterFactory.formatterForOptions(locale, fixedOptions, kind);
            this.kind = kind;
        }

        @Override
        public List<String> matches(Object value, List<String> keys, Map<String, Object> variableOptions) {
            ArrayList<String> result = new ArrayList<String>();
            if (value == null) {
                return result;
            }
            for (String key : keys) {
                if (this.matches(value, key, variableOptions)) {
                    result.add(key);
                    continue;
                }
                result.add(NO_MATCH);
            }
            result.sort(PluralSelectorImpl::pluralComparator);
            return result;
        }

        private static int pluralComparator(String o1, String o2) {
            if (o1.equals(o2)) {
                return 0;
            }
            if (NO_MATCH.equals(o1)) {
                return 1;
            }
            if (NO_MATCH.equals(o2)) {
                return -1;
            }
            if (MFDataModel.CatchallKey.isCatchAll(o1)) {
                return 1;
            }
            if (MFDataModel.CatchallKey.isCatchAll(o2)) {
                return -1;
            }
            if (OptUtils.asNumber(o1) != null) {
                return -1;
            }
            if (OptUtils.asNumber(o2) != null) {
                return 1;
            }
            return o1.compareTo(o2);
        }

        private boolean matches(Object value, String key, Map<String, Object> variableOptions) {
            Number keyNrVal;
            if (MFDataModel.CatchallKey.isCatchAll(key)) {
                return true;
            }
            boolean reportErrors = OptUtils.reportErrors(this.fixedOptions);
            Integer offset = OptUtils.getInteger(variableOptions, reportErrors, "icu:offset");
            if (offset == null && this.fixedOptions != null) {
                offset = OptUtils.getInteger(this.fixedOptions, reportErrors, "icu:offset");
            }
            if (offset == null) {
                offset = 0;
            }
            Number valToCheck = Double.MIN_VALUE;
            if (value instanceof FormattedPlaceholder) {
                FormattedPlaceholder fph = (FormattedPlaceholder)value;
                value = fph.getInput();
            }
            if (!(value instanceof Number)) {
                if (value instanceof CharSequence) {
                    return value.equals(key);
                }
                return false;
            }
            valToCheck = ((Number)value).doubleValue();
            if ("integer".equals(this.kind)) {
                valToCheck = valToCheck.longValue();
            }
            if ((keyNrVal = OptUtils.asNumber(key)) != null && valToCheck.doubleValue() == keyNrVal.doubleValue()) {
                return true;
            }
            FormattedNumber formatted = this.icuFormatter.format(valToCheck.doubleValue() - (double)offset.intValue());
            String match = this.rules.select(formatted);
            if (match.equals("other")) {
                match = "<<::CatchallKey::>>";
            }
            return match.equals(key);
        }
    }

    static class NumberFormatterImpl
    implements Formatter {
        private final Locale locale;
        private final Map<String, Object> fixedOptions;
        private final LocalizedNumberFormatter icuFormatter;
        private final String kind;

        NumberFormatterImpl(Locale locale, Map<String, Object> fixedOptions, String kind) {
            this.locale = OptUtils.getBestLocale(fixedOptions, locale);
            this.fixedOptions = new HashMap<String, Object>(fixedOptions);
            String skeleton = OptUtils.getString(fixedOptions, "icu:skeleton");
            boolean fancy = skeleton != null;
            this.icuFormatter = NumberFormatterFactory.formatterForOptions(this.locale, fixedOptions, kind);
            this.kind = kind;
        }

        LocalizedNumberFormatter getIcuFormatter() {
            return this.icuFormatter;
        }

        @Override
        public String formatToString(Object toFormat, Map<String, Object> variableOptions) {
            return this.format(toFormat, variableOptions).toString();
        }

        @Override
        public FormattedPlaceholder format(Object toFormat, Map<String, Object> variableOptions) {
            String currencyCode;
            LocalizedNumberFormatter realFormatter;
            boolean reportErrors = OptUtils.reportErrors(this.fixedOptions) || OptUtils.reportErrors(variableOptions);
            HashMap<String, Object> mergedOptions = new HashMap<String, Object>(this.fixedOptions);
            if (variableOptions.isEmpty()) {
                realFormatter = this.icuFormatter;
            } else {
                mergedOptions.putAll(variableOptions);
                realFormatter = NumberFormatterFactory.formatterForOptions(this.locale, mergedOptions, this.kind);
            }
            Integer offset = OptUtils.getInteger(variableOptions, reportErrors, "icu:offset");
            if (offset == null && this.fixedOptions != null) {
                offset = OptUtils.getInteger(this.fixedOptions, reportErrors, "icu:offset");
            }
            if (offset == null) {
                offset = 0;
            }
            Double mathOperand = null;
            if ("math".equals(this.kind)) {
                ResolvedMathOptions resolvedMathOptions = ResolvedMathOptions.of(this.fixedOptions);
                mathOperand = resolvedMathOptions.operand;
            }
            if (this.kind.equals("currency") && (currencyCode = NumberFormatterFactory.getCurrency(mergedOptions)) == null && !(toFormat instanceof CurrencyAmount)) {
                throw new IllegalArgumentException("bad-option: the `currency` must be an ISO 4217 code.");
            }
            boolean isInt = this.kind.equals("integer");
            FormattedValue result = null;
            if (toFormat == null) {
                throw new NullPointerException("Argument to format can't be null");
            }
            if (toFormat instanceof Double) {
                if (isInt) {
                    toFormat = Math.floor((Double)toFormat);
                }
                double toFormatAdjusted = (Double)toFormat - (double)offset.intValue();
                if (mathOperand != null) {
                    toFormatAdjusted += mathOperand.doubleValue();
                }
                result = realFormatter.format(toFormatAdjusted);
            } else if (toFormat instanceof Long) {
                result = mathOperand != null ? realFormatter.format((double)((Long)toFormat - (long)offset.intValue()) + mathOperand) : realFormatter.format((Long)toFormat - (long)offset.intValue());
            } else if (toFormat instanceof Integer) {
                result = mathOperand != null ? realFormatter.format((double)((Integer)toFormat - offset) + mathOperand) : realFormatter.format((Integer)toFormat - offset);
            } else if (toFormat instanceof BigDecimal) {
                BigDecimal bd = (BigDecimal)toFormat;
                if (isInt) {
                    toFormat = bd.longValue();
                }
                bd = bd.subtract(BigDecimal.valueOf(offset.intValue()));
                if (mathOperand != null) {
                    bd = bd.add(BigDecimal.valueOf(mathOperand));
                }
                result = realFormatter.format(bd);
            } else if (toFormat instanceof Number) {
                if (isInt) {
                    toFormat = Math.floor(((Number)toFormat).doubleValue());
                }
                double toFormatAdjusted = ((Number)toFormat).doubleValue() - (double)offset.intValue();
                if (mathOperand != null) {
                    toFormatAdjusted += mathOperand.doubleValue();
                }
                result = realFormatter.format(toFormatAdjusted);
            } else if (toFormat instanceof CurrencyAmount) {
                result = realFormatter.format((CurrencyAmount)toFormat);
            } else {
                String strValue = Objects.toString(toFormat);
                Number nrValue = OptUtils.asNumber(reportErrors, "argument", strValue);
                if (nrValue != null) {
                    if (isInt) {
                        toFormat = Math.floor(nrValue.doubleValue());
                    }
                    double toFormatAdjusted = nrValue.doubleValue() - (double)offset.intValue();
                    if (mathOperand != null) {
                        toFormatAdjusted += mathOperand.doubleValue();
                    }
                    result = realFormatter.format(toFormatAdjusted);
                } else {
                    result = new PlainStringFormattedValue("{|" + strValue + "|}");
                }
            }
            Directionality dir = OptUtils.getBestDirectionality(variableOptions, this.locale);
            return new FormattedPlaceholder(toFormat, result, dir, false);
        }
    }
}

