/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import com.ibm.icu.util.ULocale;

@Deprecated
public enum Directionality {
    UNKNOWN,
    LTR,
    RTL,
    AUTO,
    INHERIT;


    @Deprecated
    public static Directionality of(ULocale ulocale) {
        if (ulocale == null) {
            return INHERIT;
        }
        return ulocale.isRightToLeft() ? RTL : LTR;
    }
}

