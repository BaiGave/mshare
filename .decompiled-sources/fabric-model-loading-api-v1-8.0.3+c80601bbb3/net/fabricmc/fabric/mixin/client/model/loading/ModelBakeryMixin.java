/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.fabricmc.fabric.impl.client.model.loading.ModelLoadingEventDispatcher;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.thread.ParallelMapTransform;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ModelBakery.class})
abstract class ModelBakeryMixin {
    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private Map<Identifier, ResolvedModel> resolvedModels;
    @Unique
    private @Nullable ModelLoadingEventDispatcher fabric_eventDispatcher;

    ModelBakeryMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void onReturnInit(CallbackInfo ci) {
        this.fabric_eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
    }

    @ModifyArg(method={"bakeModels"}, at=@At(value="INVOKE", target="Lnet/minecraft/util/thread/ParallelMapTransform;schedule(Ljava/util/Map;Ljava/util/function/BiFunction;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal=0), index=1)
    private BiFunction<BlockState, BlockStateModel.UnbakedRoot, BlockStateModel> hookBlockModelBake(BiFunction<BlockState, BlockStateModel.UnbakedRoot, BlockStateModel> bifunction) {
        if (this.fabric_eventDispatcher == null) {
            return bifunction;
        }
        return (state, unbakedModel) -> {
            ModelLoadingEventDispatcher.CURRENT.set(this.fabric_eventDispatcher);
            BlockStateModel model = (BlockStateModel)bifunction.apply((BlockState)state, (BlockStateModel.UnbakedRoot)unbakedModel);
            ModelLoadingEventDispatcher.CURRENT.remove();
            return model;
        };
    }

    @ModifyReturnValue(method={"bakeModels"}, at={@At(value="RETURN")})
    private CompletableFuture<ModelBakery.BakingResult> withExtraModels(CompletableFuture<ModelBakery.BakingResult> models, @Local(argsOnly=true) Executor executor, @Local(name={"baker"}) ModelBakery.ModelBakerImpl baker) {
        if (this.fabric_eventDispatcher == null) {
            return models;
        }
        CompletableFuture<Map<ExtraModelKey, Object>> extraModels = ParallelMapTransform.schedule(this.fabric_eventDispatcher.getExtraModels(), (key, model) -> {
            try {
                return model.bake(baker);
            }
            catch (Exception e) {
                LOGGER.warn("Unable to bake extra model: '{}'", key, (Object)e);
                return null;
            }
        }, executor);
        return models.thenCombine(extraModels, (res, extra) -> {
            ((BakedModelsHooks)((Object)res)).fabric_setExtraModels((Map<ExtraModelKey<?>, ?>)extra);
            return res;
        });
    }

    @WrapOperation(method={"lambda$bakeModels$0"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel$UnbakedRoot;bake(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/client/resources/model/ModelBaker;)Lnet/minecraft/client/renderer/block/dispatch/BlockStateModel;")})
    private static BlockStateModel wrapBlockModelBake(BlockStateModel.UnbakedRoot unbakedModel, BlockState state, ModelBaker baker, Operation<BlockStateModel> operation) {
        ModelLoadingEventDispatcher eventDispatcher = ModelLoadingEventDispatcher.CURRENT.get();
        if (eventDispatcher == null) {
            return operation.call(unbakedModel, state, baker);
        }
        return eventDispatcher.modifyBlockModel(unbakedModel, state, baker, operation);
    }

    @WrapOperation(method={"lambda$bakeModels$1"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/renderer/item/ItemModel$Unbaked;bake(Lnet/minecraft/client/renderer/item/ItemModel$BakingContext;Lorg/joml/Matrix4fc;)Lnet/minecraft/client/renderer/item/ItemModel;")})
    private ItemModel wrapItemModelBake(ItemModel.Unbaked unbakedModel, ItemModel.BakingContext bakeContext, Matrix4fc transformation, Operation<ItemModel> operation, @Local(argsOnly=true) Identifier itemId) {
        if (this.fabric_eventDispatcher == null) {
            return operation.call(unbakedModel, bakeContext, transformation);
        }
        return this.fabric_eventDispatcher.modifyItemModel(unbakedModel, itemId, bakeContext, transformation, operation);
    }
}

