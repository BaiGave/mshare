/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.entity.event.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public final class ServerLivingEntityEvents {
    public static final Event<AllowDamage> ALLOW_DAMAGE = EventFactory.createArrayBacked(AllowDamage.class, callbacks -> (entity, source, amount) -> {
        for (AllowDamage callback : callbacks) {
            if (callback.allowDamage(entity, source, amount)) continue;
            return false;
        }
        return true;
    });
    public static final Event<AfterDamage> AFTER_DAMAGE = EventFactory.createArrayBacked(AfterDamage.class, callbacks -> (entity, source, baseDamageTaken, damageTaken, blocked) -> {
        for (AfterDamage callback : callbacks) {
            callback.afterDamage(entity, source, baseDamageTaken, damageTaken, blocked);
        }
    });
    public static final Event<AllowDeath> ALLOW_DEATH = EventFactory.createArrayBacked(AllowDeath.class, callbacks -> (entity, damageSource, damageAmount) -> {
        for (AllowDeath callback : callbacks) {
            if (callback.allowDeath(entity, damageSource, damageAmount)) continue;
            return false;
        }
        return true;
    });
    public static final Event<AfterDeath> AFTER_DEATH = EventFactory.createArrayBacked(AfterDeath.class, callbacks -> (entity, damageSource) -> {
        for (AfterDeath callback : callbacks) {
            callback.afterDeath(entity, damageSource);
        }
    });
    public static final Event<MobConversion> MOB_CONVERSION = EventFactory.createArrayBacked(MobConversion.class, callbacks -> (previous, converted, keepEquipment) -> {
        for (MobConversion callback : callbacks) {
            callback.onConversion(previous, converted, keepEquipment);
        }
    });

    private ServerLivingEntityEvents() {
    }

    @FunctionalInterface
    public static interface MobConversion {
        public void onConversion(Mob var1, Mob var2, ConversionParams var3);
    }

    @FunctionalInterface
    public static interface AfterDeath {
        public void afterDeath(LivingEntity var1, DamageSource var2);
    }

    @FunctionalInterface
    public static interface AllowDeath {
        public boolean allowDeath(LivingEntity var1, DamageSource var2, float var3);
    }

    @FunctionalInterface
    public static interface AfterDamage {
        public void afterDamage(LivingEntity var1, DamageSource var2, float var3, float var4, boolean var5);
    }

    @FunctionalInterface
    public static interface AllowDamage {
        public boolean allowDamage(LivingEntity var1, DamageSource var2, float var3);
    }
}

