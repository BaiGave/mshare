/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class StainedGlassPaneBlock
extends IronBarsBlock
implements BeaconBeamBlock {
    public static final MapCodec<StainedGlassPaneBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)DyeColor.CODEC.fieldOf("color")).forGetter(StainedGlassPaneBlock::getColor), StainedGlassPaneBlock.propertiesCodec()).apply((Applicative<StainedGlassPaneBlock, ?>)i, StainedGlassPaneBlock::new));
    private final DyeColor color;

    public MapCodec<StainedGlassPaneBlock> codec() {
        return CODEC;
    }

    public StainedGlassPaneBlock(DyeColor color, BlockBehaviour.Properties properties) {
        super(properties);
        this.color = color;
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(NORTH, false)).setValue(EAST, false)).setValue(SOUTH, false)).setValue(WEST, false)).setValue(WATERLOGGED, false));
    }

    @Override
    public DyeColor getColor() {
        return this.color;
    }
}

