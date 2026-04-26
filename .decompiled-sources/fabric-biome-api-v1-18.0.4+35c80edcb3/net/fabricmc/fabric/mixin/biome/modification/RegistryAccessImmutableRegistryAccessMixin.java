/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.biome.modification;

import net.fabricmc.fabric.impl.biome.modification.BiomeModificationMarker;
import net.minecraft.core.RegistryAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RegistryAccess.ImmutableRegistryAccess.class})
public class RegistryAccessImmutableRegistryAccessMixin
implements BiomeModificationMarker {
    @Unique
    private boolean modified;

    @Override
    public void fabric_markModified() {
        if (this.modified) {
            throw new IllegalStateException("This dynamic registries instance has already been modified");
        }
        this.modified = true;
    }
}

