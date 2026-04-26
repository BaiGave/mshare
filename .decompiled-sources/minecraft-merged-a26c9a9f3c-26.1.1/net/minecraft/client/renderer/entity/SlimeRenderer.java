/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.monster.slime.SlimeModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeOuterLayer;
import net.minecraft.client.renderer.entity.state.SlimeRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Slime;

@Environment(value=EnvType.CLIENT)
public class SlimeRenderer
extends MobRenderer<Slime, SlimeRenderState, SlimeModel> {
    public static final Identifier SLIME_LOCATION = Identifier.withDefaultNamespace("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new SlimeModel(context.bakeLayer(ModelLayers.SLIME)), 0.25f);
        this.addLayer(new SlimeOuterLayer(this, context.getModelSet()));
    }

    @Override
    protected float getShadowRadius(SlimeRenderState state) {
        return (float)state.size * 0.25f;
    }

    @Override
    protected void scale(SlimeRenderState state, PoseStack poseStack) {
        float s = 0.999f;
        poseStack.scale(0.999f, 0.999f, 0.999f);
        poseStack.translate(0.0f, 0.001f, 0.0f);
        float size = state.size;
        float ss = state.squish / (size * 0.5f + 1.0f);
        float w = 1.0f / (ss + 1.0f);
        poseStack.scale(w * size, 1.0f / w * size, w * size);
    }

    @Override
    public Identifier getTextureLocation(SlimeRenderState state) {
        return SLIME_LOCATION;
    }

    @Override
    public SlimeRenderState createRenderState() {
        return new SlimeRenderState();
    }

    @Override
    public void extractRenderState(Slime entity, SlimeRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        state.squish = Mth.lerp(partialTicks, entity.oSquish, entity.squish);
        state.size = entity.getSize();
    }
}

