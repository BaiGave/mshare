/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event.effect;

import net.fabricmc.fabric.api.entity.event.v1.effect.FabricMobEffect;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={MobEffect.class})
public final class MobEffectMixin
implements FabricMobEffect {
    private MobEffectMixin() {
    }
}

