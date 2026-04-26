/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.event.interaction;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.block.BlockAttackInteractionAware;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.block.state.BlockState;

public class InteractionEventsRouter
implements ModInitializer {
    @Override
    public void onInitialize() {
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            BlockState state = level.getBlockState(pos);
            if (state instanceof BlockAttackInteractionAware ? ((BlockAttackInteractionAware)((Object)state)).onAttackInteraction(state, level, pos, player, hand, direction) : state.getBlock() instanceof BlockAttackInteractionAware && ((BlockAttackInteractionAware)((Object)state.getBlock())).onAttackInteraction(state, level, pos, player, hand, direction)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });
        PlayerBlockBreakEvents.CANCELED.register((level, player, pos, state, blockEntity) -> {
            BlockPos cornerPos = pos.offset(-1, -1, -1);
            for (int x = 0; x < 3; ++x) {
                for (int y = 0; y < 3; ++y) {
                    for (int z = 0; z < 3; ++z) {
                        ((ServerPlayer)player).connection.send(new ClientboundBlockUpdatePacket(level, cornerPos.offset(x, y, z)));
                    }
                }
            }
        });
    }
}

