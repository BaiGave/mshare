/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering.fluid;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderingRegistry;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderingImpl;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={FluidRenderer.class})
public class FluidRendererMixin {
    @Inject(method={"tesselate"}, at={@At(value="HEAD")}, cancellable=true)
    public void onHeadRender(BlockAndTintGetter view, BlockPos pos, FluidRenderer.Output output, BlockState blockState, FluidState fluidState, CallbackInfo ci) {
        if (FluidRenderingImpl.IS_RENDERING_VANILLA_DEFAULT.isBound()) {
            return;
        }
        FluidRenderHandler handler = FluidRenderingRegistry.get(fluidState.getType());
        if (handler != null) {
            handler.renderFluid((FluidRenderer)((Object)this), pos, view, output, blockState, fluidState);
            ci.cancel();
        }
    }

    @ModifyExpressionValue(method={"tesselate"}, at={@At(value="MIXINEXTRAS:EXPRESSION")})
    @Definition(id="HalfTransparentBlock", type={HalfTransparentBlock.class})
    @Expression(value={"? instanceof HalfTransparentBlock"})
    private boolean modifyNonOverlayCheck(boolean original, @Local(name={"relativeBlock"}) Block block) {
        return FluidRenderingRegistry.isBlockTransparent(block);
    }
}

