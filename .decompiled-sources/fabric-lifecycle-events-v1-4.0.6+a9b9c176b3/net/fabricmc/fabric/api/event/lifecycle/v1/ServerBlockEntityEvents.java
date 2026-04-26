/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class ServerBlockEntityEvents {
    public static final Event<Load> BLOCK_ENTITY_LOAD = EventFactory.createArrayBacked(Load.class, callbacks -> (blockEntity, level) -> {
        for (Load callback : callbacks) {
            callback.onLoad(blockEntity, level);
        }
    });
    public static final Event<Unload> BLOCK_ENTITY_UNLOAD = EventFactory.createArrayBacked(Unload.class, callbacks -> (blockEntity, level) -> {
        for (Unload callback : callbacks) {
            callback.onUnload(blockEntity, level);
        }
    });

    private ServerBlockEntityEvents() {
    }

    @FunctionalInterface
    public static interface Unload {
        public void onUnload(BlockEntity var1, ServerLevel var2);
    }

    @FunctionalInterface
    public static interface Load {
        public void onLoad(BlockEntity var1, ServerLevel var2);
    }
}

