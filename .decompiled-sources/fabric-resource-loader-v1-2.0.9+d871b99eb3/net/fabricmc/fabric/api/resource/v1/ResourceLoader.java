/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1;

import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ResourceLoader {
    public static final PreparableReloadListener.StateKey<HolderLookup.Provider> REGISTRY_LOOKUP_KEY = new PreparableReloadListener.StateKey();
    public static final PreparableReloadListener.StateKey<FeatureFlagSet> FEATURE_FLAG_SET_KEY = new PreparableReloadListener.StateKey();

    public static ResourceLoader get(PackType type) {
        return ResourceLoaderImpl.get(type);
    }

    public void registerReloadListener(Identifier var1, PreparableReloadListener var2);

    public void addListenerOrdering(Identifier var1, Identifier var2);

    public static boolean registerBuiltinPack(Identifier id, ModContainer container, PackActivationType activationType) {
        return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.getPath(), container, activationType);
    }

    public static boolean registerBuiltinPack(Identifier id, ModContainer container, Component displayName, PackActivationType activationType) {
        return ResourceLoaderImpl.registerBuiltinPack(id, "resourcepacks/" + id.getPath(), container, displayName, activationType);
    }
}

