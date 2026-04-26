/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={BucketItem.class})
public class BucketItemMixin {
    @Shadow
    @Final
    private Fluid content;

    @ModifyVariable(method={"playEmptySound"}, at=@At(value="STORE"), name={"soundEvent"})
    private SoundEvent hookEmptyingSound(SoundEvent previous) {
        return FluidVariantAttributes.getHandlerOrDefault(this.content).getEmptySound(FluidVariant.of(this.content)).orElse(previous);
    }
}

