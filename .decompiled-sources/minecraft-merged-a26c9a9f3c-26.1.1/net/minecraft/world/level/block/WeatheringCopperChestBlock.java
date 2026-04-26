/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class WeatheringCopperChestBlock
extends CopperChestBlock
implements WeatheringCopper {
    public static final MapCodec<WeatheringCopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")).forGetter(CopperChestBlock::getState), ((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound")).forGetter(ChestBlock::getOpenChestSound), ((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound")).forGetter(ChestBlock::getCloseChestSound), WeatheringCopperChestBlock.propertiesCodec()).apply((Applicative<WeatheringCopperChestBlock, ?>)i, WeatheringCopperChestBlock::new));

    @Override
    public MapCodec<WeatheringCopperChestBlock> codec() {
        return CODEC;
    }

    public WeatheringCopperChestBlock(WeatheringCopper.WeatherState weatherState, SoundEvent openSound, SoundEvent closeSound, BlockBehaviour.Properties properties) {
        super(weatherState, openSound, closeSound, properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).isPresent();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ChestBlockEntity chestBlockEntity;
        BlockEntity blockEntity;
        if (!state.getValue(ChestBlock.TYPE).equals(ChestType.RIGHT) && (blockEntity = level.getBlockEntity(pos)) instanceof ChestBlockEntity && (chestBlockEntity = (ChestBlockEntity)blockEntity).getEntitiesWithContainerOpen().isEmpty()) {
            this.changeOverTime(state, level, pos, random);
        }
    }

    @Override
    public WeatheringCopper.WeatherState getAge() {
        return this.getState();
    }

    @Override
    public boolean isWaxed() {
        return false;
    }
}

