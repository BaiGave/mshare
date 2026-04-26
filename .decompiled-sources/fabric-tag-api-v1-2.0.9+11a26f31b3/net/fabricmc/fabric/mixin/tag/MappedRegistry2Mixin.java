/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryLookup;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets={"net.minecraft.core.MappedRegistry$2"})
abstract class MappedRegistry2Mixin<T>
implements TagAliasEnabledRegistryLookup {
    MappedRegistry2Mixin() {
    }

    @Shadow
    public abstract HolderLookup.RegistryLookup<T> parent();

    @Override
    public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups) {
        ((TagAliasEnabledRegistryLookup)((Object)this.parent())).fabric_loadTagAliases(aliasGroups);
    }
}

