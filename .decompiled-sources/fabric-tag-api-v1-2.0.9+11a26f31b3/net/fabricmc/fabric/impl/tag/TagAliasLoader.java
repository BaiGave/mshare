/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag;

import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.reloader.SimpleReloadListener;
import net.fabricmc.fabric.impl.tag.MappedRegistryExtension;
import net.fabricmc.fabric.impl.tag.TagAliasEnabledRegistryLookup;
import net.fabricmc.fabric.impl.tag.TagAliasGroup;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TagAliasLoader
extends SimpleReloadListener<Map<ResourceKey<? extends Registry<?>>, List<Data>>> {
    public static final Identifier ID = Identifier.fromNamespaceAndPath("fabric-tag-api-v1", "tag_alias_groups");
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-tag-api-v1");

    /*
     * Loose catch block
     */
    @Override
    protected Map<ResourceKey<? extends Registry<?>>, List<Data>> prepare(PreparableReloadListener.SharedState state) {
        HashMap dataByRegistry = new HashMap();
        HolderLookup.Provider registries = state.get(ResourceLoader.REGISTRY_LOOKUP_KEY);
        Iterator registryIterator = registries.listRegistryKeys().iterator();
        while (registryIterator.hasNext()) {
            ResourceKey resourceKey = (ResourceKey)registryIterator.next();
            FileToIdConverter fileToIdConverter = FileToIdConverter.json(TagAliasLoader.getDirectory(resourceKey));
            for (Map.Entry<Identifier, Resource> entry : fileToIdConverter.listMatchingResources(state.resourceManager()).entrySet()) {
                Identifier resourcePath = entry.getKey();
                Identifier groupId = fileToIdConverter.fileToId(resourcePath);
                try {
                    BufferedReader reader = entry.getValue().openAsReader();
                    try {
                        DataResult dataResult;
                        JsonElement json = StrictJsonParser.parse(reader);
                        Codec<TagAliasGroup<JsonElement>> codec = TagAliasGroup.codec(resourceKey);
                        Objects.requireNonNull(codec.parse(JsonOps.INSTANCE, json));
                        int n = 0;
                        switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{DataResult.Success.class, DataResult.Error.class}, dataResult, n)) {
                            default: {
                                throw new MatchException(null, null);
                            }
                            case 0: {
                                DataResult.Success success = (DataResult.Success)dataResult;
                                Object object = (TagAliasGroup)success.value();
                                TagAliasGroup group = object;
                                Object unused = object = success.lifecycle();
                                Data data = new Data(groupId, group);
                                dataByRegistry.computeIfAbsent(resourceKey, key -> new ArrayList()).add(data);
                                break;
                            }
                            case 1: {
                                DataResult.Error error = (DataResult.Error)dataResult;
                                LOGGER.error("[Fabric] Couldn't parse tag alias group file '{}' from '{}': {}", groupId, resourcePath, error.message());
                                break;
                            }
                        }
                        continue;
                        catch (Throwable throwable) {
                            throw new MatchException(throwable.toString(), throwable);
                        }
                    }
                    finally {
                        if (reader == null) continue;
                        ((Reader)reader).close();
                    }
                }
                catch (JsonParseException | IOException e) {
                    LOGGER.error("[Fabric] Couldn't parse tag alias group file '{}' from '{}'", groupId, resourcePath, e);
                }
            }
        }
        return dataByRegistry;
    }

    private static String getDirectory(ResourceKey<? extends Registry<?>> resourceKey) {
        Object directory = "fabric/tag_alias/";
        Identifier registryId = resourceKey.identifier();
        if (!"minecraft".equals(registryId.getNamespace())) {
            directory = (String)directory + registryId.getNamespace() + "/";
        }
        return (String)directory + registryId.getPath();
    }

    @Override
    protected void apply(Map<ResourceKey<? extends Registry<?>>, List<Data>> prepared, PreparableReloadListener.SharedState state) {
        for (Map.Entry<ResourceKey<Registry<?>>, List<Data>> entry : prepared.entrySet()) {
            HashMap groupsByTag = new HashMap();
            for (Data data : entry.getValue()) {
                HashSet group = new HashSet(data.group.tags());
                for (TagKey<?> tag : data.group.tags()) {
                    Set oldGroup = (Set)groupsByTag.get(tag);
                    if (oldGroup != null) {
                        group.addAll(oldGroup);
                        for (TagKey other : oldGroup) {
                            groupsByTag.put(other, group);
                        }
                    }
                    groupsByTag.put(tag, group);
                }
            }
            groupsByTag.values().removeIf(tags -> tags.size() == 1);
            HolderGetter lookup = state.get(ResourceLoader.REGISTRY_LOOKUP_KEY).lookupOrThrow(entry.getKey());
            if (lookup instanceof TagAliasEnabledRegistryLookup) {
                TagAliasEnabledRegistryLookup aliasLookup = (TagAliasEnabledRegistryLookup)((Object)lookup);
                aliasLookup.fabric_loadTagAliases(groupsByTag);
                continue;
            }
            throw new ClassCastException("[Fabric] Couldn't apply tag aliases to registry lookup %s (%s) since it doesn't implement TagAliasEnabledRegistryLookup".formatted(lookup, entry.getKey().identifier()));
        }
    }

    public static <T> void applyToDynamicRegistries(LayeredRegistryAccess<T> registries, T phase) {
        Iterator registryEntries = registries.getLayer(phase).registries().iterator();
        while (registryEntries.hasNext()) {
            Registry registry = ((RegistryAccess.RegistryEntry)registryEntries.next()).value();
            if (registry instanceof MappedRegistryExtension) {
                MappedRegistryExtension extension = (MappedRegistryExtension)((Object)registry);
                extension.fabric_applyPendingTagAliases();
                extension.fabric_refreshTags();
                continue;
            }
            throw new ClassCastException("[Fabric] Couldn't apply pending tag aliases to registry %s (%s) since it doesn't implement MappedRegistryExtension".formatted(registry, registry.getClass().getName()));
        }
    }

    protected record Data(Identifier groupId, TagAliasGroup<?> group) {
    }
}

