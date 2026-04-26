/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.object.builder;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jspecify.annotations.Nullable;

public class ExtendedBlockEntityType<T extends BlockEntity>
extends BlockEntityType<T> {
    private final @Nullable Boolean canPotentiallyExecuteCommands;

    public ExtendedBlockEntityType(BlockEntityType.BlockEntitySupplier<? extends T> factory, Set<Block> blocks, @Nullable Boolean canPotentiallyExecuteCommands) {
        super(factory, blocks);
        this.canPotentiallyExecuteCommands = canPotentiallyExecuteCommands;
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        if (this.canPotentiallyExecuteCommands != null) {
            return this.canPotentiallyExecuteCommands;
        }
        return super.onlyOpCanSetNbt();
    }
}

