/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.impl.datagen.client.FabricItemAssetDefinitions;
import net.fabricmc.fabric.impl.datagen.client.FabricModelProviderDefinitions;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ModelProvider.class})
public class ModelProviderMixin {
    @Unique
    private FabricPackOutput fabricPackOutput;

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    public void init(PackOutput output, CallbackInfo ci) {
        if (output instanceof FabricPackOutput) {
            FabricPackOutput fabricPackOutput;
            this.fabricPackOutput = fabricPackOutput = (FabricPackOutput)output;
        }
    }

    @WrapOperation(method={"run"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/data/models/BlockModelGenerators;run()V")})
    private void registerBlockStateModels(BlockModelGenerators instance, Operation<Void> original) {
        ModelProviderMixin modelProviderMixin = this;
        if (modelProviderMixin instanceof FabricModelProvider) {
            FabricModelProvider fabricModelProvider = (FabricModelProvider)((Object)modelProviderMixin);
            fabricModelProvider.generateBlockStateModels(instance);
        } else {
            original.call(instance);
        }
    }

    @WrapOperation(method={"run"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/data/models/ItemModelGenerators;run()V")})
    private void registerItemModels(ItemModelGenerators instance, Operation<Void> original) {
        ModelProviderMixin modelProviderMixin = this;
        if (modelProviderMixin instanceof FabricModelProvider) {
            FabricModelProvider fabricModelProvider = (FabricModelProvider)((Object)modelProviderMixin);
            fabricModelProvider.generateItemModels(instance);
        } else {
            original.call(instance);
        }
    }

    @Inject(method={"run"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/data/models/BlockModelGenerators;run()V")})
    private void setFabricPackOutput(CachedOutput output, CallbackInfoReturnable<CompletableFuture<?>> cir, @Local(name={"blockStateGenerators"}) ModelProvider.BlockStateGeneratorCollector blockStateGenerators, @Local(name={"itemModels"}) ModelProvider.ItemInfoCollector itemModels) {
        ((FabricModelProviderDefinitions)((Object)blockStateGenerators)).setFabricPackOutput(this.fabricPackOutput);
        ((FabricModelProviderDefinitions)((Object)itemModels)).setFabricPackOutput(this.fabricPackOutput);
        ((FabricItemAssetDefinitions)((Object)itemModels)).fabric_setProcessedBlocks(blockStateGenerators.generators.keySet());
    }
}

