/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1;

import java.util.function.Function;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.impl.resource.DataResourceLoaderImpl;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.item.crafting.RecipeManager;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface DataResourceLoader
extends ResourceLoader {
    public static final PreparableReloadListener.StateKey<RecipeManager> RECIPE_MANAGER_KEY = new PreparableReloadListener.StateKey();
    public static final PreparableReloadListener.StateKey<ServerAdvancementManager> ADVANCEMENT_LOADER_KEY = new PreparableReloadListener.StateKey();
    public static final PreparableReloadListener.StateKey<DataResourceStore.Mutable> DATA_RESOURCE_STORE_KEY = new PreparableReloadListener.StateKey();

    public static DataResourceLoader get() {
        return DataResourceLoaderImpl.INSTANCE;
    }

    public void registerReloadListener(Identifier var1, Function<HolderLookup.Provider, PreparableReloadListener> var2);
}

