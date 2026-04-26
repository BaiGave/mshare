/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.threading;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.netty.channel.ChannelHandlerContext;
import net.fabricmc.fabric.impl.client.gametest.threading.NetworkSynchronizer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Connection.class})
public class ConnectionMixin {
    @Shadow
    @Final
    private PacketFlow receiving;

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @WrapMethod(method={"channelRead0(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/protocol/Packet;)V"})
    private void onNettyReceivePacket(ChannelHandlerContext context, Packet<?> packet, Operation<Void> original) {
        NetworkSynchronizer synchronizer = this.receiving == PacketFlow.CLIENTBOUND ? NetworkSynchronizer.CLIENTBOUND : NetworkSynchronizer.SERVERBOUND;
        synchronizer.preNettyHandlePacket();
        try {
            original.call(context, packet);
        }
        finally {
            synchronizer.postNettyHandlePacket();
        }
    }

    @Inject(method={"sendPacket"}, at={@At(value="HEAD")})
    private void onSendPacket(CallbackInfo ci) {
        NetworkSynchronizer synchronizer = this.receiving == PacketFlow.CLIENTBOUND ? NetworkSynchronizer.SERVERBOUND : NetworkSynchronizer.CLIENTBOUND;
        synchronizer.preSendPacket();
    }
}

