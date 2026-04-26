/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface UseItemCallback {
    public static final Event<UseItemCallback> EVENT = EventFactory.createArrayBacked(UseItemCallback.class, listeners -> (player, level, hand) -> {
        for (UseItemCallback event : listeners) {
            InteractionResult result = event.interact(player, level, hand);
            if (result == InteractionResult.PASS) continue;
            return result;
        }
        return InteractionResult.PASS;
    });

    public InteractionResult interact(Player var1, Level var2, InteractionHand var3);
}

