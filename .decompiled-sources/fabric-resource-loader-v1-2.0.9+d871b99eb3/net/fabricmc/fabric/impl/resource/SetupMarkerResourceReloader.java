/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import net.fabricmc.fabric.api.resource.v1.DataResourceLoader;
import net.fabricmc.fabric.impl.resource.FabricDataResourceStoreHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;

public record SetupMarkerResourceReloader(ReloadableServerResources reloadableServerResources, HolderLookup.Provider registries, FeatureFlagSet featureSet) implements ResourceManagerReloadListener
{
    @Override
    public void prepareSharedState(PreparableReloadListener.SharedState store) {
        store.set(DataResourceLoader.REGISTRY_LOOKUP_KEY, this.registries);
        store.set(DataResourceLoader.FEATURE_FLAG_SET_KEY, this.featureSet);
        store.set(DataResourceLoader.ADVANCEMENT_LOADER_KEY, this.reloadableServerResources.getAdvancements());
        store.set(DataResourceLoader.RECIPE_MANAGER_KEY, this.reloadableServerResources.getRecipeManager());
        store.set(DataResourceLoader.DATA_RESOURCE_STORE_KEY, ((FabricDataResourceStoreHolder)((Object)this.reloadableServerResources)).fabric$getDataResourceStore());
    }

    @Override
    public void onResourceManagerReload(ResourceManager manager) {
    }
}

