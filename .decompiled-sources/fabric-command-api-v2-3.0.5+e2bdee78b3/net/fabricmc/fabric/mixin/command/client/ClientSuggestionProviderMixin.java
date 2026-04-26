/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command.client;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ClientSuggestionProvider.class})
abstract class ClientSuggestionProviderMixin
implements FabricClientCommandSource {
    @Shadow
    @Final
    private Minecraft minecraft;

    ClientSuggestionProviderMixin() {
    }

    @Override
    public void sendFeedback(Component message) {
        this.minecraft.gui.getChat().addClientSystemMessage(message);
        this.minecraft.getNarrator().saySystemChatQueued(message);
    }

    @Override
    public void sendError(Component message) {
        this.sendFeedback(Component.empty().append(message).withStyle(ChatFormatting.RED));
    }

    @Override
    public Minecraft getClient() {
        return this.minecraft;
    }

    @Override
    public LocalPlayer getPlayer() {
        return this.minecraft.player;
    }

    @Override
    public ClientLevel getLevel() {
        return this.minecraft.level;
    }
}

