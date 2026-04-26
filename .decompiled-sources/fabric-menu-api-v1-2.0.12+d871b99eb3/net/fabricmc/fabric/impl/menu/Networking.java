/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.menu;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Networking
implements ModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-menu-api-v1/server");
    public static final Identifier OPEN_ID = Identifier.fromNamespaceAndPath("fabric-menu-api-v1", "open_screen");
    public static final Map<Identifier, StreamCodec<? super RegistryFriendlyByteBuf, ?>> CODEC_BY_ID = new HashMap();

    public static <D> void sendOpenPacket(ServerPlayer player, ExtendedMenuProvider<D> factory, AbstractContainerMenu menu, int containerId) {
        Objects.requireNonNull(player, "player is null");
        Objects.requireNonNull(factory, "factory is null");
        Objects.requireNonNull(menu, "menu is null");
        Identifier typeId = BuiltInRegistries.MENU.getKey(menu.getType());
        if (typeId == null) {
            LOGGER.warn("Trying to open unregistered menu {}", (Object)menu);
            return;
        }
        StreamCodec<? super RegistryFriendlyByteBuf, ?> codec = Objects.requireNonNull(CODEC_BY_ID.get(typeId), () -> "Codec for " + String.valueOf(typeId) + " is not registered!");
        D data = factory.getScreenOpeningData(player);
        ServerPlayNetworking.send(player, new OpenScreenPayload(typeId, containerId, factory.getDisplayName(), codec, data));
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.clientboundPlay().register(OpenScreenPayload.ID, OpenScreenPayload.CODEC);
        Networking.forEachEntry(BuiltInRegistries.MENU, (type, id) -> {
            if (type instanceof ExtendedMenuType) {
                ExtendedMenuType extended = (ExtendedMenuType)type;
                CODEC_BY_ID.put((Identifier)id, extended.getStreamCodec());
            }
        });
    }

    private static <T> void forEachEntry(Registry<T> registry, BiConsumer<T, Identifier> consumer) {
        for (Object type2 : registry) {
            consumer.accept(type2, registry.getKey(type2));
        }
        RegistryEntryAddedCallback.event(registry).register((rawId, id, type) -> consumer.accept(type, id));
    }

    public record OpenScreenPayload<D>(Identifier identifier, int containerId, Component title, StreamCodec<RegistryFriendlyByteBuf, D> innerCodec, D data) implements CustomPacketPayload
    {
        public static final StreamCodec<RegistryFriendlyByteBuf, OpenScreenPayload<?>> CODEC = CustomPacketPayload.codec(OpenScreenPayload::write, OpenScreenPayload::fromBuf);
        public static final CustomPacketPayload.Type<OpenScreenPayload<?>> ID = new CustomPacketPayload.Type(OPEN_ID);

        private static <D> OpenScreenPayload<D> fromBuf(RegistryFriendlyByteBuf buf) {
            Identifier id = buf.readIdentifier();
            StreamCodec<? super RegistryFriendlyByteBuf, ?> codec = CODEC_BY_ID.get(id);
            return new OpenScreenPayload<Object>(id, buf.readByte(), (Component)ComponentSerialization.STREAM_CODEC.decode(buf), (StreamCodec<RegistryFriendlyByteBuf, Object>)codec, (codec == null ? null : (D)codec.decode(buf)));
        }

        private void write(RegistryFriendlyByteBuf buf) {
            buf.writeIdentifier(this.identifier);
            buf.writeByte(this.containerId);
            ComponentSerialization.STREAM_CODEC.encode(buf, this.title);
            this.innerCodec.encode(buf, this.data);
        }

        @Override
        public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
            return ID;
        }
    }
}

