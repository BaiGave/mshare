/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.fabricmc.fabric.impl.networking.CustomPayloadTypeProvider;
import net.minecraft.network.FriendlyByteBuf;

public interface FabricCustomPayloadStreamCodec<B extends FriendlyByteBuf> {
    public void fabric_setCustomPayloadTypeProvider(CustomPayloadTypeProvider<B> var1);
}

