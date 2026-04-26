/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.FormattedStringBuilder;
import com.ibm.icu.impl.SimpleFormatterImpl;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.DecimalQuantity_DualStorageBCD;
import com.ibm.icu.impl.number.LongNameHandler;
import com.ibm.icu.impl.number.LongNameMultiplexer;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.number.Modifier;
import com.ibm.icu.impl.number.ModifierStore;
import com.ibm.icu.impl.number.RoundingUtils;
import com.ibm.icu.impl.number.SimpleModifier;
import com.ibm.icu.number.LocalizedNumberFormatter;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.text.ListFormatter;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.text.SimpleFormatter;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import java.util.ArrayList;
import java.util.List;

public class MixedUnitLongNameHandler
implements MicroPropsGenerator,
ModifierStore,
LongNameMultiplexer.ParentlessMicroPropsGenerator {
    private final PluralRules rules;
    private final MicroPropsGenerator parent;
    private List<String[]> fMixedUnitData;
    private LocalizedNumberFormatter fIntegerFormatter;
    private ListFormatter fListFormatter;

    private MixedUnitLongNameHandler(PluralRules rules, MicroPropsGenerator parent) {
        this.rules = rules;
        this.parent = parent;
    }

    public static MixedUnitLongNameHandler forMeasureUnit(ULocale locale, MeasureUnit mixedUnit, NumberFormatter.UnitWidth width, String unitDisplayCase, PluralRules rules, MicroPropsGenerator parent) {
        assert (mixedUnit.getComplexity() == MeasureUnit.Complexity.MIXED) : "MixedUnitLongNameHandler only supports MIXED units";
        MixedUnitLongNameHandler result = new MixedUnitLongNameHandler(rules, parent);
        List<MeasureUnit> individualUnits = mixedUnit.splitToSingleUnits();
        result.fMixedUnitData = new ArrayList<String[]>();
        for (int i = 0; i < individualUnits.size(); ++i) {
            String[] unitData = new String[LongNameHandler.ARRAY_LENGTH];
            LongNameHandler.getMeasureData(locale, individualUnits.get(i), width, unitDisplayCase, unitData);
            result.fMixedUnitData.add(unitData);
        }
        ListFormatter.Width listWidth = ListFormatter.Width.SHORT;
        if (width == NumberFormatter.UnitWidth.NARROW) {
            listWidth = ListFormatter.Width.NARROW;
        } else if (width == NumberFormatter.UnitWidth.FULL_NAME) {
            listWidth = ListFormatter.Width.WIDE;
        }
        result.fListFormatter = ListFormatter.getInstance(locale, ListFormatter.Type.UNITS, listWidth);
        result.fIntegerFormatter = NumberFormatter.withLocale(locale);
        return result;
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        assert (this.fMixedUnitData.size() > 1);
        MicroProps micros = this.parent.processQuantity(quantity);
        micros.modOuter = this.getMixedUnitModifier(quantity, micros);
        return micros;
    }

    @Override
    public MicroProps processQuantityWithMicros(DecimalQuantity quantity, MicroProps micros) {
        assert (this.fMixedUnitData.size() > 1);
        micros.modOuter = this.getMixedUnitModifier(quantity, micros);
        return micros;
    }

    @Override
    public Modifier getModifier(Modifier.Signum signum, StandardPlural plural) {
        assert (false) : "should be unreachable";
        return null;
    }

    private Modifier getMixedUnitModifier(DecimalQuantity quantity, MicroProps micros) {
        if (micros.mixedMeasures.size() == 0) {
            assert (false) : "Mixed unit: we must have more than one unit value";
            throw new UnsupportedOperationException();
        }
        ArrayList<String> outputMeasuresList = new ArrayList<String>();
        StandardPlural quantityPlural = StandardPlural.OTHER;
        for (int i = 0; i < micros.mixedMeasures.size(); ++i) {
            if (i == micros.indexOfQuantity) {
                if (i > 0 && quantity.isNegative()) {
                    quantity.negate();
                }
                quantityPlural = RoundingUtils.getPluralSafe(micros.rounder, this.rules, quantity);
                String quantitySimpleFormat = LongNameHandler.getWithPlural(this.fMixedUnitData.get(i), quantityPlural);
                SimpleFormatter finalFormatter = SimpleFormatter.compileMinMaxArguments(quantitySimpleFormat, 0, 1);
                outputMeasuresList.add(finalFormatter.format("{0}"));
                continue;
            }
            DecimalQuantity_DualStorageBCD fdec = new DecimalQuantity_DualStorageBCD(micros.mixedMeasures.get(i).getNumber());
            if (i > 0 && fdec.isNegative()) {
                fdec.negate();
            }
            StandardPlural pluralForm = RoundingUtils.getPluralSafe(micros.rounder, this.rules, fdec);
            String simpleFormat = LongNameHandler.getWithPlural(this.fMixedUnitData.get(i), pluralForm);
            SimpleFormatter compiledFormatter = SimpleFormatter.compileMinMaxArguments(simpleFormat, 0, 1);
            FormattedStringBuilder appendable = new FormattedStringBuilder();
            this.fIntegerFormatter.formatImpl(fdec, appendable);
            outputMeasuresList.add(compiledFormatter.format(appendable.toString()));
        }
        String premixedFormatPattern = this.fListFormatter.format(outputMeasuresList);
        StringBuilder sb = new StringBuilder();
        String premixedCompiled = SimpleFormatterImpl.compileToStringMinMaxArguments(premixedFormatPattern, sb, 0, 1);
        Modifier.Parameters params = new Modifier.Parameters();
        params.obj = this;
        params.signum = Modifier.Signum.POS_ZERO;
        params.plural = quantityPlural;
        return new SimpleModifier(premixedCompiled, null, false, params);
    }
}

