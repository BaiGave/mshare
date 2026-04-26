/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.entity.event;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.portal.TeleportTransition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Entity.class})
abstract class EntityMixin {
    @Shadow
    private Level level;

    EntityMixin() {
    }

    @WrapOperation(method={"teleport"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/entity/Entity;teleportCrossDimension(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/portal/TeleportTransition;)Lnet/minecraft/world/entity/Entity;")})
    private Entity afterDimensionChanged(Entity instance, ServerLevel sourceLevel, ServerLevel targetWorld, TeleportTransition teleportTransition, Operation<Entity> original) {
        Entity ret = original.call(instance, sourceLevel, targetWorld, teleportTransition);
        if (ret != null) {
            ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.invoker().afterChangeLevel((Entity)((Object)this), ret, (ServerLevel)this.level, (ServerLevel)ret.level());
        }
        return ret;
    }
}

