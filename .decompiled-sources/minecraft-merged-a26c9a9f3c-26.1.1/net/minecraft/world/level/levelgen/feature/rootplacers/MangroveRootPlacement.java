/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record MangroveRootPlacement(HolderSet<Block> canGrowThrough, HolderSet<Block> muddyRootsIn, BlockStateProvider muddyRootsProvider, int maxRootWidth, int maxRootLength, float randomSkewChance) {
    public static final Codec<MangroveRootPlacement> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_grow_through")).forGetter(c -> c.canGrowThrough), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("muddy_roots_in")).forGetter(c -> c.muddyRootsIn), ((MapCodec)BlockStateProvider.CODEC.fieldOf("muddy_roots_provider")).forGetter(c -> c.muddyRootsProvider), ((MapCodec)Codec.intRange(1, 12).fieldOf("max_root_width")).forGetter(p -> p.maxRootWidth), ((MapCodec)Codec.intRange(1, 64).fieldOf("max_root_length")).forGetter(p -> p.maxRootLength), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("random_skew_chance")).forGetter(p -> Float.valueOf(p.randomSkewChance))).apply((Applicative<MangroveRootPlacement, ?>)i, MangroveRootPlacement::new));
}

