/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.blockentity.state.BedRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoubleBlockCombiner;
import net.minecraft.world.level.block.entity.BedBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class BedRenderer
implements BlockEntityRenderer<BedBlockEntity, BedRenderState> {
    private static final Map<Direction, Transformation> TRANSFORMATIONS = Util.makeEnumMap(Direction.class, BedRenderer::createModelTransform);
    private static final Set<Direction> VISIBLE_LEG_FACES = Util.allOfEnumExcept(Direction.DOWN);
    private final SpriteGetter sprites;
    private final Model.Simple headModel;
    private final Model.Simple footModel;

    public BedRenderer(BlockEntityRendererProvider.Context context) {
        this(context.sprites(), context.entityModelSet());
    }

    public BedRenderer(SpecialModelRenderer.BakingContext context) {
        this(context.sprites(), context.entityModelSet());
    }

    public BedRenderer(SpriteGetter sprites, EntityModelSet entityModelSet) {
        this.sprites = sprites;
        this.headModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_HEAD), RenderTypes::entitySolid);
        this.footModel = new Model.Simple(entityModelSet.bakeLayer(ModelLayers.BED_FOOT), RenderTypes::entitySolid);
    }

    public static LayerDefinition createHeadLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        Set<Direction> visibleBodyFaces = Util.allOfEnumExcept(Direction.UP);
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f, visibleBodyFaces), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 6).addBox(0.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f, VISIBLE_LEG_FACES), PartPose.rotation(1.5707964f, 0.0f, 1.5707964f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 18).addBox(-16.0f, 6.0f, 0.0f, 3.0f, 3.0f, 3.0f, VISIBLE_LEG_FACES), PartPose.rotation(1.5707964f, 0.0f, (float)Math.PI));
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static LayerDefinition createFootLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        Set<Direction> visibleBodyFaces = Util.allOfEnumExcept(Direction.DOWN);
        root.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 22).addBox(0.0f, 0.0f, 0.0f, 16.0f, 16.0f, 6.0f, visibleBodyFaces), PartPose.ZERO);
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(50, 0).addBox(0.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f, VISIBLE_LEG_FACES), PartPose.rotation(1.5707964f, 0.0f, 0.0f));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(50, 12).addBox(-16.0f, 6.0f, -16.0f, 3.0f, 3.0f, 3.0f, VISIBLE_LEG_FACES), PartPose.rotation(1.5707964f, 0.0f, 4.712389f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public BedRenderState createRenderState() {
        return new BedRenderState();
    }

    @Override
    public void extractRenderState(BedBlockEntity blockEntity, BedRenderState state, float partialTicks, Vec3 cameraPosition, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.color = blockEntity.getColor();
        state.facing = (Direction)blockEntity.getBlockState().getValue(BedBlock.FACING);
        state.part = blockEntity.getBlockState().getValue(BedBlock.PART);
        if (blockEntity.getLevel() != null) {
            DoubleBlockCombiner.NeighborCombineResult<BedBlockEntity> combineResult = DoubleBlockCombiner.combineWithNeigbour(BlockEntityType.BED, BedBlock::getBlockType, BedBlock::getConnectedDirection, ChestBlock.FACING, blockEntity.getBlockState(), blockEntity.getLevel(), blockEntity.getBlockPos(), (levelAccessor, blockPos) -> false);
            state.lightCoords = ((Int2IntFunction)combineResult.apply(new BrightnessCombiner())).get(state.lightCoords);
        }
    }

    @Override
    public void submit(BedRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        SpriteId sprite = Sheets.getBedSprite(state.color);
        poseStack.pushPose();
        poseStack.mulPose(BedRenderer.modelTransform(state.facing));
        this.submitPiece(state.part, sprite, poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.breakProgress, 0);
        poseStack.popPose();
    }

    public void submitPiece(BedPart part, SpriteId sprite, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords, int overlayCoords, @Nullable ModelFeatureRenderer.CrumblingOverlay breakProgress, int outlineColor) {
        Model.Simple model = this.getPieceModel(part);
        submitNodeCollector.submitModel(model, Unit.INSTANCE, poseStack, lightCoords, overlayCoords, -1, sprite, this.sprites, outlineColor, breakProgress);
    }

    private Model.Simple getPieceModel(BedPart part) {
        return switch (part) {
            default -> throw new MatchException(null, null);
            case BedPart.HEAD -> this.headModel;
            case BedPart.FOOT -> this.footModel;
        };
    }

    private static Transformation createModelTransform(Direction direction) {
        return new Transformation(new Matrix4f().translation(0.0f, 0.5625f, 0.0f).rotate(Axis.XP.rotationDegrees(90.0f)).rotateAround(Axis.ZP.rotationDegrees(180.0f + direction.toYRot()), 0.5f, 0.5f, 0.5f));
    }

    public static Transformation modelTransform(Direction direction) {
        return TRANSFORMATIONS.get(direction);
    }

    public void getExtents(BedPart part, Consumer<Vector3fc> output) {
        PoseStack poseStack = new PoseStack();
        this.getPieceModel(part).root().getExtentsForGui(poseStack, output);
    }
}

