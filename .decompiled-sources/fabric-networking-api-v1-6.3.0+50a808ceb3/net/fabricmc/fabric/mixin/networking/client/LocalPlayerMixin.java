/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client;

import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.fabricmc.fabric.api.networking.v1.context.PacketContextProvider;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={LocalPlayer.class})
public class LocalPlayerMixin
implements PacketContextProvider {
    @Shadow
    @Final
    public ClientPacketListener connection;

    @Override
    public PacketContext getPacketContext() {
        return this.connection.getPacketContext();
    }
}

