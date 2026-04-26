/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import org.jetbrains.annotations.VisibleForTesting;

public final class RegistryAttributeImpl
implements RegistryAttributeHolder {
    private static final Map<ResourceKey<?>, RegistryAttributeHolder> HOLDER_MAP = new ConcurrentHashMap();
    private final EnumSet<RegistryAttribute> attributes = EnumSet.noneOf(RegistryAttribute.class);

    public static RegistryAttributeHolder getHolder(ResourceKey<?> resourceKey) {
        return HOLDER_MAP.computeIfAbsent(resourceKey, key -> new RegistryAttributeImpl());
    }

    private RegistryAttributeImpl() {
    }

    @Override
    public RegistryAttributeHolder addAttribute(RegistryAttribute attribute) {
        this.attributes.add(attribute);
        return this;
    }

    @VisibleForTesting
    public void removeAttribute(RegistryAttribute attribute) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment()) {
            throw new AssertionError();
        }
        this.attributes.remove((Object)attribute);
    }

    @Override
    public boolean hasAttribute(RegistryAttribute attribute) {
        return this.attributes.contains((Object)attribute);
    }

    public EnumSet<RegistryAttribute> getAttributes() {
        return this.attributes;
    }
}

