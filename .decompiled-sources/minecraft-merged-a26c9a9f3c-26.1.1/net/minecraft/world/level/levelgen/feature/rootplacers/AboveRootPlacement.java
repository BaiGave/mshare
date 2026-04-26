/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.rootplacers;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;

public record AboveRootPlacement(BlockStateProvider aboveRootProvider, float aboveRootPlacementChance) {
    public static final Codec<AboveRootPlacement> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BlockStateProvider.CODEC.fieldOf("above_root_provider")).forGetter(c -> c.aboveRootProvider), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("above_root_placement_chance")).forGetter(p -> Float.valueOf(p.aboveRootPlacementChance))).apply((Applicative<AboveRootPlacement, ?>)i, AboveRootPlacement::new));
}

