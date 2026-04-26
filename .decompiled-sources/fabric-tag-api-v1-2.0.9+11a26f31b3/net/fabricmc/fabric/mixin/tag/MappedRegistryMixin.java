/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.fabricmc.fabric.impl.tag.MappedRegistryExtension;
import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryLookup;
import net.fabricmc.fabric.mixin.tag.SimpleRegistryTagLookup2Accessor;
import net.minecraft.core.HolderSet;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={MappedRegistry.class})
abstract class MappedRegistryMixin<T>
implements MappedRegistryExtension,
TagAliasEnabledRegistryLookup {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");
    @Unique
    private Map<TagKey<?>, Set<TagKey<?>>> pendingTagAliasGroups;
    @Shadow
    @Final
    private ResourceKey<? extends Registry<T>> key;
    @Shadow
    private MappedRegistry.TagSet<T> allTags;

    MappedRegistryMixin() {
    }

    @Shadow
    protected abstract HolderSet.Named<T> createTag(TagKey<T> var1);

    @Shadow
    protected abstract void refreshTagsInHolders();

    @Shadow
    public abstract ResourceKey<? extends Registry<T>> key();

    @Override
    public void fabric_loadTagAliases(Map<TagKey<?>, Set<TagKey<?>>> aliasGroups) {
        this.pendingTagAliasGroups = aliasGroups;
    }

    @Override
    public void fabric_applyPendingTagAliases() {
        if (this.pendingTagAliasGroups == null) {
            return;
        }
        Set<Set<TagKey<?>>> uniqueAliasGroups = Sets.newIdentityHashSet();
        uniqueAliasGroups.addAll(this.pendingTagAliasGroups.values());
        for (Set set : uniqueAliasGroups) {
            Set entries = Sets.newIdentityHashSet();
            for (TagKey tag : set) {
                HolderSet.Named entryList = this.allTags.get(tag).orElse(null);
                if (entryList != null) {
                    entries.addAll(entryList.contents);
                    continue;
                }
                LOGGER.info("[Fabric] Creating a new empty tag {} for unknown tag used in a tag alias group in {}", (Object)tag.location(), (Object)tag.registry().identifier());
                Map tagMap = ((SimpleRegistryTagLookup2Accessor)((Object)this.allTags)).fabric_getTagMap();
                if (!(tagMap instanceof HashMap)) {
                    tagMap = new HashMap(tagMap);
                    ((SimpleRegistryTagLookup2Accessor)((Object)this.allTags)).fabric_setTagMap(tagMap);
                }
                tagMap.put(tag, this.createTag(tag));
            }
            List entriesAsList = List.copyOf(entries);
            for (TagKey tag : set) {
                HolderSet.Named<T> entryList = this.allTags.get(tag).orElseThrow();
                entryList.contents = entriesAsList;
            }
        }
        LOGGER.debug("[Fabric] Loaded {} tag alias groups for {}", (Object)uniqueAliasGroups.size(), (Object)this.key.identifier());
        this.pendingTagAliasGroups = null;
    }

    @Override
    public void fabric_refreshTags() {
        this.refreshTagsInHolders();
    }
}

