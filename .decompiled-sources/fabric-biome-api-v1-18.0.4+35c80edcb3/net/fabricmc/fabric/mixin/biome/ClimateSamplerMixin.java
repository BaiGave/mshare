/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import com.google.common.base.Preconditions;
import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={Climate.Sampler.class})
public class ClimateSamplerMixin
implements MultiNoiseSamplerHooks {
    @Unique
    private Long seed = null;
    @Unique
    private ImprovedNoise endBiomesSampler = null;

    @Override
    public void fabric_setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public long fabric_getSeed() {
        return this.seed;
    }

    @Override
    public ImprovedNoise fabric_getEndBiomesSampler() {
        if (this.endBiomesSampler == null) {
            Preconditions.checkState(this.seed != null, "MultiNoiseSampler doesn't have a seed set, created using different method?");
            this.endBiomesSampler = new ImprovedNoise(new WorldgenRandom(new LegacyRandomSource(this.seed)));
        }
        return this.endBiomesSampler;
    }
}

