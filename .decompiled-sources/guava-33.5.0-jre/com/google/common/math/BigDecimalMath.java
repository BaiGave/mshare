/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.math;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.math.ToDoubleRounder;
import java.math.BigDecimal;
import java.math.RoundingMode;

@J2ktIncompatible
@GwtIncompatible
public class BigDecimalMath {
    private BigDecimalMath() {
    }

    public static double roundToDouble(BigDecimal x, RoundingMode mode) {
        return BigDecimalToDoubleRounder.INSTANCE.roundToDouble(x, mode);
    }

    private static final class BigDecimalToDoubleRounder
    extends ToDoubleRounder<BigDecimal> {
        static final BigDecimalToDoubleRounder INSTANCE = new BigDecimalToDoubleRounder();

        private BigDecimalToDoubleRounder() {
        }

        @Override
        double roundToDoubleArbitrarily(BigDecimal bigDecimal) {
            return bigDecimal.doubleValue();
        }

        @Override
        int sign(BigDecimal bigDecimal) {
            return bigDecimal.signum();
        }

        @Override
        BigDecimal toX(double d, RoundingMode mode) {
            return new BigDecimal(d);
        }

        @Override
        BigDecimal minus(BigDecimal a, BigDecimal b) {
            return a.subtract(b);
        }
    }
}

