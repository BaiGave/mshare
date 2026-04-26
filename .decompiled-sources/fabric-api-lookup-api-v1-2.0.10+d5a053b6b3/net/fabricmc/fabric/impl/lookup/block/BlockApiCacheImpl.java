/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.block;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerBlockEntityEvents;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiCache;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.impl.lookup.block.BlockApiLookupImpl;
import net.fabricmc.fabric.impl.lookup.block.ServerLevelCache;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockApiCacheImpl<A, C>
implements BlockApiCache<A, C> {
    private final BlockApiLookupImpl<A, C> lookup;
    private final ServerLevel level;
    private final BlockPos pos;
    private boolean blockEntityCacheValid = false;
    private BlockEntity cachedBlockEntity = null;
    private BlockState lastState = null;
    private BlockApiLookup.BlockApiProvider<A, C> cachedProvider = null;

    public BlockApiCacheImpl(BlockApiLookupImpl<A, C> lookup, ServerLevel level, BlockPos pos) {
        ((ServerLevelCache)((Object)level)).fabric_registerCache(pos, this);
        this.lookup = lookup;
        this.level = level;
        this.pos = pos.immutable();
    }

    public void invalidate() {
        this.blockEntityCacheValid = false;
        this.cachedBlockEntity = null;
        this.lastState = null;
        this.cachedProvider = null;
    }

    @Override
    public @Nullable A find(@Nullable BlockState state, C context) {
        this.getBlockEntity();
        if (state == null) {
            state = this.cachedBlockEntity != null ? this.cachedBlockEntity.getBlockState() : this.level.getBlockState(this.pos);
        }
        if (this.lastState != state) {
            this.cachedProvider = this.lookup.getProvider(state.getBlock());
            this.lastState = state;
        }
        A instance = null;
        if (this.cachedProvider != null) {
            instance = this.cachedProvider.find(this.level, this.pos, state, this.cachedBlockEntity, context);
        }
        if (instance != null) {
            return instance;
        }
        for (BlockApiLookup.BlockApiProvider<A, C> fallbackProvider : this.lookup.getFallbackProviders()) {
            instance = fallbackProvider.find(this.level, this.pos, state, this.cachedBlockEntity, context);
            if (instance == null) continue;
            return instance;
        }
        return null;
    }

    @Override
    public @Nullable BlockEntity getBlockEntity() {
        if (!this.blockEntityCacheValid) {
            this.cachedBlockEntity = this.level.getBlockEntity(this.pos);
            this.blockEntityCacheValid = true;
        }
        return this.cachedBlockEntity;
    }

    @Override
    public BlockApiLookupImpl<A, C> getLookup() {
        return this.lookup;
    }

    @Override
    public ServerLevel getLevel() {
        return this.level;
    }

    @Override
    public BlockPos getPos() {
        return this.pos;
    }

    static {
        ServerBlockEntityEvents.BLOCK_ENTITY_LOAD.register((blockEntity, level) -> ((ServerLevelCache)((Object)level)).fabric_invalidateCache(blockEntity.getBlockPos()));
        ServerBlockEntityEvents.BLOCK_ENTITY_UNLOAD.register((blockEntity, level) -> ((ServerLevelCache)((Object)level)).fabric_invalidateCache(blockEntity.getBlockPos()));
    }
}

