/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Set;
import java.util.function.Consumer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.impl.recipe.ingredient.ClientboundCustomIngredientPayload;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.ServerboundCustomIngredientPayload;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.Identifier;
import net.minecraft.server.network.ConfigurationTask;

public class CustomIngredientSync
implements ModInitializer {
    public static final Identifier PACKET_ID = Identifier.fromNamespaceAndPath("fabric", "custom_ingredient_sync");
    public static final int PROTOCOL_VERSION_1 = 1;
    public static final PacketContext.Key<Set<Identifier>> SUPPORTED_CUSTOM_INGREDIENTS = PacketContext.key(Identifier.fromNamespaceAndPath("fabric", "supported_custom_ingredients"));

    public static ServerboundCustomIngredientPayload createResponsePayload(int serverProtocolVersion) {
        if (serverProtocolVersion < 1) {
            return null;
        }
        return new ServerboundCustomIngredientPayload(1, CustomIngredientImpl.REGISTERED_SERIALIZERS.keySet());
    }

    public static Set<Identifier> decodeResponsePayload(ServerboundCustomIngredientPayload payload) {
        int protocolVersion = payload.protocolVersion();
        switch (protocolVersion) {
            case 1: {
                Set<Identifier> serializers = payload.registeredSerializers();
                serializers.removeIf(id -> !CustomIngredientImpl.REGISTERED_SERIALIZERS.containsKey(id));
                return serializers;
            }
        }
        throw new IllegalArgumentException("Unknown ingredient sync protocol version: " + protocolVersion);
    }

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundCustomIngredientPayload.TYPE, ServerboundCustomIngredientPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(ClientboundCustomIngredientPayload.TYPE, ClientboundCustomIngredientPayload.CODEC);
        ServerConfigurationConnectionEvents.CONFIGURE.register((handler, server) -> {
            if (ServerConfigurationNetworking.canSend(handler, PACKET_ID)) {
                handler.addTask(new IngredientSyncTask());
            }
        });
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundCustomIngredientPayload.TYPE, (payload, context) -> {
            Set<Identifier> supportedCustomIngredients = CustomIngredientSync.decodeResponsePayload(payload);
            context.packetListener().getPacketContext().set(SUPPORTED_CUSTOM_INGREDIENTS, supportedCustomIngredients);
            context.packetListener().completeTask(IngredientSyncTask.KEY);
        });
    }

    private record IngredientSyncTask() implements ConfigurationTask
    {
        public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type(PACKET_ID.toString());

        @Override
        public void start(Consumer<Packet<?>> sender) {
            sender.accept(ServerConfigurationNetworking.createClientboundPacket(new ClientboundCustomIngredientPayload(1)));
        }

        @Override
        public ConfigurationTask.Type type() {
            return KEY;
        }
    }
}

