/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={RandomState.class})
public class RandomStateMixin {
    @Shadow
    @Final
    private Climate.Sampler sampler;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(NoiseGeneratorSettings chunkGeneratorSettings, HolderGetter<NormalNoise.NoiseParameters> arg, long seed, CallbackInfo ci) {
        ((MultiNoiseSamplerHooks)((Object)this.sampler)).fabric_setSeed(seed);
    }
}

