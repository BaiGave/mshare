/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.ArrayList;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.PacketType;
import net.minecraft.network.protocol.configuration.ConfigurationProtocols;
import net.minecraft.network.protocol.game.GameProtocols;
import org.jspecify.annotations.Nullable;

public record VanillaPacketTypes(PacketType<?>[] types) {
    public static final VanillaPacketTypes PLAY_S2C = VanillaPacketTypes.of(GameProtocols.CLIENTBOUND_TEMPLATE);
    public static final VanillaPacketTypes PLAY_C2S = VanillaPacketTypes.of(GameProtocols.SERVERBOUND_TEMPLATE);
    public static final VanillaPacketTypes CONFIGURATION_S2C = VanillaPacketTypes.of(ConfigurationProtocols.CLIENTBOUND_TEMPLATE);
    public static final VanillaPacketTypes CONFIGURATION_C2S = VanillaPacketTypes.of(ConfigurationProtocols.SERVERBOUND_TEMPLATE);

    public @Nullable PacketType<?> get(int id) {
        return id > 0 && id < this.types.length ? this.types[id] : null;
    }

    private static VanillaPacketTypes of(ProtocolInfo.DetailsProvider factory) {
        ArrayList list = new ArrayList();
        factory.details().listPackets((type, i) -> list.add(type));
        return new VanillaPacketTypes((PacketType[])list.toArray(PacketType[]::new));
    }

    public static VanillaPacketTypes get(ProtocolInfo<?> protocolInfo) {
        return switch (protocolInfo.id()) {
            case ConnectionProtocol.CONFIGURATION -> {
                if (protocolInfo.flow() == PacketFlow.CLIENTBOUND) {
                    yield CONFIGURATION_S2C;
                }
                yield CONFIGURATION_C2S;
            }
            case ConnectionProtocol.PLAY -> {
                if (protocolInfo.flow() == PacketFlow.CLIENTBOUND) {
                    yield PLAY_S2C;
                }
                yield PLAY_C2S;
            }
            default -> throw new IllegalArgumentException("Not implemented for " + String.valueOf((Object)protocolInfo.id()) + "!");
        };
    }
}

