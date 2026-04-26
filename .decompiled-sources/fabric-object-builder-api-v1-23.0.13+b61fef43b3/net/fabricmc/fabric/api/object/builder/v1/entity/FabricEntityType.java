/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.object.builder.v1.entity;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.fabricmc.fabric.impl.object.builder.FabricEntityTypeImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacementType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.level.levelgen.Heightmap;

public interface FabricEntityType {

    public static interface Builder<T extends Entity> {
        default public EntityType.Builder<T> alwaysUpdateVelocity(boolean alwaysUpdateVelocity) {
            throw new AssertionError((Object)"Implemented in Mixin");
        }

        default public EntityType.Builder<T> canPotentiallyExecuteCommands(boolean canPotentiallyExecuteCommands) {
            throw new AssertionError((Object)"Implemented in Mixin");
        }

        public static <T extends LivingEntity> EntityType.Builder<T> createLiving(EntityType.EntityFactory<T> factory, MobCategory mobCategory, UnaryOperator<Living<T>> livingBuilder) {
            return FabricEntityTypeImpl.Builder.createLiving(factory, mobCategory, livingBuilder);
        }

        public static <T extends net.minecraft.world.entity.Mob> EntityType.Builder<T> createMob(EntityType.EntityFactory<T> factory, MobCategory mobCategory, UnaryOperator<Mob<T>> mobBuilder) {
            return FabricEntityTypeImpl.Builder.createMob(factory, mobCategory, mobBuilder);
        }

        public static interface Mob<T extends net.minecraft.world.entity.Mob>
        extends Living<T> {
            public Mob<T> spawnPlacement(SpawnPlacementType var1, Heightmap.Types var2, SpawnPlacements.SpawnPredicate<T> var3);

            @Override
            public Mob<T> defaultAttributes(Supplier<AttributeSupplier.Builder> var1);
        }

        public static interface Living<T extends LivingEntity> {
            public Living<T> defaultAttributes(Supplier<AttributeSupplier.Builder> var1);
        }
    }
}

