/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.impl.item.DefaultItemComponentImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentInitializers;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={DataComponentInitializers.class})
public abstract class DataComponentInitializersMixin {
    @WrapMethod(method={"createInitializerForRegistry"})
    private static <T> DataComponentInitializers.PendingComponents<T> captureLookup(HolderLookup.Provider context, DataComponentInitializers.PendingComponentBuilders<T> elementBuilders, Operation<DataComponentInitializers.PendingComponents<T>> original) {
        return ScopedValue.where(DefaultItemComponentImpl.LOOKUP_PROVIDER_SCOPED_VALUE, context).call(() -> (DataComponentInitializers.PendingComponents)original.call(context, elementBuilders));
    }
}

