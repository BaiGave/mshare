/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.network.Connection;
import net.minecraft.server.network.ServerHandshakePacketListenerImpl;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ServerHandshakePacketListenerImpl.class})
abstract class ServerHandshakePacketListenerImplMixin
implements PacketContextProvider {
    @Shadow
    @Final
    private Connection connection;

    ServerHandshakePacketListenerImplMixin() {
    }

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

