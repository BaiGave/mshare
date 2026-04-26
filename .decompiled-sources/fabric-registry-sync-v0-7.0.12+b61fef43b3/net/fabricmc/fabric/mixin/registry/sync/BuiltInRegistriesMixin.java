/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BuiltInRegistries.class})
public class BuiltInRegistriesMixin {
    @Unique
    private static boolean hasInitialised = false;

    @Inject(method={"createContents"}, at={@At(value="HEAD")}, cancellable=true)
    private static void init(CallbackInfo ci) {
        if (hasInitialised) {
            ci.cancel();
        }
        hasInitialised = true;
    }
}

