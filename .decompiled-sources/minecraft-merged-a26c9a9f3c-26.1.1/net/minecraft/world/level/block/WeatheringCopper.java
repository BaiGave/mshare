/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChangeOverTimeBlock;
import net.minecraft.world.level.block.state.BlockState;

public interface WeatheringCopper
extends ChangeOverTimeBlock<WeatherState> {
    public static final Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> ((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)((ImmutableBiMap.Builder)ImmutableBiMap.builder().put(Blocks.COPPER_BLOCK, Blocks.EXPOSED_COPPER)).put(Blocks.EXPOSED_COPPER, Blocks.WEATHERED_COPPER)).put(Blocks.WEATHERED_COPPER, Blocks.OXIDIZED_COPPER)).put(Blocks.CUT_COPPER, Blocks.EXPOSED_CUT_COPPER)).put(Blocks.EXPOSED_CUT_COPPER, Blocks.WEATHERED_CUT_COPPER)).put(Blocks.WEATHERED_CUT_COPPER, Blocks.OXIDIZED_CUT_COPPER)).put(Blocks.CHISELED_COPPER, Blocks.EXPOSED_CHISELED_COPPER)).put(Blocks.EXPOSED_CHISELED_COPPER, Blocks.WEATHERED_CHISELED_COPPER)).put(Blocks.WEATHERED_CHISELED_COPPER, Blocks.OXIDIZED_CHISELED_COPPER)).put(Blocks.CUT_COPPER_SLAB, Blocks.EXPOSED_CUT_COPPER_SLAB)).put(Blocks.EXPOSED_CUT_COPPER_SLAB, Blocks.WEATHERED_CUT_COPPER_SLAB)).put(Blocks.WEATHERED_CUT_COPPER_SLAB, Blocks.OXIDIZED_CUT_COPPER_SLAB)).put(Blocks.CUT_COPPER_STAIRS, Blocks.EXPOSED_CUT_COPPER_STAIRS)).put(Blocks.EXPOSED_CUT_COPPER_STAIRS, Blocks.WEATHERED_CUT_COPPER_STAIRS)).put(Blocks.WEATHERED_CUT_COPPER_STAIRS, Blocks.OXIDIZED_CUT_COPPER_STAIRS)).put(Blocks.COPPER_DOOR, Blocks.EXPOSED_COPPER_DOOR)).put(Blocks.EXPOSED_COPPER_DOOR, Blocks.WEATHERED_COPPER_DOOR)).put(Blocks.WEATHERED_COPPER_DOOR, Blocks.OXIDIZED_COPPER_DOOR)).put(Blocks.COPPER_TRAPDOOR, Blocks.EXPOSED_COPPER_TRAPDOOR)).put(Blocks.EXPOSED_COPPER_TRAPDOOR, Blocks.WEATHERED_COPPER_TRAPDOOR)).put(Blocks.WEATHERED_COPPER_TRAPDOOR, Blocks.OXIDIZED_COPPER_TRAPDOOR)).putAll(Blocks.COPPER_BARS.weatheringMapping())).put(Blocks.COPPER_GRATE, Blocks.EXPOSED_COPPER_GRATE)).put(Blocks.EXPOSED_COPPER_GRATE, Blocks.WEATHERED_COPPER_GRATE)).put(Blocks.WEATHERED_COPPER_GRATE, Blocks.OXIDIZED_COPPER_GRATE)).put(Blocks.COPPER_BULB, Blocks.EXPOSED_COPPER_BULB)).put(Blocks.EXPOSED_COPPER_BULB, Blocks.WEATHERED_COPPER_BULB)).put(Blocks.WEATHERED_COPPER_BULB, Blocks.OXIDIZED_COPPER_BULB)).putAll(Blocks.COPPER_LANTERN.weatheringMapping())).put(Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER_CHEST)).put(Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER_CHEST)).put(Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER_CHEST)).put(Blocks.COPPER_GOLEM_STATUE, Blocks.EXPOSED_COPPER_GOLEM_STATUE)).put(Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WEATHERED_COPPER_GOLEM_STATUE)).put(Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.OXIDIZED_COPPER_GOLEM_STATUE)).put(Blocks.LIGHTNING_ROD, Blocks.EXPOSED_LIGHTNING_ROD)).put(Blocks.EXPOSED_LIGHTNING_ROD, Blocks.WEATHERED_LIGHTNING_ROD)).put(Blocks.WEATHERED_LIGHTNING_ROD, Blocks.OXIDIZED_LIGHTNING_ROD)).putAll(Blocks.COPPER_CHAIN.weatheringMapping())).build());
    public static final Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());

    public static Optional<Block> getPrevious(Block block) {
        return Optional.ofNullable((Block)PREVIOUS_BY_BLOCK.get().get(block));
    }

    public static Block getFirst(Block block) {
        Block candiate = block;
        Block previous = (Block)PREVIOUS_BY_BLOCK.get().get(candiate);
        while (previous != null) {
            candiate = previous;
            previous = (Block)PREVIOUS_BY_BLOCK.get().get(candiate);
        }
        return candiate;
    }

    public static Optional<BlockState> getPrevious(BlockState state) {
        return WeatheringCopper.getPrevious(state.getBlock()).map(s -> s.withPropertiesOf(state));
    }

    public static Optional<Block> getNext(Block block) {
        return Optional.ofNullable((Block)NEXT_BY_BLOCK.get().get(block));
    }

    public static BlockState getFirst(BlockState state) {
        return WeatheringCopper.getFirst(state.getBlock()).withPropertiesOf(state);
    }

    @Override
    default public Optional<BlockState> getNext(BlockState state) {
        return WeatheringCopper.getNext(state.getBlock()).map(s -> s.withPropertiesOf(state));
    }

    @Override
    default public float getChanceModifier() {
        if (this.getAge() == WeatherState.UNAFFECTED) {
            return 0.75f;
        }
        return 1.0f;
    }

    public static enum WeatherState implements StringRepresentable
    {
        UNAFFECTED("unaffected"),
        EXPOSED("exposed"),
        WEATHERED("weathered"),
        OXIDIZED("oxidized");

        public static final IntFunction<WeatherState> BY_ID;
        public static final Codec<WeatherState> CODEC;
        public static final StreamCodec<ByteBuf, WeatherState> STREAM_CODEC;
        private final String name;

        private WeatherState(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public WeatherState next() {
            return BY_ID.apply(this.ordinal() + 1);
        }

        public WeatherState previous() {
            return BY_ID.apply(this.ordinal() - 1);
        }

        static {
            BY_ID = ByIdMap.continuous(Enum::ordinal, WeatherState.values(), ByIdMap.OutOfBoundsStrategy.CLAMP);
            CODEC = StringRepresentable.fromEnum(WeatherState::values);
            STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);
        }
    }
}

