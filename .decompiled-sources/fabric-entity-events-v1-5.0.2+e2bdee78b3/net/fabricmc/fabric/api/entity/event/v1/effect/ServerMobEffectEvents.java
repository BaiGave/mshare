/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1.effect;

import net.fabricmc.fabric.api.entity.event.v1.effect.EffectEventContext;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

public final class ServerMobEffectEvents {
    public static final Event<AllowAdd> ALLOW_ADD = EventFactory.createArrayBacked(AllowAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (AllowAdd callback : callbacks) {
            if (callback.allowAdd(effectInstance, entity, ctx)) continue;
            return false;
        }
        return true;
    });
    public static final Event<BeforeAdd> BEFORE_ADD = EventFactory.createArrayBacked(BeforeAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (BeforeAdd callback : callbacks) {
            callback.beforeAdd(effectInstance, entity, ctx);
        }
    });
    public static final Event<AfterAdd> AFTER_ADD = EventFactory.createArrayBacked(AfterAdd.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (AfterAdd callback : callbacks) {
            callback.afterAdd(effectInstance, entity, ctx);
        }
    });
    public static final Event<AllowEarlyRemove> ALLOW_EARLY_REMOVE = EventFactory.createArrayBacked(AllowEarlyRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (AllowEarlyRemove callback : callbacks) {
            if (callback.allowEarlyRemove(effectInstance, entity, ctx)) continue;
            return false;
        }
        return true;
    });
    public static final Event<BeforeRemove> BEFORE_REMOVE = EventFactory.createArrayBacked(BeforeRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (BeforeRemove callback : callbacks) {
            callback.beforeRemove(effectInstance, entity, ctx);
        }
    });
    public static final Event<AfterRemove> AFTER_REMOVE = EventFactory.createArrayBacked(AfterRemove.class, callbacks -> (effectInstance, entity, ctx) -> {
        for (AfterRemove callback : callbacks) {
            callback.afterRemove(effectInstance, entity, ctx);
        }
    });

    private ServerMobEffectEvents() {
    }

    static {
        BEFORE_ADD.register((effectInstance, entity, ctx) -> effectInstance.getEffect().value().onEffectAdded(effectInstance, entity));
        AFTER_ADD.register((effectInstance, entity, ctx) -> effectInstance.getEffect().value().onEffectStarted(effectInstance, entity));
        BEFORE_REMOVE.register((effectInstance, entity, ctx) -> effectInstance.getEffect().value().onEffectRemoved(effectInstance, entity));
    }

    @FunctionalInterface
    public static interface AfterRemove {
        public void afterRemove(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }

    @FunctionalInterface
    public static interface BeforeRemove {
        public void beforeRemove(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }

    @FunctionalInterface
    public static interface AllowEarlyRemove {
        public boolean allowEarlyRemove(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }

    @FunctionalInterface
    public static interface AfterAdd {
        public void afterAdd(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }

    @FunctionalInterface
    public static interface BeforeAdd {
        public void beforeAdd(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }

    @FunctionalInterface
    public static interface AllowAdd {
        public boolean allowAdd(MobEffectInstance var1, LivingEntity var2, EffectEventContext var3);
    }
}

