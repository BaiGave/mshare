/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import io.netty.util.AsciiString;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.minecraft.IdentifierException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record RegistrationPayload(CustomPacketPayload.Type<RegistrationPayload> type, List<Identifier> channels) implements CustomPacketPayload
{
    public static final CustomPacketPayload.Type<RegistrationPayload> REGISTER = new CustomPacketPayload.Type(NetworkingImpl.REGISTER_CHANNEL);
    public static final CustomPacketPayload.Type<RegistrationPayload> UNREGISTER = new CustomPacketPayload.Type(NetworkingImpl.UNREGISTER_CHANNEL);
    public static final StreamCodec<FriendlyByteBuf, RegistrationPayload> REGISTER_CODEC = RegistrationPayload.codec(REGISTER);
    public static final StreamCodec<FriendlyByteBuf, RegistrationPayload> UNREGISTER_CODEC = RegistrationPayload.codec(UNREGISTER);

    private RegistrationPayload(CustomPacketPayload.Type<RegistrationPayload> id, FriendlyByteBuf buf) {
        this(id, RegistrationPayload.read(buf));
    }

    private void write(FriendlyByteBuf buf) {
        boolean first = true;
        for (Identifier channel : this.channels) {
            if (first) {
                first = false;
            } else {
                buf.writeByte(0);
            }
            buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII));
        }
    }

    private static List<Identifier> read(FriendlyByteBuf buf) {
        ArrayList<Identifier> ids = new ArrayList<Identifier>();
        StringBuilder active = new StringBuilder();
        while (buf.isReadable()) {
            byte b = buf.readByte();
            if (b != 0) {
                active.append(AsciiString.b2c(b));
                continue;
            }
            RegistrationPayload.addId(ids, active);
            active = new StringBuilder();
        }
        RegistrationPayload.addId(ids, active);
        return Collections.unmodifiableList(ids);
    }

    private static void addId(List<Identifier> ids, StringBuilder sb) {
        String literal = sb.toString();
        try {
            ids.add(Identifier.parse(literal));
        }
        catch (IdentifierException ex) {
            NetworkingImpl.LOGGER.warn("Received invalid channel identifier \"{}\"", (Object)literal);
        }
    }

    private static StreamCodec<FriendlyByteBuf, RegistrationPayload> codec(CustomPacketPayload.Type<RegistrationPayload> id) {
        return CustomPacketPayload.codec(RegistrationPayload::write, (B buf) -> new RegistrationPayload(id, (FriendlyByteBuf)buf));
    }
}

