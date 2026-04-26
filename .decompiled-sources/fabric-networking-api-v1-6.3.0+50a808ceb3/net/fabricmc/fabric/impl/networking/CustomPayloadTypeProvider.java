/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public interface CustomPayloadTypeProvider<B extends FriendlyByteBuf> {
    public CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload> get(B var1, Identifier var2);
}

