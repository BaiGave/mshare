/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.block;

import java.util.Objects;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.impl.lookup.block.BlockApiCacheImpl;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface BlockApiCache<A, C> {
    default public @Nullable A find(C context) {
        return this.find(null, context);
    }

    public @Nullable A find(@Nullable BlockState var1, C var2);

    public @Nullable BlockEntity getBlockEntity();

    public BlockApiLookup<A, C> getLookup();

    public ServerLevel getLevel();

    public BlockPos getPos();

    public static <A, C> BlockApiCache<A, C> create(BlockApiLookup<A, C> lookup, ServerLevel level, BlockPos pos) {
        Objects.requireNonNull(pos, "BlockPos may not be null.");
        Objects.requireNonNull(level, "ServerLevel may not be null.");
        if (!(lookup instanceof BlockApiLookupImpl)) {
            throw new IllegalArgumentException("Cannot cache foreign implementation of BlockApiLookup. Use `BlockApiLookup#get(Identifier, Class<A>, Class<C>);` to get instances.");
        }
        return new BlockApiCacheImpl((BlockApiLookupImpl)lookup, level, pos);
    }
}

