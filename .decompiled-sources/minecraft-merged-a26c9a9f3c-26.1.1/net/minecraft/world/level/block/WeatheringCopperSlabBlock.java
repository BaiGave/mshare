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
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperSlabBlock
extends SlabBlock
implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperSlabBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")).forGetter(ChangeOverTimeBlock::getAge), WeatheringCopperSlabBlock.propertiesCodec()).apply((Applicative<WeatheringCopperSlabBlock, ?>)i, WeatheringCopperSlabBlock::new));
    private final WeatheringCopper.WeatherState weatherState;

    public MapCodec<WeatheringCopperSlabBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperSlabBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
        super(properties);
        this.weatherState = weatherState;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.changeOverTime(state, level, pos, random);
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

