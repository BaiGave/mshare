/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import org.slf4j.Logger;

public class V99
extends Schema {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Map<String, String> ITEM_TO_BLOCKENTITY = DataFixUtils.make(Maps.newHashMap(), map -> {
        map.put("minecraft:furnace", "Furnace");
        map.put("minecraft:lit_furnace", "Furnace");
        map.put("minecraft:chest", "Chest");
        map.put("minecraft:trapped_chest", "Chest");
        map.put("minecraft:ender_chest", "EnderChest");
        map.put("minecraft:jukebox", "RecordPlayer");
        map.put("minecraft:dispenser", "Trap");
        map.put("minecraft:dropper", "Dropper");
        map.put("minecraft:sign", "Sign");
        map.put("minecraft:mob_spawner", "MobSpawner");
        map.put("minecraft:noteblock", "Music");
        map.put("minecraft:brewing_stand", "Cauldron");
        map.put("minecraft:enhanting_table", "EnchantTable");
        map.put("minecraft:command_block", "CommandBlock");
        map.put("minecraft:beacon", "Beacon");
        map.put("minecraft:skull", "Skull");
        map.put("minecraft:daylight_detector", "DLDetector");
        map.put("minecraft:hopper", "Hopper");
        map.put("minecraft:banner", "Banner");
        map.put("minecraft:flower_pot", "FlowerPot");
        map.put("minecraft:repeating_command_block", "CommandBlock");
        map.put("minecraft:chain_command_block", "CommandBlock");
        map.put("minecraft:standing_sign", "Sign");
        map.put("minecraft:wall_sign", "Sign");
        map.put("minecraft:piston_head", "Piston");
        map.put("minecraft:daylight_detector_inverted", "DLDetector");
        map.put("minecraft:unpowered_comparator", "Comparator");
        map.put("minecraft:powered_comparator", "Comparator");
        map.put("minecraft:wall_banner", "Banner");
        map.put("minecraft:standing_banner", "Banner");
        map.put("minecraft:structure_block", "Structure");
        map.put("minecraft:end_portal", "Airportal");
        map.put("minecraft:end_gateway", "EndGateway");
        map.put("minecraft:shield", "Banner");
    });
    public static final Map<String, String> ITEM_TO_ENTITY = Map.of("minecraft:armor_stand", "ArmorStand", "minecraft:painting", "Painting");
    protected static final Hook.HookFunction ADD_NAMES = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> ops, T value) {
            return V99.addNames(new Dynamic<T>(ops, value), ITEM_TO_BLOCKENTITY, ITEM_TO_ENTITY);
        }
    };

    public V99(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected static void registerThrowableProjectile(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
    }

    protected static void registerMinecart(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema)));
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema))));
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        schema.register(map, "Item", (String name) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "XPOrb");
        V99.registerThrowableProjectile(schema, map, "ThrownEgg");
        schema.registerSimple(map, "LeashKnot");
        schema.registerSimple(map, "Painting");
        schema.register(map, "Arrow", (String name) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        schema.register(map, "TippedArrow", (String name) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        schema.register(map, "SpectralArrow", (String name) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema)));
        V99.registerThrowableProjectile(schema, map, "Snowball");
        V99.registerThrowableProjectile(schema, map, "Fireball");
        V99.registerThrowableProjectile(schema, map, "SmallFireball");
        V99.registerThrowableProjectile(schema, map, "ThrownEnderpearl");
        schema.registerSimple(map, "EyeOfEnderSignal");
        schema.register(map, "ThrownPotion", (String name) -> DSL.optionalFields("inTile", References.BLOCK_NAME.in(schema), "Potion", References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, map, "ThrownExpBottle");
        schema.register(map, "ItemFrame", (String name) -> DSL.optionalFields("Item", References.ITEM_STACK.in(schema)));
        V99.registerThrowableProjectile(schema, map, "WitherSkull");
        schema.registerSimple(map, "PrimedTnt");
        schema.register(map, "FallingSand", (String name) -> DSL.optionalFields("Block", References.BLOCK_NAME.in(schema), "TileEntityData", References.BLOCK_ENTITY.in(schema)));
        schema.register(map, "FireworksRocketEntity", (String name) -> DSL.optionalFields("FireworksItem", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "Boat");
        schema.register(map, "Minecart", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, map, "MinecartRideable");
        schema.register(map, "MinecartChest", (String name) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        V99.registerMinecart(schema, map, "MinecartFurnace");
        V99.registerMinecart(schema, map, "MinecartTNT");
        schema.register(map, "MinecartSpawner", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), References.UNTAGGED_SPAWNER.in(schema)));
        schema.register(map, "MinecartHopper", (String name) -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "Items", DSL.list(References.ITEM_STACK.in(schema))));
        schema.register(map, "MinecartCommandBlock", () -> DSL.optionalFields("DisplayTile", References.BLOCK_NAME.in(schema), "LastOutput", References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple(map, "ArmorStand");
        schema.registerSimple(map, "Creeper");
        schema.registerSimple(map, "Skeleton");
        schema.registerSimple(map, "Spider");
        schema.registerSimple(map, "Giant");
        schema.registerSimple(map, "Zombie");
        schema.registerSimple(map, "Slime");
        schema.registerSimple(map, "Ghast");
        schema.registerSimple(map, "PigZombie");
        schema.register(map, "Enderman", (String name) -> DSL.optionalFields("carried", References.BLOCK_NAME.in(schema)));
        schema.registerSimple(map, "CaveSpider");
        schema.registerSimple(map, "Silverfish");
        schema.registerSimple(map, "Blaze");
        schema.registerSimple(map, "LavaSlime");
        schema.registerSimple(map, "EnderDragon");
        schema.registerSimple(map, "WitherBoss");
        schema.registerSimple(map, "Bat");
        schema.registerSimple(map, "Witch");
        schema.registerSimple(map, "Endermite");
        schema.registerSimple(map, "Guardian");
        schema.registerSimple(map, "Pig");
        schema.registerSimple(map, "Sheep");
        schema.registerSimple(map, "Cow");
        schema.registerSimple(map, "Chicken");
        schema.registerSimple(map, "Squid");
        schema.registerSimple(map, "Wolf");
        schema.registerSimple(map, "MushroomCow");
        schema.registerSimple(map, "SnowMan");
        schema.registerSimple(map, "Ozelot");
        schema.registerSimple(map, "VillagerGolem");
        schema.register(map, "EntityHorse", (String name) -> DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)), "ArmorItem", References.ITEM_STACK.in(schema), "SaddleItem", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "Rabbit");
        schema.register(map, "Villager", (String name) -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "Offers", DSL.optionalFields("Recipes", DSL.list(References.VILLAGER_TRADE.in(schema)))));
        schema.registerSimple(map, "EnderCrystal");
        schema.register(map, "AreaEffectCloud", (String name) -> DSL.optionalFields("Particle", References.PARTICLE.in(schema)));
        schema.registerSimple(map, "ShulkerBullet");
        schema.registerSimple(map, "DragonFireball");
        schema.registerSimple(map, "Shulker");
        return map;
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        HashMap<String, Supplier<TypeTemplate>> map = Maps.newHashMap();
        V99.registerInventory(schema, map, "Furnace");
        V99.registerInventory(schema, map, "Chest");
        schema.registerSimple(map, "EnderChest");
        schema.register(map, "RecordPlayer", (String name) -> DSL.optionalFields("RecordItem", References.ITEM_STACK.in(schema)));
        V99.registerInventory(schema, map, "Trap");
        V99.registerInventory(schema, map, "Dropper");
        schema.register(map, "Sign", () -> V99.sign(schema));
        schema.register(map, "MobSpawner", (String name) -> References.UNTAGGED_SPAWNER.in(schema));
        schema.registerSimple(map, "Music");
        schema.registerSimple(map, "Piston");
        V99.registerInventory(schema, map, "Cauldron");
        schema.registerSimple(map, "EnchantTable");
        schema.registerSimple(map, "Airportal");
        schema.register(map, "Control", () -> DSL.optionalFields("LastOutput", References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple(map, "Beacon");
        schema.register(map, "Skull", () -> DSL.optionalFields("custom_name", References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple(map, "DLDetector");
        V99.registerInventory(schema, map, "Hopper");
        schema.registerSimple(map, "Comparator");
        schema.register(map, "FlowerPot", (String name) -> DSL.optionalFields("Item", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema))));
        schema.register(map, "Banner", () -> DSL.optionalFields("CustomName", References.TEXT_COMPONENT.in(schema)));
        schema.registerSimple(map, "Structure");
        schema.registerSimple(map, "EndGateway");
        return map;
    }

    public static TypeTemplate sign(Schema schema) {
        return DSL.optionalFields(Pair.of("Text1", References.TEXT_COMPONENT.in(schema)), Pair.of("Text2", References.TEXT_COMPONENT.in(schema)), Pair.of("Text3", References.TEXT_COMPONENT.in(schema)), Pair.of("Text4", References.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText1", References.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText2", References.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText3", References.TEXT_COMPONENT.in(schema)), Pair.of("FilteredText4", References.TEXT_COMPONENT.in(schema)));
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        schema.registerType(false, References.LEVEL, () -> DSL.optionalFields("CustomBossEvents", DSL.compoundList(DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema))), References.LIGHTWEIGHT_LEVEL.in(schema)));
        schema.registerType(false, References.LIGHTWEIGHT_LEVEL, DSL::remainder);
        schema.registerType(false, References.PLAYER, () -> DSL.optionalFields("Inventory", DSL.list(References.ITEM_STACK.in(schema)), "EnderItems", DSL.list(References.ITEM_STACK.in(schema))));
        schema.registerType(false, References.CHUNK, () -> DSL.fields("Level", DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema)), "TileEntities", DSL.list(DSL.or(References.BLOCK_ENTITY.in(schema), DSL.remainder())), "TileTicks", DSL.list(DSL.fields("i", References.BLOCK_NAME.in(schema))))));
        schema.registerType(true, References.BLOCK_ENTITY, () -> DSL.optionalFields("components", References.DATA_COMPONENTS.in(schema), DSL.taggedChoiceLazy("id", DSL.string(), blockEntityTypes)));
        schema.registerType(true, References.ENTITY_TREE, () -> DSL.optionalFields("Riding", References.ENTITY_TREE.in(schema), References.ENTITY.in(schema)));
        schema.registerType(false, References.ENTITY_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        schema.registerType(true, References.ENTITY, () -> DSL.and(References.ENTITY_EQUIPMENT.in(schema), DSL.optionalFields("CustomName", DSL.constType(DSL.string()), DSL.taggedChoiceLazy("id", DSL.string(), entityTypes))));
        schema.registerType(true, References.ITEM_STACK, () -> DSL.hook(DSL.optionalFields("id", DSL.or(DSL.constType(DSL.intType()), References.ITEM_NAME.in(schema)), "tag", V99.itemStackTag(schema)), ADD_NAMES, Hook.HookFunction.IDENTITY));
        schema.registerType(false, References.OPTIONS, DSL::remainder);
        schema.registerType(false, References.BLOCK_NAME, () -> DSL.or(DSL.constType(DSL.intType()), DSL.constType(NamespacedSchema.namespacedString())));
        schema.registerType(false, References.ITEM_NAME, () -> DSL.constType(NamespacedSchema.namespacedString()));
        schema.registerType(false, References.STATS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_COMMAND_STORAGE, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_CUSTOM_BOSS_EVENTS, () -> DSL.optionalFields("data", DSL.compoundList(DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema)))));
        schema.registerType(false, References.SAVED_DATA_ENDER_DRAGON_FIGHT, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_GAME_RULES, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_TICKETS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_MAP_DATA, () -> DSL.optionalFields("data", DSL.optionalFields("banners", DSL.list(DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, References.SAVED_DATA_MAP_INDEX, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_RAIDS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_RANDOM_SEQUENCES, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_SCHEDULED_EVENTS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_SCOREBOARD, () -> DSL.optionalFields("data", DSL.optionalFields("Objectives", DSL.list(References.OBJECTIVE.in(schema)), "Teams", DSL.list(References.TEAM.in(schema)), "PlayerScores", DSL.list(DSL.optionalFields("display", References.TEXT_COMPONENT.in(schema))))));
        schema.registerType(false, References.SAVED_DATA_STOPWATCHES, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_STRUCTURE_FEATURE_INDICES, () -> DSL.optionalFields("data", DSL.optionalFields("Features", DSL.compoundList(References.STRUCTURE_FEATURE.in(schema)))));
        schema.registerType(false, References.SAVED_DATA_WANDERING_TRADER, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_WEATHER, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_WORLD_BORDER, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_WORLD_CLOCKS, DSL::remainder);
        schema.registerType(false, References.SAVED_DATA_WORLD_GEN_SETTINGS, () -> DSL.fields("data", References.WORLD_GEN_SETTINGS.in(schema)));
        schema.registerType(false, References.DEBUG_PROFILE, DSL::remainder);
        schema.registerType(false, References.STRUCTURE_FEATURE, DSL::remainder);
        schema.registerType(false, References.OBJECTIVE, DSL::remainder);
        schema.registerType(false, References.TEAM, () -> DSL.optionalFields("MemberNamePrefix", References.TEXT_COMPONENT.in(schema), "MemberNameSuffix", References.TEXT_COMPONENT.in(schema), "DisplayName", References.TEXT_COMPONENT.in(schema)));
        schema.registerType(true, References.UNTAGGED_SPAWNER, DSL::remainder);
        schema.registerType(false, References.POI_CHUNK, DSL::remainder);
        schema.registerType(false, References.WORLD_GEN_SETTINGS, DSL::remainder);
        schema.registerType(false, References.ENTITY_CHUNK, () -> DSL.optionalFields("Entities", DSL.list(References.ENTITY_TREE.in(schema))));
        schema.registerType(true, References.DATA_COMPONENTS, DSL::remainder);
        schema.registerType(true, References.VILLAGER_TRADE, () -> DSL.optionalFields("buy", References.ITEM_STACK.in(schema), "buyB", References.ITEM_STACK.in(schema), "sell", References.ITEM_STACK.in(schema)));
        schema.registerType(true, References.PARTICLE, () -> DSL.constType(DSL.string()));
        schema.registerType(true, References.TEXT_COMPONENT, () -> DSL.constType(DSL.string()));
        schema.registerType(false, References.STRUCTURE, () -> DSL.optionalFields("entities", DSL.list(DSL.optionalFields("nbt", References.ENTITY_TREE.in(schema))), "blocks", DSL.list(DSL.optionalFields("nbt", References.BLOCK_ENTITY.in(schema))), "palette", DSL.list(References.BLOCK_STATE.in(schema))));
        schema.registerType(false, References.BLOCK_STATE, DSL::remainder);
        schema.registerType(false, References.FLAT_BLOCK_STATE, DSL::remainder);
        schema.registerType(true, References.ENTITY_EQUIPMENT, () -> DSL.optional(DSL.field("Equipment", DSL.list(References.ITEM_STACK.in(schema)))));
    }

    public static TypeTemplate itemStackTag(Schema schema) {
        return DSL.optionalFields(Pair.of("EntityTag", References.ENTITY_TREE.in(schema)), Pair.of("BlockEntityTag", References.BLOCK_ENTITY.in(schema)), Pair.of("CanDestroy", DSL.list(References.BLOCK_NAME.in(schema))), Pair.of("CanPlaceOn", DSL.list(References.BLOCK_NAME.in(schema))), Pair.of("Items", DSL.list(References.ITEM_STACK.in(schema))), Pair.of("ChargedProjectiles", DSL.list(References.ITEM_STACK.in(schema))), Pair.of("pages", DSL.list(References.TEXT_COMPONENT.in(schema))), Pair.of("filtered_pages", DSL.compoundList(References.TEXT_COMPONENT.in(schema))), Pair.of("display", DSL.optionalFields("Name", References.TEXT_COMPONENT.in(schema), "Lore", DSL.list(References.TEXT_COMPONENT.in(schema)))));
    }

    protected static <T> T addNames(Dynamic<T> input, Map<String, String> itemToBlockEntityMap, Map<String, String> itemToEntityMap) {
        return input.update("tag", itemStackTag -> itemStackTag.update("BlockEntityTag", blockEntity -> {
            String itemId = input.get("id").asString().result().map(NamespacedSchema::ensureNamespaced).orElse("minecraft:air");
            if (!"minecraft:air".equals(itemId)) {
                String expectedId = (String)itemToBlockEntityMap.get(itemId);
                if (expectedId == null) {
                    LOGGER.warn("Unable to resolve BlockEntity for ItemStack: {}", (Object)itemId);
                } else {
                    return blockEntity.set("id", input.createString(expectedId));
                }
            }
            return blockEntity;
        }).update("EntityTag", entity -> {
            if (entity.get("id").result().isPresent()) {
                return entity;
            }
            String itemId = NamespacedSchema.ensureNamespaced(input.get("id").asString(""));
            String expectedId = (String)itemToEntityMap.get(itemId);
            if (expectedId != null) {
                return entity.set("id", input.createString(expectedId));
            }
            return entity;
        })).getValue();
    }
}

