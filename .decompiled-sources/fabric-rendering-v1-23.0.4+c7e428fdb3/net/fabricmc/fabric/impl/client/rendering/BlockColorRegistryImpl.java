/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

public final class BlockColorRegistryImpl {
    private static @Nullable BlockColors blockColors;
    private static @Nullable Map<Block, List<BlockTintSource>> map;

    public static void initialize(BlockColors blockColors) {
        if (BlockColorRegistryImpl.blockColors != null) {
            return;
        }
        BlockColorRegistryImpl.blockColors = blockColors;
        map.forEach((block, color) -> blockColors.register((List<BlockTintSource>)color, (Block)block));
        map = null;
    }

    public static void register(List<BlockTintSource> layers, Block ... blocks) {
        if (blockColors != null) {
            blockColors.register(layers, blocks);
        } else {
            for (Block block : blocks) {
                map.put(block, layers);
            }
        }
    }

    static {
        map = new IdentityHashMap<Block, List<BlockTintSource>>();
    }
}

