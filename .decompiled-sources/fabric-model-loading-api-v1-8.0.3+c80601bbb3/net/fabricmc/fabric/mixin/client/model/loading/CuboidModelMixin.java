/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.fabricmc.fabric.impl.client.model.loading.UnbakedModelJsonDeserializer;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={CuboidModel.class})
abstract class CuboidModelMixin {
    CuboidModelMixin() {
    }

    @ModifyExpressionValue(method={"<clinit>()V"}, at={@At(value="NEW", target="com/google/gson/GsonBuilder")})
    private static GsonBuilder addUnbakedModelAdapter(GsonBuilder builder) {
        return builder.registerTypeHierarchyAdapter(UnbakedModel.class, new UnbakedModelJsonDeserializer());
    }
}

