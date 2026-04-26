/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.object.builder;

import java.util.Objects;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public interface FabricEntityTypeImpl {
    public void fabric_setAlwaysUpdateVelocity(@Nullable Boolean var1);

    public void fabric_setCanPotentiallyExecuteCommands(@Nullable Boolean var1);

    public static interface Builder {
        public void fabric_setLivingEntityBuilder(Living<? extends LivingEntity> var1);

        public void fabric_setMobEntityBuilder(Mob<? extends net.minecraft.world.entity.Mob> var1);

        public static <T extends LivingEntity> EntityType.Builder<T> createLiving(EntityType.EntityFactory<T> factory, MobCategory mobCategory, UnaryOperator<FabricEntityType.Builder.Living<T>> livingBuilder) {
            EntityType.Builder<T> builder = EntityType.Builder.of(factory, mobCategory);
            Living builderImpl = new Living();
            livingBuilder.apply(builderImpl);
            ((Builder)((Object)builder)).fabric_setLivingEntityBuilder(builderImpl);
            return builder;
        }

        public static <T extends net.minecraft.world.entity.Mob> EntityType.Builder<T> createMob(EntityType.EntityFactory<T> factory, MobCategory mobCategory, UnaryOperator<FabricEntityType.Builder.Mob<T>> mobBuilder) {
            EntityType.Builder<T> builder = EntityType.Builder.of(factory, mobCategory);
            Mob builderImpl = new Mob();
            mobBuilder.apply(builderImpl);
            ((Builder)((Object)builder)).fabric_setMobEntityBuilder(builderImpl);
            return builder;
        }

        public static sealed class Living<T extends LivingEntity>
        implements FabricEntityType.Builder.Living<T>
        permits Mob {
            private @Nullable Supplier<AttributeSupplier.Builder> defaultAttributeBuilder;

            @Override
            public FabricEntityType.Builder.Living<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
                Objects.requireNonNull(defaultAttributeBuilder, "Cannot set null attribute builder");
                this.defaultAttributeBuilder = defaultAttributeBuilder;
                return this;
            }

            public void onBuild(EntityType<T> type) {
                if (this.defaultAttributeBuilder != null) {
                    FabricDefaultAttributeRegistry.register(type, this.defaultAttributeBuilder.get());
                }
            }
        }

        public static final class Mob<T extends net.minecraft.world.entity.Mob>
        extends Living<T>
        implements FabricEntityType.Builder.Mob<T> {
            private SpawnPlacementType placementType;
            private Heightmap.Types placementHeightmap;
            private SpawnPlacements.SpawnPredicate<T> spawnPredicate;

            @Override
            public FabricEntityType.Builder.Mob<T> spawnPlacement(SpawnPlacementType placementType, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> spawnPredicate) {
                this.placementType = Objects.requireNonNull(placementType, "Placement type cannot be null.");
                this.placementHeightmap = Objects.requireNonNull(heightmap, "Heightmap type cannot be null.");
                this.spawnPredicate = Objects.requireNonNull(spawnPredicate, "Spawn predicate cannot be null.");
                return this;
            }

            @Override
            public FabricEntityType.Builder.Mob<T> defaultAttributes(Supplier<AttributeSupplier.Builder> defaultAttributeBuilder) {
                super.defaultAttributes(defaultAttributeBuilder);
                return this;
            }

            @Override
            public void onBuild(EntityType<T> type) {
                super.onBuild(type);
                if (this.spawnPredicate != null) {
                    SpawnPlacements.register(type, this.placementType, this.placementHeightmap, this.spawnPredicate);
                }
            }
        }
    }
}

