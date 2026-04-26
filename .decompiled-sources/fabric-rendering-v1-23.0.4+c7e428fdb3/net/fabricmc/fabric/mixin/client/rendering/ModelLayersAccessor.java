/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Set;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ModelLayers.class})
public interface ModelLayersAccessor {
    @Accessor(value="ALL_MODELS")
    public static Set<ModelLayerLocation> getLayers() {
        throw new AssertionError((Object)"This should not occur!");
    }
}

