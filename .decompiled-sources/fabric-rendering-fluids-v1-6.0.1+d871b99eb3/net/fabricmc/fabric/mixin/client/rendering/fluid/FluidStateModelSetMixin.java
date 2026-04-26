/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingRegistryImpl;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.block.FluidStateModelSet;
import net.minecraft.client.resources.model.sprite.MaterialBaker;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={FluidStateModelSet.class})
public abstract class FluidStateModelSetMixin {
    @WrapMethod(method={"bake"})
    private static Map<Fluid, FluidModel> bake(MaterialBaker materials, Operation<Map<Fluid, FluidModel>> original) {
        IdentityHashMap<Fluid, FluidModel> models = new IdentityHashMap<Fluid, FluidModel>(original.call(materials));
        IdentityHashMap<FluidModel.Unbaked, FluidModel> bakedModels = new IdentityHashMap<FluidModel.Unbaked, FluidModel>();
        for (Map.Entry<Fluid, FluidModel.Unbaked> entry : FluidRenderingRegistryImpl.getUnbakedModels().entrySet()) {
            FluidModel model = (FluidModel)bakedModels.get(entry.getValue());
            if (model == null) {
                model = entry.getValue().bake(materials, () -> ((Fluid)entry.getKey()).toString());
                bakedModels.put(entry.getValue(), model);
            }
            models.put(entry.getKey(), model);
        }
        return Collections.unmodifiableMap(models);
    }
}

