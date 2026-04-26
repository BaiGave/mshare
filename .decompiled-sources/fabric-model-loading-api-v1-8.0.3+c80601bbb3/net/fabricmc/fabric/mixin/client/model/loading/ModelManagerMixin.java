/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.api.client.model.loading.v1.FabricModelManager;
import net.fabricmc.fabric.api.client.model.loading.v1.UnbakedModelDeserializer;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingPluginManager;
import net.minecraft.client.resources.model.BlockStateModelLoader;
import net.minecraft.client.resources.model.ClientItemInfoLoader;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelDiscovery;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ModelManager.class})
abstract class ModelManagerMixin
implements FabricModelManager {
    @Unique
    private volatile @Nullable CompletableFuture<ModelLoadingEventDispatcher> eventDispatcherFuture;
    @Unique
    private @Nullable Map<ExtraModelKey<?>, ?> extraModels;

    ModelManagerMixin() {
    }

    @Override
    public <T> @Nullable T getModel(ExtraModelKey<T> key) {
        return this.extraModels == null ? null : (T)this.extraModels.get(key);
    }

    @Inject(method={"reload"}, at={@At(value="HEAD")})
    private void onHeadReload(PreparableReloadListener.SharedState sharedState, Executor prepareExecutor, PreparableReloadListener.PreparationBarrier synchronizer, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        this.eventDispatcherFuture = ModelLoadingPluginManager.preparePlugins(sharedState, prepareExecutor).thenApplyAsync(ModelLoadingEventDispatcher::new, prepareExecutor);
    }

    @ModifyReturnValue(method={"reload"}, at={@At(value="RETURN")})
    private CompletableFuture<Void> resetEventDispatcherFuture(CompletableFuture<Void> future) {
        return future.thenApplyAsync(v -> {
            this.eventDispatcherFuture = null;
            return v;
        });
    }

    @ModifyExpressionValue(method={"reload"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/resources/model/ModelManager;loadBlockModels(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")})
    private CompletableFuture<Map<Identifier, UnbakedModel>> hookModels(CompletableFuture<Map<Identifier, UnbakedModel>> modelsFuture) {
        return modelsFuture.thenCombine(this.eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyModelsOnLoad((Map<Identifier, UnbakedModel>)models));
    }

    @ModifyExpressionValue(method={"reload"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/resources/model/BlockStateModelLoader;loadBlockStates(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")})
    private CompletableFuture<BlockStateModelLoader.LoadedModels> hookBlockStateModels(CompletableFuture<BlockStateModelLoader.LoadedModels> modelsFuture) {
        return modelsFuture.thenCombine(this.eventDispatcherFuture, (models, eventDispatcher) -> eventDispatcher.modifyBlockModelsOnLoad((BlockStateModelLoader.LoadedModels)models));
    }

    @ModifyArg(method={"reload"}, at=@At(value="INVOKE", target="Ljava/util/concurrent/CompletableFuture;thenApplyAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal=0), index=0)
    private Function<Void, ?> hookModelCollect(Function<Void, CompletableFuture<?>> function) {
        return this.withModelDispatcher(function);
    }

    @ModifyArg(method={"reload"}, at=@At(value="INVOKE", target="Ljava/util/concurrent/CompletableFuture;thenComposeAsync(Ljava/util/function/Function;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal=0), index=0)
    private Function<Void, CompletableFuture<?>> hookModelBaking(Function<Void, CompletableFuture<?>> function) {
        return this.withModelDispatcher(function);
    }

    @Unique
    private <T, R> Function<T, R> withModelDispatcher(Function<T, R> function) {
        CompletableFuture<ModelLoadingEventDispatcher> future = this.eventDispatcherFuture;
        if (future == null) {
            return function;
        }
        return x -> {
            ModelLoadingEventDispatcher.CURRENT.set((ModelLoadingEventDispatcher)future.join());
            try {
                Object r = function.apply(x);
                return r;
            }
            finally {
                ModelLoadingEventDispatcher.CURRENT.remove();
            }
        };
    }

    @Inject(method={"discoverModelDependencies"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/resources/model/ModelDiscovery;resolve()Ljava/util/Map;")})
    private static void resolveExtraModels(Map<Identifier, UnbakedModel> modelMap, BlockStateModelLoader.LoadedModels stateDefinition, ClientItemInfoLoader.LoadedClientInfos loadedClientInfos, CallbackInfoReturnable<?> cir, @Local(name={"result"}) ModelDiscovery result) {
        ModelLoadingEventDispatcher eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
        if (eventDispatcher != null) {
            eventDispatcher.getExtraModels().values().forEach(result::addRoot);
        }
    }

    @Inject(method={"apply"}, at={@At(value="RETURN")})
    private void onReturnUpload(CallbackInfo ci, @Local(name={"bakedModels"}) ModelBakery.BakingResult bakedModels) {
        this.extraModels = ((BakedModelsHooks)((Object)bakedModels)).fabric_getExtraModels();
    }

    @Redirect(method={"lambda$loadBlockModels$2(Ljava/util/Map$Entry;)Lcom/mojang/datafixers/util/Pair;"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/resources/model/cuboid/CuboidModel;fromStream(Ljava/io/Reader;)Lnet/minecraft/client/resources/model/cuboid/CuboidModel;"))
    private static CuboidModel cancelVanillaDeserialize(Reader reader) {
        return null;
    }

    @ModifyArg(method={"lambda$loadBlockModels$2(Ljava/util/Map$Entry;)Lcom/mojang/datafixers/util/Pair;"}, at=@At(value="INVOKE", target="Lcom/mojang/datafixers/util/Pair;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/mojang/datafixers/util/Pair;"), index=1)
    private static Object actuallyDeserializeModel(Object originalModel, @Local(name={"reader"}) Reader reader) {
        return UnbakedModelDeserializer.deserialize(reader);
    }
}

