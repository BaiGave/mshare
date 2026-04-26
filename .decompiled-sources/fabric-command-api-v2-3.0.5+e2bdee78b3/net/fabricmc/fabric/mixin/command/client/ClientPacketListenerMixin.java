/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.impl.command.client.ClientCommandInternals;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientPacketListener.class})
abstract class ClientPacketListenerMixin
implements ClientCommandInternals.LastReceivedCommandsPacketAccessor {
    @Shadow
    private CommandDispatcher<SharedSuggestionProvider> commands;
    @Shadow
    @Final
    private ClientSuggestionProvider suggestionsProvider;
    @Final
    @Shadow
    private FeatureFlagSet enabledFeatures;
    @Final
    @Shadow
    private RegistryAccess.Frozen registryAccess;
    @Unique
    private @Nullable ClientboundCommandsPacket lastReceivedCommandsPacket = null;

    ClientPacketListenerMixin() {
    }

    @Inject(method={"handleLogin"}, at={@At(value="RETURN")})
    private void onGameJoin(ClientboundLoginPacket packet, CallbackInfo info) {
        CommandDispatcher<FabricClientCommandSource> dispatcher = new CommandDispatcher<FabricClientCommandSource>();
        ClientCommandInternals.setActiveDispatcher(dispatcher);
        ClientCommandRegistrationCallback.EVENT.invoker().register(dispatcher, CommandBuildContext.simple(this.registryAccess, this.enabledFeatures));
        ClientCommandInternals.finalizeInit();
    }

    @Inject(method={"handleCommands"}, at={@At(value="RETURN")})
    private void onOnCommandTree(ClientboundCommandsPacket packet, CallbackInfo info) {
        ClientCommandInternals.addCommands(this.commands, (FabricClientCommandSource)((Object)this.suggestionsProvider));
    }

    @Inject(method={"handleCommands"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/protocol/PacketUtils;ensureRunningOnSameThread(Lnet/minecraft/network/protocol/Packet;Lnet/minecraft/network/PacketListener;Lnet/minecraft/network/PacketProcessor;)V", shift=At.Shift.AFTER)})
    private void setLastReceivedCommandsPacket(ClientboundCommandsPacket packet, CallbackInfo ci) {
        this.lastReceivedCommandsPacket = packet;
    }

    @Inject(method={"sendUnattendedCommand"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendCommand(String command, Screen screen, CallbackInfo info) {
        if (ClientCommandInternals.executeCommand(command)) {
            info.cancel();
        }
    }

    @Inject(method={"sendCommand"}, at={@At(value="HEAD")}, cancellable=true)
    private void onSendCommand(String command, CallbackInfo info) {
        if (ClientCommandInternals.executeCommand(command)) {
            info.cancel();
        }
    }

    @Override
    public @Nullable ClientboundCommandsPacket fabric_api$getLastReceivedCommandsPacket() {
        return this.lastReceivedCommandsPacket;
    }
}

