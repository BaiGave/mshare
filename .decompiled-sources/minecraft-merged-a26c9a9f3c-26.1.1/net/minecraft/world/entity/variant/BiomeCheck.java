/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.entity.variant;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.variant.SpawnCondition;
import net.minecraft.world.entity.variant.SpawnContext;
import net.minecraft.world.level.biome.Biome;

public record BiomeCheck(HolderSet<Biome> requiredBiomes) implements SpawnCondition
{
    public static final MapCodec<BiomeCheck> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes")).forGetter(BiomeCheck::requiredBiomes)).apply((Applicative<BiomeCheck, ?>)i, BiomeCheck::new));

    @Override
    public boolean test(SpawnContext context) {
        return this.requiredBiomes.contains(context.biome());
    }

    public MapCodec<BiomeCheck> codec() {
        return MAP_CODEC;
    }
}

