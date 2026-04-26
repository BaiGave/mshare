/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record CommonRegisterPayload(int version, String protocol, Set<Identifier> channels) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<CommonRegisterPayload> TYPE = new CustomPacketPayload.Type(Identifier.parse("c:register"));
    public static final StreamCodec<FriendlyByteBuf, CommonRegisterPayload> CODEC = CustomPacketPayload.codec(CommonRegisterPayload::write, CommonRegisterPayload::new);
    public static final String PLAY_PROTOCOL = "play";
    public static final String CONFIGURATION_PROTOCOL = "configuration";

    private CommonRegisterPayload(FriendlyByteBuf buf) {
        this(buf.readVarInt(), buf.readUtf(), buf.readCollection(HashSet::new, FriendlyByteBuf::readIdentifier));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(this.version);
        buf.writeUtf(this.protocol);
        buf.writeCollection(this.channels, FriendlyByteBuf::writeIdentifier);
    }

    public CustomPacketPayload.Type<CommonRegisterPayload> type() {
        return TYPE;
    }
}

