/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.number.NumberFormatter;
import com.ibm.icu.number.Precision;

public abstract class FractionPrecision
extends Precision {
    FractionPrecision() {
    }

    public Precision withSignificantDigits(int minSignificantDigits, int maxSignificantDigits, NumberFormatter.RoundingPriority priority) {
        if (maxSignificantDigits >= 1 && maxSignificantDigits >= minSignificantDigits && maxSignificantDigits <= 999) {
            return FractionPrecision.constructFractionSignificant(this, minSignificantDigits, maxSignificantDigits, priority, false);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public Precision withMinDigits(int minSignificantDigits) {
        if (minSignificantDigits >= 1 && minSignificantDigits <= 999) {
            return FractionPrecision.constructFractionSignificant(this, 1, minSignificantDigits, NumberFormatter.RoundingPriority.RELAXED, true);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }

    public Precision withMaxDigits(int maxSignificantDigits) {
        if (maxSignificantDigits >= 1 && maxSignificantDigits <= 999) {
            return FractionPrecision.constructFractionSignificant(this, 1, maxSignificantDigits, NumberFormatter.RoundingPriority.STRICT, true);
        }
        throw new IllegalArgumentException("Significant digits must be between 1 and 999 (inclusive)");
    }
}

