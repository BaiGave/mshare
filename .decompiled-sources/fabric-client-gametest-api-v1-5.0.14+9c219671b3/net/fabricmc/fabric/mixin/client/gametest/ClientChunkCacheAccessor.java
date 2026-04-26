/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest;

import net.minecraft.client.multiplayer.ClientChunkCache;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ClientChunkCache.class})
public interface ClientChunkCacheAccessor {
    @Accessor
    public ClientChunkCache.Storage getStorage();
}

