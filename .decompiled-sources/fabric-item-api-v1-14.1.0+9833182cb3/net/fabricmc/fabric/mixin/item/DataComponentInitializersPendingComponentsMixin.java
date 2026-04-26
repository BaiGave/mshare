/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import java.util.List;
import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentInitializers;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets={"net.minecraft.core.component.DataComponentInitializers$1"})
public abstract class DataComponentInitializersPendingComponentsMixin<T> {
    @Unique
    private HolderLookup.Provider registryLookup;

    @Shadow
    public abstract ResourceKey<? extends Registry<?>> key();

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void store(ResourceKey<T> par1, List<DataComponentInitializers.BakedEntry<T>> par2, CallbackInfo ci) {
        this.registryLookup = DefaultItemComponentImpl.LOOKUP_PROVIDER_SCOPED_VALUE.get();
    }

    @Inject(method={"apply"}, at={@At(value="RETURN")})
    private void apply(CallbackInfo ci) {
        if (Registries.ITEM.identifier().equals(this.key().identifier())) {
            DefaultItemComponentImpl.modifyItemComponents(this.registryLookup);
        }
    }
}

