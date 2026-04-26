/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client.accessor;

import net.minecraft.client.multiplayer.ClientHandshakePacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientHandshakePacketListenerImpl.class})
public interface ClientHandshakePacketListenerImplAccessor {
    @Accessor
    public Connection getConnection();
}

