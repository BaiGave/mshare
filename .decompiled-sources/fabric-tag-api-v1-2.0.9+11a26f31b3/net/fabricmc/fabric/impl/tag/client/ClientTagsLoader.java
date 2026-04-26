/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag.client;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.impl.tag.client.ClientTagsImpl;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagFile;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StrictJsonParser;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTagsLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-tags-api-v1");

    public static LoadedTag loadTag(final TagKey<?> tagKey) {
        HashSet<TagEntry> tags = new HashSet<TagEntry>();
        HashSet<Path> tagFiles = ClientTagsLoader.getTagFiles(tagKey.registry(), tagKey.location());
        for (Path tagPath : tagFiles) {
            try {
                BufferedReader tagReader = Files.newBufferedReader(tagPath);
                try {
                    JsonElement jsonElement = StrictJsonParser.parse(tagReader);
                    TagFile maybeTagFile = TagFile.CODEC.parse(new Dynamic<JsonElement>(JsonOps.INSTANCE, jsonElement)).result().orElse(null);
                    if (maybeTagFile == null) continue;
                    if (maybeTagFile.replace()) {
                        tags.clear();
                    }
                    tags.addAll(maybeTagFile.entries());
                }
                finally {
                    if (tagReader == null) continue;
                    tagReader.close();
                }
            }
            catch (IOException e) {
                LOGGER.error("Error loading tag: " + String.valueOf(tagKey), e);
            }
        }
        HashSet completeIds = new HashSet();
        final HashSet immediateChildIds = new HashSet();
        final HashSet immediateChildTags = new HashSet();
        for (TagEntry tagEntry : tags) {
            tagEntry.build(new TagEntry.Lookup<Identifier>(){

                @Override
                public @Nullable Identifier element(Identifier id, boolean required) {
                    immediateChildIds.add(id);
                    return id;
                }

                @Override
                public @Nullable Collection<Identifier> tag(Identifier id) {
                    TagKey tag = TagKey.create(tagKey.registry(), id);
                    immediateChildTags.add(tag);
                    return ClientTagsImpl.getOrCreatePartiallySyncedTag(tag).completeIds;
                }
            }, completeIds::add);
        }
        immediateChildTags.remove(tagKey);
        return new LoadedTag(Collections.unmodifiableSet(completeIds), Collections.unmodifiableSet(immediateChildTags), Collections.unmodifiableSet(immediateChildIds));
    }

    private static HashSet<Path> getTagFiles(ResourceKey<? extends Registry<?>> resourceKey, Identifier identifier) {
        return ClientTagsLoader.getTagFiles(Registries.tagsDirPath(resourceKey), identifier);
    }

    private static HashSet<Path> getTagFiles(String tagType, Identifier identifier) {
        String tagFile = "data/%s/%s/%s.json".formatted(identifier.getNamespace(), tagType, identifier.getPath());
        return ClientTagsLoader.getResourcePaths(tagFile);
    }

    private static HashSet<Path> getResourcePaths(String path) {
        HashSet<Path> out = new HashSet<Path>();
        for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
            mod.findPath(path).ifPresent(out::add);
        }
        return out;
    }

    public record LoadedTag(Set<Identifier> completeIds, Set<TagKey<?>> immediateChildTags, Set<Identifier> immediateChildIds) {
    }
}

