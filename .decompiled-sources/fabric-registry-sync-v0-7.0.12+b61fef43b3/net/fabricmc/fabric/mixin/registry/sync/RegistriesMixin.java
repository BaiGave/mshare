/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={Registries.class})
public class RegistriesMixin {
    @ModifyReturnValue(method={"elementsDirPath"}, at={@At(value="RETURN")})
    private static String prependDirectoryWithNamespace(String original, @Local(argsOnly=true) ResourceKey<? extends Registry<?>> registryRef) {
        Identifier id = registryRef.identifier();
        if (!id.getNamespace().equals("minecraft")) {
            return id.getNamespace() + "/" + id.getPath();
        }
        return original;
    }

    @ModifyReturnValue(method={"tagsDirPath"}, at={@At(value="RETURN")})
    private static String prependTagDirectoryWithNamespace(String original, @Local(argsOnly=true) ResourceKey<? extends Registry<?>> registryRef) {
        Identifier id = registryRef.identifier();
        if (!id.getNamespace().equals("minecraft")) {
            return "tags/" + id.getNamespace() + "/" + id.getPath();
        }
        return original;
    }
}

