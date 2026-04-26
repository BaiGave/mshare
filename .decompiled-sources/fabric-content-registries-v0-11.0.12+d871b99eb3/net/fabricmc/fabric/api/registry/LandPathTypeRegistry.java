/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LandPathTypeRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(LandPathTypeRegistry.class);
    private static final Map<Block, PathTypeProvider> PATH_TYPES = new IdentityHashMap<Block, PathTypeProvider>();

    private LandPathTypeRegistry() {
    }

    public static void register(Block block, @Nullable PathType pathType, @Nullable PathType pathTypeIfNeighbor) {
        Objects.requireNonNull(block, "Block cannot be null!");
        LandPathTypeRegistry.register(block, (state, neighbor) -> neighbor ? pathTypeIfNeighbor : pathType);
    }

    public static void register(Block block, StaticPathTypeProvider provider) {
        Objects.requireNonNull(block, "Block cannot be null!");
        Objects.requireNonNull(provider, "StaticPathTypeProvider cannot be null!");
        PathTypeProvider old = PATH_TYPES.put(block, provider);
        if (old != null) {
            LOGGER.debug("Replaced PathType provider for the block {}", (Object)block);
        }
    }

    public static void registerDynamic(Block block, DynamicPathTypeProvider provider) {
        Objects.requireNonNull(block, "Block cannot be null!");
        Objects.requireNonNull(provider, "DynamicPathTypeProvider cannot be null!");
        PathTypeProvider old = PATH_TYPES.put(block, provider);
        if (old != null) {
            LOGGER.debug("Replaced PathType provider for the block {}", (Object)block);
        }
    }

    public static @Nullable PathType getPathType(BlockState state, BlockGetter level, BlockPos pos, boolean neighbor) {
        Objects.requireNonNull(state, "BlockState cannot be null!");
        Objects.requireNonNull(level, "BlockGetter cannot be null!");
        Objects.requireNonNull(pos, "BlockPos cannot be null!");
        PathTypeProvider provider = LandPathTypeRegistry.getPathTypeProvider(state.getBlock());
        if (provider == null) {
            return null;
        }
        if (provider instanceof DynamicPathTypeProvider) {
            return ((DynamicPathTypeProvider)provider).getPathType(state, level, pos, neighbor);
        }
        return ((StaticPathTypeProvider)provider).getPathType(state, neighbor);
    }

    public static @Nullable PathTypeProvider getPathTypeProvider(Block block) {
        Objects.requireNonNull(block, "Block cannot be null!");
        return PATH_TYPES.get(block);
    }

    @FunctionalInterface
    public static non-sealed interface StaticPathTypeProvider
    extends PathTypeProvider {
        public @Nullable PathType getPathType(BlockState var1, boolean var2);
    }

    public static sealed interface PathTypeProvider
    permits StaticPathTypeProvider, DynamicPathTypeProvider {
    }

    @FunctionalInterface
    public static non-sealed interface DynamicPathTypeProvider
    extends PathTypeProvider {
        public @Nullable PathType getPathType(BlockState var1, BlockGetter var2, BlockPos var3, boolean var4);
    }
}

