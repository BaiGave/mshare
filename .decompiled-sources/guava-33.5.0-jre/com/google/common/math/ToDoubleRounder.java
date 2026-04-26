/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.math;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.math.DoubleUtils;
import com.google.common.math.MathPreconditions;
import java.math.RoundingMode;

@GwtIncompatible
abstract class ToDoubleRounder<X extends Number> {
    ToDoubleRounder() {
    }

    abstract double roundToDoubleArbitrarily(X var1);

    abstract int sign(X var1);

    abstract X toX(double var1, RoundingMode var3);

    abstract X minus(X var1, X var2);

    final double roundToDouble(X x, RoundingMode mode) {
        Preconditions.checkNotNull(x, "x");
        Preconditions.checkNotNull(mode, "mode");
        double roundArbitrarily = this.roundToDoubleArbitrarily(x);
        if (Double.isInfinite(roundArbitrarily)) {
            switch (mode) {
                case DOWN: 
                case HALF_EVEN: 
                case HALF_DOWN: 
                case HALF_UP: {
                    return Double.MAX_VALUE * (double)this.sign(x);
                }
                case FLOOR: {
                    return roundArbitrarily == Double.POSITIVE_INFINITY ? Double.MAX_VALUE : Double.NEGATIVE_INFINITY;
                }
                case CEILING: {
                    return roundArbitrarily == Double.POSITIVE_INFINITY ? Double.POSITIVE_INFINITY : -1.7976931348623157E308;
                }
                case UP: {
                    return roundArbitrarily;
                }
                case UNNECESSARY: {
                    throw new ArithmeticException(x + " cannot be represented precisely as a double");
                }
            }
        }
        X roundArbitrarilyAsX = this.toX(roundArbitrarily, RoundingMode.UNNECESSARY);
        int cmpXToRoundArbitrarily = ((Comparable)x).compareTo(roundArbitrarilyAsX);
        switch (mode) {
            case UNNECESSARY: {
                MathPreconditions.checkRoundingUnnecessary(cmpXToRoundArbitrarily == 0);
                return roundArbitrarily;
            }
            case FLOOR: {
                return cmpXToRoundArbitrarily >= 0 ? roundArbitrarily : DoubleUtils.nextDown(roundArbitrarily);
            }
            case CEILING: {
                return cmpXToRoundArbitrarily <= 0 ? roundArbitrarily : Math.nextUp(roundArbitrarily);
            }
            case DOWN: {
                if (this.sign(x) >= 0) {
                    return cmpXToRoundArbitrarily >= 0 ? roundArbitrarily : DoubleUtils.nextDown(roundArbitrarily);
                }
                return cmpXToRoundArbitrarily <= 0 ? roundArbitrarily : Math.nextUp(roundArbitrarily);
            }
            case UP: {
                if (this.sign(x) >= 0) {
                    return cmpXToRoundArbitrarily <= 0 ? roundArbitrarily : Math.nextUp(roundArbitrarily);
                }
                return cmpXToRoundArbitrarily >= 0 ? roundArbitrarily : DoubleUtils.nextDown(roundArbitrarily);
            }
            case HALF_EVEN: 
            case HALF_DOWN: 
            case HALF_UP: {
                X roundCeiling;
                double roundCeilingAsDouble;
                X roundFloor;
                double roundFloorAsDouble;
                if (cmpXToRoundArbitrarily >= 0) {
                    roundFloorAsDouble = roundArbitrarily;
                    roundFloor = roundArbitrarilyAsX;
                    roundCeilingAsDouble = Math.nextUp(roundArbitrarily);
                    if (roundCeilingAsDouble == Double.POSITIVE_INFINITY) {
                        return roundFloorAsDouble;
                    }
                    roundCeiling = this.toX(roundCeilingAsDouble, RoundingMode.CEILING);
                } else {
                    roundCeilingAsDouble = roundArbitrarily;
                    roundCeiling = roundArbitrarilyAsX;
                    roundFloorAsDouble = DoubleUtils.nextDown(roundArbitrarily);
                    if (roundFloorAsDouble == Double.NEGATIVE_INFINITY) {
                        return roundCeilingAsDouble;
                    }
                    roundFloor = this.toX(roundFloorAsDouble, RoundingMode.FLOOR);
                }
                X deltaToFloor = this.minus(x, roundFloor);
                X deltaToCeiling = this.minus(roundCeiling, x);
                int diff = ((Comparable)deltaToFloor).compareTo(deltaToCeiling);
                if (diff < 0) {
                    return roundFloorAsDouble;
                }
                if (diff > 0) {
                    return roundCeilingAsDouble;
                }
                switch (mode) {
                    case HALF_EVEN: {
                        return (Double.doubleToRawLongBits(roundFloorAsDouble) & 1L) == 0L ? roundFloorAsDouble : roundCeilingAsDouble;
                    }
                    case HALF_DOWN: {
                        return this.sign(x) >= 0 ? roundFloorAsDouble : roundCeilingAsDouble;
                    }
                    case HALF_UP: {
                        return this.sign(x) >= 0 ? roundCeilingAsDouble : roundFloorAsDouble;
                    }
                }
                throw new AssertionError((Object)"impossible");
            }
        }
        throw new AssertionError((Object)"impossible");
    }
}

