/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.data.registries.RegistryPatchGenerator;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={RegistryPatchGenerator.class})
class RegistryPatchGeneratorMixin {
    RegistryPatchGeneratorMixin() {
    }

    @Redirect(at=@At(value="FIELD", target="Lnet/minecraft/resources/RegistryDataLoader;WORLDGEN_REGISTRIES:Ljava/util/List;", opcode=178), method={"lambda$createLookup$0"})
    private static List<RegistryDataLoader.RegistryData<?>> getDynamicRegistries() {
        return DynamicRegistries.getDynamicRegistries();
    }
}

