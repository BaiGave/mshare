/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.datafixers;

import com.mojang.datafixers.TypedOptic;

public interface FamilyOptic<A, B> {
    public TypedOptic<?, ?, A, B> apply(int var1);
}

