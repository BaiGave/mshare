/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.fabricmc.fabric.impl.networking.GlobalReceiverRegistry;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNetworkAddon<H> {
    protected final GlobalReceiverRegistry<H> receiver;
    protected final Logger logger;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Identifier, H> handlers = new HashMap<Identifier, H>();
    private final AtomicBoolean disconnected = new AtomicBoolean();

    protected AbstractNetworkAddon(GlobalReceiverRegistry<H> receiver, String description) {
        this.receiver = receiver;
        this.logger = LoggerFactory.getLogger(description);
    }

    public final void lateInit() {
        this.receiver.startSession(this);
        this.invokeInitEvent();
    }

    protected abstract void invokeInitEvent();

    public final void endSession() {
        this.receiver.endSession(this);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public @Nullable H getHandler(Identifier channel) {
        Lock lock = this.lock.readLock();
        lock.lock();
        try {
            H h = this.handlers.get(channel);
            return h;
        }
        finally {
            lock.unlock();
        }
    }

    private void assertNotReserved(Identifier channel) {
        if (this.isReservedChannel(channel)) {
            throw new IllegalArgumentException(String.format("Cannot (un)register handler for reserved channel with name \"%s\"", channel));
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void registerChannels(Map<Identifier, H> map) {
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            for (Map.Entry<Identifier, H> entry : map.entrySet()) {
                this.assertNotReserved(entry.getKey());
                boolean unique = this.handlers.putIfAbsent(entry.getKey(), entry.getValue()) == null;
                if (!unique) continue;
                this.handleRegistration(entry.getKey());
            }
        }
        finally {
            lock.unlock();
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean registerChannel(Identifier channelName, H handler) {
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        Objects.requireNonNull(handler, "Packet handler cannot be null");
        this.assertNotReserved(channelName);
        this.receiver.assertPayloadType(channelName);
        Lock lock = this.lock.writeLock();
        lock.lock();
        try {
            boolean replaced;
            boolean bl = replaced = this.handlers.putIfAbsent(channelName, handler) == null;
            if (replaced) {
                this.handleRegistration(channelName);
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
    public H unregisterChannel(Identifier channelName) {
        Objects.requireNonNull(channelName, "Channel name cannot be null");
        this.assertNotReserved(channelName);
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

    public Set<Identifier> getReceivableChannels() {
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

    protected abstract void handleRegistration(Identifier var1);

    protected abstract void handleUnregistration(Identifier var1);

    public final void handleDisconnect() {
        if (this.disconnected.compareAndSet(false, true)) {
            this.invokeDisconnectEvent();
            this.endSession();
        }
    }

    protected abstract void invokeDisconnectEvent();

    protected abstract boolean isReservedChannel(Identifier var1);
}

