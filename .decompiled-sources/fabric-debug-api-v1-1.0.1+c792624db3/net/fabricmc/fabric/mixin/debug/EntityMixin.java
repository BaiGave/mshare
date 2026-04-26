/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.debug;

import net.fabricmc.fabric.impl.debug.EntityDebugSubscriptionRegistryImpl;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Entity.class, Mob.class})
abstract class EntityMixin {
    EntityMixin() {
    }

    @Inject(method={"registerDebugValues"}, at={@At(value="HEAD")})
    private void addDebugValues(ServerLevel level, DebugValueSource.Registration registration, CallbackInfo ci) {
        EntityDebugSubscriptionRegistryImpl.addDebugValues(this, registration);
    }
}

