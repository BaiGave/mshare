/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.List;
import net.fabricmc.fabric.impl.resource.FabricMultiPackResourceManager;
import net.fabricmc.fabric.impl.resource.ResourceLoaderImpl;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={SimpleReloadInstance.class})
public class SimpleReloadInstanceMixin {
    @ModifyArg(method={"create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/resources/SimpleReloadInstance;of(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static List<PreparableReloadListener> sortSimple(List<PreparableReloadListener> reloaders, @Local(argsOnly=true) ResourceManager resourceManager) {
        if (resourceManager instanceof FabricMultiPackResourceManager) {
            FabricMultiPackResourceManager flrm = (FabricMultiPackResourceManager)((Object)resourceManager);
            return ResourceLoaderImpl.sort(flrm.fabric$getPackType(), reloaders);
        }
        return reloaders;
    }

    @ModifyArg(method={"create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/resources/ProfiledReloadInstance;of(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;)Lnet/minecraft/server/packs/resources/ReloadInstance;"))
    private static List<PreparableReloadListener> sortProfiled(List<PreparableReloadListener> reloaders, @Local(argsOnly=true) ResourceManager resourceManager) {
        if (resourceManager instanceof FabricMultiPackResourceManager) {
            FabricMultiPackResourceManager flrm = (FabricMultiPackResourceManager)((Object)resourceManager);
            return ResourceLoaderImpl.sort(flrm.fabric$getPackType(), reloaders);
        }
        return reloaders;
    }

    @ModifyVariable(method={"create(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/server/packs/resources/ReloadInstance;"}, at=@At(value="LOAD", ordinal=0), argsOnly=true, name={"enableProfiling"})
    private static boolean adjustProfiledCheck(boolean profiled) {
        return profiled || ResourceLoaderImpl.DEBUG_PROFILE_RESOURCE_RELOADERS;
    }
}

