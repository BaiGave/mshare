/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client.accessor;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientConfigurationPacketListenerImpl.class})
public interface ClientConfigurationPacketListenerImplAccessor {
    @Accessor
    public GameProfile getLocalGameProfile();
}

