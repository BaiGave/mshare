/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.ingredient;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import java.util.Optional;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredient;
import net.fabricmc.fabric.api.recipe.v1.ingredient.CustomIngredientSerializer;
import net.fabricmc.fabric.api.recipe.v1.ingredient.FabricIngredient;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientImpl;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientStreamCodec;
import net.fabricmc.fabric.impl.recipe.ingredient.OptionalCustomIngredientStreamCodec;
import net.minecraft.core.HolderSet;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={Ingredient.class})
public class IngredientMixin
implements FabricIngredient {
    @Mutable
    @Shadow
    @Final
    public static Codec<Ingredient> CODEC;
    @Shadow
    @Final
    private HolderSet<Item> values;

    @ModifyExpressionValue(method={"<clinit>"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;", ordinal=0)})
    private static StreamCodec<RegistryFriendlyByteBuf, Ingredient> useCustomIngredientStreamCodec(StreamCodec<RegistryFriendlyByteBuf, Ingredient> original) {
        return new CustomIngredientStreamCodec(original);
    }

    @ModifyExpressionValue(method={"<clinit>"}, at={@At(value="INVOKE", target="Lnet/minecraft/network/codec/StreamCodec;map(Ljava/util/function/Function;Ljava/util/function/Function;)Lnet/minecraft/network/codec/StreamCodec;", ordinal=1)})
    private static StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> useOptionalCustomIngredientStreamCodec(StreamCodec<RegistryFriendlyByteBuf, Optional<Ingredient>> original) {
        return new OptionalCustomIngredientStreamCodec(original);
    }

    @Inject(method={"<clinit>"}, at={@At(value="TAIL")})
    private static void injectCodec(CallbackInfo ci) {
        Codec<CustomIngredient> customIngredientCodec = CustomIngredientImpl.CODEC.dispatch("fabric:type", CustomIngredient::getSerializer, CustomIngredientSerializer::getCodec);
        CODEC = Codec.either(customIngredientCodec, CODEC).xmap(either -> either.map(CustomIngredient::toVanilla, ingredient -> ingredient), ingredient -> {
            CustomIngredient customIngredient = ingredient.getCustomIngredient();
            return customIngredient == null ? Either.right(ingredient) : Either.left(customIngredient);
        });
    }

    @Inject(method={"lambda$static$4", "lambda$static$2", "lambda$static$0"}, at={@At(value="HEAD")}, cancellable=true)
    private static void onGetEntries(Ingredient ingredient, CallbackInfoReturnable<HolderSet<Item>> cir) {
        if (ingredient instanceof CustomIngredientImpl) {
            CustomIngredientImpl customIngredient = (CustomIngredientImpl)ingredient;
            cir.setReturnValue(HolderSet.direct(customIngredient.getCustomMatchingItems()));
        }
    }

    @Inject(method={"equals(Ljava/lang/Object;)Z"}, at={@At(value="HEAD")}, cancellable=true)
    private void onHeadEquals(Object obj, CallbackInfoReturnable<Boolean> cir) {
        if (obj instanceof CustomIngredientImpl) {
            cir.setReturnValue(false);
        }
    }

    public int hashCode() {
        return this.values.hashCode();
    }
}

