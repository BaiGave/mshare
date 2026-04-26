/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleEvents;
import net.fabricmc.fabric.impl.gamerule.GameRuleEventsImpl;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftServer.class})
public class MinecraftServerMixin {
    @Inject(method={"onGameRuleChanged"}, at={@At(value="RETURN")})
    private <T> void handleGameRuleUpdate(GameRule<T> gameRule, T value, CallbackInfo ci) {
        Event<GameRuleEvents.ValueUpdate<T>> event = GameRuleEventsImpl.getValueUpdate(gameRule);
        if (event != null) {
            event.invoker().onGameRuleUpdated(value, (MinecraftServer)((Object)this));
        }
    }
}

