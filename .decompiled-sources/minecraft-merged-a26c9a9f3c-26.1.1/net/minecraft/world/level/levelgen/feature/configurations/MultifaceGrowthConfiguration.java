/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.MultifaceSpreadeableBlock;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;

public class MultifaceGrowthConfiguration
implements FeatureConfiguration {
    public static final Codec<MultifaceGrowthConfiguration> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)BuiltInRegistries.BLOCK.byNameCodec().fieldOf("block")).flatXmap(MultifaceGrowthConfiguration::apply, DataResult::success).orElse((MultifaceSpreadeableBlock)Blocks.GLOW_LICHEN).forGetter(c -> c.placeBlock), ((MapCodec)Codec.intRange(1, 64).fieldOf("search_range")).orElse(10).forGetter(c -> c.searchRange), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_floor")).orElse(false).forGetter(c -> c.canPlaceOnFloor), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_ceiling")).orElse(false).forGetter(c -> c.canPlaceOnCeiling), ((MapCodec)Codec.BOOL.fieldOf("can_place_on_wall")).orElse(false).forGetter(c -> c.canPlaceOnWall), ((MapCodec)Codec.floatRange(0.0f, 1.0f).fieldOf("chance_of_spreading")).orElse(Float.valueOf(0.5f)).forGetter(c -> Float.valueOf(c.chanceOfSpreading)), ((MapCodec)RegistryCodecs.homogeneousList(Registries.BLOCK).fieldOf("can_be_placed_on")).forGetter(c -> c.canBePlacedOn)).apply((Applicative<MultifaceGrowthConfiguration, ?>)i, MultifaceGrowthConfiguration::new));
    public final MultifaceSpreadeableBlock placeBlock;
    public final int searchRange;
    public final boolean canPlaceOnFloor;
    public final boolean canPlaceOnCeiling;
    public final boolean canPlaceOnWall;
    public final float chanceOfSpreading;
    public final HolderSet<Block> canBePlacedOn;
    private final ObjectArrayList<Direction> validDirections;

    private static DataResult<MultifaceSpreadeableBlock> apply(Block block) {
        DataResult<MultifaceSpreadeableBlock> dataResult;
        if (block instanceof MultifaceSpreadeableBlock) {
            MultifaceSpreadeableBlock multifaceBlock = (MultifaceSpreadeableBlock)block;
            dataResult = DataResult.success(multifaceBlock);
        } else {
            dataResult = DataResult.error(() -> "Growth block should be a multiface spreadeable block");
        }
        return dataResult;
    }

    public MultifaceGrowthConfiguration(MultifaceSpreadeableBlock placeBlock, int searchRange, boolean canPlaceOnFloor, boolean canPlaceOnCeiling, boolean canPlaceOnWall, float chanceOfSpreading, HolderSet<Block> canBePlacedOn) {
        this.placeBlock = placeBlock;
        this.searchRange = searchRange;
        this.canPlaceOnFloor = canPlaceOnFloor;
        this.canPlaceOnCeiling = canPlaceOnCeiling;
        this.canPlaceOnWall = canPlaceOnWall;
        this.chanceOfSpreading = chanceOfSpreading;
        this.canBePlacedOn = canBePlacedOn;
        this.validDirections = new ObjectArrayList(6);
        if (canPlaceOnCeiling) {
            this.validDirections.add(Direction.UP);
        }
        if (canPlaceOnFloor) {
            this.validDirections.add(Direction.DOWN);
        }
        if (canPlaceOnWall) {
            Direction.Plane.HORIZONTAL.forEach(this.validDirections::add);
        }
    }

    public List<Direction> getShuffledDirectionsExcept(RandomSource random, Direction excludeDirection) {
        return Util.toShuffledList(this.validDirections.stream().filter(direction -> direction != excludeDirection), random);
    }

    public List<Direction> getShuffledDirections(RandomSource random) {
        return Util.shuffledCopy(this.validDirections, random);
    }
}

