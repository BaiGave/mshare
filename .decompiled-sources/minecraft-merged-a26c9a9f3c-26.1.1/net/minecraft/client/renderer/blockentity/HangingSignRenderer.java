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
import net.minecraft.client.renderer.blockentity.state.HangingSignRenderState;
import net.minecraft.client.renderer.blockentity.state.SignRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
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
public class HangingSignRenderer
extends AbstractSignRenderer<HangingSignRenderState> {
    private static final String PLANK = "plank";
    private static final String V_CHAINS = "vChains";
    private static final String NORMAL_CHAINS = "normalChains";
    private static final String CHAIN_L_1 = "chainL1";
    private static final String CHAIN_L_2 = "chainL2";
    private static final String CHAIN_R_1 = "chainR1";
    private static final String CHAIN_R_2 = "chainR2";
    private static final String BOARD = "board";
    private static final float MODEL_RENDER_SCALE = 1.0f;
    private static final float TEXT_RENDER_SCALE = 0.9f;
    private static final Vector3fc TEXT_OFFSET = new Vector3f(0.0f, -0.32f, 0.073f);
    public static final WallAndGroundTransformations<SignRenderState.SignTransformations> TRANSFORMATIONS = new WallAndGroundTransformations<SignRenderState.SignTransformations>(HangingSignRenderer::createWallTransformation, HangingSignRenderer::createGroundTransformation, 16);
    private final Map<WoodType, Models> signModels = WoodType.values().collect(ImmutableMap.toImmutableMap(type -> type, type -> Models.create(context, type)));

    public HangingSignRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public HangingSignRenderState createRenderState() {
        return new HangingSignRenderState();
    }

    @Override
    public void extractRenderState(SignBlockEntity blockEntity, HangingSignRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        BlockState blockState = blockEntity.getBlockState();
        state.attachmentType = HangingSignBlock.getAttachmentPoint(blockState);
        state.transformations = blockState.getBlock() instanceof WallHangingSignBlock ? TRANSFORMATIONS.wallTransformation(blockState.getValue(WallHangingSignBlock.FACING)) : TRANSFORMATIONS.freeTransformations(blockState.getValue(CeilingHangingSignBlock.ROTATION));
    }

    public static Model.Simple createSignModel(EntityModelSet entityModelSet, WoodType woodType, HangingSignBlock.Attachment attachmentType) {
        return new Model.Simple(entityModelSet.bakeLayer(ModelLayers.createHangingSignModelName(woodType, attachmentType)), RenderTypes::entityCutout);
    }

    private static Matrix4f baseTransformation(float angle) {
        return new Matrix4f().translation(0.5f, 0.9375f, 0.5f).rotate(Axis.YP.rotationDegrees(-angle)).translate(0.0f, -0.3125f, 0.0f);
    }

    private static Transformation bodyTransformation(float angle) {
        return new Transformation(HangingSignRenderer.baseTransformation(angle).scale(1.0f, -1.0f, -1.0f));
    }

    private static Transformation textTransformation(float angle, boolean isFrontText) {
        Matrix4f result = HangingSignRenderer.baseTransformation(angle);
        if (!isFrontText) {
            result.rotate(Axis.YP.rotationDegrees(180.0f));
        }
        float s = 0.0140625f;
        result.translate(TEXT_OFFSET);
        result.scale(0.0140625f, -0.0140625f, 0.0140625f);
        return new Transformation(result);
    }

    private static SignRenderState.SignTransformations createTransformations(float angle) {
        return new SignRenderState.SignTransformations(HangingSignRenderer.bodyTransformation(angle), HangingSignRenderer.textTransformation(angle, true), HangingSignRenderer.textTransformation(angle, false));
    }

    private static SignRenderState.SignTransformations createGroundTransformation(int segment) {
        return HangingSignRenderer.createTransformations(RotationSegment.convertToDegrees(segment));
    }

    private static SignRenderState.SignTransformations createWallTransformation(Direction direction) {
        return HangingSignRenderer.createTransformations(direction.toYRot());
    }

    @Override
    protected Model.Simple getSignModel(HangingSignRenderState state) {
        return this.signModels.get(state.woodType).get(state.attachmentType);
    }

    @Override
    protected SpriteId getSignSprite(WoodType type) {
        return Sheets.getHangingSignSprite(type);
    }

    public static void submitSpecial(SpriteGetter sprites, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, Model.Simple model, SpriteId sprite) {
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, sprites, 0, null);
    }

    public static LayerDefinition createHangingSignLayer(HangingSignBlock.Attachment type) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild(BOARD, CubeListBuilder.create().texOffs(0, 12).addBox(-7.0f, 0.0f, -1.0f, 14.0f, 10.0f, 2.0f), PartPose.ZERO);
        if (type == HangingSignBlock.Attachment.WALL) {
            root.addOrReplaceChild(PLANK, CubeListBuilder.create().texOffs(0, 0).addBox(-8.0f, -6.0f, -2.0f, 16.0f, 2.0f, 4.0f), PartPose.ZERO);
        }
        if (type == HangingSignBlock.Attachment.WALL || type == HangingSignBlock.Attachment.CEILING) {
            PartDefinition normalChains = root.addOrReplaceChild(NORMAL_CHAINS, CubeListBuilder.create(), PartPose.ZERO);
            normalChains.addOrReplaceChild(CHAIN_L_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
            normalChains.addOrReplaceChild(CHAIN_L_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(-5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
            normalChains.addOrReplaceChild(CHAIN_R_1, CubeListBuilder.create().texOffs(0, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, -0.7853982f, 0.0f));
            normalChains.addOrReplaceChild(CHAIN_R_2, CubeListBuilder.create().texOffs(6, 6).addBox(-1.5f, 0.0f, 0.0f, 3.0f, 6.0f, 0.0f), PartPose.offsetAndRotation(5.0f, -6.0f, 0.0f, 0.0f, 0.7853982f, 0.0f));
        }
        if (type == HangingSignBlock.Attachment.CEILING_MIDDLE) {
            root.addOrReplaceChild(V_CHAINS, CubeListBuilder.create().texOffs(14, 6).addBox(-6.0f, -6.0f, 0.0f, 12.0f, 6.0f, 0.0f), PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Environment(value=EnvType.CLIENT)
    private record Models(Model.Simple ceiling, Model.Simple ceilingMiddle, Model.Simple wall) {
        public static Models create(BlockEntityRendererProvider.Context context, WoodType type) {
            return new Models(HangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.CEILING), HangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.CEILING_MIDDLE), HangingSignRenderer.createSignModel(context.entityModelSet(), type, HangingSignBlock.Attachment.WALL));
        }

        public Model.Simple get(HangingSignBlock.Attachment attachmentType) {
            return switch (attachmentType) {
                default -> throw new MatchException(null, null);
                case HangingSignBlock.Attachment.CEILING -> this.ceiling;
                case HangingSignBlock.Attachment.CEILING_MIDDLE -> this.ceilingMiddle;
                case HangingSignBlock.Attachment.WALL -> this.wall;
            };
        }
    }
}

