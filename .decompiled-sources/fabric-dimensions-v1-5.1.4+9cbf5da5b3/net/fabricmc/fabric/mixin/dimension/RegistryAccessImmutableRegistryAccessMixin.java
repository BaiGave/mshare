/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import net.fabricmc.fabric.impl.dimension.DimensionModificationMarker;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RegistryAccess.ImmutableRegistryAccess.class})
public class RegistryAccessImmutableRegistryAccessMixin
implements DimensionModificationMarker {
    @Unique
    private boolean dimensionsModified;

    @Override
    public void fabric_markDimensionsModified() {
        if (this.dimensionsModified) {
            throw new IllegalStateException("Dimensions in this dynamic registries instance have already been modified");
        }
        this.dimensionsModified = true;
    }
}

