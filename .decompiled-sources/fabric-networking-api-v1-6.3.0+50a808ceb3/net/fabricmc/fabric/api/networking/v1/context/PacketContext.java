/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.networking.v1.context;

import com.mojang.authlib.GameProfile;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.fabricmc.fabric.impl.networking.context.PacketContextImpl;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.Connection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public interface PacketContext {
    public static final ReadKey<MinecraftServer> SERVER_INSTANCE = PacketContextImpl.SERVER_INSTANCE;
    public static final ReadKey<RegistryAccess> REGISTRY_ACCESS = PacketContextImpl.REGISTRY_ACCESS;
    public static final ReadKey<GameProfile> GAME_PROFILE = PacketContextImpl.GAME_PROFILE;
    public static final ReadKey<@NonNull Connection> CONNECTION = PacketContextImpl.CONNECTION;

    public <T> @Nullable T get(ReadKey<T> var1);

    default public <T> T orElseThrow(ReadKey<T> key) {
        return Objects.requireNonNull(this.get(key), () -> "Packet Context is missing the '" + String.valueOf(((PacketContextImpl.KeyImpl)key).key()) + "' value!");
    }

    default public <T> T orElse(ReadKey<T> key, T defaultValue) {
        return Objects.requireNonNullElse(this.get(key), defaultValue);
    }

    public <T> void set(Key<T> var1, @Nullable T var2);

    public static @Nullable PacketContext get() {
        if (PacketContextImpl.VALUE.isBound()) {
            return PacketContextImpl.VALUE.get();
        }
        return null;
    }

    public static PacketContext orElseThrow() {
        PacketContext ctx = PacketContextImpl.VALUE.orElseThrow(() -> new RuntimeException("PacketContext is required, but it wasn't set up!"));
        if (ctx == null) {
            throw new RuntimeException("PacketContext is required, but it was disabled!");
        }
        return ctx;
    }

    public static void runWithContext(PacketContextProvider provider, Runnable runnable) {
        ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).run(runnable);
    }

    public static <T> T supplyWithContext(PacketContextProvider provider, Supplier<T> supplier) {
        return (T)ScopedValue.where(PacketContextImpl.VALUE, provider.getPacketContext()).call(supplier::get);
    }

    public static void runWithoutContext(Runnable runnable) {
        if (PacketContextImpl.VALUE.isBound()) {
            ScopedValue.where(PacketContextImpl.VALUE, null).run(runnable);
            return;
        }
        runnable.run();
    }

    public static <T> T supplyWithoutContext(Supplier<T> supplier) {
        if (PacketContextImpl.VALUE.isBound()) {
            return (T)ScopedValue.where(PacketContextImpl.VALUE, null).call(supplier::get);
        }
        return supplier.get();
    }

    public static <T> Key<T> key(Identifier key) {
        return new PacketContextImpl.KeyImpl(key);
    }

    @ApiStatus.NonExtendable
    public static interface ReadKey<T> {
    }

    @ApiStatus.NonExtendable
    public static interface Key<T>
    extends ReadKey<T> {
    }
}

