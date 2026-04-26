/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import java.util.Objects;
import net.fabricmc.fabric.mixin.content.registry.ShovelItemAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlattenableBlockRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(FlattenableBlockRegistry.class);

    private FlattenableBlockRegistry() {
    }

    public static void register(Block input, BlockState flattened) {
        Objects.requireNonNull(input, "input block cannot be null");
        Objects.requireNonNull(flattened, "flattened block state cannot be null");
        BlockState old = ShovelItemAccessor.getFlattenables().put(input, flattened);
        if (old != null) {
            LOGGER.debug("Replaced old flattening mapping from {} to {} with {}", input, old, flattened);
        }
    }
}

