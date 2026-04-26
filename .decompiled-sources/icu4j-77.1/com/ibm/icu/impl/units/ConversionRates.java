/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.units;

import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.IllegalIcuArgumentException;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.SingleUnitImpl;
import com.ibm.icu.impl.units.UnitsConverter;
import com.ibm.icu.util.MeasureUnit;
import com.ibm.icu.util.UResourceBundle;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;

public class ConversionRates {
    private HashMap<String, ConversionRateInfo> mapToConversionRate;

    public ConversionRates() {
        ICUResourceBundle resource = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "units");
        ConversionRatesSink sink = new ConversionRatesSink();
        resource.getAllItemsWithFallback("convertUnits", sink);
        this.mapToConversionRate = sink.getMapToConversionRate();
    }

    private UnitsConverter.Factor getFactorToBase(SingleUnitImpl singleUnit) {
        int power = singleUnit.getDimensionality();
        MeasureUnit.MeasurePrefix unitPrefix = singleUnit.getPrefix();
        UnitsConverter.Factor result = UnitsConverter.Factor.processFactor(this.mapToConversionRate.get(singleUnit.getSimpleUnitID()).getConversionRate());
        return result.applyPrefix(unitPrefix).power(power);
    }

    public UnitsConverter.Factor getFactorToBase(MeasureUnitImpl measureUnit) {
        UnitsConverter.Factor result = new UnitsConverter.Factor();
        for (SingleUnitImpl singleUnit : measureUnit.getSingleUnits()) {
            result = result.multiply(this.getFactorToBase(singleUnit));
        }
        if (measureUnit.getConstantDenominator() != 0L) {
            result = result.divide(BigDecimal.valueOf(measureUnit.getConstantDenominator()));
        }
        return result;
    }

    protected BigDecimal getOffset(MeasureUnitImpl source, MeasureUnitImpl target, UnitsConverter.Factor sourceToBase, UnitsConverter.Factor targetToBase, UnitsConverter.Convertibility convertibility) {
        if (convertibility != UnitsConverter.Convertibility.CONVERTIBLE) {
            return BigDecimal.valueOf(0L);
        }
        if (!this.checkSimpleUnit(source) || !this.checkSimpleUnit(target)) {
            return BigDecimal.valueOf(0L);
        }
        String sourceSimpleIdentifier = source.getSingleUnits().get(0).getSimpleUnitID();
        String targetSimpleIdentifier = target.getSingleUnits().get(0).getSimpleUnitID();
        BigDecimal sourceOffset = this.mapToConversionRate.get(sourceSimpleIdentifier).getOffset();
        BigDecimal targetOffset = this.mapToConversionRate.get(targetSimpleIdentifier).getOffset();
        return sourceOffset.subtract(targetOffset).divide(targetToBase.getConversionRate(), MathContext.DECIMAL128);
    }

    protected String getSpecialMappingName(MeasureUnitImpl simpleUnit) {
        if (!this.checkSimpleUnit(simpleUnit)) {
            return null;
        }
        String simpleIdentifier = simpleUnit.getSingleUnits().get(0).getSimpleUnitID();
        return this.mapToConversionRate.get(simpleIdentifier).getSpecialMappingName();
    }

    public MeasureUnitImpl extractCompoundBaseUnit(MeasureUnitImpl measureUnit) {
        ArrayList<SingleUnitImpl> baseUnits = this.extractBaseUnits(measureUnit);
        MeasureUnitImpl result = new MeasureUnitImpl();
        for (SingleUnitImpl baseUnit : baseUnits) {
            result.appendSingleUnit(baseUnit);
        }
        return result;
    }

    public ArrayList<SingleUnitImpl> extractBaseUnits(MeasureUnitImpl measureUnitImpl) {
        ArrayList<SingleUnitImpl> result = new ArrayList<SingleUnitImpl>();
        ArrayList<SingleUnitImpl> singleUnits = measureUnitImpl.getSingleUnits();
        for (SingleUnitImpl singleUnit : singleUnits) {
            result.addAll(this.extractBaseUnits(singleUnit));
        }
        return result;
    }

    public ArrayList<SingleUnitImpl> extractBaseUnits(SingleUnitImpl singleUnit) {
        String target = this.mapToConversionRate.get(singleUnit.getSimpleUnitID()).getTarget();
        MeasureUnitImpl targetImpl = MeasureUnitImpl.UnitsParser.parseForIdentifier(target);
        targetImpl.applyDimensionality(singleUnit.getDimensionality());
        return targetImpl.getSingleUnits();
    }

    public String extractSystems(SingleUnitImpl singleUnit) {
        return this.mapToConversionRate.get(singleUnit.getSimpleUnitID()).getSystems();
    }

    private boolean checkSimpleUnit(MeasureUnitImpl measureUnitImpl) {
        if (measureUnitImpl.getComplexity() != MeasureUnit.Complexity.SINGLE) {
            return false;
        }
        SingleUnitImpl singleUnit = measureUnitImpl.getSingleUnits().get(0);
        if (singleUnit.getPrefix() != MeasureUnit.MeasurePrefix.ONE) {
            return false;
        }
        return singleUnit.getDimensionality() == 1;
    }

    public static class ConversionRateInfo {
        private final String simpleUnit;
        private final String target;
        private final String conversionRate;
        private final BigDecimal offset;
        private final String specialMappingName;
        private final String systems;

        public ConversionRateInfo(String simpleUnit, String target, String conversionRate, String offset, String special, String systems) {
            this.simpleUnit = simpleUnit;
            this.target = target;
            this.conversionRate = conversionRate;
            this.offset = ConversionRateInfo.forNumberWithDivision(offset);
            this.specialMappingName = special;
            this.systems = systems;
        }

        private static BigDecimal forNumberWithDivision(String numberWithDivision) {
            String[] numbers = numberWithDivision.split("/");
            assert (numbers.length <= 2);
            if (numbers.length == 1) {
                return new BigDecimal(numbers[0]);
            }
            return new BigDecimal(numbers[0]).divide(new BigDecimal(numbers[1]), MathContext.DECIMAL128);
        }

        public String getTarget() {
            return this.target;
        }

        public BigDecimal getOffset() {
            return this.offset;
        }

        public String getConversionRate() {
            if (this.conversionRate == null) {
                throw new IllegalIcuArgumentException("trying to use a null conversion rate (for special?)");
            }
            return this.conversionRate;
        }

        public String getSpecialMappingName() {
            return this.specialMappingName;
        }

        public String getSystems() {
            return this.systems;
        }
    }

    public static class ConversionRatesSink
    extends UResource.Sink {
        private HashMap<String, ConversionRateInfo> mapToConversionRate = new HashMap();

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            assert ("convertUnits".equals(key.toString()));
            UResource.Table conversionRateTable = value.getTable();
            int i = 0;
            while (conversionRateTable.getKeyAndValue(i, key, value)) {
                assert (value.getType() == 2);
                String simpleUnit = key.toString();
                UResource.Table simpleUnitConversionInfo = value.getTable();
                String target = null;
                String factor = null;
                String offset = "0";
                String special = null;
                String systems = null;
                int j = 0;
                while (simpleUnitConversionInfo.getKeyAndValue(j, key, value)) {
                    assert (value.getType() == 0);
                    String keyString = key.toString();
                    String valueString = value.toString().replaceAll(" ", "");
                    if ("target".equals(keyString)) {
                        target = valueString;
                    } else if ("factor".equals(keyString)) {
                        factor = valueString;
                    } else if ("offset".equals(keyString)) {
                        offset = valueString;
                    } else if ("special".equals(keyString)) {
                        special = valueString;
                    } else if ("systems".equals(keyString)) {
                        systems = value.toString();
                    } else assert (false) : "The key must be target, factor, offset, special, or systems";
                    ++j;
                }
                assert (target != null);
                assert (factor != null || special != null);
                this.mapToConversionRate.put(simpleUnit, new ConversionRateInfo(simpleUnit, target, factor, offset, special, systems));
                ++i;
            }
        }

        public HashMap<String, ConversionRateInfo> getMapToConversionRate() {
            return this.mapToConversionRate;
        }
    }
}

