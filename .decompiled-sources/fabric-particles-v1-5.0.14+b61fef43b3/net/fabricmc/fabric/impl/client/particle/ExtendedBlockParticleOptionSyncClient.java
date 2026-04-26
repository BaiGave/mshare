/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.particle;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.particle.ExtendedBlockParticleOptionSync;

public class ExtendedBlockParticleOptionSyncClient
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ExtendedBlockParticleOptionSync.DummyPayload.ID, (dummyPayload, context) -> {});
    }
}

