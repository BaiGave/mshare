/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BuiltInRegistries.class})
public abstract class BuiltInRegistriesMixin {
    @Inject(method={"freeze"}, at={@At(value="HEAD")})
    private static void modifyDefaultItemComponents(CallbackInfo ci) {
    }
}

