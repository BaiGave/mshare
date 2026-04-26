/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WeightedPressurePlateBlock
extends BasePressurePlateBlock {
    public static final MapCodec<WeightedPressurePlateBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)Codec.intRange(1, 1024).fieldOf("max_weight")).forGetter(b -> b.maxWeight), ((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(b -> b.type), WeightedPressurePlateBlock.propertiesCodec()).apply((Applicative<WeightedPressurePlateBlock, ?>)i, WeightedPressurePlateBlock::new));
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    private final int maxWeight;

    public MapCodec<WeightedPressurePlateBlock> codec() {
        return CODEC;
    }

    public WeightedPressurePlateBlock(int maxWeight, BlockSetType type, BlockBehaviour.Properties properties) {
        super(properties, type);
        this.registerDefaultState((BlockState)((BlockState)this.stateDefinition.any()).setValue(POWER, 0));
        this.maxWeight = maxWeight;
    }

    @Override
    protected int getSignalStrength(Level level, BlockPos pos) {
        int count = Math.min(WeightedPressurePlateBlock.getEntityCount(level, TOUCH_AABB.move(pos), Entity.class), this.maxWeight);
        if (count > 0) {
            float percent = (float)Math.min(this.maxWeight, count) / (float)this.maxWeight;
            return Mth.ceil(percent * 15.0f);
        }
        return 0;
    }

    @Override
    protected int getSignalForState(BlockState state) {
        return state.getValue(POWER);
    }

    @Override
    protected BlockState setSignalForState(BlockState state, int signal) {
        return (BlockState)state.setValue(POWER, signal);
    }

    @Override
    protected int getPressedTime() {
        return 10;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
}

