/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.minecraft.client.gui.screens.debug.DebugOptionsScreen$OptionEntry"})
public abstract class DebugOptionsScreenEntryMixin {
    @WrapOperation(method={"<init>"}, at={@At(value="INVOKE", target="Lnet/minecraft/resources/Identifier;getPath()Ljava/lang/String;")})
    private String showNamespace(Identifier instance, Operation<String> original) {
        if (!"minecraft".equals(instance.getNamespace())) {
            return instance.toString();
        }
        return original.call(instance);
    }
}

