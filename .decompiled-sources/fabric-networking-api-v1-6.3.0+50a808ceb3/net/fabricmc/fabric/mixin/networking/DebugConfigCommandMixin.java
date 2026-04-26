/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.minecraft.server.commands.DebugConfigCommand;
import net.minecraft.server.network.ServerConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={DebugConfigCommand.class})
public class DebugConfigCommandMixin {
    @Redirect(method={"unconfig"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/network/ServerConfigurationPacketListenerImpl;returnToWorld()V"))
    private static void sendConfigurations(ServerConfigurationPacketListenerImpl packetListener) {
        packetListener.startConfiguration();
    }
}

