/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.List;
import java.util.function.Function;
import net.fabricmc.fabric.impl.client.model.loading.CustomUnbakedBlockStateModelRegistry;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.WeightedVariants;
import net.minecraft.util.random.Weighted;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={BlockStateModel.Unbaked.class})
interface BlockStateModelUnbakedMixin {
    @Redirect(method={"<clinit>()V"}, at=@At(value="INVOKE", target="Lcom/mojang/serialization/Codec;flatComapMap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", ordinal=0))
    private static Codec<WeightedVariants.Unbaked> replaceWeightedCodec(Codec<List<Weighted<Variant>>> codec, Function<?, ?> to, Function<?, ?> from) {
        return CustomUnbakedBlockStateModelRegistry.WEIGHTED_MODEL_CODEC;
    }

    @Redirect(method={"<clinit>()V"}, at=@At(value="INVOKE", target="Lcom/mojang/serialization/Codec;flatComapMap(Ljava/util/function/Function;Ljava/util/function/Function;)Lcom/mojang/serialization/Codec;", ordinal=1))
    private static Codec<BlockStateModel.Unbaked> replaceCodec(Codec<Either<WeightedVariants.Unbaked, SingleVariant.Unbaked>> codec, Function<?, ?> to, Function<?, ?> from) {
        return CustomUnbakedBlockStateModelRegistry.MODEL_CODEC;
    }
}

