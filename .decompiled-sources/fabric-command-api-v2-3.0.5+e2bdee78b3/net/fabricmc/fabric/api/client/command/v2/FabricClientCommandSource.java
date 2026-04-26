/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.command.v2;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public interface FabricClientCommandSource
extends SharedSuggestionProvider {
    public void sendFeedback(Component var1);

    public void sendError(Component var1);

    public Minecraft getClient();

    public LocalPlayer getPlayer();

    default public Entity getEntity() {
        return this.getPlayer();
    }

    default public Vec3 getPosition() {
        return this.getPlayer().position();
    }

    default public Vec2 getRotation() {
        return this.getPlayer().getRotationVector();
    }

    public ClientLevel getLevel();

    default public @Nullable Object getMeta(String key) {
        return null;
    }
}

