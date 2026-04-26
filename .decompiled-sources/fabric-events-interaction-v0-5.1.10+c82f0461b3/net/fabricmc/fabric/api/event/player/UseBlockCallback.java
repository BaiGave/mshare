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
import net.minecraft.world.phys.BlockHitResult;

public interface UseBlockCallback {
    public static final Event<UseBlockCallback> EVENT = EventFactory.createArrayBacked(UseBlockCallback.class, listeners -> (player, level, hand, hitResult) -> {
        for (UseBlockCallback event : listeners) {
            InteractionResult result = event.interact(player, level, hand, hitResult);
            if (result == InteractionResult.PASS) continue;
            return result;
        }
        return InteractionResult.PASS;
    });

    public InteractionResult interact(Player var1, Level var2, InteractionHand var3, BlockHitResult var4);
}

