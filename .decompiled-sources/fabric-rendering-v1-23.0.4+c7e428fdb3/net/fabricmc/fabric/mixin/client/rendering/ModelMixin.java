/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.function.Function;
import net.fabricmc.fabric.api.client.rendering.v1.FabricModel;
import net.fabricmc.fabric.impl.client.rendering.ModelExtensions;
import net.fabricmc.fabric.mixin.client.rendering.ModelPartAccessor;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Model.class})
abstract class ModelMixin<S>
implements FabricModel<S>,
ModelExtensions {
    @Unique
    private final Map<String, ModelPart> childPartMap = new Object2ObjectOpenHashMap<String, ModelPart>();

    ModelMixin() {
    }

    @Shadow
    public abstract ModelPart root();

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void fillChildPartMap(ModelPart root, Function<Identifier, RenderType> layerFactory, CallbackInfo ci) {
        this.fabric$calculateChildParts(root);
    }

    @Override
    public void fabric$calculateChildParts(ModelPart root) {
        ((ModelPartAccessor)((Object)root)).fabric$callAddAllChildren(this.childPartMap::putIfAbsent);
    }

    @Override
    public @Nullable ModelPart getChildPart(String name) {
        return this.childPartMap.get(name);
    }

    @Override
    public void copyTransforms(Model<?> model) {
        ModelMixin.copyTransforms(model.root(), this.root());
        ((ModelPartAccessor)((Object)model.root())).fabric$callAddAllChildren((name, part) -> {
            ModelPart childPart = this.getChildPart((String)name);
            if (childPart != null) {
                ModelMixin.copyTransforms(part, childPart);
            }
        });
    }

    @Unique
    private static void copyTransforms(ModelPart from, ModelPart to) {
        to.x = from.x;
        to.y = from.y;
        to.z = from.z;
        to.xRot = from.xRot;
        to.yRot = from.yRot;
        to.zRot = from.zRot;
        to.xScale = from.xScale;
        to.yScale = from.yScale;
        to.zScale = from.zScale;
    }
}

