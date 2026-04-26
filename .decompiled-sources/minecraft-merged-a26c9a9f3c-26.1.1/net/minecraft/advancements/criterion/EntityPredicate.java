/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.DistancePredicate;
import net.minecraft.advancements.criterion.EntityEquipmentPredicate;
import net.minecraft.advancements.criterion.EntityFlagsPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntityTypePredicate;
import net.minecraft.advancements.criterion.LocationPredicate;
import net.minecraft.advancements.criterion.MobEffectsPredicate;
import net.minecraft.advancements.criterion.MovementPredicate;
import net.minecraft.advancements.criterion.NbtPredicate;
import net.minecraft.advancements.criterion.SlotsPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

public record EntityPredicate(Optional<EntityTypePredicate> entityType, Optional<DistancePredicate> distanceToPlayer, Optional<MovementPredicate> movement, LocationWrapper location, Optional<MobEffectsPredicate> effects, Optional<NbtPredicate> nbt, Optional<EntityFlagsPredicate> flags, Optional<EntityEquipmentPredicate> equipment, Optional<EntitySubPredicate> subPredicate, Optional<Integer> periodicTick, Optional<EntityPredicate> vehicle, Optional<EntityPredicate> passenger, Optional<EntityPredicate> targetedEntity, Optional<String> team, Optional<SlotsPredicate> slots, DataComponentMatchers components) {
    public static final Codec<EntityPredicate> CODEC = Codec.recursive("EntityPredicate", subCodec -> RecordCodecBuilder.create(i -> i.group(EntityTypePredicate.CODEC.optionalFieldOf("type").forGetter(EntityPredicate::entityType), DistancePredicate.CODEC.optionalFieldOf("distance").forGetter(EntityPredicate::distanceToPlayer), MovementPredicate.CODEC.optionalFieldOf("movement").forGetter(EntityPredicate::movement), LocationWrapper.CODEC.forGetter(EntityPredicate::location), MobEffectsPredicate.CODEC.optionalFieldOf("effects").forGetter(EntityPredicate::effects), NbtPredicate.CODEC.optionalFieldOf("nbt").forGetter(EntityPredicate::nbt), EntityFlagsPredicate.CODEC.optionalFieldOf("flags").forGetter(EntityPredicate::flags), EntityEquipmentPredicate.CODEC.optionalFieldOf("equipment").forGetter(EntityPredicate::equipment), EntitySubPredicate.CODEC.optionalFieldOf("type_specific").forGetter(EntityPredicate::subPredicate), ExtraCodecs.POSITIVE_INT.optionalFieldOf("periodic_tick").forGetter(EntityPredicate::periodicTick), subCodec.optionalFieldOf("vehicle").forGetter(EntityPredicate::vehicle), subCodec.optionalFieldOf("passenger").forGetter(EntityPredicate::passenger), subCodec.optionalFieldOf("targeted_entity").forGetter(EntityPredicate::targetedEntity), Codec.STRING.optionalFieldOf("team").forGetter(EntityPredicate::team), SlotsPredicate.CODEC.optionalFieldOf("slots").forGetter(EntityPredicate::slots), DataComponentMatchers.CODEC.forGetter(EntityPredicate::components)).apply((Applicative<EntityPredicate, ?>)i, EntityPredicate::new)));
    public static final Codec<ContextAwarePredicate> ADVANCEMENT_CODEC = Codec.withAlternative(ContextAwarePredicate.CODEC, CODEC, EntityPredicate::wrap);

    public static ContextAwarePredicate wrap(Builder singlePredicate) {
        return EntityPredicate.wrap(singlePredicate.build());
    }

    public static Optional<ContextAwarePredicate> wrap(Optional<EntityPredicate> singlePredicate) {
        return singlePredicate.map(EntityPredicate::wrap);
    }

    public static List<ContextAwarePredicate> wrap(Builder ... predicates) {
        return Stream.of(predicates).map(EntityPredicate::wrap).toList();
    }

