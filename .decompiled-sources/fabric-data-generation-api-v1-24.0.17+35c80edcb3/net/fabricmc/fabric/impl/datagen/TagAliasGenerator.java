/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.datagen;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.impl.tag.TagAliasGroup;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class TagAliasGenerator {
    public static String getDirectory(ResourceKey<? extends Registry<?>> registryKey) {
        Object directory = "fabric/tag_aliases/";
        Identifier registryId = registryKey.identifier();
        if (!"minecraft".equals(registryId.getNamespace())) {
            directory = (String)directory + registryId.getNamespace() + "/";
        }
        return (String)directory + registryId.getPath();
    }

    public static <T> CompletableFuture<?> writeTagAlias(CachedOutput cache, PackOutput.PathProvider pathResolver, ResourceKey<? extends Registry<T>> registryRef, Identifier groupId, List<TagKey<T>> tags) {
        Path path = pathResolver.json(groupId);
        return DataProvider.saveStable(cache, TagAliasGroup.codec(registryRef), new TagAliasGroup<T>(tags), path);
    }
}

