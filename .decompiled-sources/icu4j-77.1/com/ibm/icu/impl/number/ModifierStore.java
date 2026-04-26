/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.Modifier;

public interface ModifierStore {
    public Modifier getModifier(Modifier.Signum var1, StandardPlural var2);
}

