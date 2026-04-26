/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.biome;

import net.minecraft.world.level.levelgen.synth.ImprovedNoise;

public interface MultiNoiseSamplerHooks {
    public ImprovedNoise fabric_getEndBiomesSampler();

    public void fabric_setSeed(long var1);

    public long fabric_getSeed();
}

