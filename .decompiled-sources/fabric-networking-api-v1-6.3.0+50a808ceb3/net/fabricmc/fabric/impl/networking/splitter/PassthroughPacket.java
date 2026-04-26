/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.splitter;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.resources.Identifier;

public record PassthroughPacket(ByteBuf buf) implements Packet<PacketListener>
{
    private static final PacketType<? extends Packet<PacketListener>> FAKE_TYPE = new PacketType(PacketFlow.SERVERBOUND, Identifier.fromNamespaceAndPath("fabric-networking-api-v1", "passthrough"));

    @Override
    public PacketType<? extends Packet<PacketListener>> type() {
        return FAKE_TYPE;
    }

    @Override
    public void handle(PacketListener listener) {
        throw new UnsupportedOperationException("This is not a real packet!");
    }
}

