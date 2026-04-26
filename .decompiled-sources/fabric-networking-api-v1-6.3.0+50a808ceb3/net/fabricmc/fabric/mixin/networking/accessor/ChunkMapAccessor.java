/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking.accessor;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.mixin.networking.accessor.EntityTrackerAccessor;
import net.minecraft.server.level.ChunkMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ChunkMap.class})
public interface ChunkMapAccessor {
    @Accessor
    public Int2ObjectMap<EntityTrackerAccessor> getEntityMap();
}

