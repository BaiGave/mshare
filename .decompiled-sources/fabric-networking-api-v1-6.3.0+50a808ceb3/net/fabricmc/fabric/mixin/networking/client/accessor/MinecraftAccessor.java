/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.client.accessor;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Connection;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={Minecraft.class})
public interface MinecraftAccessor {
    @Accessor
    public @Nullable Connection getPendingConnection();
}

