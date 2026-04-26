/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client.accessor;

import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.network.Connection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientCommonPacketListenerImpl.class})
public interface ClientCommonPacketListenerImplAccessor {
    @Accessor
    public Connection getConnection();
}

