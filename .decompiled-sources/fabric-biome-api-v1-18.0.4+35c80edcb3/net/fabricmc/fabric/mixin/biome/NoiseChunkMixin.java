/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import java.util.List;
import net.fabricmc.fabric.impl.biome.MultiNoiseSamplerHooks;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.Aquifer;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseChunk;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={NoiseChunk.class})
public class NoiseChunkMixin {
    @Unique
    private long seed;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(int horizontalSize, RandomState randomState, int i, int j, NoiseSettings generationShapeConfig, DensityFunctions.BeardifierOrMarker arg, NoiseGeneratorSettings chunkGeneratorSettings, Aquifer.FluidPicker fluidLevelSampler, Blender blender, CallbackInfo ci) {
        this.seed = ((MultiNoiseSamplerHooks)((Object)randomState.sampler())).fabric_getSeed();
    }

    @Inject(method={"cachedClimateSampler"}, at={@At(value="RETURN")})
    private void createMultiNoiseSampler(NoiseRouter noiseRouter, List<Climate.ParameterPoint> list, CallbackInfoReturnable<Climate.Sampler> cir) {
        ((MultiNoiseSamplerHooks)((Object)cir.getReturnValue())).fabric_setSeed(this.seed);
    }
}

