/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.block.entity;

import com.mojang.datafixers.types.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.impl.object.builder.ExtendedBlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class FabricBlockEntityTypeBuilder<T extends BlockEntity> {
    private final Factory<? extends T> factory;
    private final Set<Block> blocks = new HashSet<Block>();
    private @Nullable Boolean canPotentiallyExecuteCommands = null;

    private FabricBlockEntityTypeBuilder(Factory<? extends T> factory) {
        this.factory = factory;
    }

    public static <T extends BlockEntity> FabricBlockEntityTypeBuilder<T> create(Factory<? extends T> factory, Block ... blocks) {
        return new FabricBlockEntityTypeBuilder<T>(factory).addBlocks(blocks);
    }

    public FabricBlockEntityTypeBuilder<T> addBlock(Block block) {
        this.blocks.add(block);
        return this;
    }

    public FabricBlockEntityTypeBuilder<T> addBlocks(Block ... blocks) {
        Collections.addAll(this.blocks, blocks);
        return this;
    }

    public FabricBlockEntityTypeBuilder<T> addBlocks(Collection<? extends Block> blocks) {
        this.blocks.addAll(blocks);
        return this;
    }

    public FabricBlockEntityTypeBuilder<T> canPotentiallyExecuteCommands(boolean canPotentiallyExecuteCommands) {
        this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
        return this;
    }

    public BlockEntityType<T> build() {
        return new ExtendedBlockEntityType<BlockEntity>(this.factory::create, new HashSet<Block>(this.blocks), this.canPotentiallyExecuteCommands);
    }

    @Deprecated
    public BlockEntityType<T> build(@Nullable Type<?> type) {
        return this.build();
    }

    @FunctionalInterface
    public static interface Factory<T extends BlockEntity> {
        public T create(BlockPos var1, BlockState var2);
    }
}

