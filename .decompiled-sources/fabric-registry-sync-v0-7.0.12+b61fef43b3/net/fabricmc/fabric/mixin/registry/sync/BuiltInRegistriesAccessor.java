/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import net.minecraft.core.WritableRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BuiltInRegistries.class})
public interface BuiltInRegistriesAccessor<T> {
    @Accessor
    public static WritableRegistry<WritableRegistry<?>> getWRITABLE_REGISTRY() {
        throw new UnsupportedOperationException();
    }
}

