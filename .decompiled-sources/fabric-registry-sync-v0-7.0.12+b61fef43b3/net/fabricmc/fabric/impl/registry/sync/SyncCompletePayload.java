/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public class SyncCompletePayload
implements CustomPacketPayload {
    public static final SyncCompletePayload INSTANCE = new SyncCompletePayload();
    public static final CustomPacketPayload.Type<SyncCompletePayload> ID = new CustomPacketPayload.Type(Identifier.fromNamespaceAndPath("fabric", "registry/sync/complete"));
    public static final StreamCodec<FriendlyByteBuf, SyncCompletePayload> CODEC = StreamCodec.unit(INSTANCE);

    private SyncCompletePayload() {
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}

