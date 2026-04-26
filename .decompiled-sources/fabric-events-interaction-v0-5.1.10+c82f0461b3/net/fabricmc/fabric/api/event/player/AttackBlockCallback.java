/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface AttackBlockCallback {
    public static final Event<AttackBlockCallback> EVENT = EventFactory.createArrayBacked(AttackBlockCallback.class, listeners -> (player, level, hand, pos, direction) -> {
        for (AttackBlockCallback event : listeners) {
            InteractionResult result = event.interact(player, level, hand, pos, direction);
            if (result == InteractionResult.PASS) continue;
            return result;
        }
        return InteractionResult.PASS;
    });

    public InteractionResult interact(Player var1, Level var2, InteractionHand var3, BlockPos var4, Direction var5);
}

