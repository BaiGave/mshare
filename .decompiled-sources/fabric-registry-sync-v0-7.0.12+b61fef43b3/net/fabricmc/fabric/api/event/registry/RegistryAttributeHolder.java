/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.registry;

import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.impl.registry.sync.RegistryAttributeImpl;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface RegistryAttributeHolder {
    public static RegistryAttributeHolder get(ResourceKey<?> key) {
        return RegistryAttributeImpl.getHolder(key);
    }

    public static RegistryAttributeHolder get(Registry<?> registry) {
        return RegistryAttributeHolder.get(registry.key());
    }

    public RegistryAttributeHolder addAttribute(RegistryAttribute var1);

    public boolean hasAttribute(RegistryAttribute var1);
}

