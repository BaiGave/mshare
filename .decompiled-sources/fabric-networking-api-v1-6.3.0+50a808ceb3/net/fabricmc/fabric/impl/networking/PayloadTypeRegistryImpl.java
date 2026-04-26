/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import io.netty.buffer.ByteBufUtil;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.IntSupplier;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.ProtocolInfo;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public class PayloadTypeRegistryImpl<B extends FriendlyByteBuf>
implements PayloadTypeRegistry<B> {
    public static final PayloadTypeRegistryImpl<FriendlyByteBuf> SERVERBOUND_CONFIGURATION = new PayloadTypeRegistryImpl(ConnectionProtocol.CONFIGURATION, PacketFlow.SERVERBOUND);
    public static final PayloadTypeRegistryImpl<FriendlyByteBuf> CLIENTBOUND_CONFIGURATION = new PayloadTypeRegistryImpl(ConnectionProtocol.CONFIGURATION, PacketFlow.CLIENTBOUND);
    public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> SERVERBOUND_PLAY = new PayloadTypeRegistryImpl(ConnectionProtocol.PLAY, PacketFlow.SERVERBOUND);
    public static final PayloadTypeRegistryImpl<RegistryFriendlyByteBuf> CLIENTBOUND_PLAY = new PayloadTypeRegistryImpl(ConnectionProtocol.PLAY, PacketFlow.CLIENTBOUND);
    private final Map<Identifier, CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload>> packetTypes = new HashMap<Identifier, CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload>>();
    private final Object2IntMap<Identifier> maxPacketSizes = new Object2IntOpenHashMap<Identifier>();
    private final Object2ObjectMap<Identifier, IntSupplier> pendingMaxPacketSizes = new Object2ObjectOpenHashMap<Identifier, IntSupplier>();
    private final ConnectionProtocol protocol;
    private final PacketFlow flow;
    private final int minimalSplittableSize;

    private PayloadTypeRegistryImpl(ConnectionProtocol protocol, PacketFlow flow) {
        this.protocol = protocol;
        this.flow = flow;
        this.minimalSplittableSize = flow == PacketFlow.CLIENTBOUND ? 0x100000 : Short.MAX_VALUE;
    }

    public static @Nullable PayloadTypeRegistryImpl<?> get(ProtocolInfo<?> state) {
        return switch (state.id()) {
            case ConnectionProtocol.CONFIGURATION -> {
                if (state.flow() == PacketFlow.CLIENTBOUND) {
                    yield CLIENTBOUND_CONFIGURATION;
                }
                yield SERVERBOUND_CONFIGURATION;
            }
            case ConnectionProtocol.PLAY -> {
                if (state.flow() == PacketFlow.CLIENTBOUND) {
                    yield CLIENTBOUND_PLAY;
                }
                yield SERVERBOUND_PLAY;
            }
            default -> null;
        };
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> register(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(codec, "codec");
        CustomPacketPayload.TypeAndCodec payloadType = new CustomPacketPayload.TypeAndCodec(type, codec.cast());
        if (this.packetTypes.containsKey(type.id())) {
            throw new IllegalArgumentException("Packet type " + String.valueOf(type) + " is already registered!");
        }
        this.packetTypes.put(type.id(), payloadType);
        return payloadType;
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec, int maxPacketSize) {
        if (maxPacketSize < 0) {
            throw new IllegalArgumentException("Provided maxPacketSize needs to be positive!");
        }
        CustomPacketPayload.TypeAndCodec<? super B, T> typeAndCodec = this.register(type, codec);
        this.padAndSetMaxPacketSize(type.id(), maxPacketSize);
        return typeAndCodec;
    }

    @Override
    public <T extends CustomPacketPayload> CustomPacketPayload.TypeAndCodec<? super B, T> registerLarge(CustomPacketPayload.Type<T> type, StreamCodec<? super B, T> codec, IntSupplier maxPacketSizeSupplier) {
        Objects.requireNonNull(maxPacketSizeSupplier, "maxPacketSizeSupplier");
        CustomPacketPayload.TypeAndCodec<? super B, T> typeAndCodec = this.register(type, codec);
        this.pendingMaxPacketSizes.put(type.id(), maxPacketSizeSupplier);
        return typeAndCodec;
    }

    private void padAndSetMaxPacketSize(Identifier id, int maxSize) {
        int identifierSize = ByteBufUtil.utf8MaxBytes(id.toString());
        int paddingSize = VarInt.getByteSize(identifierSize) + identifierSize + 10;
        int maxPacketSize = maxSize + paddingSize;
        if (maxPacketSize < 0) {
            maxPacketSize = Integer.MAX_VALUE;
        }
        if (maxPacketSize > this.minimalSplittableSize) {
            this.maxPacketSizes.put(id, maxPacketSize);
        }
    }

    public @Nullable CustomPacketPayload.TypeAndCodec<B, ? extends CustomPacketPayload> get(Identifier id) {
        return this.packetTypes.get(id);
    }

    public <T extends CustomPacketPayload> @Nullable CustomPacketPayload.TypeAndCodec<B, T> get(CustomPacketPayload.Type<T> type) {
        return this.packetTypes.get(type.id());
    }

    public int getMaxPacketSizeForSplitting(Identifier id) {
        IntSupplier supplier = this.pendingMaxPacketSizes.remove(id);
        if (supplier != null) {
            int maxPacketSize = supplier.getAsInt();
            if (maxPacketSize < 0) {
                throw new IllegalArgumentException("maxPacketSize supplier for packet type " + String.valueOf(id) + ": must be positive!");
            }
            this.padAndSetMaxPacketSize(id, maxPacketSize);
        }
        return this.maxPacketSizes.getOrDefault((Object)id, -1);
    }

    public ConnectionProtocol getProtocol() {
        return this.protocol;
    }

    public PacketFlow getFlow() {
        return this.flow;
    }
}

