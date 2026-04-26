/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync.client;

import java.util.List;
import java.util.Map;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets={"net.minecraft.client.multiplayer.RegistryDataCollector$ContentsCollector"})
public interface RegistryDataCollectorContentsCollectorAccessor {
    @Accessor
    public Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> getElements();
}

