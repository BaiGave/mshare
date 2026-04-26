/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import io.netty.channel.ChannelFutureListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.ChannelInfoHolder;
import net.fabricmc.fabric.impl.networking.CommonPacketHandler;
import net.fabricmc.fabric.impl.networking.CommonRegisterPayload;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.RegistrationPayload;
import net.minecraft.network.Connection;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.RunningOnDifferentThreadException;
import org.jspecify.annotations.Nullable;

public abstract class AbstractChanneledNetworkAddon<H>
extends AbstractNetworkAddon<H>
implements PacketSender,
CommonPacketHandler {
    private static final int MAX_CHANNELS = Integer.getInteger("fabric.networking.maxChannels", 8192);
    private static final int MAX_CHANNEL_NAME_LENGTH = Math.max(Integer.getInteger("fabric.networking.maxChannelNameLength", 128), 128);
    protected final Connection connection;
    protected final GlobalReceiverRegistry<H> receiver;
    protected final Set<Identifier> sendableChannels;
    protected int commonVersion = -1;

    protected AbstractChanneledNetworkAddon(GlobalReceiverRegistry<H> receiver, Connection connection, String description) {
        super(receiver, description);
        this.connection = connection;
        this.receiver = receiver;
        this.sendableChannels = Collections.synchronizedSet(new HashSet());
    }

    protected void registerPendingChannels(ChannelInfoHolder holder, ConnectionProtocol state) {
        Collection<Identifier> pending = holder.fabric_getPendingChannelsNames(state);
        if (!pending.isEmpty()) {
            this.register(new ArrayList<Identifier>(pending));
            pending.clear();
        }
    }

    public boolean handle(CustomPacketPayload payload) {
        Object handler;
        Identifier channelName = payload.type().id();
        this.logger.debug("Handling inbound packet from channel with name \"{}\"", (Object)channelName);
        if (payload instanceof RegistrationPayload) {
            RegistrationPayload registrationPayload = (RegistrationPayload)payload;
            if (NetworkingImpl.REGISTER_CHANNEL.equals(channelName)) {
                this.receiveRegistration(true, registrationPayload);
                return true;
            }
            if (NetworkingImpl.UNREGISTER_CHANNEL.equals(channelName)) {
                this.receiveRegistration(false, registrationPayload);
                return true;
            }
        }
        if ((handler = this.getHandler(channelName)) == null) {
            return false;
        }
        if (!this.isOnReceiveThread()) {
            throw RunningOnDifferentThreadException.RUNNING_ON_DIFFERENT_THREAD;
        }
        try {
            this.receive(handler, payload);
        }
        catch (Throwable ex) {
            this.logger.error("Encountered exception while handling in channel with name \"{}\"", (Object)channelName, (Object)ex);
            throw ex;
        }
        return true;
    }

    protected abstract boolean isOnReceiveThread();

    protected abstract void receive(H var1, CustomPacketPayload var2);

    protected void sendInitialChannelRegistrationPacket() {
        RegistrationPayload payload = this.createRegistrationPayload(RegistrationPayload.REGISTER, this.getReceivableChannels());
        if (payload != null) {
            this.sendPacket(payload);
        }
    }

    protected @Nullable RegistrationPayload createRegistrationPayload(CustomPacketPayload.Type<RegistrationPayload> type, Collection<Identifier> channels) {
        if (channels.isEmpty()) {
            return null;
        }
        return new RegistrationPayload(type, new ArrayList<Identifier>(channels));
    }

    protected void receiveRegistration(boolean register, RegistrationPayload payload) {
        if (register) {
            this.register(payload.channels());
        } else {
            this.unregister(payload.channels());
        }
    }

    void register(List<Identifier> ids) {
        ids.forEach(this::registerChannel);
        this.schedule(() -> this.invokeRegisterEvent(ids));
    }

    private void registerChannel(Identifier id) {
        if (this.sendableChannels.size() >= MAX_CHANNELS) {
            throw new IllegalArgumentException("Cannot register more than " + MAX_CHANNELS + " channels");
        }
        if (id.toString().length() > MAX_CHANNEL_NAME_LENGTH) {
            throw new IllegalArgumentException("Channel name is too long");
        }
        this.sendableChannels.add(id);
    }

    void unregister(List<Identifier> ids) {
        this.sendableChannels.removeAll(ids);
        this.schedule(() -> this.invokeUnregisterEvent(ids));
    }

    @Override
    public void sendPacket(Packet<?> packet, ChannelFutureListener callback) {
        Objects.requireNonNull(packet, "Packet cannot be null");
        this.connection.send(packet, callback);
    }

    @Override
    public void disconnect(Component disconnectReason) {
        Objects.requireNonNull(disconnectReason, "Disconnect reason cannot be null");
        this.connection.disconnect(disconnectReason);
    }

    protected abstract void schedule(Runnable var1);

    protected abstract void invokeRegisterEvent(List<Identifier> var1);

    protected abstract void invokeUnregisterEvent(List<Identifier> var1);

    public Set<Identifier> getSendableChannels() {
        return Collections.unmodifiableSet(this.sendableChannels);
    }

    @Override
    public void onCommonVersionPacket(int negotiatedVersion) {
        if (negotiatedVersion != 1) {
            throw new UnsupportedOperationException("Unsupported common packet version: " + negotiatedVersion);
        }
        this.commonVersion = negotiatedVersion;
        this.logger.debug("Negotiated common packet version {}", (Object)this.commonVersion);
    }

    @Override
    public void onCommonRegisterPacket(CommonRegisterPayload payload) {
        if (payload.version() != this.getNegotiatedVersion()) {
            throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(this.commonVersion, payload.version()));
        }
        String currentPhase = this.getProtocol();
        if (currentPhase == null) {
            this.logger.warn("Received common register packet for protocol {} in protocol: {}", (Object)payload.protocol(), (Object)this.receiver.getProtocol());
            return;
        }
        if (!payload.protocol().equals(currentPhase)) {
            throw new IllegalStateException("Register packet received for protocol (%s) on handler for protocol(%s)".formatted(payload.protocol(), currentPhase));
        }
        this.register(new ArrayList<Identifier>(payload.channels()));
    }

    @Override
    public CommonRegisterPayload createRegisterPayload() {
        return new CommonRegisterPayload(this.getNegotiatedVersion(), this.getProtocol(), this.getReceivableChannels());
    }

    @Override
    public int getNegotiatedVersion() {
        if (this.commonVersion == -1) {
            throw new IllegalStateException("Not yet negotiated common packet version");
        }
        return this.commonVersion;
    }

    private @Nullable String getProtocol() {
        return switch (this.receiver.getProtocol()) {
            case ConnectionProtocol.PLAY -> "play";
            case ConnectionProtocol.CONFIGURATION -> "configuration";
            default -> null;
        };
    }
}

