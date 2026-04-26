/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.client.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class ClientPlayerBlockBreakEvents {
    public static final Event<After> AFTER = EventFactory.createArrayBacked(After.class, listeners -> (level, player, pos, state) -> {
        for (After event : listeners) {
            event.afterBlockBreak(level, player, pos, state);
        }
    });

    private ClientPlayerBlockBreakEvents() {
    }

    @FunctionalInterface
    public static interface After {
        public void afterBlockBreak(ClientLevel var1, LocalPlayer var2, BlockPos var3, BlockState var4);
    }
}

