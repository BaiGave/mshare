/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.attachment;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={BannerBlockEntity.class})
abstract class BannerBlockEntityMixin {
    BannerBlockEntityMixin() {
    }

    @ModifyExpressionValue(method={"getUpdateTag"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/level/block/entity/BannerBlockEntity;saveWithoutMetadata(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/nbt/CompoundTag;")})
    private CompoundTag removeAttachments(CompoundTag original) {
        original.remove("fabric:attachments");
        return original;
    }
}

