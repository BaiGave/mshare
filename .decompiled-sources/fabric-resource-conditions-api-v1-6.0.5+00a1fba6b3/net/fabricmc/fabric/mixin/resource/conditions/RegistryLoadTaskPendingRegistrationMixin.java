/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import com.google.gson.JsonElement;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Decoder;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.resources.RegistryLoadTask;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RegistryLoadTask.PendingRegistration.class})
public abstract class RegistryLoadTaskPendingRegistrationMixin {
    @Inject(method={"loadFromResource"}, at={@At(value="INVOKE", target="Lcom/mojang/serialization/Decoder;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;")}, cancellable=true)
    private static <T> void loadFromResource(Decoder<T> elementDecoder, RegistryOps<JsonElement> ops, ResourceKey<T> elementKey, Resource thunk, CallbackInfoReturnable<Either<T, Exception>> cir, @Local(name={"json"}) JsonElement json) {
        if (json.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(json.getAsJsonObject(), elementKey.registry().toString(), elementKey.identifier(), ops.lookupProvider)) {
            cir.setReturnValue(Either.right(ResourceConditionsImpl.DISABLED_RESOURCE_EXCEPTION));
        }
    }
}

