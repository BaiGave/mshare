/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import com.google.common.collect.ImmutableSet;
import java.util.Objects;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

@Deprecated
public class FabricEntityTypeBuilder<T extends Entity> {
    private MobCategory mobCategory;
    private EntityType.EntityFactory<T> factory;
    private boolean saveable = true;
    private boolean summonable = true;
    private int trackRange = 5;
    private int trackedUpdateRate = 3;
    private Boolean forceTrackedVelocityUpdates;
    private boolean fireImmune = false;
    private boolean spawnableFarFromPlayer;
    private EntityDimensions dimensions = EntityDimensions.scalable(-1.0f, -1.0f);
    private ImmutableSet<Block> specificSpawnBlocks = ImmutableSet.of();
    private @Nullable FeatureFlag[] requiredFeatures = null;

    protected FabricEntityTypeBuilder(MobCategory mobCategory, EntityType.EntityFactory<T> factory) {
        this.mobCategory = mobCategory;
        this.factory = factory;
        this.spawnableFarFromPlayer = mobCategory == MobCategory.CREATURE || mobCategory == MobCategory.MISC;
    }

    @Deprecated
    public static <T extends Entity> FabricEntityTypeBuilder<T> create() {
        return FabricEntityTypeBuilder.create(MobCategory.MISC);
    }

    @Deprecated
    public static <T extends Entity> FabricEntityTypeBuilder<T> create(MobCategory mobCategory) {
        return FabricEntityTypeBuilder.create(mobCategory, FabricEntityTypeBuilder::emptyFactory);
    }

    @Deprecated
    public static <T extends Entity> FabricEntityTypeBuilder<T> create(MobCategory mobCategory, EntityType.EntityFactory<T> factory) {
        return new FabricEntityTypeBuilder<T>(mobCategory, factory);
    }

    @Deprecated
    public static <T extends LivingEntity> Living<T> createLiving() {
        return new Living<LivingEntity>(MobCategory.MISC, FabricEntityTypeBuilder::emptyFactory);
    }

    public static <T extends net.minecraft.world.entity.Mob> Mob<T> createMob() {
        return new Mob<net.minecraft.world.entity.Mob>(MobCategory.MISC, FabricEntityTypeBuilder::emptyFactory);
    }

