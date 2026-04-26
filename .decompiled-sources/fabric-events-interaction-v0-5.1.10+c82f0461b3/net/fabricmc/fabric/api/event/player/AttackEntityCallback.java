/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jspecify.annotations.Nullable;

public interface AttackEntityCallback {
    public static final Event<AttackEntityCallback> EVENT = EventFactory.createArrayBacked(AttackEntityCallback.class, listeners -> (player, level, hand, entity, hitResult) -> {
        for (AttackEntityCallback event : listeners) {
            InteractionResult result = event.interact(player, level, hand, entity, hitResult);
            if (result == InteractionResult.PASS) continue;
            return result;
        }
        return InteractionResult.PASS;
    });

    public InteractionResult interact(Player var1, Level var2, InteractionHand var3, Entity var4, @Nullable EntityHitResult var5);
}

