/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class ServerEntityLevelChangeEvents {
    public static final Event<AfterEntityChange> AFTER_ENTITY_CHANGE_LEVEL = EventFactory.createArrayBacked(AfterEntityChange.class, callbacks -> (originalEntity, newEntity, origin, destination) -> {
        for (AfterEntityChange callback : callbacks) {
            callback.afterChangeLevel(originalEntity, newEntity, origin, destination);
        }
    });
    public static final Event<AfterPlayerChange> AFTER_PLAYER_CHANGE_LEVEL = EventFactory.createArrayBacked(AfterPlayerChange.class, callbacks -> (player, origin, destination) -> {
        for (AfterPlayerChange callback : callbacks) {
            callback.afterChangeLevel(player, origin, destination);
        }
    });

    private ServerEntityLevelChangeEvents() {
    }

    @FunctionalInterface
    public static interface AfterPlayerChange {
        public void afterChangeLevel(ServerPlayer var1, ServerLevel var2, ServerLevel var3);
    }

    @FunctionalInterface
    public static interface AfterEntityChange {
        public void afterChangeLevel(Entity var1, Entity var2, ServerLevel var3, ServerLevel var4);
    }
}

