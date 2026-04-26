/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag.convention.v2;

import net.fabricmc.fabric.impl.tag.convention.v2.TagRegistration;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public final class ConventionalBiomeTags {
    public static final TagKey<Biome> NO_DEFAULT_MONSTERS = ConventionalBiomeTags.register("no_default_monsters");
    public static final TagKey<Biome> HIDDEN_FROM_LOCATOR_SELECTION = ConventionalBiomeTags.register("hidden_from_locator_selection");
    public static final TagKey<Biome> IS_VOID = ConventionalBiomeTags.register("is_void");
    public static final TagKey<Biome> IS_OVERWORLD = ConventionalBiomeTags.register("is_overworld");
    public static final TagKey<Biome> IS_HOT = ConventionalBiomeTags.register("is_hot");
    public static final TagKey<Biome> IS_HOT_OVERWORLD = ConventionalBiomeTags.register("is_hot/overworld");
    public static final TagKey<Biome> IS_HOT_NETHER = ConventionalBiomeTags.register("is_hot/nether");
    public static final TagKey<Biome> IS_HOT_END = ConventionalBiomeTags.register("is_hot/end");
    public static final TagKey<Biome> IS_TEMPERATE = ConventionalBiomeTags.register("is_temperate");
    public static final TagKey<Biome> IS_TEMPERATE_OVERWORLD = ConventionalBiomeTags.register("is_temperate/overworld");
    public static final TagKey<Biome> IS_TEMPERATE_NETHER = ConventionalBiomeTags.register("is_temperate/nether");
    public static final TagKey<Biome> IS_TEMPERATE_END = ConventionalBiomeTags.register("is_temperate/end");
    public static final TagKey<Biome> IS_COLD = ConventionalBiomeTags.register("is_cold");
    public static final TagKey<Biome> IS_COLD_OVERWORLD = ConventionalBiomeTags.register("is_cold/overworld");
    public static final TagKey<Biome> IS_COLD_NETHER = ConventionalBiomeTags.register("is_cold/nether");
    public static final TagKey<Biome> IS_COLD_END = ConventionalBiomeTags.register("is_cold/end");
    public static final TagKey<Biome> IS_WET = ConventionalBiomeTags.register("is_wet");
    public static final TagKey<Biome> IS_WET_OVERWORLD = ConventionalBiomeTags.register("is_wet/overworld");
    public static final TagKey<Biome> IS_WET_NETHER = ConventionalBiomeTags.register("is_wet/nether");
    public static final TagKey<Biome> IS_WET_END = ConventionalBiomeTags.register("is_wet/end");
    public static final TagKey<Biome> IS_DRY = ConventionalBiomeTags.register("is_dry");
    public static final TagKey<Biome> IS_DRY_OVERWORLD = ConventionalBiomeTags.register("is_dry/overworld");
    public static final TagKey<Biome> IS_DRY_NETHER = ConventionalBiomeTags.register("is_dry/nether");
    public static final TagKey<Biome> IS_DRY_END = ConventionalBiomeTags.register("is_dry/end");
    public static final TagKey<Biome> IS_VEGETATION_SPARSE = ConventionalBiomeTags.register("is_sparse_vegetation");
    public static final TagKey<Biome> IS_VEGETATION_SPARSE_OVERWORLD = ConventionalBiomeTags.register("is_sparse_vegetation/overworld");
    public static final TagKey<Biome> IS_VEGETATION_SPARSE_NETHER = ConventionalBiomeTags.register("is_sparse_vegetation/nether");
    public static final TagKey<Biome> IS_VEGETATION_SPARSE_END = ConventionalBiomeTags.register("is_sparse_vegetation/end");
    public static final TagKey<Biome> IS_VEGETATION_DENSE = ConventionalBiomeTags.register("is_dense_vegetation");
    public static final TagKey<Biome> IS_VEGETATION_DENSE_OVERWORLD = ConventionalBiomeTags.register("is_dense_vegetation/overworld");
    public static final TagKey<Biome> IS_VEGETATION_DENSE_NETHER = ConventionalBiomeTags.register("is_dense_vegetation/nether");
    public static final TagKey<Biome> IS_VEGETATION_DENSE_END = ConventionalBiomeTags.register("is_dense_vegetation/end");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE = ConventionalBiomeTags.register("primary_wood_type");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_OAK = ConventionalBiomeTags.register("primary_wood_type/oak");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BIRCH = ConventionalBiomeTags.register("primary_wood_type/birch");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_SPRUCE = ConventionalBiomeTags.register("primary_wood_type/spruce");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_JUNGLE = ConventionalBiomeTags.register("primary_wood_type/jungle");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_ACACIA = ConventionalBiomeTags.register("primary_wood_type/acacia");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_DARK_OAK = ConventionalBiomeTags.register("primary_wood_type/dark_oak");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_MANGROVE = ConventionalBiomeTags.register("primary_wood_type/mangrove");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CHERRY = ConventionalBiomeTags.register("primary_wood_type/cherry");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_PALE_OAK = ConventionalBiomeTags.register("primary_wood_type/pale_oak");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_BAMBOO = ConventionalBiomeTags.register("primary_wood_type/bamboo");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_CRIMSON = ConventionalBiomeTags.register("primary_wood_type/crimson");
    public static final TagKey<Biome> PRIMARY_WOOD_TYPE_WARPED = ConventionalBiomeTags.register("primary_wood_type/warped");
    public static final TagKey<Biome> IS_CONIFEROUS_TREE = ConventionalBiomeTags.register("is_tree/coniferous");
    public static final TagKey<Biome> IS_SAVANNA_TREE = ConventionalBiomeTags.register("is_tree/savanna");
    public static final TagKey<Biome> IS_JUNGLE_TREE = ConventionalBiomeTags.register("is_tree/jungle");
    public static final TagKey<Biome> IS_DECIDUOUS_TREE = ConventionalBiomeTags.register("is_tree/deciduous");
    public static final TagKey<Biome> IS_MOUNTAIN = ConventionalBiomeTags.register("is_mountain");
    public static final TagKey<Biome> IS_MOUNTAIN_PEAK = ConventionalBiomeTags.register("is_mountain/peak");
    public static final TagKey<Biome> IS_MOUNTAIN_SLOPE = ConventionalBiomeTags.register("is_mountain/slope");
    public static final TagKey<Biome> IS_PLAINS = ConventionalBiomeTags.register("is_plains");
    public static final TagKey<Biome> IS_SNOWY_PLAINS = ConventionalBiomeTags.register("is_snowy_plains");
    public static final TagKey<Biome> IS_FOREST = ConventionalBiomeTags.register("is_forest");
    public static final TagKey<Biome> IS_BIRCH_FOREST = ConventionalBiomeTags.register("is_birch_forest");
    public static final TagKey<Biome> IS_DARK_FOREST = ConventionalBiomeTags.register("is_dark_forest");
    public static final TagKey<Biome> IS_FLOWER_FOREST = ConventionalBiomeTags.register("is_flower_forest");
    public static final TagKey<Biome> IS_TAIGA = ConventionalBiomeTags.register("is_taiga");
    public static final TagKey<Biome> IS_OLD_GROWTH = ConventionalBiomeTags.register("is_old_growth");
    public static final TagKey<Biome> IS_HILL = ConventionalBiomeTags.register("is_hill");
    public static final TagKey<Biome> IS_WINDSWEPT = ConventionalBiomeTags.register("is_windswept");
    public static final TagKey<Biome> IS_JUNGLE = ConventionalBiomeTags.register("is_jungle");
    public static final TagKey<Biome> IS_SAVANNA = ConventionalBiomeTags.register("is_savanna");
    public static final TagKey<Biome> IS_SWAMP = ConventionalBiomeTags.register("is_swamp");
    public static final TagKey<Biome> IS_DESERT = ConventionalBiomeTags.register("is_desert");
    public static final TagKey<Biome> IS_BADLANDS = ConventionalBiomeTags.register("is_badlands");
    public static final TagKey<Biome> IS_BEACH = ConventionalBiomeTags.register("is_beach");
    public static final TagKey<Biome> IS_STONY_SHORES = ConventionalBiomeTags.register("is_stony_shores");
    public static final TagKey<Biome> IS_MUSHROOM = ConventionalBiomeTags.register("is_mushroom");
    public static final TagKey<Biome> IS_RIVER = ConventionalBiomeTags.register("is_river");
    public static final TagKey<Biome> IS_OCEAN = ConventionalBiomeTags.register("is_ocean");
    public static final TagKey<Biome> IS_DEEP_OCEAN = ConventionalBiomeTags.register("is_deep_ocean");
    public static final TagKey<Biome> IS_SHALLOW_OCEAN = ConventionalBiomeTags.register("is_shallow_ocean");
    public static final TagKey<Biome> IS_UNDERGROUND = ConventionalBiomeTags.register("is_underground");
    public static final TagKey<Biome> IS_CAVE = ConventionalBiomeTags.register("is_cave");
    public static final TagKey<Biome> IS_WASTELAND = ConventionalBiomeTags.register("is_wasteland");
    public static final TagKey<Biome> IS_DEAD = ConventionalBiomeTags.register("is_dead");
    public static final TagKey<Biome> IS_LUSH = ConventionalBiomeTags.register("is_lush");
    public static final TagKey<Biome> IS_MAGICAL = ConventionalBiomeTags.register("is_magical");
    public static final TagKey<Biome> IS_RARE = ConventionalBiomeTags.register("is_rare");
    public static final TagKey<Biome> IS_PLATEAU = ConventionalBiomeTags.register("is_plateau");
    public static final TagKey<Biome> IS_SPOOKY = ConventionalBiomeTags.register("is_spooky");
    public static final TagKey<Biome> IS_FLORAL = ConventionalBiomeTags.register("is_floral");
    public static final TagKey<Biome> IS_SANDY = ConventionalBiomeTags.register("is_sandy");
    public static final TagKey<Biome> IS_SNOWY = ConventionalBiomeTags.register("is_snowy");
    public static final TagKey<Biome> IS_ICY = ConventionalBiomeTags.register("is_icy");
    public static final TagKey<Biome> IS_AQUATIC = ConventionalBiomeTags.register("is_aquatic");
    public static final TagKey<Biome> IS_AQUATIC_ICY = ConventionalBiomeTags.register("is_aquatic_icy");
    public static final TagKey<Biome> IS_NETHER = ConventionalBiomeTags.register("is_nether");
    public static final TagKey<Biome> IS_NETHER_FOREST = ConventionalBiomeTags.register("is_nether_forest");
    public static final TagKey<Biome> IS_END = ConventionalBiomeTags.register("is_end");
    public static final TagKey<Biome> IS_OUTER_END_ISLAND = ConventionalBiomeTags.register("is_outer_end_island");

    private ConventionalBiomeTags() {
    }

    private static TagKey<Biome> register(String tagId) {
        return TagRegistration.BIOME_TAG.registerC(tagId);
    }
}

