/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1;

import net.minecraft.server.network.ConfigurationTask;

public interface FabricServerConfigurationPacketListenerImpl {
    default public void addTask(ConfigurationTask task) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public void completeTask(ConfigurationTask.Type key) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

