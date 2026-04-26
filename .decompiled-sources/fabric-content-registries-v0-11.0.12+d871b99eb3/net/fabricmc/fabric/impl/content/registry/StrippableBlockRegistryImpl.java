/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.registry.StrippableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.util.ImmutableCollectionUtils;
import net.fabricmc.fabric.mixin.content.registry.AxeItemAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StrippableBlockRegistryImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger(StrippableBlockRegistryImpl.class);
    private static final IdentityHashMap<Block, StrippableBlockRegistry.StrippingTransformer> TRANSFORMERS = new IdentityHashMap();

    public static void register(Block input, Block stripped, StrippableBlockRegistry.StrippingTransformer transformer) {
        Objects.requireNonNull(input, "input block cannot be null");
        Objects.requireNonNull(stripped, "stripped block cannot be null");
        Block old = StrippableBlockRegistryImpl.getRegistry().put(input, stripped);
        TRANSFORMERS.put(input, transformer);
        if (old != null) {
            LOGGER.debug("Replaced old stripping mapping from {} to {} with {}", input, old, stripped);
        }
    }

    private static Map<Block, Block> getRegistry() {
        return ImmutableCollectionUtils.getAsMutableMap(AxeItemAccessor::getStrippables, AxeItemAccessor::setStrippables);
    }

    public static @Nullable BlockState getStrippedBlockState(BlockState state) {
        Block strippedBlock = StrippableBlockRegistryImpl.getRegistry().get(state.getBlock());
        if (strippedBlock == null) {
            return null;
        }
        return TRANSFORMERS.getOrDefault(state.getBlock(), StrippableBlockRegistry.StrippingTransformer.VANILLA).getStrippedBlockState(strippedBlock, state);
    }

    public static @Nullable StrippableBlockRegistry.StrippingTransformer getTransformer(Block block) {
        return TRANSFORMERS.get(block);
    }
}

