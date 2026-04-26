/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerLoginPacketListenerImpl.class})
public interface ServerLoginPacketListenerImplAccessor {
    @Accessor
    public MinecraftServer getServer();

    @Accessor
    public Connection getConnection();
}

