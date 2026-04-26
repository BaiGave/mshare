/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.registry.sync;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.client.registry.sync.ClientRegistrySyncHandler;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FabricRegistryClientInit
implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FabricRegistryClientInit.class);

    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(RegistrySyncPayload.ID, ClientRegistrySyncHandler::receivePacket);
    }
}

