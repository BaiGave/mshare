/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag;

import java.util.Map;
import java.util.Set;
import net.minecraft.tags.TagKey;

public interface TagAliasEnabledRegistryLookup {
    public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> var1);
}

