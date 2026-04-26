/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.valueproviders.SampledFloat;

public interface FloatProvider
extends SampledFloat {
    public float min();

    public float max();

    public MapCodec<? extends FloatProvider> codec();
}

