/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={TagLoader.class})
public class TagLoaderMixin {
    @WrapOperation(method={"loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/core/WritableRegistry;)V"}, at={@At(value="INVOKE", target="Lnet/minecraft/tags/TagLoader;loadTagsForRegistry(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/tags/TagLoader$ElementLookup;)Ljava/util/Map;")})
    private static <T> Map<TagKey<T>, List<Holder<T>>> loadTagsForRegistry(ResourceManager manager, ResourceKey<? extends Registry<T>> registryKey, TagLoader.ElementLookup<Holder<T>> lookup, Operation<Map<TagKey<T>, List<Holder<T>>>> original, @Local(argsOnly=true) WritableRegistry<T> registry) {
        Map<TagKey<T>, List<Holder<T>>> tags = original.call(manager, registryKey, lookup);
        registry.bindTags(tags);
        return tags;
    }
}

