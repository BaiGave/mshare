/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.api.util.Item2ObjectMap;
import net.fabricmc.fabric.impl.content.registry.CompostableRegistryImpl;

public interface CompostableRegistry
extends Item2ObjectMap<Float> {
    public static final CompostableRegistry INSTANCE = new CompostableRegistryImpl();
}

