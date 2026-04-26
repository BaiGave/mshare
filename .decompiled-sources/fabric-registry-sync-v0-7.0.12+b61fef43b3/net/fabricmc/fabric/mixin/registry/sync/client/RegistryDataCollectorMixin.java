/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.fabricmc.fabric.mixin.registry.sync.client.RegistryDataCollectorContentsCollectorAccessor;
import net.minecraft.client.multiplayer.RegistryDataCollector;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(value={RegistryDataCollector.class})
public class RegistryDataCollectorMixin {
    @WrapOperation(method={"loadNewElementsAndTags"}, at={@At(value="FIELD", target="Lnet/minecraft/resources/RegistryDataLoader;SYNCHRONIZED_REGISTRIES:Ljava/util/List;", opcode=178)})
    private List<RegistryDataLoader.RegistryData<?>> skipEmptyRegistries(Operation<List<RegistryDataLoader.RegistryData<?>>> operation, ResourceProvider resourceFactory, @Coerce RegistryDataCollectorContentsCollectorAccessor storage, boolean bl) {
        Map<ResourceKey<? extends Registry<?>>, List<RegistrySynchronization.PackedRegistryEntry>> dynamicRegistries = storage.getElements();
        ArrayList result = new ArrayList((Collection)operation.call(new Object[0]));
        result.removeIf(entry -> DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key()) && !dynamicRegistries.containsKey(entry.key()));
        return result;
    }
}

