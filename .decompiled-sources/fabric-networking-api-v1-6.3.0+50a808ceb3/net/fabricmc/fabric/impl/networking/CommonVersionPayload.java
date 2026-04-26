/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CommonVersionPayload(int[] versions) implements CustomPacketPayload
{
    public static final StreamCodec<FriendlyByteBuf, CommonVersionPayload> CODEC = CustomPacketPayload.codec(CommonVersionPayload::write, CommonVersionPayload::new);
    public static final CustomPacketPayload.Type<CommonVersionPayload> TYPE = new CustomPacketPayload.Type(Identifier.parse("c:version"));

    private CommonVersionPayload(FriendlyByteBuf buf) {
        this(buf.readVarIntArray());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarIntArray(this.versions);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}

