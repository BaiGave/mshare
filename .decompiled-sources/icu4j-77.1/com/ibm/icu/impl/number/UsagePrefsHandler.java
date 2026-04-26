/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.units.ComplexUnitsConverter;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.UnitsRouter;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import java.math.BigDecimal;
import java.util.List;

public class UsagePrefsHandler
implements MicroPropsGenerator {
    private final MicroPropsGenerator fParent;
    private UnitsRouter fUnitsRouter;

    public UsagePrefsHandler(ULocale locale, MeasureUnit inputUnit, String usage, MicroPropsGenerator parent) {
        assert (parent != null);
        this.fParent = parent;
        this.fUnitsRouter = new UnitsRouter(MeasureUnitImpl.forIdentifier(inputUnit.getIdentifier()), locale, usage);
    }

    protected static void mixedMeasuresToMicros(ComplexUnitsConverter.ComplexConverterResult complexConverterResult, DecimalQuantity quantity, MicroProps outMicros) {
        outMicros.mixedMeasures = complexConverterResult.measures;
        outMicros.indexOfQuantity = complexConverterResult.indexOfQuantity;
        quantity.setToBigDecimal((BigDecimal)outMicros.mixedMeasures.get(outMicros.indexOfQuantity).getNumber());
    }

    public List<MeasureUnit> getOutputUnits() {
        return this.fUnitsRouter.getOutputUnits();
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps micros = this.fParent.processQuantity(quantity);
        quantity.roundToInfinity();
        UnitsRouter.RouteResult routed = this.fUnitsRouter.route(quantity.toBigDecimal(), micros);
        micros.outputUnit = routed.outputUnit.build();
        UsagePrefsHandler.mixedMeasuresToMicros(routed.complexConverterResult, quantity, micros);
        return micros;
    }
}

