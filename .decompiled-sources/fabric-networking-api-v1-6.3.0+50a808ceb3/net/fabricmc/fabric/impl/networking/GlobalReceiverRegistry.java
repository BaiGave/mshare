/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.fabricmc.fabric.impl.networking.AbstractNetworkAddon;
import net.fabricmc.fabric.impl.networking.NetworkingImpl;
import net.fabricmc.fabric.impl.networking.PayloadTypeRegistryImpl;
import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class GlobalReceiverRegistry<H> {
    public static final int DEFAULT_CHANNEL_NAME_MAX_LENGTH = 128;
    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalReceiverRegistry.class);
    private final PacketFlow flow;
    private final ConnectionProtocol protocol;
    private final @Nullable PayloadTypeRegistryImpl<?> payloadTypeRegistry;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Identifier, H> handlers = new HashMap<Identifier, H>();
    private final Set<AbstractNetworkAddon<H>> trackedAddons = new HashSet<AbstractNetworkAddon<H>>();

    public GlobalReceiverRegistry(PacketFlow flow, ConnectionProtocol protocol, @Nullable PayloadTypeRegistryImpl<?> payloadTypeRegistry) {
        this.flow = flow;
        this.protocol = protocol;
        this.payloadTypeRegistry = payloadTypeRegistry;
        if (payloadTypeRegistry != null) {
            if (protocol != payloadTypeRegistry.getProtocol()) {
                throw new IllegalStateException();
            }
            if (flow != payloadTypeRegistry.getFlow()) {
                throw new IllegalStateException();
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public @Nullable H getHandler(Identifier channelName) {
        Lock lock = this.lock.readLock();
        lock.lock();
        try {
            H h = this.handlers.get(channelName);
            return h;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean registerGlobalReceiver(Identifier channelName, H handler) {
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        Objects.requireNonNull(handler, "Channel handler cannot be null");
        if (NetworkingImpl.isReservedCommonChannel(channelName)) {
            throw new IllegalArgumentException(String.format("Cannot register handler for reserved channel with name \"%s\"", channelName));
        }
        this.assertPayloadType(channelName);
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            boolean replaced;
            boolean bl = replaced = this.handlers.putIfAbsent(channelName, handler) == null;
            if (replaced) {
                this.handleRegistration(channelName, handler);
            }
            boolean bl2 = replaced;
            return bl2;
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public @Nullable H unregisterGlobalReceiver(Identifier channelName) {
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        if (NetworkingImpl.isReservedCommonChannel(channelName)) {
            throw new IllegalArgumentException(String.format("Cannot unregister packet handler for reserved channel with name \"%s\"", channelName));
        }
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            H removed = this.handlers.remove(channelName);
            if (removed != null) {
                this.handleUnregistration(channelName);
            }
            H h = removed;
            return h;
        }
        finally {
            lock.unlock();
        }
    }

    public Map<Identifier, H> getHandlers() {
        Lock lock = this.lock.readLock();
        lock.lock();
        try {
            HashMap<Identifier, H> hashMap = new HashMap<Identifier, H>(this.handlers);
            return hashMap;
        }
        finally {
            lock.unlock();
        }
    }

    public Set<Identifier> getChannels() {
        Lock lock = this.lock.readLock();
        lock.lock();
        try {
            HashSet<Identifier> hashSet = new HashSet<Identifier>(this.handlers.keySet());
            return hashSet;
        }
        finally {
            lock.unlock();
        }
    }

    public void startSession(AbstractNetworkAddon<H> addon) {
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            if (this.trackedAddons.add(addon)) {
                addon.registerChannels(this.handlers);
            }
            this.logTrackedAddonSize();
        }
        finally {
            lock.unlock();
        }
    }

    public void endSession(AbstractNetworkAddon<H> addon) {
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.logTrackedAddonSize();
            this.trackedAddons.remove(addon);
        }
        finally {
            lock.unlock();
        }
    }

    private void logTrackedAddonSize() {
        if (LOGGER.isTraceEnabled() && this.trackedAddons.size() > 1) {
            LOGGER.trace("{} receiver registry tracks {} addon instances", (Object)this.protocol.id(), (Object)this.trackedAddons.size());
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleRegistration(Identifier channelName, H handler) {
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.logTrackedAddonSize();
            for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
                addon.registerChannel(channelName, handler);
            }
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void handleUnregistration(Identifier channelName) {
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            this.logTrackedAddonSize();
            for (AbstractNetworkAddon<H> addon : this.trackedAddons) {
                addon.unregisterChannel(channelName);
            }
        }
        finally {
            lock.unlock();
        }
    }

    public void assertPayloadType(Identifier channelName) {
        if (this.payloadTypeRegistry == null) {
            return;
        }
        if (this.payloadTypeRegistry.get(channelName) == null) {
            throw new IllegalArgumentException(String.format("Cannot register handler as no payload type has been registered with name \"%s\" for %s %s", new Object[]{channelName, this.flow, this.protocol}));
        }
        if (channelName.toString().length() > 128) {
            throw new IllegalArgumentException(String.format("Cannot register handler for channel with name \"%s\" as it exceeds the maximum length of 128 characters", channelName));
        }
    }

    public ConnectionProtocol getProtocol() {
        return this.protocol;
    }
}

