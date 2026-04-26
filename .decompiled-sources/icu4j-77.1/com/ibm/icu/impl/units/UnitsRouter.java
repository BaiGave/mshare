/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.units;

import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.impl.number.MicroProps;
import com.ibm.icu.impl.units.ComplexUnitsConverter;
import com.ibm.icu.impl.units.ConversionRates;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.UnitPreferences;
import com.ibm.icu.impl.units.UnitsData;
import com.ibm.icu.number.Precision;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.ULocale;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UnitsRouter {
    private ArrayList<MeasureUnit> outputUnits_ = new ArrayList();
    private ArrayList<ConverterPreference> converterPreferences_ = new ArrayList();

    public UnitsRouter(String inputUnitIdentifier, ULocale locale, String usage) {
        this(MeasureUnitImpl.forIdentifier(inputUnitIdentifier), locale, usage);
    }

    public UnitsRouter(MeasureUnitImpl inputUnit, ULocale locale, String usage) {
        UnitsData data = new UnitsData();
        String category = data.getCategory(inputUnit);
        UnitPreferences.UnitPreference[] unitPreferences = data.getPreferencesFor(category, usage, locale);
        for (int i = 0; i < unitPreferences.length; ++i) {
            UnitPreferences.UnitPreference preference = unitPreferences[i];
            MeasureUnitImpl complexTargetUnitImpl = MeasureUnitImpl.UnitsParser.parseForIdentifier(preference.getUnit());
            String precision = preference.getSkeleton();
            if (!precision.isEmpty() && !precision.startsWith("precision-increment")) {
                throw new AssertionError((Object)"Only `precision-increment` is allowed");
            }
            this.outputUnits_.add(complexTargetUnitImpl.build());
            this.converterPreferences_.add(new ConverterPreference(inputUnit, complexTargetUnitImpl, preference.getGeq(), precision, data.getConversionRates()));
        }
    }

    public RouteResult route(BigDecimal quantity, MicroProps micros) {
        Precision rounder = micros == null ? null : micros.rounder;
        ConverterPreference converterPreference = null;
        Iterator<ConverterPreference> iterator = this.converterPreferences_.iterator();
        while (iterator.hasNext()) {
            ConverterPreference itr;
            converterPreference = itr = iterator.next();
            if (!converterPreference.converter.greaterThanOrEqual(quantity.abs(), converterPreference.limit)) continue;
            break;
        }
        assert (converterPreference != null);
        assert (converterPreference.precision != null);
        if (rounder != null && rounder instanceof Precision.BogusRounder) {
            Precision.BogusRounder bogus = (Precision.BogusRounder)rounder;
            rounder = converterPreference.precision.length() > 0 ? bogus.into(UnitsRouter.parseSkeletonToPrecision(converterPreference.precision)) : bogus.into(Precision.integer().withMinDigits(2));
        }
        if (micros != null) {
            micros.rounder = rounder;
        }
        return new RouteResult(converterPreference.converter.convert(quantity, rounder), converterPreference.targetUnit);
    }

    private static Precision parseSkeletonToPrecision(String precisionSkeleton) {
        String kSkeletonPrefix = "precision-increment/";
        if (!precisionSkeleton.startsWith("precision-increment/")) {
            throw new IllegalIcuArgumentException("precisionSkeleton is only precision-increment");
        }
        String incrementValue = precisionSkeleton.substring("precision-increment/".length());
        return Precision.increment(new BigDecimal(incrementValue));
    }

    public List<MeasureUnit> getOutputUnits() {
        return this.outputUnits_;
    }

    public class RouteResult {
        public final ComplexUnitsConverter.ComplexConverterResult complexConverterResult;
        public final MeasureUnitImpl outputUnit;

        RouteResult(ComplexUnitsConverter.ComplexConverterResult complexConverterResult, MeasureUnitImpl outputUnit) {
            this.complexConverterResult = complexConverterResult;
            this.outputUnit = outputUnit;
        }
    }

    public static class ConverterPreference {
        final MeasureUnitImpl targetUnit;
        final ComplexUnitsConverter converter;
        final BigDecimal limit;
        final String precision;

        public ConverterPreference(MeasureUnitImpl source, MeasureUnitImpl targetUnit, String precision, ConversionRates conversionRates) {
            this(source, targetUnit, BigDecimal.valueOf(Double.MIN_VALUE), precision, conversionRates);
        }

        public ConverterPreference(MeasureUnitImpl source, MeasureUnitImpl targetUnit, BigDecimal limit, String precision, ConversionRates conversionRates) {
            this.converter = new ComplexUnitsConverter(source, targetUnit, conversionRates);
            this.limit = limit;
            this.precision = precision;
            this.targetUnit = targetUnit;
        }
    }
}

