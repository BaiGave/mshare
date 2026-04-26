/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.loot;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.fabricmc.fabric.impl.loot.LootUtil;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.world.level.storage.loot.LootDataType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SimpleJsonResourceReloadListener.class})
public class SimpleJsonResourceReloadListenerMixin {
    @Inject(method={"scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V"}, at={@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/resources/FileToIdConverter;fileToId(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/resources/Identifier;")})
    private static <T> void fillSourceMap(ResourceManager manager, FileToIdConverter fileToIdConverter, DynamicOps<JsonElement> ops, Codec<T> codec, Map<Identifier, T> result, CallbackInfo ci, @Local(name={"entry"}) Map.Entry<Identifier, Resource> entry, @Local(name={"id"}) Identifier id) {
        String dirName = fileToIdConverter.prefix();
        if (!LootDataType.TABLE.registryKey().identifier().getPath().equals(dirName)) {
            return;
        }
        LootUtil.SOURCES.get().put(id, LootUtil.determineSource(entry.getValue()));
    }
}