    private static <T extends Entity> T emptyFactory(EntityType<T> type, Level level) {
        return null;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> mobCategory(MobCategory category) {
        Objects.requireNonNull(category, "Category cannot be null");
        this.mobCategory = category;
        return this;
    }

    @Deprecated
    public <N extends T> FabricEntityTypeBuilder<N> entityFactory(EntityType.EntityFactory<N> factory) {
        Objects.requireNonNull(factory, "Entity Factory cannot be null");
        this.factory = factory;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> disableSummon() {
        this.summonable = false;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> disableSaving() {
        this.saveable = false;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> fireImmune() {
        this.fireImmune = true;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> spawnableFarFromPlayer() {
        this.spawnableFarFromPlayer = true;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> dimensions(EntityDimensions dimensions) {
        Objects.requireNonNull(dimensions, "Cannot set null dimensions");
        this.dimensions = dimensions;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> trackable(int trackRangeBlocks, int trackedUpdateRate) {
        return this.trackable(trackRangeBlocks, trackedUpdateRate, true);
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> trackable(int trackRangeBlocks, int trackedUpdateRate, boolean forceTrackedVelocityUpdates) {
        this.trackRangeBlocks(trackRangeBlocks);
        this.trackedUpdateRate(trackedUpdateRate);
        this.forceTrackedVelocityUpdates(forceTrackedVelocityUpdates);
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> trackRangeChunks(int range) {
        this.trackRange = range;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> trackRangeBlocks(int range) {
        return this.trackRangeChunks((range + 15) / 16);
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> trackedUpdateRate(int rate) {
        this.trackedUpdateRate = rate;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
        this.forceTrackedVelocityUpdates = forceTrackedVelocityUpdates;
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> specificSpawnBlocks(Block ... blocks) {
        this.specificSpawnBlocks = ImmutableSet.copyOf(blocks);
        return this;
    }

    @Deprecated
    public FabricEntityTypeBuilder<T> requires(FeatureFlag ... requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
        return this;
    }

    @Deprecated
    public EntityType<T> build(ResourceKey<EntityType<?>> key) {
        EntityType.Builder<T> builder = EntityType.Builder.of(this.factory, this.mobCategory).immuneTo((Block[])this.specificSpawnBlocks.toArray(Block[]::new)).clientTrackingRange(this.trackRange).updateInterval(this.trackedUpdateRate).sized(this.dimensions.width(), this.dimensions.height());
        if (!this.saveable) {
            builder = builder.noSave();
        }
        if (!this.summonable) {
            builder = builder.noSummon();
        }
        if (this.fireImmune) {
            builder = builder.fireImmune();
        }
        if (this.spawnableFarFromPlayer) {
            builder = builder.canSpawnFarFromPlayer();
        }
        if (this.requiredFeatures != null) {
            builder = builder.requiredFeatures(this.requiredFeatures);
        }
        if (this.forceTrackedVelocityUpdates != null) {
            builder = builder.alwaysUpdateVelocity(this.forceTrackedVelocityUpdates);
        }
        return builder.build(key);
    }

    @Deprecated
    public static class Living<T extends LivingEntity>
    extends FabricEntityTypeBuilder<T> {
        private @Nullable Supplier<AttributeSupplier.Builder> defaultAttributeBuilder;

        protected Living(MobCategory mobCategory, EntityType.EntityFactory<T> function) {
            super(mobCategory, function);
        }

        @Override
        public Living<T> mobCategory(MobCategory category) {
            super.mobCategory(category);
            return this;
        }

        @Override
        public <N extends T> Living<N> entityFactory(EntityType.EntityFactory<N> factory) {
            super.entityFactory(factory);
            return this;
        }

        @Override
        public Living<T> disableSummon() {
            super.disableSummon();
            return this;
        }

        @Override
        public Living<T> disableSaving() {
            super.disableSaving();
            return this;
        }

        @Override
        public Living<T> fireImmune() {
            super.fireImmune();
            return this;
        }

        @Override
        public Living<T> spawnableFarFromPlayer() {
            super.spawnableFarFromPlayer();
            return this;
        }

        @Override
        public Living<T> dimensions(EntityDimensions dimensions) {
            super.dimensions(dimensions);
            return this;
        }

        @Override
        @Deprecated
        public Living<T> trackable(int trackRangeBlocks, int trackedUpdateRate) {
            super.trackable(trackRangeBlocks, trackedUpdateRate);
            return this;
        }

        @Override
        @Deprecated
        public Living<T> trackable(int trackRangeBlocks, int trackedUpdateRate, boolean forceTrackedVelocityUpdates) {
            super.trackable(trackRangeBlocks, trackedUpdateRate, forceTrackedVelocityUpdates);
            return this;
        }

        @Override
        public Living<T> trackRangeChunks(int range) {
            super.trackRangeChunks(range);
            return this;
        }

        @Override
        public Living<T> trackRangeBlocks(int range) {
            super.trackRangeBlocks(range);
            return this;
        }

        @Override
        public Living<T> trackedUpdateRate(int rate) {
            super.trackedUpdateRate(rate);
            return this;
        }

        @Override
        public Living<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
            super.forceTrackedVelocityUpdates(forceTrackedVelocityUpdates);
            return this;
        }

        @Override
        public Living<T> specificSpawnBlocks(Block ... blocks) {
            super.specificSpawnBlocks(blocks);
            return this;
        }

        @Deprecated
        public Living<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
            Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
            this.defaultAttributeBuilder = defaultAttributeBuilder;
            return this;
        }

        @Override
        @Deprecated
        public EntityType<T> build(ResourceKey<EntityType<?>> key) {
            EntityType type = super.build(key);
            if (this.defaultAttributeBuilder != null) {
                FabricDefaultAttributeRegistry.register(type, this.defaultAttributeBuilder.get());
            }
            return type;
        }
    }

    @Deprecated
    public static class Mob<T extends net.minecraft.world.entity.Mob>
    extends Living<T> {
        private SpawnPlacementType spawnPlacementType;
        private Heightmap.Types placementHeightmap;
        private SpawnPlacements.SpawnPredicate<T> spawnPredicate;

        protected Mob(MobCategory mobCategory, EntityType.EntityFactory<T> function) {
            super(mobCategory, function);
        }

        @Override
        public Mob<T> mobCategory(MobCategory category) {
            super.mobCategory(category);
            return this;
        }

        @Override
        public <N extends T> Mob<N> entityFactory(EntityType.EntityFactory<N> factory) {
            super.entityFactory((EntityType.EntityFactory)factory);
            return this;
        }

        @Override
        public Mob<T> disableSummon() {
            super.disableSummon();
            return this;
        }

        @Override
        public Mob<T> disableSaving() {
            super.disableSaving();
            return this;
        }

        @Override
        public Mob<T> fireImmune() {
            super.fireImmune();
            return this;
        }

        @Override
        public Mob<T> spawnableFarFromPlayer() {
            super.spawnableFarFromPlayer();
            return this;
        }

        @Override
        public Mob<T> dimensions(EntityDimensions dimensions) {
            super.dimensions(dimensions);
            return this;
        }

        @Override
        @Deprecated
        public Mob<T> trackable(int trackRangeBlocks, int trackedUpdateRate) {
            super.trackable(trackRangeBlocks, trackedUpdateRate);
            return this;
        }

        @Override
        @Deprecated
        public Mob<T> trackable(int trackRangeBlocks, int trackedUpdateRate, boolean forceTrackedVelocityUpdates) {
            super.trackable(trackRangeBlocks, trackedUpdateRate, forceTrackedVelocityUpdates);
            return this;
        }

        @Override
        public Mob<T> trackRangeChunks(int range) {
            super.trackRangeChunks(range);
            return this;
        }

        @Override
        public Mob<T> trackRangeBlocks(int range) {
            super.trackRangeBlocks(range);
            return this;
        }

        @Override
        public Mob<T> trackedUpdateRate(int rate) {
            super.trackedUpdateRate(rate);
            return this;
        }

        @Override
        public Mob<T> forceTrackedVelocityUpdates(boolean forceTrackedVelocityUpdates) {
            super.forceTrackedVelocityUpdates(forceTrackedVelocityUpdates);
            return this;
        }

        @Override
        public Mob<T> specificSpawnBlocks(Block ... blocks) {
            super.specificSpawnBlocks(blocks);
            return this;
        }

        @Override
        public Mob<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
            super.defaultAttributes(defaultAttributeBuilder);
            return this;
        }

        @Deprecated
        public Mob<T> spawnPlacement(SpawnPlacementType spawnPlacementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
            this.spawnPlacementType = Objects.requireNonNull(spawnPlacementType, "Spawn placement type cannot be null.");
            this.placementHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
            this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
            return this;
        }

        @Override
        public EntityType<T> build(ResourceKey<EntityType<?>> key) {
            EntityType type = super.build(key);
            if (this.spawnPredicate != null) {
                SpawnPlacements.register(type, this.spawnPlacementType, this.placementHeightmap, this.spawnPredicate);
            }
            return type;
        }
    }
}

