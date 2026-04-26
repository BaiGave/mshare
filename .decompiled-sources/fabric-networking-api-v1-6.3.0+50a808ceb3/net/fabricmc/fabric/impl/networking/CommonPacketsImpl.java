/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.Arrays;
import java.util.function.Consumer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.CommonVersionPayload;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.server.ServerConfigurationNetworkAddon;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.network.ConfigurationTask;

public class CommonPacketsImpl {
    public static final int PACKET_VERSION_1 = 1;
    public static final int[] SUPPORTED_COMMON_PACKET_VERSIONS = new int[]{1};

    public static void init() {
        PayloadTypeRegistry.serverboundConfiguration().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CommonVersionPayload.TYPE, CommonVersionPayload.CODEC);
        PayloadTypeRegistry.serverboundConfiguration().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(CommonRegisterPayload.TYPE, CommonRegisterPayload.CODEC);
        ServerConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.TYPE, (payload, context) -> {
            ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.packetListener());
            addon.onCommonVersionPacket(CommonPacketsImpl.getNegotiatedVersion(payload));
            context.packetListener().completeTask(CommonVersionConfigurationTask.KEY);
        });
        ServerConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.TYPE, (payload, context) -> {
            ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(context.packetListener());
            if ("play".equals(payload.protocol())) {
                if (payload.version() != addon.getNegotiatedVersion()) {
                    throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
                }
                addon.getChannelInfoHolder().fabric_getPendingChannelsNames(ConnectionProtocol.PLAY).addAll(payload.channels());
                NetworkingImpl.LOGGER.debug("Received accepted channels from the client for play phase");
            } else {
                addon.onCommonRegisterPacket((CommonRegisterPayload)payload);
            }
            context.packetListener().completeTask(CommonRegisterConfigurationTask.KEY);
        });
        ServerConfigurationConnectionEvents.CONFIGURE.register((listener, server) -> {
            ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(listener);
            if (ServerConfigurationNetworking.canSend(listener, CommonVersionPayload.TYPE)) {
                listener.addTask(new CommonVersionConfigurationTask(addon));
                if (ServerConfigurationNetworking.canSend(listener, CommonRegisterPayload.TYPE)) {
                    listener.addTask(new CommonRegisterConfigurationTask(addon));
                }
            }
        });
    }

    private static int getNegotiatedVersion(CommonVersionPayload payload) {
        int version = CommonPacketsImpl.getHighestCommonVersion(payload.versions(), SUPPORTED_COMMON_PACKET_VERSIONS);
        if (version <= 0) {
            throw new UnsupportedOperationException("server does not support any requested versions from client");
        }
        return version;
    }

    public static int getHighestCommonVersion(int[] a, int[] b) {
        int[] as = (int[])a.clone();
        int[] bs = (int[])b.clone();
        Arrays.sort(as);
        Arrays.sort(bs);
        int ap = as.length - 1;
        int bp = bs.length - 1;
        while (ap >= 0 && bp >= 0) {
            if (as[ap] == bs[bp]) {
                return as[ap];
            }
            if (as[ap] > bs[bp]) {
                --ap;
                continue;
            }
            --bp;
        }
        return -1;
    }

    private record CommonVersionConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask
    {
        public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type(CommonVersionPayload.TYPE.id().toString());

        @Override
        public void start(Consumer<Packet<?>> sender) {
            this.addon.sendPacket(new CommonVersionPayload(SUPPORTED_COMMON_PACKET_VERSIONS));
        }

        @Override
        public ConfigurationTask.Type type() {
            return KEY;
        }
    }

    private record CommonRegisterConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask
    {
        public static final ConfigurationTask.Type KEY = new ConfigurationTask.Type(CommonRegisterPayload.TYPE.id().toString());

        @Override
        public void start(Consumer<Packet<?>> sender) {
            this.addon.sendPacket(new CommonRegisterPayload(this.addon.getNegotiatedVersion(), "play", ServerPlayNetworking.getGlobalReceivers()));
        }

        @Override
        public ConfigurationTask.Type type() {
            return KEY;
        }
    }
}

