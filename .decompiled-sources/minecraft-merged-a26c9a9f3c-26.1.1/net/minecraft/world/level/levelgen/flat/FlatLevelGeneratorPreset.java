/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.flat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;

public record FlatLevelGeneratorPreset(Holder<Item> displayItem, FlatLevelGeneratorSettings settings) {
    public static final Codec<FlatLevelGeneratorPreset> DIRECT_CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Item.CODEC.fieldOf("display")).forGetter(e -> e.displayItem), ((MapCodec)FlatLevelGeneratorSettings.CODEC.fieldOf("settings")).forGetter(e -> e.settings)).apply((Applicative<FlatLevelGeneratorPreset, ?>)i, FlatLevelGeneratorPreset::new));
    public static final Codec<Holder<FlatLevelGeneratorPreset>> CODEC = RegistryFileCodec.create(Registries.FLAT_LEVEL_GENERATOR_PRESET, DIRECT_CODEC);
}

