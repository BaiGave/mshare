/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={ServerPlayer.class})
public class ServerPlayerMixin
implements PacketContextProvider {
    @Shadow
    public ServerGamePacketListenerImpl connection;

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

