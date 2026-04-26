/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.flat;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;

public class FlatLayerInfo {
    public static final Codec<FlatLayerInfo> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.intRange(0, DimensionType.Y_SIZE).fieldOf("height")).forGetter(FlatLayerInfo::getHeight), ((MapCodec)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block")).orElse(Blocks.AIR).forGetter(l -> l.getBlockState().getBlock())).apply((Applicative<FlatLayerInfo, ?>)i, FlatLayerInfo::new));
    private final Block block;
    private final int height;

    public FlatLayerInfo(int height, Block block) {
        this.height = height;
        this.block = block;
    }

    public int getHeight() {
        return this.height;
    }

    public BlockState getBlockState() {
        return this.block.defaultBlockState();
    }

    public FlatLayerInfo heightLimited(int maxHeight) {
        if (this.height > maxHeight) {
            return new FlatLayerInfo(maxHeight, this.block);
        }
        return this;
    }

    public String toString() {
        return (String)(this.height != 1 ? this.height + "*" : "") + String.valueOf(BuiltInRegistries.BLOCK.getKey(this.block));
    }
}

