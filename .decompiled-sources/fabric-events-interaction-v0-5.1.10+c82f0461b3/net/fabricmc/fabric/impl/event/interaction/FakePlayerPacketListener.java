/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.event.interaction;

import io.netty.channel.ChannelFutureListener;
import net.fabricmc.fabric.impl.networking.UntrackedPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.jspecify.annotations.Nullable;

public final class FakePlayerPacketListener
extends ServerGamePacketListenerImpl
implements UntrackedPacketListener {
    private static final Connection FAKE_CONNECTION = new FakeConnection();

    public FakePlayerPacketListener(ServerPlayer player) {
        super(player.level().getServer(), FAKE_CONNECTION, player, CommonListenerCookie.createInitial(player.getGameProfile(), false));
    }

    @Override
    public void send(Packet<?> packet, @Nullable ChannelFutureListener callbacks) {
    }

    private static final class FakeConnection
    extends Connection {
        private FakeConnection() {
            super(PacketFlow.CLIENTBOUND);
        }
    }
}

