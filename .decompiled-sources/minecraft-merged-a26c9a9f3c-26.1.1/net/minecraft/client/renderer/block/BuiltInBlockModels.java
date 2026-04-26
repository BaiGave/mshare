/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block;

import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.block.SelectBlockModel;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.BlockStateModelWrapper;
import net.minecraft.client.renderer.block.model.CompositeBlockModel;
import net.minecraft.client.renderer.block.model.ConditionalBlockModel;
import net.minecraft.client.renderer.block.model.EmptyBlockModel;
import net.minecraft.client.renderer.block.model.SpecialBlockModelWrapper;
import net.minecraft.client.renderer.block.model.properties.conditional.IsXmas;
import net.minecraft.client.renderer.block.model.properties.select.DisplayContext;
import net.minecraft.client.renderer.blockentity.BannerRenderer;
import net.minecraft.client.renderer.blockentity.BedRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.blockentity.ConduitRenderer;
import net.minecraft.client.renderer.blockentity.CopperGolemStatueBlockRenderer;
import net.minecraft.client.renderer.blockentity.DecoratedPotRenderer;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.ShulkerBoxRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.blockentity.StandingSignRenderer;
import net.minecraft.client.renderer.blockentity.TheEndPortalRenderer;
import net.minecraft.client.renderer.entity.CopperGolemRenderer;
import net.minecraft.client.renderer.special.BannerSpecialRenderer;
import net.minecraft.client.renderer.special.BedSpecialRenderer;
import net.minecraft.client.renderer.special.BellSpecialRenderer;
import net.minecraft.client.renderer.special.BookSpecialRenderer;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.special.ConduitSpecialRenderer;
import net.minecraft.client.renderer.special.CopperGolemStatueSpecialRenderer;
import net.minecraft.client.renderer.special.DecoratedPotSpecialRenderer;
import net.minecraft.client.renderer.special.EndCubeSpecialRenderer;
import net.minecraft.client.renderer.special.HangingSignSpecialRenderer;
import net.minecraft.client.renderer.special.PlayerHeadSpecialRenderer;
import net.minecraft.client.renderer.special.ShulkerBoxSpecialRenderer;
import net.minecraft.client.renderer.special.SkullSpecialRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.special.StandingSignSpecialRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CeilingHangingSignBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.HangingSignBlock;
import net.minecraft.world.level.block.PlainSignBlock;
import net.minecraft.world.level.block.PlayerHeadBlock;
import net.minecraft.world.level.block.PlayerWallHeadBlock;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.WallHangingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.joml.Vector3f;

