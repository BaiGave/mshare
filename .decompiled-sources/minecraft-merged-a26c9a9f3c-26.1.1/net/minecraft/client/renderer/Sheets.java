/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiblockChestResources;
import net.minecraft.client.renderer.SpriteMapper;
import net.minecraft.client.renderer.blockentity.state.ChestRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.ChestSpecialRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.DecoratedPotPattern;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class Sheets {
    public static final Identifier SHULKER_SHEET = Identifier.withDefaultNamespace("textures/atlas/shulker_boxes.png");
    public static final Identifier BED_SHEET = Identifier.withDefaultNamespace("textures/atlas/beds.png");
    public static final Identifier BANNER_SHEET = Identifier.withDefaultNamespace("textures/atlas/banner_patterns.png");
    public static final Identifier SHIELD_SHEET = Identifier.withDefaultNamespace("textures/atlas/shield_patterns.png");
    public static final Identifier SIGN_SHEET = Identifier.withDefaultNamespace("textures/atlas/signs.png");
    public static final Identifier CHEST_SHEET = Identifier.withDefaultNamespace("textures/atlas/chest.png");
    public static final Identifier ARMOR_TRIMS_SHEET = Identifier.withDefaultNamespace("textures/atlas/armor_trims.png");
    public static final Identifier DECORATED_POT_SHEET = Identifier.withDefaultNamespace("textures/atlas/decorated_pot.png");
    public static final Identifier GUI_SHEET = Identifier.withDefaultNamespace("textures/atlas/gui.png");
    public static final Identifier MAP_DECORATIONS_SHEET = Identifier.withDefaultNamespace("textures/atlas/map_decorations.png");
    public static final Identifier PAINTINGS_SHEET = Identifier.withDefaultNamespace("textures/atlas/paintings.png");
    public static final Identifier CELESTIAL_SHEET = Identifier.withDefaultNamespace("textures/atlas/celestials.png");
    private static final RenderType ARMOR_TRIMS_SHEET_TYPE = RenderTypes.armorCutoutNoCull(ARMOR_TRIMS_SHEET);
    private static final RenderType ARMOR_TRIMS_DECAL_SHEET_TYPE = RenderTypes.createArmorDecalCutoutNoCull(ARMOR_TRIMS_SHEET);
    private static final RenderType CUTOUT_BLOCK_SHEET = RenderTypes.entityCutoutCull(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType TRANSLUCENT_BLOCK_SHEET = RenderTypes.entityTranslucentCullItemTarget(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType CUTOUT_BLOCK_ITEM_SHEET = RenderTypes.itemCutout(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType TRANSLUCENT_BLOCK_ITEM_SHEET = RenderTypes.itemTranslucent(TextureAtlas.LOCATION_BLOCKS);
    private static final RenderType CUTOUT_ITEM_SHEET = RenderTypes.itemCutout(TextureAtlas.LOCATION_ITEMS);
    private static final RenderType TRANSLUCENT_ITEM_SHEET = RenderTypes.itemTranslucent(TextureAtlas.LOCATION_ITEMS);
    public static final SpriteMapper ITEMS_MAPPER = new SpriteMapper(TextureAtlas.LOCATION_ITEMS, "item");
    public static final SpriteMapper BLOCKS_MAPPER = new SpriteMapper(TextureAtlas.LOCATION_BLOCKS, "block");
    public static final SpriteMapper BLOCK_ENTITIES_MAPPER = new SpriteMapper(TextureAtlas.LOCATION_BLOCKS, "entity");
    public static final SpriteMapper BANNER_MAPPER = new SpriteMapper(BANNER_SHEET, "entity/banner");
    public static final SpriteMapper SHIELD_MAPPER = new SpriteMapper(SHIELD_SHEET, "entity/shield");
    public static final SpriteMapper CHEST_MAPPER = new SpriteMapper(CHEST_SHEET, "entity/chest");
    public static final SpriteMapper DECORATED_POT_MAPPER = new SpriteMapper(DECORATED_POT_SHEET, "entity/decorated_pot");
    public static final SpriteMapper BED_MAPPER = new SpriteMapper(BED_SHEET, "entity/bed");
    public static final SpriteMapper SHULKER_MAPPER = new SpriteMapper(SHULKER_SHEET, "entity/shulker");
    public static final SpriteMapper SIGN_MAPPER = new SpriteMapper(SIGN_SHEET, "entity/signs");
    public static final SpriteMapper HANGING_SIGN_MAPPER = new SpriteMapper(SIGN_SHEET, "entity/signs/hanging");
    public static final SpriteId DEFAULT_SHULKER_TEXTURE_LOCATION = SHULKER_MAPPER.defaultNamespaceApply("shulker");
    public static final List<SpriteId> SHULKER_TEXTURE_LOCATION = Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(Sheets::createShulkerSprite).collect(ImmutableList.toImmutableList());
    public static final Map<WoodType, SpriteId> SIGN_SPRITES = WoodType.values().collect(Collectors.toMap(Function.identity(), Sheets::createSignSprite));
    public static final Map<WoodType, SpriteId> HANGING_SIGN_SPRITES = WoodType.values().collect(Collectors.toMap(Function.identity(), Sheets::createHangingSignSprite));
    public static final SpriteId BANNER_BASE = BANNER_MAPPER.defaultNamespaceApply("banner_base");
    public static final SpriteId SHIELD_BASE = SHIELD_MAPPER.defaultNamespaceApply("shield_base");
    public static final SpriteId SHIELD_BASE_NO_PATTERN = SHIELD_MAPPER.defaultNamespaceApply("shield_base_nopattern");
    public static final SpriteId BANNER_PATTERN_BASE = BANNER_MAPPER.defaultNamespaceApply("base");
    public static final SpriteId SHIELD_PATTERN_BASE = SHIELD_MAPPER.defaultNamespaceApply("base");
    private static final Map<Identifier, SpriteId> BANNER_SPRITES = new HashMap<Identifier, SpriteId>();
    private static final Map<Identifier, SpriteId> SHIELD_SPRITES = new HashMap<Identifier, SpriteId>();
    public static final Map<ResourceKey<DecoratedPotPattern>, SpriteId> DECORATED_POT_SPRITES = BuiltInRegistries.DECORATED_POT_PATTERN.listElements().collect(Collectors.toMap(Holder.Reference::key, holder -> DECORATED_POT_MAPPER.apply(((DecoratedPotPattern)holder.value()).assetId())));
    public static final SpriteId DECORATED_POT_BASE = DECORATED_POT_MAPPER.defaultNamespaceApply("decorated_pot_base");
    public static final SpriteId DECORATED_POT_SIDE = DECORATED_POT_MAPPER.defaultNamespaceApply("decorated_pot_side");
    private static final SpriteId[] BED_TEXTURES = (SpriteId[])Arrays.stream(DyeColor.values()).sorted(Comparator.comparingInt(DyeColor::getId)).map(Sheets::createBedSprite).toArray(SpriteId[]::new);
    public static final SpriteId ENDER_CHEST_LOCATION = CHEST_MAPPER.defaultNamespaceApply("ender");
    public static final MultiblockChestResources<SpriteId> CHEST_REGULAR = ChestSpecialRenderer.REGULAR.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_TRAPPED = ChestSpecialRenderer.TRAPPED.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_CHRISTMAS = ChestSpecialRenderer.CHRISTMAS.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_COPPER_UNAFFECTED = ChestSpecialRenderer.COPPER_UNAFFECTED.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_COPPER_EXPOSED = ChestSpecialRenderer.COPPER_EXPOSED.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_COPPER_WEATHERED = ChestSpecialRenderer.COPPER_WEATHERED.map(CHEST_MAPPER::apply);
    public static final MultiblockChestResources<SpriteId> CHEST_COPPER_OXIDIZED = ChestSpecialRenderer.COPPER_OXIDIZED.map(CHEST_MAPPER::apply);

    public static RenderType armorTrimsSheet(boolean decal) {
        return decal ? ARMOR_TRIMS_DECAL_SHEET_TYPE : ARMOR_TRIMS_SHEET_TYPE;
    }

    public static RenderType cutoutBlockSheet() {
        return CUTOUT_BLOCK_SHEET;
    }

    public static RenderType translucentBlockSheet() {
        return TRANSLUCENT_BLOCK_SHEET;
    }

    public static RenderType cutoutBlockItemSheet() {
        return CUTOUT_BLOCK_ITEM_SHEET;
    }

    public static RenderType cutoutItemSheet() {
        return CUTOUT_ITEM_SHEET;
    }

    public static RenderType translucentItemSheet() {
        return TRANSLUCENT_ITEM_SHEET;
    }

    public static RenderType translucentBlockItemSheet() {
        return TRANSLUCENT_BLOCK_ITEM_SHEET;
    }

    public static SpriteId getBedSprite(DyeColor color) {
        return BED_TEXTURES[color.getId()];
    }

    public static Identifier colorToResourceSprite(DyeColor color) {
        return Identifier.withDefaultNamespace(color.getName());
    }

    public static SpriteId createBedSprite(DyeColor color) {
        return BED_MAPPER.apply(Sheets.colorToResourceSprite(color));
    }

    public static SpriteId getShulkerBoxSprite(DyeColor color) {
        return SHULKER_TEXTURE_LOCATION.get(color.getId());
    }

    public static Identifier colorToShulkerSprite(DyeColor color) {
        return Identifier.withDefaultNamespace("shulker_" + color.getName());
    }

    public static SpriteId createShulkerSprite(DyeColor color) {
        return SHULKER_MAPPER.apply(Sheets.colorToShulkerSprite(color));
    }

    private static SpriteId createSignSprite(WoodType type) {
        return SIGN_MAPPER.defaultNamespaceApply(type.name());
    }

    private static SpriteId createHangingSignSprite(WoodType type) {
        return HANGING_SIGN_MAPPER.defaultNamespaceApply(type.name());
    }

    public static SpriteId getSignSprite(WoodType type) {
        return SIGN_SPRITES.get(type);
    }

    public static SpriteId getHangingSignSprite(WoodType type) {
        return HANGING_SIGN_SPRITES.get(type);
    }

    public static SpriteId getBannerSprite(Holder<BannerPattern> pattern) {
        return BANNER_SPRITES.computeIfAbsent(pattern.value().assetId(), BANNER_MAPPER::apply);
    }

    public static SpriteId getShieldSprite(Holder<BannerPattern> pattern) {
        return SHIELD_SPRITES.computeIfAbsent(pattern.value().assetId(), SHIELD_MAPPER::apply);
    }

    public static @Nullable SpriteId getDecoratedPotSprite(@Nullable ResourceKey<DecoratedPotPattern> pattern) {
        if (pattern == null) {
            return null;
        }
        return DECORATED_POT_SPRITES.get(pattern);
    }

    public static SpriteId chooseSprite(ChestRenderState.ChestMaterialType materialType, ChestType type) {
        return switch (materialType) {
            default -> throw new MatchException(null, null);
            case ChestRenderState.ChestMaterialType.ENDER_CHEST -> ENDER_CHEST_LOCATION;
            case ChestRenderState.ChestMaterialType.REGULAR -> CHEST_REGULAR.select(type);
            case ChestRenderState.ChestMaterialType.CHRISTMAS -> CHEST_CHRISTMAS.select(type);
            case ChestRenderState.ChestMaterialType.TRAPPED -> CHEST_TRAPPED.select(type);
            case ChestRenderState.ChestMaterialType.COPPER_UNAFFECTED -> CHEST_COPPER_UNAFFECTED.select(type);
            case ChestRenderState.ChestMaterialType.COPPER_EXPOSED -> CHEST_COPPER_EXPOSED.select(type);
            case ChestRenderState.ChestMaterialType.COPPER_WEATHERED -> CHEST_COPPER_WEATHERED.select(type);
            case ChestRenderState.ChestMaterialType.COPPER_OXIDIZED -> CHEST_COPPER_OXIDIZED.select(type);
        };
    }
}

