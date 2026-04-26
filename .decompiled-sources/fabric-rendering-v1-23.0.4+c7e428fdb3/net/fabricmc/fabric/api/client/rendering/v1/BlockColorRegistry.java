/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.List;
import net.fabricmc.fabric.impl.client.rendering.BlockColorRegistryImpl;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;

public final class BlockColorRegistry {
    private BlockColorRegistry() {
    }

    public static void register(List<BlockTintSource> layers, Block ... blocks) {
        BlockColorRegistryImpl.register(layers, blocks);
    }
}

