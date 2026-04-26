/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.impl.number.DecimalQuantity;
import com.ibm.icu.impl.number.MultiplierProducer;
import com.ibm.icu.impl.number.RoundingUtils;
import com.ibm.icu.number.CurrencyPrecision;
import com.ibm.icu.number.FractionPrecision;
import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.text.PluralRules;
import com.ibm.icu.util.Currency;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;

public abstract class Precision {
    MathContext mathContext = RoundingUtils.DEFAULT_MATH_CONTEXT_UNLIMITED;
    NumberFormatter.TrailingZeroDisplay trailingZeroDisplay;
    @Deprecated
    public static final BogusRounder BOGUS_PRECISION = new BogusRounder();
    static final InfiniteRounderImpl NONE = new InfiniteRounderImpl();
    static final FractionRounderImpl FIXED_FRAC_0 = new FractionRounderImpl(0, 0);
    static final FractionRounderImpl FIXED_FRAC_2 = new FractionRounderImpl(2, 2);
    static final FractionRounderImpl DEFAULT_MAX_FRAC_6 = new FractionRounderImpl(0, 6);
    static final SignificantRounderImpl FIXED_SIG_2 = new SignificantRounderImpl(2, 2);
    static final SignificantRounderImpl FIXED_SIG_3 = new SignificantRounderImpl(3, 3);
    static final SignificantRounderImpl RANGE_SIG_2_3 = new SignificantRounderImpl(2, 3);
    static final FracSigRounderImpl COMPACT_STRATEGY = new FracSigRounderImpl(0, 0, 1, 2, NumberFormatter.RoundingPriority.RELAXED, false);
    static final IncrementFiveRounderImpl NICKEL = new IncrementFiveRounderImpl(new BigDecimal("0.05"), 2, 2);
    static final CurrencyRounderImpl MONETARY_STANDARD = new CurrencyRounderImpl(Currency.CurrencyUsage.STANDARD);
    static final CurrencyRounderImpl MONETARY_CASH = new CurrencyRounderImpl(Currency.CurrencyUsage.CASH);

    Precision() {
    }

    public static Precision unlimited() {
        return Precision.constructInfinite();
    }

    public static FractionPrecision integer() {
        return Precision.constructFraction(0, 0);
    }

