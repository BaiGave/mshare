/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource;

import java.util.function.Function;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.fabric.impl.resource.loader.ResourceManagerHelperImpl;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import org.jetbrains.annotations.ApiStatus;

@Deprecated
@ApiStatus.NonExtendable
public interface ResourceManagerHelper {
    @Deprecated
    default public void addReloadListener(IdentifiableResourceReloadListener listener) {
        this.registerReloadListener(listener);
    }

    @Deprecated
    public void registerReloadListener(IdentifiableResourceReloadListener var1);

    @Deprecated
    public void registerReloadListener(Identifier var1, Function<HolderLookup.Provider, IdentifiableResourceReloadListener> var2);

    public static ResourceManagerHelper get(PackType type) {
        return ResourceManagerHelperImpl.get(type);
    }

    @Deprecated
    public static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, ResourcePackActivationType activationType) {
        return ResourceLoader.registerBuiltinPack(id, container, activationType.replacement);
    }

    @Deprecated
    public static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, Component displayName, ResourcePackActivationType activationType) {
        return ResourceLoader.registerBuiltinPack(id, container, displayName, activationType.replacement);
    }

    @Deprecated
    public static boolean registerBuiltinResourcePack(Identifier id, ModContainer container, String displayName, ResourcePackActivationType activationType) {
        return ResourceLoader.registerBuiltinPack(id, container, Component.literal(displayName), activationType.replacement);
    }

    @Deprecated
    public static boolean registerBuiltinResourcePack(Identifier id, String subPath, ModContainer container, boolean enabledByDefault) {
        return ResourceLoaderImpl.registerBuiltinPack(id, subPath, container, Component.literal(id.getNamespace() + "/" + id.getPath()), enabledByDefault ? PackActivationType.DEFAULT_ENABLED : PackActivationType.NORMAL);
    }
}

