/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen;

import com.google.common.collect.ImmutableSet;
import com.google.common.hash.HashCode;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets={"net.minecraft.data.HashCache$ProviderCache"})
public abstract class HashCacheProviderCacheMixin {
    @ModifyExpressionValue(method={"save"}, at={@At(value="INVOKE", target="Lcom/google/common/collect/ImmutableMap;entrySet()Lcom/google/common/collect/ImmutableSet;")})
    private ImmutableSet<Map.Entry<Path, HashCode>> sortPaths(ImmutableSet<Map.Entry<Path, HashCode>> original) {
        return original.stream().sorted(Map.Entry.comparingByKey(Comparator.comparing(k -> HashCacheProviderCacheMixin.normalizePath(k.toString())))).collect(ImmutableSet.toImmutableSet());
    }

    @ModifyExpressionValue(method={"save"}, at={@At(value="INVOKE", target="Ljava/nio/file/Path;toString()Ljava/lang/String;")})
    private String pathToString(String original) {
        return HashCacheProviderCacheMixin.normalizePath(original);
    }

    @Unique
    private static String normalizePath(String path) {
        return path.replace('\\', '/');
    }
}

