/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.units;

import com.ibm.icu.impl.number.DecimalQuantity_DualStorageBCD;
import com.ibm.icu.impl.units.ConversionRates;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.UnitsConverter;
import com.ibm.icu.number.Precision;
import com.ibm.icu.util.Measure;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComplexUnitsConverter {
    public static final BigDecimal EPSILON = BigDecimal.valueOf(Math.ulp(1.0));
    public static final BigDecimal EPSILON_MULTIPLIER = BigDecimal.valueOf(1L).add(EPSILON);
    public ArrayList<UnitsConverter> unitsConverters_;
    public List<MeasureUnitImpl.MeasureUnitImplWithIndex> units_;
    private MeasureUnitImpl inputUnit_;

    public ComplexUnitsConverter(MeasureUnitImpl targetUnit, ConversionRates conversionRates) {
        this.units_ = targetUnit.extractIndividualUnitsWithIndices();
        assert (!this.units_.isEmpty());
        this.inputUnit_ = this.units_.get((int)0).unitImpl;
        MeasureUnitImpl.MeasureUnitImplComparator comparator = new MeasureUnitImpl.MeasureUnitImplComparator(conversionRates);
        for (MeasureUnitImpl.MeasureUnitImplWithIndex unitWithIndex : this.units_) {
            if (comparator.compare(unitWithIndex.unitImpl, this.inputUnit_) <= 0) continue;
            this.inputUnit_ = unitWithIndex.unitImpl;
        }
        this.init(conversionRates);
    }

    public ComplexUnitsConverter(String inputUnitIdentifier, String outputUnitsIdentifier) {
        this(MeasureUnitImpl.forIdentifier(inputUnitIdentifier), MeasureUnitImpl.forIdentifier(outputUnitsIdentifier), new ConversionRates());
    }

    public ComplexUnitsConverter(MeasureUnitImpl inputUnit, MeasureUnitImpl outputUnits, ConversionRates conversionRates) {
        this.inputUnit_ = inputUnit;
        this.units_ = outputUnits.extractIndividualUnitsWithIndices();
        assert (!this.units_.isEmpty());
        this.init(conversionRates);
    }

    private void init(ConversionRates conversionRates) {
        Collections.sort(this.units_, Collections.reverseOrder(new MeasureUnitImpl.MeasureUnitImplWithIndexComparator(conversionRates)));
        this.unitsConverters_ = new ArrayList();
        int n = this.units_.size();
        for (int i = 0; i < n; ++i) {
            if (i == 0) {
                this.unitsConverters_.add(new UnitsConverter(this.inputUnit_, this.units_.get((int)i).unitImpl, conversionRates));
                continue;
            }
            this.unitsConverters_.add(new UnitsConverter(this.units_.get((int)(i - 1)).unitImpl, this.units_.get((int)i).unitImpl, conversionRates));
        }
    }

    public boolean greaterThanOrEqual(BigDecimal quantity, BigDecimal limit) {
        assert (!this.units_.isEmpty());
        return this.unitsConverters_.get(0).convert(quantity).multiply(EPSILON_MULTIPLIER).compareTo(limit) >= 0;
    }

    public ComplexConverterResult convert(BigDecimal quantity, Precision rounder) {
        BigInteger sign = BigInteger.ONE;
        if (quantity.compareTo(BigDecimal.ZERO) < 0 && this.unitsConverters_.size() > 1) {
            quantity = quantity.abs();
            sign = sign.negate();
        }
        ArrayList<BigInteger> intValues = new ArrayList<BigInteger>(this.unitsConverters_.size() - 1);
        int n = this.unitsConverters_.size();
        for (int i = 0; i < n; ++i) {
            quantity = this.unitsConverters_.get(i).convert(quantity);
            if (i >= n - 1) continue;
            BigInteger flooredQuantity = quantity.multiply(EPSILON_MULTIPLIER).setScale(0, RoundingMode.FLOOR).toBigInteger();
            intValues.add(flooredQuantity);
            BigDecimal remainder = quantity.subtract(BigDecimal.valueOf(flooredQuantity.longValue()));
            quantity = remainder.compareTo(BigDecimal.ZERO) == -1 ? BigDecimal.ZERO : remainder;
        }
        quantity = this.applyRounder(intValues, quantity, rounder);
        ArrayList<Measure> measures = new ArrayList<Measure>(this.unitsConverters_.size());
        for (int i = 0; i < this.unitsConverters_.size(); ++i) {
            measures.add(null);
        }
        int indexOfQuantity = -1;
        int n2 = this.unitsConverters_.size();
        for (int i = 0; i < n2; ++i) {
            Measure measure;
            if (i < n2 - 1) {
                measure = new Measure(((BigInteger)intValues.get(i)).multiply(sign), this.units_.get((int)i).unitImpl.build());
                measures.set(this.units_.get((int)i).index, measure);
                continue;
            }
            indexOfQuantity = this.units_.get((int)i).index;
            measure = new Measure(quantity.multiply(BigDecimal.valueOf(sign.longValue())), this.units_.get((int)i).unitImpl.build());
            measures.set(indexOfQuantity, measure);
        }
        return new ComplexConverterResult(indexOfQuantity, measures);
    }

    private BigDecimal applyRounder(List<BigInteger> intValues, BigDecimal quantity, Precision rounder) {
        if (rounder == null) {
            return quantity;
        }
        DecimalQuantity_DualStorageBCD quantityBCD = new DecimalQuantity_DualStorageBCD(quantity);
        rounder.apply(quantityBCD);
        quantity = quantityBCD.toBigDecimal();
        if (intValues.size() == 0) {
            return quantity;
        }
        int lastIndex = this.unitsConverters_.size() - 1;
        BigDecimal carry = this.unitsConverters_.get(lastIndex).convertInverse(quantity).multiply(EPSILON_MULTIPLIER).setScale(0, RoundingMode.FLOOR);
        if (carry.compareTo(BigDecimal.ZERO) <= 0) {
            return quantity;
        }
        quantity = quantity.subtract(this.unitsConverters_.get(lastIndex).convert(carry));
        intValues.set(lastIndex - 1, intValues.get(lastIndex - 1).add(carry.toBigInteger()));
        for (int j = lastIndex - 1; j > 0 && (carry = this.unitsConverters_.get(j).convertInverse(BigDecimal.valueOf(intValues.get(j).longValue())).multiply(EPSILON_MULTIPLIER).setScale(0, RoundingMode.FLOOR)).compareTo(BigDecimal.ZERO) > 0; --j) {
            intValues.set(j, intValues.get(j).subtract(this.unitsConverters_.get(j).convert(carry).toBigInteger()));
            intValues.set(j - 1, intValues.get(j - 1).add(carry.toBigInteger()));
        }
        return quantity;
    }

    public String toString() {
        return "ComplexUnitsConverter [unitsConverters_=" + this.unitsConverters_ + ", units_=" + this.units_ + "]";
    }

    public static class ComplexConverterResult {
        public final int indexOfQuantity;
        public final List<Measure> measures;

        ComplexConverterResult(int indexOfQuantity, List<Measure> measures) {
            this.indexOfQuantity = indexOfQuantity;
            this.measures = measures;
        }
    }
}

