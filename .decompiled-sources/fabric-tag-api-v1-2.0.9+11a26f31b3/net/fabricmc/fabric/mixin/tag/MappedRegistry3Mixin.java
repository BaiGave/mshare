/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import net.fabricmc.fabric.impl.tag.MappedRegistryExtension;
import net.minecraft.core.MappedRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.core.MappedRegistry$3"})
abstract class MappedRegistry3Mixin {
    @Shadow
    @Final
    MappedRegistry<?> this$0;

    MappedRegistry3Mixin() {
    }

    @Inject(method={"apply"}, at={@At(value="INVOKE", target="Lnet/minecraft/core/MappedRegistry;refreshTagsInHolders()V")})
    private void applyTagAliases(CallbackInfo info) {
        ((MappedRegistryExtension)((Object)this.this$0)).fabric_applyPendingTagAliases();
    }
}

