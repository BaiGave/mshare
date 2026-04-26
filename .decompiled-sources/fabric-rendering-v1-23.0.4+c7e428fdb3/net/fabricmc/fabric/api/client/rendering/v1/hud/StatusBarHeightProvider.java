/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.hud;

import java.util.function.ToIntFunction;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.ApiStatus;

@FunctionalInterface
public interface StatusBarHeightProvider
extends ToIntFunction<Player> {
    public int getStatusBarHeight(Player var1);

    @Override
    @ApiStatus.NonExtendable
    default public int applyAsInt(Player player) {
        return this.getStatusBarHeight(player);
    }
}

