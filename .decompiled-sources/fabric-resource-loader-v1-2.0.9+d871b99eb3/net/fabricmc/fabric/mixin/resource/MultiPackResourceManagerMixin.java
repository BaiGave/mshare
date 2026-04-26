/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import java.util.List;
import net.fabricmc.fabric.impl.resource.FabricMultiPackResourceManager;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MultiPackResourceManager.class})
public class MultiPackResourceManagerMixin
implements FabricMultiPackResourceManager {
    @Unique
    private PackType packType;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(PackType type, List<PackResources> list, CallbackInfo ci) {
        this.packType = type;
    }

    @Override
    public PackType fabric$getPackType() {
        return this.packType;
    }
}

