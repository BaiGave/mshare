/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.indigo.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.item.ItemDisplayContext;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ItemFeatureRenderer.class})
public interface ItemFeatureRendererAccessor {
    @Invoker(value="getFoilBuffer")
    public static VertexConsumer fabric_getFoilBuffer(MultiBufferSource bufferSource, RenderType renderType,  @Nullable PoseStack.Pose foilDecalPose) {
        throw new AssertionError();
    }

    @Invoker(value="computeFoilDecalPose")
    public static PoseStack.Pose fabric_computeFoilDecalPose(ItemDisplayContext type, PoseStack.Pose pose) {
        throw new AssertionError();
    }
}

