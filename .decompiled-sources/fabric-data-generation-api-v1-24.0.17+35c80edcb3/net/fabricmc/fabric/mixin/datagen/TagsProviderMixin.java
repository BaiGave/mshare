/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.datagen;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider;
import net.fabricmc.fabric.impl.datagen.FabricTagBuilder;
import net.fabricmc.fabric.impl.datagen.TagAliasGenerator;
import net.minecraft.core.Registry;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagBuilder;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={TagsProvider.class})
public class TagsProviderMixin<T> {
    @Shadow
    @Final
    protected ResourceKey<? extends Registry<T>> registryKey;
    @Unique
    private PackOutput.PathProvider tagAliasPathResolver;

    @Inject(method={"<init>(Lnet/minecraft/data/PackOutput;Lnet/minecraft/resources/ResourceKey;Ljava/util/concurrent/CompletableFuture;Ljava/util/concurrent/CompletableFuture;)V"}, at={@At(value="RETURN")})
    private void initPathResolver(PackOutput output, ResourceKey<? extends Registry<T>> registryRef, CompletableFuture<?> registriesFuture, CompletableFuture<?> parentTagLookupFuture, CallbackInfo info) {
        this.tagAliasPathResolver = output.createPathProvider(PackOutput.Target.DATA_PACK, TagAliasGenerator.getDirectory(registryRef));
    }

    @ModifyArg(method={"lambda$run$5"}, at=@At(value="INVOKE", target="Lnet/minecraft/tags/TagFile;<init>(Ljava/util/List;Z)V"), index=1)
    private boolean addReplaced(boolean replaced, @Local(name={"builder"}) TagBuilder builder) {
        if (builder instanceof FabricTagBuilder) {
            FabricTagBuilder fabricTagBuilder = (FabricTagBuilder)((Object)builder);
            return fabricTagBuilder.fabric_isReplaced();
        }
        return replaced;
    }

    @WrapOperation(method={"lambda$run$2"}, at={@At(value="INVOKE", target="Ljava/util/concurrent/CompletableFuture;allOf([Ljava/util/concurrent/CompletableFuture;)Ljava/util/concurrent/CompletableFuture;")})
    private CompletableFuture<Void> addTagAliasGroupBuilders(CompletableFuture<?>[] futures, Operation<CompletableFuture<Void>> original, @Local(argsOnly=true) CachedOutput writer) {
        if (this instanceof FabricTagsProvider) {
            Map<Identifier, FabricTagsProvider.AliasGroupBuilder> builders = ((FabricTagsProvider)((Object)this)).getAliasGroupBuilders();
            CompletableFuture<?>[] newFutures = Arrays.copyOf(futures, futures.length + builders.size());
            int index = futures.length;
            for (Map.Entry<Identifier, FabricTagsProvider.AliasGroupBuilder> entry : builders.entrySet()) {
                newFutures[index++] = TagAliasGenerator.writeTagAlias(writer, this.tagAliasPathResolver, this.registryKey, entry.getKey(), entry.getValue().getTags());
            }
            return original.call(new Object[]{newFutures});
        }
        return original.call(new Object[]{futures});
    }
}

