/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.client;

import java.util.Collections;
import net.fabricmc.fabric.impl.networking.AbstractChanneledNetworkAddon;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;

abstract class ClientCommonNetworkAddon<H, T extends ClientCommonPacketListenerImpl>
extends AbstractChanneledNetworkAddon<H> {
    protected final T listener;
    protected final Minecraft client;
    protected boolean isServerReady = false;

    protected ClientCommonNetworkAddon(GlobalReceiverRegistry<H> receiver, Connection connection, String description, T listener, Minecraft client) {
        super(receiver, connection, description);
        this.listener = listener;
        this.client = client;
    }

    public void onServerReady() {
        this.isServerReady = true;
    }

    @Override
    protected void handleRegistration(Identifier channelName) {
        RegistrationPayload payload;
        if (this.isServerReady && (payload = this.createRegistrationPayload(RegistrationPayload.REGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(payload);
        }
    }

    @Override
    protected void handleUnregistration(Identifier channelName) {
        RegistrationPayload payload;
        if (this.isServerReady && (payload = this.createRegistrationPayload(RegistrationPayload.UNREGISTER, Collections.singleton(channelName))) != null) {
            this.sendPacket(payload);
        }
    }

    @Override
    protected boolean isReservedChannel(Identifier channelName) {
        return NetworkingImpl.isReservedCommonChannel(channelName);
    }

    @Override
    protected void schedule(Runnable task) {
        this.client.execute(task);
    }
}

