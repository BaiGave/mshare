/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.number.MicroPropsGenerator;
import com.ibm.icu.impl.number.UsagePrefsHandler;
import com.ibm.icu.impl.units.ComplexUnitsConverter;
import com.ibm.icu.impl.units.ConversionRates;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.util.MeasureUnit;

public class UnitConversionHandler
implements MicroPropsGenerator {
    private final MicroPropsGenerator fParent;
    private MeasureUnit fOutputUnit;
    private ComplexUnitsConverter fComplexUnitConverter;

    public UnitConversionHandler(MeasureUnit targetUnit, MicroPropsGenerator parent) {
        this.fOutputUnit = targetUnit;
        this.fParent = parent;
        MeasureUnitImpl targetUnitImpl = MeasureUnitImpl.forIdentifier(targetUnit.getIdentifier());
        this.fComplexUnitConverter = new ComplexUnitsConverter(targetUnitImpl, new ConversionRates());
    }

    @Override
    public MicroProps processQuantity(DecimalQuantity quantity) {
        MicroProps result = this.fParent.processQuantity(quantity);
        quantity.roundToInfinity();
        ComplexUnitsConverter.ComplexConverterResult complexConverterResult = this.fComplexUnitConverter.convert(quantity.toBigDecimal(), result.rounder);
        result.outputUnit = this.fOutputUnit;
        UsagePrefsHandler.mixedMeasuresToMicros(complexConverterResult, quantity, result);
        return result;
    }
}

