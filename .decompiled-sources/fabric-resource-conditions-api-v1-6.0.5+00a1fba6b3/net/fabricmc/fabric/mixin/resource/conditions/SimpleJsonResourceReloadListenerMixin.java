/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import java.util.Map;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.fabricmc.fabric.mixin.resource.conditions.RegistryOpsAccessor;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={SimpleJsonResourceReloadListener.class})
public class SimpleJsonResourceReloadListenerMixin {
    @Unique
    private static final Object SKIP_DATA_MARKER = new Object();

    @WrapOperation(method={"scanDirectory(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/resources/FileToIdConverter;Lcom/mojang/serialization/DynamicOps;Lcom/mojang/serialization/Codec;Ljava/util/Map;)V"}, at={@At(value="INVOKE", target="Lcom/mojang/serialization/Codec;parse(Lcom/mojang/serialization/DynamicOps;Ljava/lang/Object;)Lcom/mojang/serialization/DataResult;")})
    private static DataResult<?> applyResourceConditions(Codec<?> instance, DynamicOps<JsonElement> dynamicOps, Object object, Operation<DataResult<?>> original, @Local(argsOnly=true) FileToIdConverter resourceFinder, @Local(name={"entry"}) Map.Entry<Identifier, Resource> entry) {
        String dataType;
        JsonObject obj;
        JsonElement resourceData = (JsonElement)object;
        RegistryOps.RegistryInfoLookup registryInfo = null;
        if (dynamicOps instanceof RegistryOpsAccessor) {
            RegistryOpsAccessor registryOps = (RegistryOpsAccessor)((Object)dynamicOps);
            registryInfo = registryOps.getRegistryInfoGetter();
        }
        if (resourceData.isJsonObject() && !ResourceConditionsImpl.applyResourceConditions(obj = resourceData.getAsJsonObject(), dataType = resourceFinder.prefix(), entry.getKey(), registryInfo)) {
            return DataResult.success(SKIP_DATA_MARKER);
        }
        return original.call(instance, dynamicOps, object);
    }

    @Inject(method={"lambda$scanDirectory$0"}, at={@At(value="HEAD")}, cancellable=true)
    private static void skipData(Map<?, ?> map, Identifier identifier, Object object, CallbackInfo ci) {
        if (object == SKIP_DATA_MARKER) {
            ci.cancel();
        }
    }
}

