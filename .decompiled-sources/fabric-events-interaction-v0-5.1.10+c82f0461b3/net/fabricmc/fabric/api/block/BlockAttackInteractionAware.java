/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Deprecated
public interface BlockAttackInteractionAware {
    public boolean onAttackInteraction(BlockState var1, Level var2, BlockPos var3, Player var4, InteractionHand var5, Direction var6);
}

