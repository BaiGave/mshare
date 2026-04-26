/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.AbstractSignRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.WallAndGroundTransformations;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.blockentity.state.StandingSignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class StandingSignRenderer
extends AbstractSignRenderer<StandingSignRenderState> {
    private static final float RENDER_SCALE = 0.6666667f;
    private static final Vector3fc TEXT_OFFSET = new Vector3f(0.0f, 0.33333334f, 0.046666667f);
    public static final WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS = new WallAndGroundTransformations<SignRenderState.SignTransformations>(StandingSignRenderer::createWallTransformation, StandingSignRenderer::createGroundTransformation, 16);
    private final Map<WoodType, Models> signModels = WoodType.values().collect(ImmutableMap.toImmutableMap(type -> type, type -> Models.create(context, type)));

    public StandingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public StandingSignRenderState createRenderState() {
        return new StandingSignRenderState();
    }

    @Override
    public void extractRenderState(SignBlockEntity blockEntity, StandingSignRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.attachmentType = PlainSignBlock.getAttachmentPoint(blockState);
        state.transformations = blockState.getBlock() instanceof WallSignBlock ? TRANSFORMATIONS.wallTransformation(blockState.getValue(WallSignBlock.FACING)) : TRANSFORMATIONS.freeTransformations(blockState.getValue(StandingSignBlock.ROTATION));
    }

    @Override
    protected Model.Simple getSignModel(StandingSignRenderState state) {
        return this.signModels.get(state.woodType).get(state.attachmentType);
    }

    @Override
    protected SpriteId getSignSprite(WoodType type) {
        return Sheets.getSignSprite(type);
    }

    private static Matrix4f baseTransformation(float angle, PlainSignBlock.Attachment attachmentType) {
        Matrix4f result = new Matrix4f().translate(0.5f, 0.5f, 0.5f).rotate(Axis.YP.rotationDegrees(-angle));
        if (attachmentType == PlainSignBlock.Attachment.WALL) {
            result.translate(0.0f, -0.3125f, -0.4375f);
        }
        return result;
    }

    private static Transformation bodyTransformation(PlainSignBlock.Attachment attachmentType, float angle) {
        return new Transformation(StandingSignRenderer.baseTransformation(angle, attachmentType).scale(0.6666667f, -0.6666667f, -0.6666667f));
    }

    private static Transformation textTransformation(PlainSignBlock.Attachment attachmentType, float angle, boolean isFrontText) {
        Matrix4f result = StandingSignRenderer.baseTransformation(angle, attachmentType);
        if (!isFrontText) {
            result.rotate(Axis.YP.rotationDegrees(180.0f));
        }
        float s = 0.010416667f;
        return new Transformation(result.translate(TEXT_OFFSET).scale(0.010416667f, -0.010416667f, 0.010416667f));
    }

    private static SignRenderState.SignTransformations createTransformations(PlainSignBlock.Attachment attachmentType, float angle) {
        return new SignRenderState.SignTransformations(StandingSignRenderer.bodyTransformation(attachmentType, angle), StandingSignRenderer.textTransformation(attachmentType, angle, true), StandingSignRenderer.textTransformation(attachmentType, angle, false));
    }

    private static SignRenderState.SignTransformations createGroundTransformation(int segment) {
        return StandingSignRenderer.createTransformations(PlainSignBlock.Attachment.GROUND, RotationSegment.convertToDegrees(segment));
    }

    private static SignRenderState.SignTransformations createWallTransformation(Direction direction) {
        return StandingSignRenderer.createTransformations(PlainSignBlock.Attachment.WALL, direction.toYRot());
    }

    public static void submitSpecial(SpriteGetter sprites, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, Model.Simple model, SpriteId sprite) {
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, 0, null);
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, PlainSignBlock.Attachment attachment) {
        ModelLayerLocation layer = switch (attachment) {
            default -> throw new MatchException(null, null);
            case PlainSignBlock.Attachment.GROUND -> ModelLayers.createStandingSignModelName(woodType);
            case PlainSignBlock.Attachment.WALL -> ModelLayers.createWallSignModelName(woodType);
        };
        return new Model.Simple(entityModelSet.bakeLayer(layer), RenderTypes::entityCutout);
    }

    public static LayerDefinition createSignLayer(boolean standing) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("sign", CubeListBuilder.create().texOffs(0, 0).addBox(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f), PartPose.ZERO);
        if (standing) {
            root.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 14).addBox(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    private record Models(Model.Simple standing, Model.Simple wall) {
        public static Models create(BlockEntityRendererProvider.Context context, WoodType type) {
            return new Models(StandingSignRenderer.createSignModel(context.entityModelSet(), type, PlainSignBlock.Attachment.GROUND), StandingSignRenderer.createSignModel(context.entityModelSet(), type, PlainSignBlock.Attachment.WALL));
        }

        public Model.Simple get(PlainSignBlock.Attachment attachmentType) {
            return switch (attachmentType) {
                default -> throw new MatchException(null, null);
                case PlainSignBlock.Attachment.GROUND -> this.standing;
                case PlainSignBlock.Attachment.WALL -> this.wall;
            };
        }
    }
}

