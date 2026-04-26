/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.enchantment.effects;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.util.valueproviders.FloatProviders;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.effects.EnchantmentEntityEffect;
import net.minecraft.world.phys.Vec3;

public record SpawnParticlesEffect(ParticleOptions particle, PositionSource horizontalPosition, PositionSource verticalPosition, VelocitySource horizontalVelocity, VelocitySource verticalVelocity, FloatProvider speed) implements EnchantmentEntityEffect
{
    public static final MapCodec<SpawnParticlesEffect> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ParticleTypes.CODEC.fieldOf("particle")).forGetter(SpawnParticlesEffect::particle), PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEffect::horizontalPosition), PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEffect::verticalPosition), VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEffect::horizontalVelocity), VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEffect::verticalVelocity), FloatProviders.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect::speed)).apply((Applicative<SpawnParticlesEffect, ?>)i, SpawnParticlesEffect::new));

    public static PositionSource offsetFromEntityPosition(float offset) {
        return new PositionSource(PositionSourceType.ENTITY_POSITION, offset, 1.0f);
    }

    public static PositionSource inBoundingBox() {
        return new PositionSource(PositionSourceType.BOUNDING_BOX, 0.0f, 1.0f);
    }

    public static VelocitySource movementScaled(float scale) {
        return new VelocitySource(scale, ConstantFloat.ZERO);
    }

    public static VelocitySource fixedVelocity(FloatProvider provider) {
        return new VelocitySource(0.0f, provider);
    }

    @Override
    public void apply(ServerLevel serverLevel, int enchantmentLevel, EnchantedItemInUse item, Entity entity, Vec3 position) {
        RandomSource random = entity.getRandom();
        Vec3 movement = entity.getKnownMovement();
        float bbWidth = entity.getBbWidth();
        float bbHeight = entity.getBbHeight();
        serverLevel.sendParticles(this.particle, this.horizontalPosition.getCoordinate(position.x(), position.x(), bbWidth, random), this.verticalPosition.getCoordinate(position.y(), position.y() + (double)(bbHeight / 2.0f), bbHeight, random), this.horizontalPosition.getCoordinate(position.z(), position.z(), bbWidth, random), 0, this.horizontalVelocity.getVelocity(movement.x(), random), this.verticalVelocity.getVelocity(movement.y(), random), this.horizontalVelocity.getVelocity(movement.z(), random), this.speed.sample(random));
    }

    public MapCodec<SpawnParticlesEffect> codec() {
        return CODEC;
    }

    public record PositionSource(PositionSourceType type, float offset, float scale) {
        public static final MapCodec<PositionSource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)PositionSourceType.CODEC.fieldOf("type")).forGetter(PositionSource::type), Codec.FLOAT.optionalFieldOf("offset", Float.valueOf(0.0f)).forGetter(PositionSource::offset), ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", Float.valueOf(1.0f)).forGetter(PositionSource::scale)).apply((Applicative<PositionSource, ?>)i, PositionSource::new)).validate(positioning -> {
            if (positioning.type() == PositionSourceType.ENTITY_POSITION && positioning.scale() != 1.0f) {
                return DataResult.error(() -> "Cannot scale an entity position coordinate source");
            }
            return DataResult.success(positioning);
        });

        public double getCoordinate(double position, double center, float boundingBoxSpan, RandomSource random) {
            return this.type.getCoordinate(position, center, boundingBoxSpan * this.scale, random) + (double)this.offset;
        }
    }

    public record VelocitySource(float movementScale, FloatProvider base) {
        public static final MapCodec<VelocitySource> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(Codec.FLOAT.optionalFieldOf("movement_scale", Float.valueOf(0.0f)).forGetter(VelocitySource::movementScale), FloatProviders.CODEC.optionalFieldOf("base", ConstantFloat.ZERO).forGetter(VelocitySource::base)).apply((Applicative<VelocitySource, ?>)i, VelocitySource::new));

        public double getVelocity(double movement, RandomSource random) {
            return movement * (double)this.movementScale + (double)this.base.sample(random);
        }
    }

    public static enum PositionSourceType implements StringRepresentable
    {
        ENTITY_POSITION("entity_position", (pos, center, bbSpan, random) -> pos),
        BOUNDING_BOX("in_bounding_box", (pos, center, bbSpan, random) -> center + (random.nextDouble() - 0.5) * (double)bbSpan);

        public static final Codec<PositionSourceType> CODEC;
        private final String id;
        private final CoordinateSource source;

        private PositionSourceType(String id, CoordinateSource source) {
            this.id = id;
            this.source = source;
        }

        public double getCoordinate(double position, double center, float boundingBoxSpan, RandomSource random) {
            return this.source.getCoordinate(position, center, boundingBoxSpan, random);
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }

        static {
            CODEC = StringRepresentable.fromEnum(PositionSourceType::values);
        }

        @FunctionalInterface
        private static interface CoordinateSource {
            public double getCoordinate(double var1, double var3, float var5, RandomSource var6);
        }
    }
}

