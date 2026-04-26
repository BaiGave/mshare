/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model.sprite;

import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public record SpriteId(Identifier atlasLocation, Identifier texture) {
    public RenderType renderType(Function<Identifier, RenderType> renderType) {
        return renderType.apply(this.atlasLocation);
    }

    public VertexConsumer buffer(SpriteGetter sprites, MultiBufferSource bufferSource, Function<Identifier, RenderType> renderType) {
        return sprites.get(this).wrap(bufferSource.getBuffer(this.renderType(renderType)));
    }

    public VertexConsumer buffer(SpriteGetter sprites, MultiBufferSource bufferSource, Function<Identifier, RenderType> renderType, boolean sheeted, boolean hasFoil) {
        return sprites.get(this).wrap(ItemFeatureRenderer.getFoilBuffer(bufferSource, this.renderType(renderType), sheeted, hasFoil));
    }
}

