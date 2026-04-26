/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.client.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public interface ClientPreAttackCallback {
    public static final Event<ClientPreAttackCallback> EVENT = EventFactory.createArrayBacked(ClientPreAttackCallback.class, listeners -> (client, player, clickCount) -> {
        for (ClientPreAttackCallback event : listeners) {
            if (!event.onClientPlayerPreAttack(client, player, clickCount)) continue;
            return true;
        }
        return false;
    });

    public boolean onClientPlayerPreAttack(Minecraft var1, LocalPlayer var2, int var3);
}

