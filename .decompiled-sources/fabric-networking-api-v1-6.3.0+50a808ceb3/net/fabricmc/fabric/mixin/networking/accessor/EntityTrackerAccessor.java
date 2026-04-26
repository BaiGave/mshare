/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import java.util.Set;
import net.minecraft.server.network.ServerPlayerConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.server.level.ChunkMap$TrackedEntity"})
public interface EntityTrackerAccessor {
    @Accessor
    public Set<ServerPlayerConnection> getSeenBy();
}

