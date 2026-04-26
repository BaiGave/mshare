/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.valueproviders;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.RandomSource;

public interface IntProvider {
    public int sample(RandomSource var1);

    public int minInclusive();

    public int maxInclusive();

    public MapCodec<? extends IntProvider> codec();
}