    public static FractionPrecision fixedFraction(int minMaxFractionPlaces) {
        if (minMaxFractionPlaces >= 0 && minMaxFractionPlaces <= 999) {
            return Precision.constructFraction(minMaxFractionPlaces, minMaxFractionPlaces);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision minFraction(int minFractionPlaces) {
        if (minFractionPlaces >= 0 && minFractionPlaces <= 999) {
            return Precision.constructFraction(minFractionPlaces, -1);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision maxFraction(int maxFractionPlaces) {
        if (maxFractionPlaces >= 0 && maxFractionPlaces <= 999) {
            return Precision.constructFraction(0, maxFractionPlaces);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static FractionPrecision minMaxFraction(int minFractionPlaces, int maxFractionPlaces) {
        if (minFractionPlaces >= 0 && maxFractionPlaces <= 999 && minFractionPlaces <= maxFractionPlaces) {
            return Precision.constructFraction(minFractionPlaces, maxFractionPlaces);
        }
        throw new IllegalArgumentException("Fraction length must be between 0 and 999 (inclusive)");
    }

    public static Precision fixedSignificantDigits(int minMaxSignificantDigits) {
        if (minMaxSignificantDigits >= 1 && minMaxSignificantDigits <= 999) {
            return Precision.constructSignificant(minMaxSignificantDigits, minMaxSignificantDigits);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision minSignificantDigits(int minSignificantDigits) {
        if (minSignificantDigits >= 1 && minSignificantDigits <= 999) {
            return Precision.constructSignificant(minSignificantDigits, -1);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision maxSignificantDigits(int maxSignificantDigits) {
        if (maxSignificantDigits >= 1 && maxSignificantDigits <= 999) {
            return Precision.constructSignificant(1, maxSignificantDigits);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision minMaxSignificantDigits(int minSignificantDigits, int maxSignificantDigits) {
        if (minSignificantDigits >= 1 && maxSignificantDigits <= 999 && minSignificantDigits <= maxSignificantDigits) {
            return Precision.constructSignificant(minSignificantDigits, maxSignificantDigits);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public static Precision increment(BigDecimal roundingIncrement) {
        if (roundingIncrement != null && roundingIncrement.compareTo(BigDecimal.ZERO) > 0) {
            return Precision.constructIncrement(roundingIncrement);
        }
        throw new IllegalArgumentException("Rounding increment must be positive and non-null");
    }

    public static CurrencyPrecision currency(Currency.CurrencyUsage currencyUsage) {
        if (currencyUsage != null) {
            return Precision.constructCurrency(currencyUsage);
        }
        throw new IllegalArgumentException("CurrencyUsage must be non-null");
    }

    public Precision trailingZeroDisplay(NumberFormatter.TrailingZeroDisplay trailingZeroDisplay) {
        Precision result = this.createCopy();
        result.trailingZeroDisplay = trailingZeroDisplay;
        return result;
    }

    @Deprecated
    public Precision withMode(MathContext mathContext) {
        if (this.mathContext.equals(mathContext)) {
            return this;
        }
        Precision other = this.createCopy();
        other.mathContext = mathContext;
        return other;
    }

    abstract Precision createCopy();

    void createCopyHelper(Precision copy) {
        copy.mathContext = this.mathContext;
        copy.trailingZeroDisplay = this.trailingZeroDisplay;
    }

    @Deprecated
    public abstract void apply(DecimalQuantity var1);

    static Precision constructInfinite() {
        return NONE;
    }

    static FractionPrecision constructFraction(int minFrac, int maxFrac) {
        if (minFrac == 0 && maxFrac == 0) {
            return FIXED_FRAC_0;
        }
        if (minFrac == 2 && maxFrac == 2) {
            return FIXED_FRAC_2;
        }
        if (minFrac == 0 && maxFrac == 6) {
            return DEFAULT_MAX_FRAC_6;
        }
        return new FractionRounderImpl(minFrac, maxFrac);
    }

    static Precision constructSignificant(int minSig, int maxSig) {
        if (minSig == 2 && maxSig == 2) {
            return FIXED_SIG_2;
        }
        if (minSig == 3 && maxSig == 3) {
            return FIXED_SIG_3;
        }
        if (minSig == 2 && maxSig == 3) {
            return RANGE_SIG_2_3;
        }
        return new SignificantRounderImpl(minSig, maxSig);
    }

    static Precision constructFractionSignificant(FractionPrecision base_, int minSig, int maxSig, NumberFormatter.RoundingPriority priority, boolean retain) {
        assert (base_ instanceof FractionRounderImpl);
        FractionRounderImpl base = (FractionRounderImpl)base_;
        FracSigRounderImpl returnValue = base.minFrac == 0 && base.maxFrac == 0 && minSig == 1 && maxSig == 2 && priority == NumberFormatter.RoundingPriority.RELAXED && !retain ? COMPACT_STRATEGY : new FracSigRounderImpl(base.minFrac, base.maxFrac, minSig, maxSig, priority, retain);
        return returnValue.withMode(base.mathContext);
    }

    static Precision constructIncrement(BigDecimal increment) {
        if (increment.equals(Precision.NICKEL.increment)) {
            return NICKEL;
        }
        BigDecimal reduced = increment.stripTrailingZeros();
        if (reduced.precision() == 1) {
            int minFrac = increment.scale();
            int maxFrac = reduced.scale();
            BigInteger digit = reduced.unscaledValue();
            if (digit.intValue() == 1) {
                return new IncrementOneRounderImpl(increment, minFrac, maxFrac);
            }
            if (digit.intValue() == 5) {
                return new IncrementFiveRounderImpl(increment, minFrac, maxFrac);
            }
        }
        return new IncrementRounderImpl(increment);
    }

    static CurrencyPrecision constructCurrency(Currency.CurrencyUsage usage) {
        if (usage == Currency.CurrencyUsage.STANDARD) {
            return MONETARY_STANDARD;
        }
        if (usage == Currency.CurrencyUsage.CASH) {
            return MONETARY_CASH;
        }
        throw new AssertionError();
    }

    static Precision constructFromCurrency(CurrencyPrecision base_, Currency currency) {
        Precision returnValue;
        assert (base_ instanceof CurrencyRounderImpl);
        CurrencyRounderImpl base = (CurrencyRounderImpl)base_;
        double incrementDouble = currency.getRoundingIncrement(base.usage);
        if (incrementDouble != 0.0) {
            BigDecimal increment = BigDecimal.valueOf(incrementDouble);
            returnValue = Precision.constructIncrement(increment);
        } else {
            int minMaxFrac = currency.getDefaultFractionDigits(base.usage);
            returnValue = Precision.constructFraction(minMaxFrac, minMaxFrac);
        }
        return returnValue.withMode(base.mathContext);
    }

    Precision withLocaleData(Currency currency) {
        if (this instanceof CurrencyPrecision) {
            return ((CurrencyPrecision)this).withCurrency(currency);
        }
        return this;
    }

    int chooseMultiplierAndApply(DecimalQuantity input, MultiplierProducer producer) {
        assert (!input.isZeroish());
        int magnitude = input.getMagnitude();
        int multiplier = producer.getMultiplier(magnitude);
        input.adjustMagnitude(multiplier);
        this.apply(input);
        if (input.isZeroish()) {
            return multiplier;
        }
        if (input.getMagnitude() == magnitude + multiplier) {
            return multiplier;
        }
        int _multiplier = producer.getMultiplier(magnitude + 1);
        if (multiplier == _multiplier) {
            return multiplier;
        }
        input.adjustMagnitude(_multiplier - multiplier);
        this.apply(input);
        return _multiplier;
    }

    private static int getRoundingMagnitudeFraction(int maxFrac) {
        if (maxFrac == -1) {
            return Integer.MIN_VALUE;
        }
        return -maxFrac;
    }

    private static int getRoundingMagnitudeSignificant(DecimalQuantity value, int maxSig) {
        if (maxSig == -1) {
            return Integer.MIN_VALUE;
        }
        int magnitude = value.isZeroish() ? 0 : value.getMagnitude();
        return magnitude - maxSig + 1;
    }

    private static int getDisplayMagnitudeFraction(int minFrac) {
        if (minFrac == 0) {
            return Integer.MAX_VALUE;
        }
        return -minFrac;
    }

    void setResolvedMinFraction(DecimalQuantity value, int resolvedMinFraction) {
        if (this.trailingZeroDisplay == null || this.trailingZeroDisplay == NumberFormatter.TrailingZeroDisplay.AUTO || value.getPluralOperand(PluralRules.Operand.t) != 0.0) {
            value.setMinFraction(resolvedMinFraction);
        }
    }

    private static int getDisplayMagnitudeSignificant(DecimalQuantity value, int minSig) {
        int magnitude = value.isZeroish() ? 0 : value.getMagnitude();
        return magnitude - minSig + 1;
    }

    static class CurrencyRounderImpl
    extends CurrencyPrecision {
        final Currency.CurrencyUsage usage;

        public CurrencyRounderImpl(Currency.CurrencyUsage usage) {
            this.usage = usage;
        }

        @Override
        public void apply(DecimalQuantity value) {
            throw new AssertionError();
        }

        @Override
        CurrencyRounderImpl createCopy() {
            CurrencyRounderImpl copy = new CurrencyRounderImpl(this.usage);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class IncrementFiveRounderImpl
    extends IncrementRounderImpl {
        final int minFrac;
        final int maxFrac;

        public IncrementFiveRounderImpl(BigDecimal increment, int minFrac, int maxFrac) {
            super(increment);
            this.minFrac = minFrac;
            this.maxFrac = maxFrac;
        }

        @Override
        public void apply(DecimalQuantity value) {
            value.roundToNickel(-this.maxFrac, this.mathContext);
            this.setResolvedMinFraction(value, this.minFrac);
        }

        @Override
        IncrementFiveRounderImpl createCopy() {
            IncrementFiveRounderImpl copy = new IncrementFiveRounderImpl(this.increment, this.minFrac, this.maxFrac);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class IncrementOneRounderImpl
    extends IncrementRounderImpl {
        final int minFrac;
        final int maxFrac;

        public IncrementOneRounderImpl(BigDecimal increment, int minFrac, int maxFrac) {
            super(increment);
            this.minFrac = minFrac;
            this.maxFrac = maxFrac;
        }

        @Override
        public void apply(DecimalQuantity value) {
            value.roundToMagnitude(-this.maxFrac, this.mathContext);
            this.setResolvedMinFraction(value, this.minFrac);
        }

        @Override
        IncrementOneRounderImpl createCopy() {
            IncrementOneRounderImpl copy = new IncrementOneRounderImpl(this.increment, this.minFrac, this.maxFrac);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class IncrementRounderImpl
    extends Precision {
        final BigDecimal increment;

        public IncrementRounderImpl(BigDecimal increment) {
            this.increment = increment;
        }

        @Override
        public void apply(DecimalQuantity value) {
            value.roundToIncrement(this.increment, this.mathContext);
            this.setResolvedMinFraction(value, Math.max(0, this.increment.scale()));
        }

        @Override
        IncrementRounderImpl createCopy() {
            IncrementRounderImpl copy = new IncrementRounderImpl(this.increment);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class FracSigRounderImpl
    extends Precision {
        final int minFrac;
        final int maxFrac;
        final int minSig;
        final int maxSig;
        final NumberFormatter.RoundingPriority priority;
        final boolean retain;

        public FracSigRounderImpl(int minFrac, int maxFrac, int minSig, int maxSig, NumberFormatter.RoundingPriority priority, boolean retain) {
            this.minFrac = minFrac;
            this.maxFrac = maxFrac;
            this.minSig = minSig;
            this.maxSig = maxSig;
            this.priority = priority;
            this.retain = retain;
        }

        @Override
        public void apply(DecimalQuantity value) {
            int displayMag;
            int roundingMag1 = Precision.getRoundingMagnitudeFraction(this.maxFrac);
            int roundingMag2 = Precision.getRoundingMagnitudeSignificant(value, this.maxSig);
            int roundingMag = this.priority == NumberFormatter.RoundingPriority.RELAXED ? Math.min(roundingMag1, roundingMag2) : Math.max(roundingMag1, roundingMag2);
            if (!value.isZeroish()) {
                int upperMag = value.getMagnitude();
                value.roundToMagnitude(roundingMag, this.mathContext);
                if (!value.isZeroish() && value.getMagnitude() != upperMag && roundingMag1 == roundingMag2) {
                    ++roundingMag2;
                }
            }
            int displayMag1 = Precision.getDisplayMagnitudeFraction(this.minFrac);
            int displayMag2 = Precision.getDisplayMagnitudeSignificant(value, this.minSig);
            if (this.retain) {
                displayMag = Math.min(displayMag1, displayMag2);
            } else if (this.priority == NumberFormatter.RoundingPriority.RELAXED) {
                displayMag = roundingMag2 <= roundingMag1 ? displayMag2 : displayMag1;
            } else {
                assert (this.priority == NumberFormatter.RoundingPriority.STRICT);
                displayMag = roundingMag2 <= roundingMag1 ? displayMag1 : displayMag2;
            }
            this.setResolvedMinFraction(value, Math.max(0, -displayMag));
        }

        @Override
        FracSigRounderImpl createCopy() {
            FracSigRounderImpl copy = new FracSigRounderImpl(this.minFrac, this.maxFrac, this.minSig, this.maxSig, this.priority, this.retain);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class SignificantRounderImpl
    extends Precision {
        final int minSig;
        final int maxSig;

        public SignificantRounderImpl(int minSig, int maxSig) {
            this.minSig = minSig;
            this.maxSig = maxSig;
        }

        @Override
        public void apply(DecimalQuantity value) {
            value.roundToMagnitude(Precision.getRoundingMagnitudeSignificant(value, this.maxSig), this.mathContext);
            this.setResolvedMinFraction(value, Math.max(0, -Precision.getDisplayMagnitudeSignificant(value, this.minSig)));
            if (value.isZeroish() && this.minSig > 0) {
                value.setMinInteger(1);
            }
        }

        public void apply(DecimalQuantity quantity, int minInt) {
            assert (quantity.isZeroish());
            this.setResolvedMinFraction(quantity, this.minSig - minInt);
        }

        @Override
        SignificantRounderImpl createCopy() {
            SignificantRounderImpl copy = new SignificantRounderImpl(this.minSig, this.maxSig);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class FractionRounderImpl
    extends FractionPrecision {
        final int minFrac;
        final int maxFrac;

        public FractionRounderImpl(int minFrac, int maxFrac) {
            this.minFrac = minFrac;
            this.maxFrac = maxFrac;
        }

        @Override
        public void apply(DecimalQuantity value) {
            value.roundToMagnitude(Precision.getRoundingMagnitudeFraction(this.maxFrac), this.mathContext);
            this.setResolvedMinFraction(value, Math.max(0, -Precision.getDisplayMagnitudeFraction(this.minFrac)));
        }

        @Override
        FractionRounderImpl createCopy() {
            FractionRounderImpl copy = new FractionRounderImpl(this.minFrac, this.maxFrac);
            this.createCopyHelper(copy);
            return copy;
        }
    }

    static class InfiniteRounderImpl
    extends Precision {
        @Override
        public void apply(DecimalQuantity value) {
            value.roundToInfinity();
            this.setResolvedMinFraction(value, 0);
        }

        @Override
        InfiniteRounderImpl createCopy() {
            InfiniteRounderImpl copy = new InfiniteRounderImpl();
            this.createCopyHelper(copy);
            return copy;
        }
    }

    @Deprecated
    public static class BogusRounder
    extends Precision {
        @Deprecated
        public BogusRounder() {
        }

        @Override
        @Deprecated
        public void apply(DecimalQuantity value) {
            throw new AssertionError((Object)"BogusRounder must not be applied");
        }

        @Override
        BogusRounder createCopy() {
            BogusRounder copy = new BogusRounder();
            this.createCopyHelper(copy);
            return copy;
        }

        @Deprecated
        public Precision into(Precision precision) {
            Precision copy = precision.createCopy();
            this.createCopyHelper(copy);
            return copy;
        }
    }
}

