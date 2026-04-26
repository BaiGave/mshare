/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class PlayerPickItemEvents {
    public static final Event<PickItemFromBlock> BLOCK = EventFactory.createArrayBacked(PickItemFromBlock.class, callbacks -> (player, pos, state, includeData) -> {
        for (PickItemFromBlock callback : callbacks) {
            ItemStack stack = callback.onPickItemFromBlock(player, pos, state, includeData);
            if (stack == null) continue;
            return stack;
        }
        return null;
    });
    public static final Event<PickItemFromEntity> ENTITY = EventFactory.createArrayBacked(PickItemFromEntity.class, callbacks -> (player, entity, includeData) -> {
        for (PickItemFromEntity callback : callbacks) {
            ItemStack stack = callback.onPickItemFromEntity(player, entity, includeData);
            if (stack == null) continue;
            return stack;
        }
        return null;
    });

    private PlayerPickItemEvents() {
    }

    @FunctionalInterface
    public static interface PickItemFromEntity {
        public @Nullable ItemStack onPickItemFromEntity(ServerPlayer var1, Entity var2, boolean var3);
    }

    @FunctionalInterface
    public static interface PickItemFromBlock {
        public @Nullable ItemStack onPickItemFromBlock(ServerPlayer var1, BlockPos var2, BlockState var3, boolean var4);
    }
}

