/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public final class DebugMessages {
    public static String forGlobalPos(@Nullable Level level, BlockPos pos) {
        String dimension = level != null ? level.dimensionTypeRegistration().getRegisteredName() : "<no dimension>";
        return dimension + "@" + pos.toShortString();
    }

    public static String forPlayer(Player player) {
        return String.valueOf(player.getDisplayName()) + "/" + player.getStringUUID();
    }

    public static String forInventory(@Nullable Container inventory) {
        if (inventory == null) {
            return "~~NULL~~";
        }
        if (inventory instanceof Inventory) {
            Inventory playerInventory = (Inventory)inventory;
            return DebugMessages.forPlayer(playerInventory.player);
        }
        Object result = inventory.toString();
        if (inventory instanceof BlockEntity) {
            BlockEntity blockEntity = (BlockEntity)((Object)inventory);
            result = (String)result + " (%s, %s)".formatted(blockEntity.getBlockState(), DebugMessages.forGlobalPos(blockEntity.getLevel(), blockEntity.getBlockPos()));
        }
        return result;
    }
}

