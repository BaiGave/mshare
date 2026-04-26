/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.LongNameHandler;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.number.MixedUnitLongNameHandler;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import java.util.ArrayList;
import java.util.List;

public class LongNameMultiplexer
implements MicroPropsGenerator {
    private final MicroPropsGenerator fParent;
    private List<ParentlessMicroPropsGenerator> fHandlers;
    private List<MeasureUnit> fMeasureUnits;

    public LongNameMultiplexer(MicroPropsGenerator fParent) {
        this.fParent = fParent;
    }

    public static LongNameMultiplexer forMeasureUnits(ULocale locale, List<MeasureUnit> units, NumberFormatter.UnitWidth width, String unitDisplayCase, PluralRules rules, MicroPropsGenerator parent) {
        LongNameMultiplexer result = new LongNameMultiplexer(parent);
        assert (units.size() > 0);
        result.fMeasureUnits = new ArrayList<MeasureUnit>();
        result.fHandlers = new ArrayList<ParentlessMicroPropsGenerator>();
        for (int i = 0; i < units.size(); ++i) {
            MeasureUnit unit = units.get(i);
            result.fMeasureUnits.add(unit);
            if (unit.getComplexity() == MeasureUnit.Complexity.MIXED) {
                MixedUnitLongNameHandler mlnh = MixedUnitLongNameHandler.forMeasureUnit(locale, unit, width, unitDisplayCase, rules, null);
                result.fHandlers.add(mlnh);
                continue;
            }
            LongNameHandler lnh = LongNameHandler.forMeasureUnit(locale, unit, width, unitDisplayCase, rules, null);
            result.fHandlers.add(lnh);
        }
        return result;
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps micros = this.fParent.processQuantity(quantity);
        for (int i = 0; i < this.fHandlers.size(); ++i) {
            if (!this.fMeasureUnits.get(i).equals(micros.outputUnit)) continue;
            ParentlessMicroPropsGenerator handler = this.fHandlers.get(i);
            return handler.processQuantityWithMicros(quantity, micros);
        }
        throw new AssertionError((Object)" We shouldn't receive any outputUnit for which we haven't already got a LongNameHandler");
    }

    public static interface ParentlessMicroPropsGenerator {
        public MicroProps processQuantityWithMicros(DecimalQuantity var1, MicroProps var2);
    }
}