    public static ContextAwarePredicate wrap(EntityPredicate singlePredicate) {
        LootItemCondition asCondition = LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, singlePredicate).build();
        return new ContextAwarePredicate(List.of(asCondition));
    }

    public boolean matches(ServerPlayer player, @Nullable Entity entity) {
        return this.matches(player.level(), player.position(), entity);
    }

    public boolean matches(ServerLevel level, @Nullable Vec3 position, @Nullable Entity entity) {
        PlayerTeam team;
        Vec3 onPos;
        if (entity == null) {
            return false;
        }
        if (this.entityType.isPresent() && !this.entityType.get().matches(entity.typeHolder())) {
            return false;
        }
        if (position == null ? this.distanceToPlayer.isPresent() : this.distanceToPlayer.isPresent() && !this.distanceToPlayer.get().matches(position.x, position.y, position.z, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.movement.isPresent()) {
            Vec3 knownMovement = entity.getKnownMovement();
            Vec3 velocity = knownMovement.scale(20.0);
            if (!this.movement.get().matches(velocity.x, velocity.y, velocity.z, entity.fallDistance)) {
                return false;
            }
        }
        if (this.location.located.isPresent() && !this.location.located.get().matches(level, entity.getX(), entity.getY(), entity.getZ())) {
            return false;
        }
        if (this.location.steppingOn.isPresent()) {
            onPos = Vec3.atCenterOf(entity.getOnPos());
            if (!entity.onGround() || !this.location.steppingOn.get().matches(level, onPos.x(), onPos.y(), onPos.z())) {
                return false;
            }
        }
        if (this.location.affectsMovement.isPresent()) {
            onPos = Vec3.atCenterOf(entity.getBlockPosBelowThatAffectsMyMovement());
            if (!this.location.affectsMovement.get().matches(level, onPos.x(), onPos.y(), onPos.z())) {
                return false;
            }
        }
        if (this.effects.isPresent() && !this.effects.get().matches(entity)) {
            return false;
        }
        if (this.flags.isPresent() && !this.flags.get().matches(entity)) {
            return false;
        }
        if (this.equipment.isPresent() && !this.equipment.get().matches(entity)) {
            return false;
        }
        if (this.subPredicate.isPresent() && !this.subPredicate.get().matches(entity, level, position)) {
            return false;
        }
        if (this.vehicle.isPresent() && !this.vehicle.get().matches(level, position, entity.getVehicle())) {
            return false;
        }
        if (this.passenger.isPresent() && entity.getPassengers().stream().noneMatch(p -> this.passenger.get().matches(level, position, (Entity)p))) {
            return false;
        }
        if (this.targetedEntity.isPresent() && !this.targetedEntity.get().matches(level, position, entity instanceof Mob ? ((Mob)entity).getTarget() : null)) {
            return false;
        }
        if (this.periodicTick.isPresent() && entity.tickCount % this.periodicTick.get() != 0) {
            return false;
        }
        if (this.team.isPresent() && ((team = entity.getTeam()) == null || !this.team.get().equals(((Team)team).getName()))) {
            return false;
        }
        if (this.slots.isPresent() && !this.slots.get().matches(entity)) {
            return false;
        }
        if (!this.components.test(entity)) {
            return false;
        }
        return this.nbt.isEmpty() || this.nbt.get().matches(entity);
    }

    public static LootContext createContext(ServerPlayer player, Entity entity) {
        LootParams lootParams = new LootParams.Builder(player.level()).withParameter(LootContextParams.THIS_ENTITY, entity).withParameter(LootContextParams.ORIGIN, player.position()).create(LootContextParamSets.ADVANCEMENT_ENTITY);
        return new LootContext.Builder(lootParams).create(Optional.empty());
    }

    public record LocationWrapper(Optional<LocationPredicate> located, Optional<LocationPredicate> steppingOn, Optional<LocationPredicate> affectsMovement) {
        public static final MapCodec<LocationWrapper> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(LocationPredicate.CODEC.optionalFieldOf("location").forGetter(LocationWrapper::located), LocationPredicate.CODEC.optionalFieldOf("stepping_on").forGetter(LocationWrapper::steppingOn), LocationPredicate.CODEC.optionalFieldOf("movement_affected_by").forGetter(LocationWrapper::affectsMovement)).apply((Applicative<LocationWrapper, ?>)i, LocationWrapper::new));
    }

    public static class Builder {
        private Optional<EntityTypePredicate> entityType = Optional.empty();
        private Optional<DistancePredicate> distanceToPlayer = Optional.empty();
        private Optional<MovementPredicate> movement = Optional.empty();
        private Optional<LocationPredicate> located = Optional.empty();
        private Optional<LocationPredicate> steppingOnLocation = Optional.empty();
        private Optional<LocationPredicate> movementAffectedBy = Optional.empty();
        private Optional<MobEffectsPredicate> effects = Optional.empty();
        private Optional<NbtPredicate> nbt = Optional.empty();
        private Optional<EntityFlagsPredicate> flags = Optional.empty();
        private Optional<EntityEquipmentPredicate> equipment = Optional.empty();
        private Optional<EntitySubPredicate> subPredicate = Optional.empty();
        private Optional<Integer> periodicTick = Optional.empty();
        private Optional<EntityPredicate> vehicle = Optional.empty();
        private Optional<EntityPredicate> passenger = Optional.empty();
        private Optional<EntityPredicate> targetedEntity = Optional.empty();
        private Optional<String> team = Optional.empty();
        private Optional<SlotsPredicate> slots = Optional.empty();
        private DataComponentMatchers components = DataComponentMatchers.ANY;

        public static Builder entity() {
            return new Builder();
        }

        public Builder of(HolderGetter<EntityType<?>> lookup, EntityType<?> entityType) {
            this.entityType = Optional.of(EntityTypePredicate.of(lookup, entityType));
            return this;
        }

        public Builder of(HolderGetter<EntityType<?>> lookup, TagKey<EntityType<?>> entityTypeTag) {
            this.entityType = Optional.of(EntityTypePredicate.of(lookup, entityTypeTag));
            return this;
        }

        public Builder entityType(EntityTypePredicate entityType) {
            this.entityType = Optional.of(entityType);
            return this;
        }

        public Builder distance(DistancePredicate distanceToPlayer) {
            this.distanceToPlayer = Optional.of(distanceToPlayer);
            return this;
        }

        public Builder moving(MovementPredicate movement) {
            this.movement = Optional.of(movement);
            return this;
        }

        public Builder located(LocationPredicate.Builder location) {
            this.located = Optional.of(location.build());
            return this;
        }

        public Builder steppingOn(LocationPredicate.Builder location) {
            this.steppingOnLocation = Optional.of(location.build());
            return this;
        }

        public Builder movementAffectedBy(LocationPredicate.Builder location) {
            this.movementAffectedBy = Optional.of(location.build());
            return this;
        }

        public Builder effects(MobEffectsPredicate.Builder effects) {
            this.effects = effects.build();
            return this;
        }

        public Builder nbt(NbtPredicate nbt) {
            this.nbt = Optional.of(nbt);
            return this;
        }

        public Builder flags(EntityFlagsPredicate.Builder flags) {
            this.flags = Optional.of(flags.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate.Builder equipment) {
            this.equipment = Optional.of(equipment.build());
            return this;
        }

        public Builder equipment(EntityEquipmentPredicate equipment) {
            this.equipment = Optional.of(equipment);
            return this;
        }

        public Builder subPredicate(EntitySubPredicate subPredicate) {
            this.subPredicate = Optional.of(subPredicate);
            return this;
        }

        public Builder periodicTick(int period) {
            this.periodicTick = Optional.of(period);
            return this;
        }

        public Builder vehicle(Builder vehicle) {
            this.vehicle = Optional.of(vehicle.build());
            return this;
        }

        public Builder passenger(Builder passenger) {
            this.passenger = Optional.of(passenger.build());
            return this;
        }

        public Builder targetedEntity(Builder targetedEntity) {
            this.targetedEntity = Optional.of(targetedEntity.build());
            return this;
        }

        public Builder team(String team) {
            this.team = Optional.of(team);
            return this;
        }

        public Builder slots(SlotsPredicate slots) {
            this.slots = Optional.of(slots);
            return this;
        }

        public Builder components(DataComponentMatchers components) {
            this.components = components;
            return this;
        }

        public EntityPredicate build() {
            return new EntityPredicate(this.entityType, this.distanceToPlayer, this.movement, new LocationWrapper(this.located, this.steppingOnLocation, this.movementAffectedBy), this.effects, this.nbt, this.flags, this.equipment, this.subPredicate, this.periodicTick, this.vehicle, this.passenger, this.targetedEntity, this.team, this.slots, this.components);
        }
    }
}

