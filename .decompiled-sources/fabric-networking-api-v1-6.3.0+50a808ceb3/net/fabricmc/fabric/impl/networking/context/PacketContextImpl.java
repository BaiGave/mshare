/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.context;

import com.mojang.authlib.GameProfile;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public final class PacketContextImpl
implements PacketContext {
    public static final ScopedValue<PacketContext> VALUE = ScopedValue.newInstance();
    public static final PacketContext.Key<MinecraftServer> SERVER_INSTANCE = PacketContextImpl.fabricKey("server_instance");
    public static final PacketContext.Key<RegistryAccess> REGISTRY_ACCESS = PacketContextImpl.fabricKey("registry_access");
    public static final PacketContext.Key<GameProfile> GAME_PROFILE = PacketContextImpl.fabricKey("game_profile");
    public static final PacketContext.Key<@NonNull Connection> CONNECTION = PacketContextImpl.fabricKey("connection");
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<PacketContext.Key<?>, Object> contextMap = new IdentityHashMap();

    public PacketContextImpl(Connection connection) {
        this.contextMap.put(CONNECTION, connection);
    }

    @Override
    public <T> @Nullable T get(PacketContext.ReadKey<T> key) {
        this.lock.readLock().lock();
        try {
            Object object = this.contextMap.get(key);
            return (T)object;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public <T> void set(PacketContext.Key<T> key, T value) {
        this.lock.writeLock().lock();
        if (value == null) {
            this.contextMap.remove(key);
        } else {
            this.contextMap.put(key, value);
        }
        this.lock.writeLock().unlock();
    }

    private static <T> PacketContext.Key<T> fabricKey(String path) {
        return PacketContext.key(Identifier.fromNamespaceAndPath("fabric", path));
    }

    public static final class KeyImpl<T>
    implements PacketContext.Key<T> {
        private final Identifier key;

        public KeyImpl(Identifier key) {
            this.key = key;
        }

        public String toString() {
            return "PacketContext.Key[" + String.valueOf(this.key) + "]";
        }

        public Identifier key() {
            return this.key;
        }
    }
}

