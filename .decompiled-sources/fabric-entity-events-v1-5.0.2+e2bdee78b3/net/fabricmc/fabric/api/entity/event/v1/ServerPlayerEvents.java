/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;

public final class ServerPlayerEvents {
    public static final Event<CopyFrom> COPY_FROM = EventFactory.createArrayBacked(CopyFrom.class, callbacks -> (oldPlayer, newPlayer, alive) -> {
        for (CopyFrom callback : callbacks) {
            callback.copyFromPlayer(oldPlayer, newPlayer, alive);
        }
    });
    public static final Event<AfterRespawn> AFTER_RESPAWN = EventFactory.createArrayBacked(AfterRespawn.class, callbacks -> (oldPlayer, newPlayer, alive) -> {
        for (AfterRespawn callback : callbacks) {
            callback.afterRespawn(oldPlayer, newPlayer, alive);
        }
    });
    public static final Event<Join> JOIN = EventFactory.createArrayBacked(Join.class, callbacks -> player -> {
        for (Join callback : callbacks) {
            callback.onJoin(player);
        }
    });
    public static final Event<Leave> LEAVE = EventFactory.createArrayBacked(Leave.class, callbacks -> player -> {
        for (Leave callback : callbacks) {
            callback.onLeave(player);
        }
    });
    @Deprecated
    public static final Event<AllowDeath> ALLOW_DEATH = EventFactory.createArrayBacked(AllowDeath.class, callbacks -> (player, damageSource, damageAmount) -> {
        for (AllowDeath callback : callbacks) {
            if (callback.allowDeath(player, damageSource, damageAmount)) continue;
            return false;
        }
        return true;
    });

    private ServerPlayerEvents() {
    }

    static {
        ServerLivingEntityEvents.ALLOW_DEATH.register((entity, damageSource, damageAmount) -> {
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)entity;
                return ALLOW_DEATH.invoker().allowDeath(player, damageSource, damageAmount);
            }
            return true;
        });
    }

    @Deprecated
    @FunctionalInterface
    public static interface AllowDeath {
        public boolean allowDeath(ServerPlayer var1, DamageSource var2, float var3);
    }

    @FunctionalInterface
    public static interface Leave {
        public void onLeave(ServerPlayer var1);
    }

    @FunctionalInterface
    public static interface Join {
        public void onJoin(ServerPlayer var1);
    }

    @FunctionalInterface
    public static interface AfterRespawn {
        public void afterRespawn(ServerPlayer var1, ServerPlayer var2, boolean var3);
    }

    @FunctionalInterface
    public static interface CopyFrom {
        public void copyFromPlayer(ServerPlayer var1, ServerPlayer var2, boolean var3);
    }
}

