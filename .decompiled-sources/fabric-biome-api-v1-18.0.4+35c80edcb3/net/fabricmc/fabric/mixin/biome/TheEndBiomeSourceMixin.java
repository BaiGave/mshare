/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome;

import com.google.common.base.Suppliers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;
import net.fabricmc.fabric.impl.biome.TheEndBiomeData;
import net.fabricmc.fabric.mixin.biome.BiomeSourceMixin;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.biome.TheEndBiomeSource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={TheEndBiomeSource.class})
public class TheEndBiomeSourceMixin
extends BiomeSourceMixin {
    @Shadow
    @Mutable
    @Final
    public static MapCodec<TheEndBiomeSource> CODEC;
    @Unique
    private Supplier<TheEndBiomeData.Overrides> overrides;
    @Unique
    private boolean biomeSetModified = false;
    @Unique
    private boolean hasCheckedForModifiedSet = false;

    @Inject(method={"<clinit>"}, at={@At(value="TAIL")})
    private static void modifyCodec(CallbackInfo ci) {
        CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(RegistryOps.retrieveGetter(Registries.BIOME)).apply(instance, instance.stable(TheEndBiomeSource::create)));
    }

    @Inject(method={"create"}, at={@At(value="HEAD")})
    private static void rememberLookup(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> ci) {
        TheEndBiomeData.biomeRegistry.set(biomes);
    }

    @Inject(method={"create"}, at={@At(value="TAIL")})
    private static void clearLookup(HolderGetter<Biome> biomes, CallbackInfoReturnable<?> ci) {
        TheEndBiomeData.biomeRegistry.remove();
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void init(Holder<Biome> centerBiome, Holder<Biome> highlandsBiome, Holder<Biome> midlandsBiome, Holder<Biome> smallIslandsBiome, Holder<Biome> barrensBiome, CallbackInfo ci) {
        HolderGetter<Biome> biomes = TheEndBiomeData.biomeRegistry.get();
        if (biomes == null) {
            throw new IllegalStateException("Biome registry not set by Mixin");
        }
        this.overrides = Suppliers.memoize(() -> TheEndBiomeData.createOverrides(biomes));
    }

    @Inject(method={"getNoiseBiome"}, at={@At(value="RETURN")}, cancellable=true)
    private void getWeightedEndBiome(int biomeX, int biomeY, int biomeZ, Climate.Sampler noise, CallbackInfoReturnable<Holder<Biome>> cir) {
        cir.setReturnValue(this.overrides.get().pick(biomeX, biomeY, biomeZ, noise, cir.getReturnValue()));
    }

    @Override
    protected Set<Holder<Biome>> modifyBiomeSet(Set<Holder<Biome>> biomes) {
        if (!this.hasCheckedForModifiedSet) {
            this.hasCheckedForModifiedSet = true;
            boolean bl = this.biomeSetModified = !this.overrides.get().customBiomes.isEmpty();
        }
        if (this.biomeSetModified) {
            LinkedHashSet<Holder<Biome>> modifiedBiomes = new LinkedHashSet<Holder<Biome>>(biomes);
            modifiedBiomes.addAll(this.overrides.get().customBiomes);
            return Collections.unmodifiableSet(modifiedBiomes);
        }
        return biomes;
    }
}

