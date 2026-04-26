/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class PlayerBlockBreakEvents {
    public static final Event<Before> BEFORE = EventFactory.createArrayBacked(Before.class, listeners -> (level, player, pos, state, entity) -> {
        for (Before event : listeners) {
            boolean result = event.beforeBlockBreak(level, player, pos, state, entity);
            if (result) continue;
            return false;
        }
        return true;
    });
    public static final Event<After> AFTER = EventFactory.createArrayBacked(After.class, listeners -> (level, player, pos, state, entity) -> {
        for (After event : listeners) {
            event.afterBlockBreak(level, player, pos, state, entity);
        }
    });
    public static final Event<Canceled> CANCELED = EventFactory.createArrayBacked(Canceled.class, listeners -> (level, player, pos, state, entity) -> {
        for (Canceled event : listeners) {
            event.onBlockBreakCanceled(level, player, pos, state, entity);
        }
    });

    private PlayerBlockBreakEvents() {
    }

    @FunctionalInterface
    public static interface Canceled {
        public void onBlockBreakCanceled(Level var1, Player var2, BlockPos var3, BlockState var4, @Nullable BlockEntity var5);
    }

    @FunctionalInterface
    public static interface After {
        public void afterBlockBreak(Level var1, Player var2, BlockPos var3, BlockState var4, @Nullable BlockEntity var5);
    }

    @FunctionalInterface
    public static interface Before {
        public boolean beforeBlockBreak(Level var1, Player var2, BlockPos var3, BlockState var4, @Nullable BlockEntity var5);
    }
}

