/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.impl.resource.DataResourceStoreImpl;
import net.fabricmc.fabric.impl.resource.FabricDataResourceStoreHolder;
import net.fabricmc.fabric.impl.resource.SetupMarkerResourceReloader;
import net.minecraft.server.ReloadableServerRegistries;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ReloadableServerResources.class})
public class ReloadableServerResourcesMixin
implements FabricDataResourceStoreHolder {
    @Unique
    private final DataResourceStore.Mutable dataResourceStore = new DataResourceStoreImpl();

    @ModifyArg(method={"lambda$loadResources$2"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/resources/SimpleReloadInstance;create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static List<PreparableReloadListener> onSetupDataReloaders(List<PreparableReloadListener> reloaders, @Local(argsOnly=true) ReloadableServerRegistries.LoadResult loadResult, @Local(argsOnly=true) FeatureFlagSet featureSet, @Local(name={"result"}) ReloadableServerResources result) {
        ArrayList<PreparableReloadListener> list = new ArrayList<PreparableReloadListener>(reloaders);
        list.addFirst(new SetupMarkerResourceReloader(result, loadResult.lookupWithUpdatedTags(), featureSet));
        return Collections.unmodifiableList(list);
    }

    @Override
    public DataResourceStore.Mutable fabric$getDataResourceStore() {
        return this.dataResourceStore;
    }
}

