/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.dimension;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.dimension.v1.DimensionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.dimension.DimensionModificationMarker;
import net.fabricmc.fabric.mixin.dimension.DimensionTypeAccessor;
import net.fabricmc.fabric.mixin.dimension.MappedRegistryAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionModificationImpl
implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTING.register(server -> DimensionModificationImpl.finalizeWorldGen(server.registryAccess()));
    }

    public static void finalizeWorldGen(RegistryAccess registries) {
        DimensionModificationMarker modificationTracker = (DimensionModificationMarker)((Object)registries);
        modificationTracker.fabric_markDimensionsModified();
        HolderLookup.RegistryLookup dimensions = registries.lookupOrThrow(Registries.DIMENSION_TYPE);
        List<ResourceKey> keys = dimensions.entrySet().stream().map(Map.Entry::getKey).sorted(Comparator.comparingInt(arg_0 -> DimensionModificationImpl.lambda$finalizeWorldGen$0((Registry)dimensions, arg_0))).toList();
        for (ResourceKey key : keys) {
            Holder.Reference<DimensionType> reference = dimensions.getOrThrow(key);
            if (!DimensionModificationImpl.applyChanges(reference, registries) || !(dimensions instanceof MappedRegistry)) continue;
            MappedRegistry registry = (MappedRegistry)dimensions;
            Map registrationInfos = ((MappedRegistryAccessor)((Object)registry)).fabric_getRegistrationInfos();
            RegistrationInfo info = registrationInfos.get(key);
            RegistrationInfo newInfo = new RegistrationInfo(Optional.empty(), info.lifecycle());
            registrationInfos.put(key, newInfo);
        }
    }

    private static boolean applyChanges(Holder<DimensionType> dimension, RegistryAccess registries) {
        boolean changed;
        EnvironmentAttributeMap oldAttributes = dimension.value().attributes();
        EnvironmentAttributeMap.Builder attributeBuilder = EnvironmentAttributeMap.builder().putAll(oldAttributes);
        DimensionEvents.MODIFY_ATTRIBUTES.invoker().modifyDimensionAttributes(dimension, attributeBuilder, registries);
        EnvironmentAttributeMap newAttributes = attributeBuilder.build();
        boolean bl = changed = !oldAttributes.equals(newAttributes);
        if (changed) {
            ((DimensionTypeAccessor)((Object)dimension.value())).fabric_setAttributes(newAttributes);
        }
        return changed;
    }

    private static /* synthetic */ int lambda$finalizeWorldGen$0(Registry dimensions, ResourceKey key) {
        return dimensions.getId((DimensionType)dimensions.getValueOrThrow(key));
    }
}

