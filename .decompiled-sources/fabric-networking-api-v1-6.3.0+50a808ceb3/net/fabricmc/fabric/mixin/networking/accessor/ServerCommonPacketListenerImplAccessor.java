/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import net.minecraft.network.Connection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ServerCommonPacketListenerImpl.class})
public interface ServerCommonPacketListenerImplAccessor {
    @Accessor
    public Connection getConnection();

    @Accessor
    public MinecraftServer getServer();
}

