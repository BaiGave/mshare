/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.special;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import java.util.function.Consumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.object.projectile.TridentModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import org.joml.Vector3f;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public class TridentSpecialRenderer
implements NoDataSpecialModelRenderer {
    public static final Transformation DEFAULT_TRANSFORMATION = new Transformation(null, null, new Vector3f(1.0f, -1.0f, -1.0f), null);
    private final TridentModel model;

    public TridentSpecialRenderer(TridentModel model) {
        this.model = model;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
        submitNodeCollector.submitModelPart(this.model.root(), poseStack, this.model.renderType(TridentModel.TEXTURE), lightCoords, overlayCoords, null, false, hasFoil, -1, null, outlineColor);
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.model.root().getExtentsForGui(poseStack, output);
    }

    @Environment(value=EnvType.CLIENT)
    public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked
    {
        public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        public TridentSpecialRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TridentSpecialRenderer(new TridentModel(context.entityModelSet().bakeLayer(ModelLayers.TRIDENT)));
        }
    }
}

