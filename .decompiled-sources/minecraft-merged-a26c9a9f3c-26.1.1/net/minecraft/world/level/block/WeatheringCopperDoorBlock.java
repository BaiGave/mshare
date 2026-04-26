/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

public class WeatheringCopperDoorBlock
extends DoorBlock
implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperDoorBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BlockSetType.CODEC.fieldOf("block_set_type")).forGetter(DoorBlock::type), ((MapCodec)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")).forGetter(WeatheringCopperDoorBlock::getAge), WeatheringCopperDoorBlock.propertiesCodec()).apply((Applicative<WeatheringCopperDoorBlock, ?>)i, WeatheringCopperDoorBlock::new));
    private final WeatheringCopper.WeatherState weatherState;

    public MapCodec<WeatheringCopperDoorBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperDoorBlock(BlockSetType type, WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
        super(type, properties);
        this.weatherState = weatherState;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
            this.changeOverTime(state, level, pos, random);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.weatherState;
    }
}