@Environment(value=EnvType.CLIENT)
public class BuiltInBlockModels {
    private static void addDefaults(Builder builder) {
        BuiltInBlockModels.createAir(builder, Blocks.AIR);
        BuiltInBlockModels.createAir(builder, Blocks.CAVE_AIR);
        BuiltInBlockModels.createAir(builder, Blocks.VOID_AIR);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.SKELETON, Blocks.SKELETON_SKULL, Blocks.SKELETON_WALL_SKULL);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.ZOMBIE, Blocks.ZOMBIE_HEAD, Blocks.ZOMBIE_WALL_HEAD);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.CREEPER, Blocks.CREEPER_HEAD, Blocks.CREEPER_WALL_HEAD);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.DRAGON, Blocks.DRAGON_HEAD, Blocks.DRAGON_WALL_HEAD);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.PIGLIN, Blocks.PIGLIN_HEAD, Blocks.PIGLIN_WALL_HEAD);
        BuiltInBlockModels.createMobHeads(builder, SkullBlock.Types.WITHER_SKELETON, Blocks.WITHER_SKELETON_SKULL, Blocks.WITHER_SKELETON_WALL_SKULL);
        builder.put(BuiltInBlockModels.createPlayerHead(), Blocks.PLAYER_HEAD);
        builder.put(BuiltInBlockModels.createPlayerWallHead(), Blocks.PLAYER_WALL_HEAD);
        BuiltInBlockModels.createBanners(builder, DyeColor.WHITE, Blocks.WHITE_BANNER, Blocks.WHITE_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.ORANGE, Blocks.ORANGE_BANNER, Blocks.ORANGE_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.MAGENTA, Blocks.MAGENTA_BANNER, Blocks.MAGENTA_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.LIGHT_BLUE, Blocks.LIGHT_BLUE_BANNER, Blocks.LIGHT_BLUE_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.YELLOW, Blocks.YELLOW_BANNER, Blocks.YELLOW_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.LIME, Blocks.LIME_BANNER, Blocks.LIME_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.PINK, Blocks.PINK_BANNER, Blocks.PINK_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.GRAY, Blocks.GRAY_BANNER, Blocks.GRAY_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.LIGHT_GRAY, Blocks.LIGHT_GRAY_BANNER, Blocks.LIGHT_GRAY_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.CYAN, Blocks.CYAN_BANNER, Blocks.CYAN_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.PURPLE, Blocks.PURPLE_BANNER, Blocks.PURPLE_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.BLUE, Blocks.BLUE_BANNER, Blocks.BLUE_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.BROWN, Blocks.BROWN_BANNER, Blocks.BROWN_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.GREEN, Blocks.GREEN_BANNER, Blocks.GREEN_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.RED, Blocks.RED_BANNER, Blocks.RED_WALL_BANNER);
        BuiltInBlockModels.createBanners(builder, DyeColor.BLACK, Blocks.BLACK_BANNER, Blocks.BLACK_WALL_BANNER);
        builder.put(BuiltInBlockModels.createBed(DyeColor.WHITE), Blocks.WHITE_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.ORANGE), Blocks.ORANGE_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.MAGENTA), Blocks.MAGENTA_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.LIGHT_BLUE), Blocks.LIGHT_BLUE_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.YELLOW), Blocks.YELLOW_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.LIME), Blocks.LIME_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.PINK), Blocks.PINK_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.GRAY), Blocks.GRAY_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.LIGHT_GRAY), Blocks.LIGHT_GRAY_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.CYAN), Blocks.CYAN_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.PURPLE), Blocks.PURPLE_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.BLUE), Blocks.BLUE_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.BROWN), Blocks.BROWN_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.GREEN), Blocks.GREEN_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.RED), Blocks.RED_BED);
        builder.put(BuiltInBlockModels.createBed(DyeColor.BLACK), Blocks.BLACK_BED);
        builder.put(BuiltInBlockModels.createShulkerBox(), Blocks.SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.WHITE), Blocks.WHITE_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.ORANGE), Blocks.ORANGE_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.MAGENTA), Blocks.MAGENTA_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.LIGHT_BLUE), Blocks.LIGHT_BLUE_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.YELLOW), Blocks.YELLOW_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.LIME), Blocks.LIME_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.PINK), Blocks.PINK_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.GRAY), Blocks.GRAY_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.LIGHT_GRAY), Blocks.LIGHT_GRAY_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.CYAN), Blocks.CYAN_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.PURPLE), Blocks.PURPLE_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.BLUE), Blocks.BLUE_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.BROWN), Blocks.BROWN_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.GREEN), Blocks.GREEN_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.RED), Blocks.RED_SHULKER_BOX);
        builder.put(BuiltInBlockModels.createDyedShulkerBox(DyeColor.BLACK), Blocks.BLACK_SHULKER_BOX);
        BuiltInBlockModels.createSigns(builder, WoodType.OAK, Blocks.OAK_SIGN, Blocks.OAK_WALL_SIGN, Blocks.OAK_HANGING_SIGN, Blocks.OAK_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.SPRUCE, Blocks.SPRUCE_SIGN, Blocks.SPRUCE_WALL_SIGN, Blocks.SPRUCE_HANGING_SIGN, Blocks.SPRUCE_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.BIRCH, Blocks.BIRCH_SIGN, Blocks.BIRCH_WALL_SIGN, Blocks.BIRCH_HANGING_SIGN, Blocks.BIRCH_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.ACACIA, Blocks.ACACIA_SIGN, Blocks.ACACIA_WALL_SIGN, Blocks.ACACIA_HANGING_SIGN, Blocks.ACACIA_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.CHERRY, Blocks.CHERRY_SIGN, Blocks.CHERRY_WALL_SIGN, Blocks.CHERRY_HANGING_SIGN, Blocks.CHERRY_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.JUNGLE, Blocks.JUNGLE_SIGN, Blocks.JUNGLE_WALL_SIGN, Blocks.JUNGLE_HANGING_SIGN, Blocks.JUNGLE_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.DARK_OAK, Blocks.DARK_OAK_SIGN, Blocks.DARK_OAK_WALL_SIGN, Blocks.DARK_OAK_HANGING_SIGN, Blocks.DARK_OAK_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.PALE_OAK, Blocks.PALE_OAK_SIGN, Blocks.PALE_OAK_WALL_SIGN, Blocks.PALE_OAK_HANGING_SIGN, Blocks.PALE_OAK_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.MANGROVE, Blocks.MANGROVE_SIGN, Blocks.MANGROVE_WALL_SIGN, Blocks.MANGROVE_HANGING_SIGN, Blocks.MANGROVE_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.BAMBOO, Blocks.BAMBOO_SIGN, Blocks.BAMBOO_WALL_SIGN, Blocks.BAMBOO_HANGING_SIGN, Blocks.BAMBOO_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.CRIMSON, Blocks.CRIMSON_SIGN, Blocks.CRIMSON_WALL_SIGN, Blocks.CRIMSON_HANGING_SIGN, Blocks.CRIMSON_WALL_HANGING_SIGN);
        BuiltInBlockModels.createSigns(builder, WoodType.WARPED, Blocks.WARPED_SIGN, Blocks.WARPED_WALL_SIGN, Blocks.WARPED_HANGING_SIGN, Blocks.WARPED_WALL_HANGING_SIGN);
        builder.put(BuiltInBlockModels.createSingletonChest(ChestSpecialRenderer.ENDER_CHEST), Blocks.ENDER_CHEST);
        builder.put(BuiltInBlockModels.createXmasChest(ChestSpecialRenderer.REGULAR), Blocks.CHEST);
        builder.put(BuiltInBlockModels.createXmasChest(ChestSpecialRenderer.TRAPPED), Blocks.TRAPPED_CHEST);
        builder.put(BuiltInBlockModels.createChest(ChestSpecialRenderer.COPPER_UNAFFECTED), Blocks.COPPER_CHEST, Blocks.WAXED_COPPER_CHEST);
        builder.put(BuiltInBlockModels.createChest(ChestSpecialRenderer.COPPER_EXPOSED), Blocks.EXPOSED_COPPER_CHEST, Blocks.WAXED_EXPOSED_COPPER_CHEST);
        builder.put(BuiltInBlockModels.createChest(ChestSpecialRenderer.COPPER_WEATHERED), Blocks.WEATHERED_COPPER_CHEST, Blocks.WAXED_WEATHERED_COPPER_CHEST);
        builder.put(BuiltInBlockModels.createChest(ChestSpecialRenderer.COPPER_OXIDIZED), Blocks.OXIDIZED_COPPER_CHEST, Blocks.WAXED_OXIDIZED_COPPER_CHEST);
        builder.put(BuiltInBlockModels.createCopperGolem(WeatheringCopper.WeatherState.UNAFFECTED), Blocks.COPPER_GOLEM_STATUE, Blocks.WAXED_COPPER_GOLEM_STATUE);
        builder.put(BuiltInBlockModels.createCopperGolem(WeatheringCopper.WeatherState.EXPOSED), Blocks.EXPOSED_COPPER_GOLEM_STATUE, Blocks.WAXED_EXPOSED_COPPER_GOLEM_STATUE);
        builder.put(BuiltInBlockModels.createCopperGolem(WeatheringCopper.WeatherState.WEATHERED), Blocks.WEATHERED_COPPER_GOLEM_STATUE, Blocks.WAXED_WEATHERED_COPPER_GOLEM_STATUE);
        builder.put(BuiltInBlockModels.createCopperGolem(WeatheringCopper.WeatherState.OXIDIZED), Blocks.OXIDIZED_COPPER_GOLEM_STATUE, Blocks.WAXED_OXIDIZED_COPPER_GOLEM_STATUE);
        builder.put(BuiltInBlockModels.special(new BellSpecialRenderer.Unbaked()), Blocks.BELL);
        builder.put(BuiltInBlockModels.special(new ConduitSpecialRenderer.Unbaked(), ConduitRenderer.DEFAULT_TRANSFORMATION), Blocks.CONDUIT);
        builder.put(BuiltInBlockModels.createDecoratedPot(), Blocks.DECORATED_POT);
        builder.put(BuiltInBlockModels.createEnchantingTable(), Blocks.ENCHANTING_TABLE);
        builder.put(BuiltInBlockModels.special(new EndCubeSpecialRenderer.Unbaked(EndCubeSpecialRenderer.Type.GATEWAY)), Blocks.END_GATEWAY);
        builder.put(BuiltInBlockModels.special(new EndCubeSpecialRenderer.Unbaked(EndCubeSpecialRenderer.Type.PORTAL), TheEndPortalRenderer.TRANSFORMATION), Blocks.END_PORTAL);
        builder.put(BuiltInBlockModels::createFlowerBedModel, Blocks.WILDFLOWERS, Blocks.PINK_PETALS);
    }

    private static void createAir(Builder builder, Block block) {
        builder.put(new EmptyBlockModel.Unbaked(), block);
    }

    private static BlockModel.Unbaked special(SpecialModelRenderer.Unbaked<?> model) {
        return new SpecialBlockModelWrapper.Unbaked(model, Optional.empty());
    }

    private static BlockModel.Unbaked special(SpecialModelRenderer.Unbaked<?> model, Transformation transformation) {
        return new SpecialBlockModelWrapper.Unbaked(model, Optional.of(transformation));
    }

    private static SpecialModelFactory createMobHead(SkullBlock.Types type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(SkullBlock.ROTATION, rotation -> BuiltInBlockModels.special(new SkullSpecialRenderer.Unbaked(type), SkullBlockRenderer.TRANSFORMATIONS.freeTransformations((int)rotation)));
    }

    private static SpecialModelFactory createMobWallHead(SkullBlock.Types type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(WallSkullBlock.FACING, facing -> BuiltInBlockModels.special(new SkullSpecialRenderer.Unbaked(type), SkullBlockRenderer.TRANSFORMATIONS.wallTransformation((Direction)facing)));
    }

    private static void createMobHeads(Builder builder, SkullBlock.Types type, Block ground, Block wall) {
        builder.put(BuiltInBlockModels.createMobHead(type), ground);
        builder.put(BuiltInBlockModels.createMobWallHead(type), wall);
    }

    private static SpecialModelFactory createPlayerHead() {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(PlayerHeadBlock.ROTATION, rotation -> BuiltInBlockModels.special(new PlayerHeadSpecialRenderer.Unbaked(), SkullBlockRenderer.TRANSFORMATIONS.freeTransformations((int)rotation)));
    }

    private static SpecialModelFactory createPlayerWallHead() {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(PlayerWallHeadBlock.FACING, facing -> BuiltInBlockModels.special(new PlayerHeadSpecialRenderer.Unbaked(), SkullBlockRenderer.TRANSFORMATIONS.wallTransformation((Direction)facing)));
    }

    private static SpecialModelFactory createBanner(DyeColor color) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(BannerBlock.ROTATION, rotation -> BuiltInBlockModels.special(new BannerSpecialRenderer.Unbaked(color, BannerBlock.AttachmentType.GROUND), BannerRenderer.TRANSFORMATIONS.freeTransformations((int)rotation)));
    }

    private static SpecialModelFactory createWallBanner(DyeColor color) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(WallBannerBlock.FACING, facing -> BuiltInBlockModels.special(new BannerSpecialRenderer.Unbaked(color, BannerBlock.AttachmentType.WALL), BannerRenderer.TRANSFORMATIONS.wallTransformation((Direction)facing)));
    }

    private static void createBanners(Builder builder, DyeColor dye, Block ground, Block wall) {
        builder.put(BuiltInBlockModels.createBanner(dye), ground);
        builder.put(BuiltInBlockModels.createWallBanner(dye), wall);
    }

    private static SpecialModelFactory createBed(DyeColor color) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(BedBlock.FACING, BedBlock.PART, (facing, part) -> BuiltInBlockModels.special(new BedSpecialRenderer.Unbaked(color, (BedPart)part), BedRenderer.modelTransform(facing)));
    }

    private static SpecialModelFactory createShulkerBox() {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(ShulkerBoxBlock.FACING, facing -> BuiltInBlockModels.special(new ShulkerBoxSpecialRenderer.Unbaked(), ShulkerBoxRenderer.modelTransform(facing)));
    }

    private static SpecialModelFactory createDyedShulkerBox(DyeColor color) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(ShulkerBoxBlock.FACING, facing -> BuiltInBlockModels.special(new ShulkerBoxSpecialRenderer.Unbaked(color), ShulkerBoxRenderer.modelTransform(facing)));
    }

    private static SpecialModelFactory createStandingSign(WoodType type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(StandingSignBlock.ROTATION, rotation -> BuiltInBlockModels.special(new StandingSignSpecialRenderer.Unbaked(type, PlainSignBlock.Attachment.GROUND), StandingSignRenderer.TRANSFORMATIONS.freeTransformations((int)rotation).body()));
    }

    private static SpecialModelFactory createWallSign(WoodType type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(WallSignBlock.FACING, facing -> BuiltInBlockModels.special(new StandingSignSpecialRenderer.Unbaked(type, PlainSignBlock.Attachment.WALL), StandingSignRenderer.TRANSFORMATIONS.wallTransformation((Direction)facing).body()));
    }

    private static SpecialModelFactory createCeilingHangingSign(WoodType type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(CeilingHangingSignBlock.ROTATION, CeilingHangingSignBlock.ATTACHED, (rotation, attached) -> BuiltInBlockModels.special(new HangingSignSpecialRenderer.Unbaked(type, CeilingHangingSignBlock.getAttachmentPoint(attached)), HangingSignRenderer.TRANSFORMATIONS.freeTransformations((int)rotation).body()));
    }

    private static SpecialModelFactory createWallHangingSign(WoodType type) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(WallHangingSignBlock.FACING, facing -> BuiltInBlockModels.special(new HangingSignSpecialRenderer.Unbaked(type, HangingSignBlock.Attachment.WALL), HangingSignRenderer.TRANSFORMATIONS.wallTransformation((Direction)facing).body()));
    }

    private static void createSigns(Builder builder, WoodType woodType, Block standing, Block wall, Block hanging, Block wallHanging) {
        builder.put(BuiltInBlockModels.createStandingSign(woodType), standing);
        builder.put(BuiltInBlockModels.createWallSign(woodType), wall);
        builder.put(BuiltInBlockModels.createCeilingHangingSign(woodType), hanging);
        builder.put(BuiltInBlockModels.createWallHangingSign(woodType), wallHanging);
    }

    private static BlockModel.Unbaked createChest(Identifier texture, ChestType chestType, Direction facing) {
        return BuiltInBlockModels.special(new ChestSpecialRenderer.Unbaked(texture, chestType), ChestRenderer.modelTransformation(facing));
    }

    private static SpecialModelFactory createSingletonChest(Identifier texture) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(ChestBlock.FACING, facing -> BuiltInBlockModels.createChest(texture, ChestType.SINGLE, facing));
    }

    private static SpecialModelFactory createChest(MultiblockChestResources<Identifier> textures) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(ChestBlock.FACING, ChestBlock.TYPE, (facing, type) -> BuiltInBlockModels.createChest((Identifier)textures.select((ChestType)type), type, facing));
    }

    private static SpecialModelFactory createXmasChest(MultiblockChestResources<Identifier> textures) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(ChestBlock.FACING, ChestBlock.TYPE, (facing, type) -> new ConditionalBlockModel.Unbaked(Optional.empty(), new IsXmas(), BuiltInBlockModels.createChest(ChestSpecialRenderer.CHRISTMAS.select((ChestType)type), type, facing), BuiltInBlockModels.createChest((Identifier)textures.select((ChestType)type), type, facing)));
    }

    private static SpecialModelFactory createCopperGolem(WeatheringCopper.WeatherState weatherState) {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(CopperGolemStatueBlock.FACING, CopperGolemStatueBlock.POSE, (facing, pose) -> BuiltInBlockModels.special(new CopperGolemStatueSpecialRenderer.Unbaked(weatherState, (CopperGolemStatueBlock.Pose)pose), CopperGolemStatueBlockRenderer.modelTransformation(facing)));
    }

    private static SpecialModelFactory createDecoratedPot() {
        return BuiltInBlockModels.specialModelWithPropertyDispatch(DecoratedPotBlock.HORIZONTAL_FACING, facing -> BuiltInBlockModels.special(new DecoratedPotSpecialRenderer.Unbaked(), DecoratedPotRenderer.modelTransformation(facing)));
    }

    private static BlockStateModelWrapper.Unbaked createBlockStateModelWrapper(BlockColors blockColors, BlockState blockState) {
        return new BlockStateModelWrapper.Unbaked(blockState, blockColors.getTintSources(blockState), Optional.empty());
    }

    private static CompositeBlockModel.Unbaked combineSpecialAndBlockModels(BlockModel.Unbaked specialModel, BlockColors blockColors, BlockState blockState) {
        return new CompositeBlockModel.Unbaked(BuiltInBlockModels.createBlockStateModelWrapper(blockColors, blockState), specialModel, Optional.empty());
    }

    private static SelectBlockModel.Unbaked createFlowerBedModel(BlockColors blockColors, BlockState blockState) {
        List<BlockTintSource> tintSources = blockColors.getTintSources(blockState);
        Transformation customFlowerTransform = new Transformation(new Vector3f(0.25f, 0.0f, 0.25f), null, null, null);
        BlockStateModelWrapper.Unbaked customTransformModel = new BlockStateModelWrapper.Unbaked(blockState, tintSources, Optional.of(customFlowerTransform));
        BlockStateModelWrapper.Unbaked normalTransformModel = new BlockStateModelWrapper.Unbaked(blockState, tintSources, Optional.empty());
        return new SelectBlockModel.Unbaked(Optional.empty(), new SelectBlockModel.UnbakedSwitch(new DisplayContext(), List.of(new SelectBlockModel.SwitchCase<BlockDisplayContext>(List.of(CopperGolemRenderer.BLOCK_DISPLAY_CONTEXT), customTransformModel))), Optional.of(normalTransformModel));
    }

    private static BlockModel.Unbaked createEnchantingTable() {
        return BuiltInBlockModels.special(new BookSpecialRenderer.Unbaked(0.0f, 0.0f, 0.0f), new Transformation(new Vector3f(0.5f, 0.8125f, 0.5f), Axis.ZP.rotationDegrees(180.0f), null, Axis.XP.rotationDegrees(90.0f)));
    }

    private static <P extends Comparable<P>> SpecialModelFactory specialModelWithPropertyDispatch(Property<P> property, Function<P, BlockModel.Unbaked> blockModel) {
        return state -> {
            Object value = state.getValue(property);
            return (BlockModel.Unbaked)blockModel.apply(value);
        };
    }

    private static <P1 extends Comparable<P1>, P2 extends Comparable<P2>> SpecialModelFactory specialModelWithPropertyDispatch(Property<P1> property1, Property<P2> property2, BiFunction<P1, P2, BlockModel.Unbaked> blockModel) {
        return state -> {
            Object value1 = state.getValue(property1);
            Object value2 = state.getValue(property2);
            return (BlockModel.Unbaked)blockModel.apply(value1, value2);
        };
    }

    public static Map<BlockState, BlockModel.Unbaked> createBlockModels(BlockColors blockColors) {
        Builder builder = new Builder(blockColors);
        BuiltInBlockModels.addDefaults(builder);
        return builder.build();
    }

    @Environment(value=EnvType.CLIENT)
    private static class Builder {
        private final BlockColors blockColors;
        private final Map<BlockState, BlockModel.Unbaked> result = new HashMap<BlockState, BlockModel.Unbaked>();

        private Builder(BlockColors blockColors) {
            this.blockColors = blockColors;
        }

        private void put(ModelFactory factory, Block a, Block b) {
            this.put(factory, a);
            this.put(factory, b);
        }

        private void put(BlockModel.Unbaked specialModel, Block block) {
            this.put(blockState -> specialModel, block);
        }

        private void put(ModelFactory factory, Block block) {
            for (BlockState blockState : block.getStateDefinition().getPossibleStates()) {
                this.result.put(blockState, factory.create(this.blockColors, blockState));
            }
        }

        public Map<BlockState, BlockModel.Unbaked> build() {
            return Map.copyOf(this.result);
        }
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    private static interface SpecialModelFactory
    extends ModelFactory {
        @Override
        default public BlockModel.Unbaked create(BlockColors colors, BlockState state) {
            return BuiltInBlockModels.combineSpecialAndBlockModels(this.createSpecial(state), colors, state);
        }

        public BlockModel.Unbaked createSpecial(BlockState var1);
    }

    @FunctionalInterface
    @Environment(value=EnvType.CLIENT)
    private static interface ModelFactory {
        public BlockModel.Unbaked create(BlockColors var1, BlockState var2);
    }
}

