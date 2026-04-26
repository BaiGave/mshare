/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public interface ItemEvents {
    public static final Event<UseOnCallback> USE_ON = EventFactory.createArrayBacked(UseOnCallback.class, listeners -> useOnContext -> {
        for (UseOnCallback event : listeners) {
            InteractionResult result = event.useOn(useOnContext);
            if (result == null) continue;
            return result;
        }
        return null;
    });
    public static final Event<UseCallback> USE = EventFactory.createArrayBacked(UseCallback.class, listeners -> (level, player, interactionHand) -> {
        for (UseCallback event : listeners) {
            InteractionResult result = event.use(level, player, interactionHand);
            if (result == null) continue;
            return result;
        }
        return null;
    });

    @FunctionalInterface
    public static interface UseCallback {
        public @Nullable InteractionResult use(Level var1, Player var2, InteractionHand var3);
    }

    @FunctionalInterface
    public static interface UseOnCallback {
        public @Nullable InteractionResult useOn(UseOnContext var1);
    }
}

