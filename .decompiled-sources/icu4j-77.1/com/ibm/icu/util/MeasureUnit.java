/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.util;

import com.ibm.icu.impl.CollectionSet;
import com.ibm.icu.impl.ICUResourceBundle;
import com.ibm.icu.impl.UResource;
import com.ibm.icu.impl.units.MeasureUnitImpl;
import com.ibm.icu.impl.units.SingleUnitImpl;
import com.ibm.icu.text.UnicodeSet;
import com.ibm.icu.util.Currency;
import com.ibm.icu.util.NoUnit;
import com.ibm.icu.util.TimeUnit;
import com.ibm.icu.util.UResourceBundle;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MeasureUnit
implements Serializable {
    private static final long serialVersionUID = -1839973855554750484L;
    private static final Map<String, Map<String, MeasureUnit>> cache = new HashMap<String, Map<String, MeasureUnit>>();
    private static boolean cacheIsPopulated = false;
    @Deprecated
    protected final String type;
    @Deprecated
    protected final String subType;
    private MeasureUnitImpl measureUnitImpl;
    static final UnicodeSet ASCII = new UnicodeSet(97, 122).freeze();
    static final UnicodeSet ASCII_HYPHEN_DIGITS = new UnicodeSet(45, 45, 48, 57, 97, 122).freeze();
    private static Factory UNIT_FACTORY = new Factory(){

        @Override
        public MeasureUnit create(String type, String subType) {
            return new MeasureUnit(type, subType);
        }
    };
    static Factory CURRENCY_FACTORY = new Factory(){

        @Override
        public MeasureUnit create(String unusedType, String subType) {
            return new Currency(subType);
        }
    };
    static Factory TIMEUNIT_FACTORY = new Factory(){

        @Override
        public MeasureUnit create(String type, String subType) {
            return new TimeUnit(type, subType);
        }
    };
    public static final MeasureUnit G_FORCE = MeasureUnit.internalGetInstance("acceleration", "g-force");
    public static final MeasureUnit METER_PER_SECOND_SQUARED = MeasureUnit.internalGetInstance("acceleration", "meter-per-square-second");
    public static final MeasureUnit ARC_MINUTE = MeasureUnit.internalGetInstance("angle", "arc-minute");
    public static final MeasureUnit ARC_SECOND = MeasureUnit.internalGetInstance("angle", "arc-second");
    public static final MeasureUnit DEGREE = MeasureUnit.internalGetInstance("angle", "degree");
    public static final MeasureUnit RADIAN = MeasureUnit.internalGetInstance("angle", "radian");
    public static final MeasureUnit REVOLUTION_ANGLE = MeasureUnit.internalGetInstance("angle", "revolution");
    public static final MeasureUnit ACRE = MeasureUnit.internalGetInstance("area", "acre");
    public static final MeasureUnit DUNAM = MeasureUnit.internalGetInstance("area", "dunam");
    public static final MeasureUnit HECTARE = MeasureUnit.internalGetInstance("area", "hectare");
    public static final MeasureUnit SQUARE_CENTIMETER = MeasureUnit.internalGetInstance("area", "square-centimeter");
    public static final MeasureUnit SQUARE_FOOT = MeasureUnit.internalGetInstance("area", "square-foot");
    public static final MeasureUnit SQUARE_INCH = MeasureUnit.internalGetInstance("area", "square-inch");
    public static final MeasureUnit SQUARE_KILOMETER = MeasureUnit.internalGetInstance("area", "square-kilometer");
    public static final MeasureUnit SQUARE_METER = MeasureUnit.internalGetInstance("area", "square-meter");
    public static final MeasureUnit SQUARE_MILE = MeasureUnit.internalGetInstance("area", "square-mile");
    public static final MeasureUnit SQUARE_YARD = MeasureUnit.internalGetInstance("area", "square-yard");
    public static final MeasureUnit ITEM = MeasureUnit.internalGetInstance("concentr", "item");
    public static final MeasureUnit KARAT = MeasureUnit.internalGetInstance("concentr", "karat");
    public static final MeasureUnit MILLIGRAM_OFGLUCOSE_PER_DECILITER = MeasureUnit.internalGetInstance("concentr", "milligram-ofglucose-per-deciliter");
    public static final MeasureUnit MILLIGRAM_PER_DECILITER = MeasureUnit.internalGetInstance("concentr", "milligram-per-deciliter");
    public static final MeasureUnit MILLIMOLE_PER_LITER = MeasureUnit.internalGetInstance("concentr", "millimole-per-liter");
    public static final MeasureUnit MOLE = MeasureUnit.internalGetInstance("concentr", "mole");
    public static final MeasureUnit PERCENT = MeasureUnit.internalGetInstance("concentr", "percent");
    public static final MeasureUnit PERMILLE = MeasureUnit.internalGetInstance("concentr", "permille");
    public static final MeasureUnit PART_PER_MILLION = MeasureUnit.internalGetInstance("concentr", "permillion");
    public static final MeasureUnit PERMYRIAD = MeasureUnit.internalGetInstance("concentr", "permyriad");
    public static final MeasureUnit PORTION_PER_1E9 = MeasureUnit.internalGetInstance("concentr", "portion-per-1e9");
    public static final MeasureUnit LITER_PER_100KILOMETERS = MeasureUnit.internalGetInstance("consumption", "liter-per-100-kilometer");
    public static final MeasureUnit LITER_PER_KILOMETER = MeasureUnit.internalGetInstance("consumption", "liter-per-kilometer");
    public static final MeasureUnit MILE_PER_GALLON = MeasureUnit.internalGetInstance("consumption", "mile-per-gallon");
    public static final MeasureUnit MILE_PER_GALLON_IMPERIAL = MeasureUnit.internalGetInstance("consumption", "mile-per-gallon-imperial");
    public static final MeasureUnit BIT = MeasureUnit.internalGetInstance("digital", "bit");
    public static final MeasureUnit BYTE = MeasureUnit.internalGetInstance("digital", "byte");
    public static final MeasureUnit GIGABIT = MeasureUnit.internalGetInstance("digital", "gigabit");
    public static final MeasureUnit GIGABYTE = MeasureUnit.internalGetInstance("digital", "gigabyte");
    public static final MeasureUnit KILOBIT = MeasureUnit.internalGetInstance("digital", "kilobit");
    public static final MeasureUnit KILOBYTE = MeasureUnit.internalGetInstance("digital", "kilobyte");
    public static final MeasureUnit MEGABIT = MeasureUnit.internalGetInstance("digital", "megabit");
    public static final MeasureUnit MEGABYTE = MeasureUnit.internalGetInstance("digital", "megabyte");
    public static final MeasureUnit PETABYTE = MeasureUnit.internalGetInstance("digital", "petabyte");
    public static final MeasureUnit TERABIT = MeasureUnit.internalGetInstance("digital", "terabit");
    public static final MeasureUnit TERABYTE = MeasureUnit.internalGetInstance("digital", "terabyte");
    public static final MeasureUnit CENTURY = MeasureUnit.internalGetInstance("duration", "century");
    public static final TimeUnit DAY = (TimeUnit)MeasureUnit.internalGetInstance("duration", "day");
    public static final MeasureUnit DAY_PERSON = MeasureUnit.internalGetInstance("duration", "day-person");
    public static final MeasureUnit DECADE = MeasureUnit.internalGetInstance("duration", "decade");
    public static final TimeUnit HOUR = (TimeUnit)MeasureUnit.internalGetInstance("duration", "hour");
    public static final MeasureUnit MICROSECOND = MeasureUnit.internalGetInstance("duration", "microsecond");
    public static final MeasureUnit MILLISECOND = MeasureUnit.internalGetInstance("duration", "millisecond");
    public static final TimeUnit MINUTE = (TimeUnit)MeasureUnit.internalGetInstance("duration", "minute");
    public static final TimeUnit MONTH = (TimeUnit)MeasureUnit.internalGetInstance("duration", "month");
    public static final MeasureUnit MONTH_PERSON = MeasureUnit.internalGetInstance("duration", "month-person");
    public static final MeasureUnit NANOSECOND = MeasureUnit.internalGetInstance("duration", "nanosecond");
    public static final MeasureUnit NIGHT = MeasureUnit.internalGetInstance("duration", "night");
    public static final MeasureUnit QUARTER = MeasureUnit.internalGetInstance("duration", "quarter");
    public static final TimeUnit SECOND = (TimeUnit)MeasureUnit.internalGetInstance("duration", "second");
    public static final TimeUnit WEEK = (TimeUnit)MeasureUnit.internalGetInstance("duration", "week");
    public static final MeasureUnit WEEK_PERSON = MeasureUnit.internalGetInstance("duration", "week-person");
    public static final TimeUnit YEAR = (TimeUnit)MeasureUnit.internalGetInstance("duration", "year");
    public static final MeasureUnit YEAR_PERSON = MeasureUnit.internalGetInstance("duration", "year-person");
    public static final MeasureUnit AMPERE = MeasureUnit.internalGetInstance("electric", "ampere");
    public static final MeasureUnit MILLIAMPERE = MeasureUnit.internalGetInstance("electric", "milliampere");
    public static final MeasureUnit OHM = MeasureUnit.internalGetInstance("electric", "ohm");
    public static final MeasureUnit VOLT = MeasureUnit.internalGetInstance("electric", "volt");
    public static final MeasureUnit BRITISH_THERMAL_UNIT = MeasureUnit.internalGetInstance("energy", "british-thermal-unit");
    public static final MeasureUnit CALORIE = MeasureUnit.internalGetInstance("energy", "calorie");
    public static final MeasureUnit ELECTRONVOLT = MeasureUnit.internalGetInstance("energy", "electronvolt");
    public static final MeasureUnit FOODCALORIE = MeasureUnit.internalGetInstance("energy", "foodcalorie");
    public static final MeasureUnit JOULE = MeasureUnit.internalGetInstance("energy", "joule");
    public static final MeasureUnit KILOCALORIE = MeasureUnit.internalGetInstance("energy", "kilocalorie");
    public static final MeasureUnit KILOJOULE = MeasureUnit.internalGetInstance("energy", "kilojoule");
    public static final MeasureUnit KILOWATT_HOUR = MeasureUnit.internalGetInstance("energy", "kilowatt-hour");
    public static final MeasureUnit THERM_US = MeasureUnit.internalGetInstance("energy", "therm-us");
    public static final MeasureUnit KILOWATT_HOUR_PER_100_KILOMETER = MeasureUnit.internalGetInstance("force", "kilowatt-hour-per-100-kilometer");
    public static final MeasureUnit NEWTON = MeasureUnit.internalGetInstance("force", "newton");
    public static final MeasureUnit POUND_FORCE = MeasureUnit.internalGetInstance("force", "pound-force");
    public static final MeasureUnit GIGAHERTZ = MeasureUnit.internalGetInstance("frequency", "gigahertz");
    public static final MeasureUnit HERTZ = MeasureUnit.internalGetInstance("frequency", "hertz");
    public static final MeasureUnit KILOHERTZ = MeasureUnit.internalGetInstance("frequency", "kilohertz");
    public static final MeasureUnit MEGAHERTZ = MeasureUnit.internalGetInstance("frequency", "megahertz");
    public static final MeasureUnit DOT = MeasureUnit.internalGetInstance("graphics", "dot");
    public static final MeasureUnit DOT_PER_CENTIMETER = MeasureUnit.internalGetInstance("graphics", "dot-per-centimeter");
    public static final MeasureUnit DOT_PER_INCH = MeasureUnit.internalGetInstance("graphics", "dot-per-inch");
    public static final MeasureUnit EM = MeasureUnit.internalGetInstance("graphics", "em");
    public static final MeasureUnit MEGAPIXEL = MeasureUnit.internalGetInstance("graphics", "megapixel");
    public static final MeasureUnit PIXEL = MeasureUnit.internalGetInstance("graphics", "pixel");
    public static final MeasureUnit PIXEL_PER_CENTIMETER = MeasureUnit.internalGetInstance("graphics", "pixel-per-centimeter");
    public static final MeasureUnit PIXEL_PER_INCH = MeasureUnit.internalGetInstance("graphics", "pixel-per-inch");
    public static final MeasureUnit ASTRONOMICAL_UNIT = MeasureUnit.internalGetInstance("length", "astronomical-unit");
    public static final MeasureUnit CENTIMETER = MeasureUnit.internalGetInstance("length", "centimeter");
    public static final MeasureUnit DECIMETER = MeasureUnit.internalGetInstance("length", "decimeter");
    public static final MeasureUnit EARTH_RADIUS = MeasureUnit.internalGetInstance("length", "earth-radius");
    public static final MeasureUnit FATHOM = MeasureUnit.internalGetInstance("length", "fathom");
    public static final MeasureUnit FOOT = MeasureUnit.internalGetInstance("length", "foot");
    public static final MeasureUnit FURLONG = MeasureUnit.internalGetInstance("length", "furlong");
    public static final MeasureUnit INCH = MeasureUnit.internalGetInstance("length", "inch");
    public static final MeasureUnit KILOMETER = MeasureUnit.internalGetInstance("length", "kilometer");
    public static final MeasureUnit LIGHT_YEAR = MeasureUnit.internalGetInstance("length", "light-year");
    public static final MeasureUnit METER = MeasureUnit.internalGetInstance("length", "meter");
    public static final MeasureUnit MICROMETER = MeasureUnit.internalGetInstance("length", "micrometer");
    public static final MeasureUnit MILE = MeasureUnit.internalGetInstance("length", "mile");
    public static final MeasureUnit MILE_SCANDINAVIAN = MeasureUnit.internalGetInstance("length", "mile-scandinavian");
    public static final MeasureUnit MILLIMETER = MeasureUnit.internalGetInstance("length", "millimeter");
    public static final MeasureUnit NANOMETER = MeasureUnit.internalGetInstance("length", "nanometer");
    public static final MeasureUnit NAUTICAL_MILE = MeasureUnit.internalGetInstance("length", "nautical-mile");
    public static final MeasureUnit PARSEC = MeasureUnit.internalGetInstance("length", "parsec");
    public static final MeasureUnit PICOMETER = MeasureUnit.internalGetInstance("length", "picometer");
    public static final MeasureUnit POINT = MeasureUnit.internalGetInstance("length", "point");
    public static final MeasureUnit SOLAR_RADIUS = MeasureUnit.internalGetInstance("length", "solar-radius");
    public static final MeasureUnit YARD = MeasureUnit.internalGetInstance("length", "yard");
    public static final MeasureUnit CANDELA = MeasureUnit.internalGetInstance("light", "candela");
    public static final MeasureUnit LUMEN = MeasureUnit.internalGetInstance("light", "lumen");
    public static final MeasureUnit LUX = MeasureUnit.internalGetInstance("light", "lux");
    public static final MeasureUnit SOLAR_LUMINOSITY = MeasureUnit.internalGetInstance("light", "solar-luminosity");
    public static final MeasureUnit CARAT = MeasureUnit.internalGetInstance("mass", "carat");
    public static final MeasureUnit DALTON = MeasureUnit.internalGetInstance("mass", "dalton");
    public static final MeasureUnit EARTH_MASS = MeasureUnit.internalGetInstance("mass", "earth-mass");
    public static final MeasureUnit GRAIN = MeasureUnit.internalGetInstance("mass", "grain");
    public static final MeasureUnit GRAM = MeasureUnit.internalGetInstance("mass", "gram");
    public static final MeasureUnit KILOGRAM = MeasureUnit.internalGetInstance("mass", "kilogram");
    public static final MeasureUnit METRIC_TON = MeasureUnit.internalGetInstance("mass", "tonne");
    public static final MeasureUnit MICROGRAM = MeasureUnit.internalGetInstance("mass", "microgram");
    public static final MeasureUnit MILLIGRAM = MeasureUnit.internalGetInstance("mass", "milligram");
    public static final MeasureUnit OUNCE = MeasureUnit.internalGetInstance("mass", "ounce");
    public static final MeasureUnit OUNCE_TROY = MeasureUnit.internalGetInstance("mass", "ounce-troy");
    public static final MeasureUnit POUND = MeasureUnit.internalGetInstance("mass", "pound");
    public static final MeasureUnit SOLAR_MASS = MeasureUnit.internalGetInstance("mass", "solar-mass");
    public static final MeasureUnit STONE = MeasureUnit.internalGetInstance("mass", "stone");
    public static final MeasureUnit TON = MeasureUnit.internalGetInstance("mass", "ton");
    public static final MeasureUnit TONNE = MeasureUnit.internalGetInstance("mass", "tonne");
    public static final MeasureUnit GIGAWATT = MeasureUnit.internalGetInstance("power", "gigawatt");
    public static final MeasureUnit HORSEPOWER = MeasureUnit.internalGetInstance("power", "horsepower");
    public static final MeasureUnit KILOWATT = MeasureUnit.internalGetInstance("power", "kilowatt");
    public static final MeasureUnit MEGAWATT = MeasureUnit.internalGetInstance("power", "megawatt");
    public static final MeasureUnit MILLIWATT = MeasureUnit.internalGetInstance("power", "milliwatt");
    public static final MeasureUnit WATT = MeasureUnit.internalGetInstance("power", "watt");
    public static final MeasureUnit ATMOSPHERE = MeasureUnit.internalGetInstance("pressure", "atmosphere");
    public static final MeasureUnit BAR = MeasureUnit.internalGetInstance("pressure", "bar");
    public static final MeasureUnit GASOLINE_ENERGY_DENSITY = MeasureUnit.internalGetInstance("pressure", "gasoline-energy-density");
    public static final MeasureUnit HECTOPASCAL = MeasureUnit.internalGetInstance("pressure", "hectopascal");
    public static final MeasureUnit INCH_HG = MeasureUnit.internalGetInstance("pressure", "inch-ofhg");
    public static final MeasureUnit KILOPASCAL = MeasureUnit.internalGetInstance("pressure", "kilopascal");
    public static final MeasureUnit MEGAPASCAL = MeasureUnit.internalGetInstance("pressure", "megapascal");
    public static final MeasureUnit MILLIBAR = MeasureUnit.internalGetInstance("pressure", "millibar");
    public static final MeasureUnit MILLIMETER_OF_MERCURY = MeasureUnit.internalGetInstance("pressure", "millimeter-ofhg");
    public static final MeasureUnit PASCAL = MeasureUnit.internalGetInstance("pressure", "pascal");
    public static final MeasureUnit POUND_PER_SQUARE_INCH = MeasureUnit.internalGetInstance("pressure", "pound-force-per-square-inch");
    public static final MeasureUnit BEAUFORT = MeasureUnit.internalGetInstance("speed", "beaufort");
    public static final MeasureUnit KILOMETER_PER_HOUR = MeasureUnit.internalGetInstance("speed", "kilometer-per-hour");
    public static final MeasureUnit KNOT = MeasureUnit.internalGetInstance("speed", "knot");
    public static final MeasureUnit LIGHT_SPEED = MeasureUnit.internalGetInstance("speed", "light-speed");
    public static final MeasureUnit METER_PER_SECOND = MeasureUnit.internalGetInstance("speed", "meter-per-second");
    public static final MeasureUnit MILE_PER_HOUR = MeasureUnit.internalGetInstance("speed", "mile-per-hour");
    public static final MeasureUnit CELSIUS = MeasureUnit.internalGetInstance("temperature", "celsius");
    public static final MeasureUnit FAHRENHEIT = MeasureUnit.internalGetInstance("temperature", "fahrenheit");
    public static final MeasureUnit GENERIC_TEMPERATURE = MeasureUnit.internalGetInstance("temperature", "generic");
    public static final MeasureUnit KELVIN = MeasureUnit.internalGetInstance("temperature", "kelvin");
    public static final MeasureUnit NEWTON_METER = MeasureUnit.internalGetInstance("torque", "newton-meter");
    public static final MeasureUnit POUND_FOOT = MeasureUnit.internalGetInstance("torque", "pound-force-foot");
    public static final MeasureUnit ACRE_FOOT = MeasureUnit.internalGetInstance("volume", "acre-foot");
    public static final MeasureUnit BARREL = MeasureUnit.internalGetInstance("volume", "barrel");
    public static final MeasureUnit BUSHEL = MeasureUnit.internalGetInstance("volume", "bushel");
    public static final MeasureUnit CENTILITER = MeasureUnit.internalGetInstance("volume", "centiliter");
    public static final MeasureUnit CUBIC_CENTIMETER = MeasureUnit.internalGetInstance("volume", "cubic-centimeter");
    public static final MeasureUnit CUBIC_FOOT = MeasureUnit.internalGetInstance("volume", "cubic-foot");
    public static final MeasureUnit CUBIC_INCH = MeasureUnit.internalGetInstance("volume", "cubic-inch");
    public static final MeasureUnit CUBIC_KILOMETER = MeasureUnit.internalGetInstance("volume", "cubic-kilometer");
    public static final MeasureUnit CUBIC_METER = MeasureUnit.internalGetInstance("volume", "cubic-meter");
    public static final MeasureUnit CUBIC_MILE = MeasureUnit.internalGetInstance("volume", "cubic-mile");
    public static final MeasureUnit CUBIC_YARD = MeasureUnit.internalGetInstance("volume", "cubic-yard");
    public static final MeasureUnit CUP = MeasureUnit.internalGetInstance("volume", "cup");
    public static final MeasureUnit CUP_METRIC = MeasureUnit.internalGetInstance("volume", "cup-metric");
    public static final MeasureUnit DECILITER = MeasureUnit.internalGetInstance("volume", "deciliter");
    public static final MeasureUnit DESSERT_SPOON = MeasureUnit.internalGetInstance("volume", "dessert-spoon");
    public static final MeasureUnit DESSERT_SPOON_IMPERIAL = MeasureUnit.internalGetInstance("volume", "dessert-spoon-imperial");
    public static final MeasureUnit DRAM = MeasureUnit.internalGetInstance("volume", "dram");
    public static final MeasureUnit DROP = MeasureUnit.internalGetInstance("volume", "drop");
    public static final MeasureUnit FLUID_OUNCE = MeasureUnit.internalGetInstance("volume", "fluid-ounce");
    public static final MeasureUnit FLUID_OUNCE_IMPERIAL = MeasureUnit.internalGetInstance("volume", "fluid-ounce-imperial");
    public static final MeasureUnit GALLON = MeasureUnit.internalGetInstance("volume", "gallon");
    public static final MeasureUnit GALLON_IMPERIAL = MeasureUnit.internalGetInstance("volume", "gallon-imperial");
    public static final MeasureUnit HECTOLITER = MeasureUnit.internalGetInstance("volume", "hectoliter");
    public static final MeasureUnit JIGGER = MeasureUnit.internalGetInstance("volume", "jigger");
    public static final MeasureUnit LITER = MeasureUnit.internalGetInstance("volume", "liter");
    public static final MeasureUnit MEGALITER = MeasureUnit.internalGetInstance("volume", "megaliter");
    public static final MeasureUnit MILLILITER = MeasureUnit.internalGetInstance("volume", "milliliter");
    public static final MeasureUnit PINCH = MeasureUnit.internalGetInstance("volume", "pinch");
    public static final MeasureUnit PINT = MeasureUnit.internalGetInstance("volume", "pint");
    public static final MeasureUnit PINT_METRIC = MeasureUnit.internalGetInstance("volume", "pint-metric");
    public static final MeasureUnit QUART = MeasureUnit.internalGetInstance("volume", "quart");
    public static final MeasureUnit QUART_IMPERIAL = MeasureUnit.internalGetInstance("volume", "quart-imperial");
    public static final MeasureUnit TABLESPOON = MeasureUnit.internalGetInstance("volume", "tablespoon");
    public static final MeasureUnit TEASPOON = MeasureUnit.internalGetInstance("volume", "teaspoon");

    @Deprecated
    protected MeasureUnit(String type, String subType) {
        this.type = type;
        this.subType = subType;
    }

    public static MeasureUnit forIdentifier(String identifier) {
        if (identifier == null || identifier.isEmpty()) {
            return NoUnit.BASE;
        }
        return MeasureUnitImpl.forIdentifier(identifier).build();
    }

    @Deprecated
    public static MeasureUnit fromMeasureUnitImpl(MeasureUnitImpl measureUnitImpl) {
        measureUnitImpl.serialize();
        String identifier = measureUnitImpl.getIdentifier();
        MeasureUnit result = MeasureUnit.findBySubType(identifier);
        if (result != null) {
            return result;
        }
        return new MeasureUnit(measureUnitImpl);
    }

    private MeasureUnit(MeasureUnitImpl measureUnitImpl) {
        this.type = null;
        this.subType = null;
        this.measureUnitImpl = measureUnitImpl.copy();
    }

    public String getType() {
        return this.type;
    }

    public String getSubtype() {
        return this.subType;
    }

    public String getIdentifier() {
        String result = this.measureUnitImpl == null ? this.getSubtype() : this.measureUnitImpl.getIdentifier();
        return result == null ? "" : result;
    }

    public Complexity getComplexity() {
        if (this.measureUnitImpl == null) {
            return MeasureUnitImpl.forIdentifier(this.getIdentifier()).getComplexity();
        }
        return this.measureUnitImpl.getComplexity();
    }

    public MeasureUnit withPrefix(MeasurePrefix prefix) {
        SingleUnitImpl singleUnit = this.getSingleUnitImpl();
        singleUnit.setPrefix(prefix);
        return singleUnit.build();
    }

    public MeasurePrefix getPrefix() {
        return this.getSingleUnitImpl().getPrefix();
    }

    public MeasureUnit withConstantDenominator(long denominator) {
        if (denominator < 0L) {
            throw new IllegalArgumentException("Denominator cannot be negative");
        }
        if (this.getComplexity() != Complexity.COMPOUND && this.getComplexity() != Complexity.SINGLE) {
            throw new UnsupportedOperationException("Constant denominator can only be applied to COMPOUND & SINGLE units");
        }
        MeasureUnitImpl measureUnitImpl = this.getCopyOfMeasureUnitImpl();
        measureUnitImpl.setConstantDenominator(denominator);
        measureUnitImpl.setComplexity(denominator == 0L && measureUnitImpl.getSingleUnits().size() < 2 ? Complexity.SINGLE : Complexity.COMPOUND);
        return measureUnitImpl.build();
    }

    public long getConstantDenominator() {
        if (this.getComplexity() != Complexity.COMPOUND && this.getComplexity() != Complexity.SINGLE) {
            throw new UnsupportedOperationException("Constant denominator is only supported for COMPOUND & SINGLE units");
        }
        if (this.measureUnitImpl == null) {
            return 0L;
        }
        return this.measureUnitImpl.getConstantDenominator();
    }

    public int getDimensionality() {
        return this.getSingleUnitImpl().getDimensionality();
    }

    public MeasureUnit withDimensionality(int dimensionality) {
        SingleUnitImpl singleUnit = this.getSingleUnitImpl();
        singleUnit.setDimensionality(dimensionality);
        return singleUnit.build();
    }

    public MeasureUnit reciprocal() {
        if (this.getComplexity() == Complexity.COMPOUND && this.getConstantDenominator() != 0L) {
            throw new UnsupportedOperationException("Cannot take reciprocal of a unit with a constant denominator");
        }
        MeasureUnitImpl measureUnit = this.getCopyOfMeasureUnitImpl();
        measureUnit.takeReciprocal();
        return measureUnit.build();
    }

    public MeasureUnit product(MeasureUnit other) {
        MeasureUnitImpl implCopy = this.getCopyOfMeasureUnitImpl();
        if (other == null) {
            return implCopy.build();
        }
        MeasureUnitImpl otherImplRef = other.getMaybeReferenceOfMeasureUnitImpl();
        if (implCopy.getComplexity() == Complexity.MIXED || otherImplRef.getComplexity() == Complexity.MIXED) {
            throw new UnsupportedOperationException();
        }
        for (SingleUnitImpl singleUnit : otherImplRef.getSingleUnits()) {
            implCopy.appendSingleUnit(singleUnit);
        }
        long thisConstantDenominator = this.getConstantDenominator();
        long otherConstantDenominator = other.getConstantDenominator();
        if (thisConstantDenominator != 0L && otherConstantDenominator != 0L) {
            throw new UnsupportedOperationException("Cannot multiply units that both of them have a constant denominator");
        }
        implCopy.setConstantDenominator(Math.max(thisConstantDenominator, otherConstantDenominator));
        return implCopy.build();
    }

    public List<MeasureUnit> splitToSingleUnits() {
        ArrayList<SingleUnitImpl> singleUnits = this.getMaybeReferenceOfMeasureUnitImpl().getSingleUnits();
        ArrayList<MeasureUnit> result = new ArrayList<MeasureUnit>(singleUnits.size());
        for (SingleUnitImpl singleUnit : singleUnits) {
            result.add(singleUnit.build());
        }
        return result;
    }

    public int hashCode() {
        return 31 * this.type.hashCode() + this.subType.hashCode();
    }

    public boolean equals(Object rhs) {
        if (rhs == this) {
            return true;
        }
        if (!(rhs instanceof MeasureUnit)) {
            return false;
        }
        return this.getIdentifier().equals(((MeasureUnit)rhs).getIdentifier());
    }

    public String toString() {
        String result = this.measureUnitImpl == null ? this.type + "-" + this.subType : this.measureUnitImpl.getIdentifier();
        return result == null ? "" : result;
    }

    public static Set<String> getAvailableTypes() {
        MeasureUnit.populateCache();
        return Collections.unmodifiableSet(cache.keySet());
    }

    public static Set<MeasureUnit> getAvailable(String type) {
        MeasureUnit.populateCache();
        Map<String, MeasureUnit> units = cache.get(type);
        return units == null ? Collections.emptySet() : Collections.unmodifiableSet(new CollectionSet<MeasureUnit>(units.values()));
    }

    public static synchronized Set<MeasureUnit> getAvailable() {
        HashSet<MeasureUnit> result = new HashSet<MeasureUnit>();
        for (String type : new HashSet<String>(MeasureUnit.getAvailableTypes())) {
            for (MeasureUnit unit : MeasureUnit.getAvailable(type)) {
                result.add(unit);
            }
        }
        return Collections.unmodifiableSet(result);
    }

    @Deprecated
    public static MeasureUnit internalGetInstance(String type, String subType) {
        if (type == null || subType == null) {
            throw new NullPointerException("Type and subType must be non-null");
        }
        if (!("currency".equals(type) || ASCII.containsAll(type) && ASCII_HYPHEN_DIGITS.containsAll(subType))) {
            throw new IllegalArgumentException("The type or subType are invalid.");
        }
        Factory factory = "currency".equals(type) ? CURRENCY_FACTORY : ("duration".equals(type) ? TIMEUNIT_FACTORY : UNIT_FACTORY);
        return MeasureUnit.addUnit(type, subType, factory);
    }

    @Deprecated
    public static MeasureUnit findBySubType(String subType) {
        MeasureUnit.populateCache();
        for (Map<String, MeasureUnit> unitsForType : cache.values()) {
            if (!unitsForType.containsKey(subType)) continue;
            return unitsForType.get(subType);
        }
        return null;
    }

    private static synchronized void populateCache() {
        if (cacheIsPopulated) {
            return;
        }
        cacheIsPopulated = true;
        ICUResourceBundle rb1 = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata/unit", "en");
        rb1.getAllItemsWithFallback("units", new MeasureUnitSink());
        ICUResourceBundle rb2 = (ICUResourceBundle)UResourceBundle.getBundleInstance("com/ibm/icu/impl/data/icudata", "currencyNumericCodes", ICUResourceBundle.ICU_DATA_CLASS_LOADER);
        rb2.getAllItemsWithFallback("codeMap", new CurrencyNumericCodeSink());
    }

    @Deprecated
    protected static synchronized MeasureUnit addUnit(String type, String unitName, Factory factory) {
        Map<String, MeasureUnit> tmp = cache.get(type);
        if (tmp == null) {
            tmp = new HashMap<String, MeasureUnit>();
            cache.put(type, tmp);
        } else {
            type = tmp.entrySet().iterator().next().getValue().type;
        }
        MeasureUnit unit = tmp.get(unitName);
        if (unit == null) {
            unit = factory.create(type, unitName);
            tmp.put(unitName, unit);
        }
        return unit;
    }

    private Object writeReplace() throws ObjectStreamException {
        return new MeasureUnitProxy(this.type, this.subType);
    }

    private SingleUnitImpl getSingleUnitImpl() {
        if (this.measureUnitImpl == null) {
            return MeasureUnitImpl.forIdentifier(this.getIdentifier()).getSingleUnitImpl();
        }
        return this.measureUnitImpl.getSingleUnitImpl();
    }

    @Deprecated
    public MeasureUnitImpl getCopyOfMeasureUnitImpl() {
        return this.measureUnitImpl == null ? MeasureUnitImpl.forIdentifier(this.getIdentifier()) : this.measureUnitImpl.copy();
    }

    private MeasureUnitImpl getMaybeReferenceOfMeasureUnitImpl() {
        return this.measureUnitImpl == null ? MeasureUnitImpl.forIdentifier(this.getIdentifier()) : this.measureUnitImpl;
    }

    static final class MeasureUnitProxy
    implements Externalizable {
        private static final long serialVersionUID = -3910681415330989598L;
        private String type;
        private String subType;

        public MeasureUnitProxy(String type, String subType) {
            this.type = type;
            this.subType = subType;
        }

        public MeasureUnitProxy() {
        }

        @Override
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeByte(0);
            out.writeUTF(this.type);
            out.writeUTF(this.subType);
            out.writeShort(0);
        }

        @Override
        public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
            in.readByte();
            this.type = in.readUTF();
            this.subType = in.readUTF();
            short extra = in.readShort();
            if (extra > 0) {
                byte[] extraBytes = new byte[extra];
                in.read(extraBytes, 0, extra);
            }
        }

        private Object readResolve() throws ObjectStreamException {
            return MeasureUnit.internalGetInstance(this.type, this.subType);
        }
    }

    private static final class CurrencyNumericCodeSink
    extends UResource.Sink {
        private CurrencyNumericCodeSink() {
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table codesTable = value.getTable();
            int i1 = 0;
            while (codesTable.getKeyAndValue(i1, key, value)) {
                MeasureUnit.internalGetInstance("currency", key.toString());
                ++i1;
            }
        }
    }

    private static final class MeasureUnitSink
    extends UResource.Sink {
        private MeasureUnitSink() {
        }

        @Override
        public void put(UResource.Key key, UResource.Value value, boolean noFallback) {
            UResource.Table unitTypesTable = value.getTable();
            int i2 = 0;
            while (unitTypesTable.getKeyAndValue(i2, key, value)) {
                if (!key.contentEquals("compound") && !key.contentEquals("coordinate")) {
                    String unitType = key.toString();
                    UResource.Table unitNamesTable = value.getTable();
                    int i3 = 0;
                    while (unitNamesTable.getKeyAndValue(i3, key, value)) {
                        String unitName = key.toString();
                        MeasureUnit.internalGetInstance(unitType, unitName);
                        ++i3;
                    }
                }
                ++i2;
            }
        }
    }

    @Deprecated
    protected static interface Factory {
        @Deprecated
        public MeasureUnit create(String var1, String var2);
    }

    public static enum MeasurePrefix {
        QUETTA(30, "quetta", 10),
        RONNA(27, "ronna", 10),
        YOTTA(24, "yotta", 10),
        ZETTA(21, "zetta", 10),
        EXA(18, "exa", 10),
        PETA(15, "peta", 10),
        TERA(12, "tera", 10),
        GIGA(9, "giga", 10),
        MEGA(6, "mega", 10),
        KILO(3, "kilo", 10),
        HECTO(2, "hecto", 10),
        DEKA(1, "deka", 10),
        ONE(0, "", 10),
        DECI(-1, "deci", 10),
        CENTI(-2, "centi", 10),
        MILLI(-3, "milli", 10),
        MICRO(-6, "micro", 10),
        NANO(-9, "nano", 10),
        PICO(-12, "pico", 10),
        FEMTO(-15, "femto", 10),
        ATTO(-18, "atto", 10),
        ZEPTO(-21, "zepto", 10),
        YOCTO(-24, "yocto", 10),
        RONTO(-27, "ronto", 10),
        QUECTO(-30, "quecto", 10),
        KIBI(1, "kibi", 1024),
        MEBI(2, "mebi", 1024),
        GIBI(3, "gibi", 1024),
        TEBI(4, "tebi", 1024),
        PEBI(5, "pebi", 1024),
        EXBI(6, "exbi", 1024),
        ZEBI(7, "zebi", 1024),
        YOBI(8, "yobi", 1024);

        private final int base;
        private final int power;
        private final String identifier;

        private MeasurePrefix(int power, String identifier, int base) {
            this.base = base;
            this.power = power;
            this.identifier = identifier;
        }

        @Deprecated
        public String getIdentifier() {
            return this.identifier;
        }

        public int getBase() {
            return this.base;
        }

        public int getPower() {
            return this.power;
        }
    }

    public static enum Complexity {
        SINGLE,
        COMPOUND,
        MIXED;

    }
}

