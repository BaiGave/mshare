/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.block;

import net.fabricmc.fabric.impl.lookup.block.BlockApiCacheImpl;
import net.minecraft.core.BlockPos;

public interface ServerLevelCache {
    public void fabric_registerCache(BlockPos var1, BlockApiCacheImpl<?, ?> var2);

    public void fabric_invalidateCache(BlockPos var1);
}

