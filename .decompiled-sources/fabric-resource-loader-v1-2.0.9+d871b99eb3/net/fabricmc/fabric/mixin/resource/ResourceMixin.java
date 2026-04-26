/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import net.fabricmc.fabric.api.resource.v1.FabricResource;
import net.fabricmc.fabric.impl.resource.PackSourceTracker;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={Resource.class})
class ResourceMixin
implements FabricResource {
    ResourceMixin() {
    }

    @Override
    public PackSource getFabricPackSource() {
        Resource self = (Resource)((Object)this);
        return PackSourceTracker.getSource(self.source());
    }
}

