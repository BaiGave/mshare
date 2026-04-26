/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import java.util.Optional;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantCache;
import net.fabricmc.fabric.impl.transfer.fluid.FluidVariantImpl;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Fluid.class})
public class FluidMixin
implements FluidVariantCache {
    @Unique
    private final FluidVariant cachedFluidVariant = new FluidVariantImpl((Fluid)((Object)this), DataComponentPatch.EMPTY);

    @Override
    public FluidVariant fabric_getCachedFluidVariant() {
        return this.cachedFluidVariant;
    }

    @Inject(method={"getPickupSound"}, at={@At(value="HEAD")}, cancellable=true)
    public void hookGetBucketFillSound(CallbackInfoReturnable<Optional<SoundEvent>> cir) {
        Fluid fluid = (Fluid)((Object)this);
        Optional<SoundEvent> sound = FluidVariantAttributes.getHandlerOrDefault(fluid).getFillSound(FluidVariant.of(fluid));
        if (sound.isPresent()) {
            cir.setReturnValue(sound);
        }
    }
}

