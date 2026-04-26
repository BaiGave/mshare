/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import com.mojang.serialization.Lifecycle;
import java.util.EnumSet;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.mixin.registry.sync.BuiltInRegistriesAccessor;
import net.minecraft.core.DefaultedMappedRegistry;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class FabricRegistryBuilder<T, R extends WritableRegistry<T>> {
    private final R registry;
    private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

    public static <T, R extends WritableRegistry<T>> FabricRegistryBuilder<T, R> from(R registry) {
        return new FabricRegistryBuilder<T, R>(registry);
    }

    public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> create(ResourceKey<Registry<T>> key) {
        return FabricRegistryBuilder.from(new MappedRegistry<T>(key, Lifecycle.stable(), false));
    }

    public static <T> FabricRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(ResourceKey<Registry<T>> key, Identifier defaultId) {
        return FabricRegistryBuilder.from(new DefaultedMappedRegistry<T>(defaultId.toString(), key, Lifecycle.stable(), false));
    }

    @Deprecated
    public static <T> FabricRegistryBuilder<T, MappedRegistry<T>> create(Class<T> type, Identifier registryId) {
        return FabricRegistryBuilder.create(ResourceKey.createRegistryKey(registryId));
    }

    @Deprecated
    public static <T> FabricRegistryBuilder<T, DefaultedMappedRegistry<T>> createDefaulted(Class<T> type, Identifier registryId, Identifier defaultId) {
        return FabricRegistryBuilder.createDefaulted(ResourceKey.createRegistryKey(registryId), defaultId);
    }

    private FabricRegistryBuilder(R registry) {
        this.registry = registry;
        this.attribute(RegistryAttribute.MODDED);
    }

    public FabricRegistryBuilder<T, R> attribute(RegistryAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    public R buildAndRegister() {
        ResourceKey key = this.registry.key();
        for (RegistryAttribute attribute : this.attributes) {
            RegistryAttributeHolder.get(key).addAttribute(attribute);
        }
        BuiltInRegistriesAccessor.getWRITABLE_REGISTRY().register(key, (WritableRegistry<?>)this.registry, RegistrationInfo.BUILT_IN);
        return this.registry;
    }
}

