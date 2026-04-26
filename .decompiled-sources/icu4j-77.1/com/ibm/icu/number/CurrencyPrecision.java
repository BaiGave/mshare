/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.number;

import com.ibm.icu.number.Precision;
import com.ibm.icu.util.Currency;

public abstract class CurrencyPrecision
extends Precision {
    CurrencyPrecision() {
    }

    public Precision withCurrency(Currency currency) {
        if (currency != null) {
            Precision retval = CurrencyPrecision.constructFromCurrency(this, currency);
            retval.trailingZeroDisplay = this.trailingZeroDisplay;
            return retval;
        }
        throw new IllegalArgumentException("Currency must not be null");
    }
}

