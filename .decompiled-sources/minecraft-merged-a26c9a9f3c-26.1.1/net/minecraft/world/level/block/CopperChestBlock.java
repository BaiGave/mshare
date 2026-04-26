/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

public class CopperChestBlock
extends ChestBlock {
    public static final MapCodec<CopperChestBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)WeatheringCopper.WeatherState.CODEC.fieldOf("weathering_state")).forGetter(CopperChestBlock::getState), ((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("open_sound")).forGetter(ChestBlock::getOpenChestSound), ((MapCodec)BuiltInRegistries.SOUND_EVENT.byNameCodec().fieldOf("close_sound")).forGetter(ChestBlock::getCloseChestSound), CopperChestBlock.propertiesCodec()).apply((Applicative<CopperChestBlock, ?>)i, CopperChestBlock::new));
    private static final Map<Block, Supplier<Block>> COPPER_TO_COPPER_CHEST_MAPPING = Map.of(Blocks.COPPER_BLOCK, () -> Blocks.COPPER_CHEST, Blocks.EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, Blocks.WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, Blocks.OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_COPPER_BLOCK, () -> Blocks.COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER, () -> Blocks.EXPOSED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER, () -> Blocks.WEATHERED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER, () -> Blocks.OXIDIZED_COPPER_CHEST);
    private final WeatheringCopper.WeatherState weatherState;

    @Override
    public MapCodec<? extends CopperChestBlock> codec() {
        return CODEC;
    }

    public CopperChestBlock(WeatheringCopper.WeatherState weatherState, SoundEvent openSound, SoundEvent closeSound, BlockBehaviour.Properties properties) {
        super(() -> BlockEntityType.CHEST, openSound, closeSound, properties);
        this.weatherState = weatherState;
    }

    @Override
    public boolean chestCanConnectTo(BlockState blockState) {
        return blockState.is(BlockTags.COPPER_CHESTS) && blockState.hasProperty(ChestBlock.TYPE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return CopperChestBlock.getLeastOxidizedChestOfConnectedBlocks(state, context.getLevel(), context.getClickedPos());
    }

    private static BlockState getLeastOxidizedChestOfConnectedBlocks(BlockState state, Level level, BlockPos pos) {
        Block block;
        BlockState connectedState = level.getBlockState(pos.relative(CopperChestBlock.getConnectedDirection(state)));
        if (!state.getValue(ChestBlock.TYPE).equals(ChestType.SINGLE) && (block = state.getBlock()) instanceof CopperChestBlock) {
            CopperChestBlock copperChestBlock = (CopperChestBlock)block;
            block = connectedState.getBlock();
            if (block instanceof CopperChestBlock) {
                CopperChestBlock connectedCopperChestBlock = (CopperChestBlock)block;
                BlockState updatedBlockState = state;
                BlockState connectedPredictedBlockState = connectedState;
                if (copperChestBlock.isWaxed() != connectedCopperChestBlock.isWaxed()) {
                    updatedBlockState = CopperChestBlock.unwaxBlock(copperChestBlock, state).orElse(updatedBlockState);
                    connectedPredictedBlockState = CopperChestBlock.unwaxBlock(connectedCopperChestBlock, connectedState).orElse(connectedPredictedBlockState);
                }
                Block leastOxidizedBlock = copperChestBlock.weatherState.ordinal() <= connectedCopperChestBlock.weatherState.ordinal() ? updatedBlockState.getBlock() : connectedPredictedBlockState.getBlock();
                return leastOxidizedBlock.withPropertiesOf(updatedBlockState);
            }
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
        ChestType chestType;
        BlockState blockState = super.updateShape(state, level, ticks, pos, directionToNeighbour, neighbourPos, neighbourState, random);
        if (this.chestCanConnectTo(neighbourState) && !(chestType = blockState.getValue(ChestBlock.TYPE)).equals(ChestType.SINGLE) && CopperChestBlock.getConnectedDirection(blockState) == directionToNeighbour) {
            return neighbourState.getBlock().withPropertiesOf(blockState);
        }
        return blockState;
    }

    private static Optional<BlockState> unwaxBlock(CopperChestBlock copperChestBlock, BlockState state) {
        if (!copperChestBlock.isWaxed()) {
            return Optional.of(state);
        }
        return Optional.ofNullable((Block)HoneycombItem.WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map(b -> b.withPropertiesOf(state));
    }

    public WeatheringCopper.WeatherState getState() {
        return this.weatherState;
    }

    public static BlockState getFromCopperBlock(Block copperBlock, Direction facing, Level level, BlockPos pos) {
        CopperChestBlock block = (CopperChestBlock)COPPER_TO_COPPER_CHEST_MAPPING.getOrDefault(copperBlock, Blocks.COPPER_CHEST::asBlock).get();
        ChestType chestType = block.getChestType(level, pos, facing);
        BlockState state = (BlockState)((BlockState)block.defaultBlockState().setValue(FACING, facing)).setValue(TYPE, chestType);
        return CopperChestBlock.getLeastOxidizedChestOfConnectedBlocks(state, level, pos);
    }

    public boolean isWaxed() {
        return true;
    }

    @Override
    public boolean shouldChangedStateKeepBlockEntity(BlockState oldState) {
        return oldState.is(BlockTags.COPPER_CHESTS);
    }
}

