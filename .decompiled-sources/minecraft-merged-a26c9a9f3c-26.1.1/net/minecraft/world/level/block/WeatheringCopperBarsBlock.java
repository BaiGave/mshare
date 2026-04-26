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
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class WeatheringCopperBarsBlock
extends IronBarsBlock
implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperBarsBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")).forGetter(WeatheringCopperBarsBlock::getAge), WeatheringCopperBarsBlock.propertiesCodec()).apply((Applicative<WeatheringCopperBarsBlock, ?>)i, WeatheringCopperBarsBlock::new));
    private final WeatheringCopper.WeatherState weatherState;

    public MapCodec<WeatheringCopperBarsBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperBarsBlock(WeatheringCopper.WeatherState weatherState, BlockBehaviour.Properties properties) {
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

