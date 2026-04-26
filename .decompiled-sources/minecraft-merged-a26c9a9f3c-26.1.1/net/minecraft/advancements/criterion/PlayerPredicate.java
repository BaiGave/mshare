/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.advancements.criterion;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanMaps;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.CriterionProgress;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicate;
import net.minecraft.advancements.criterion.EntitySubPredicates;
import net.minecraft.advancements.criterion.FoodPredicate;
import net.minecraft.advancements.criterion.GameTypePredicate;
import net.minecraft.advancements.criterion.InputPredicate;
import net.minecraft.advancements.criterion.MinMaxBounds;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.ServerRecipeBook;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.stats.StatsCounter;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record PlayerPredicate(MinMaxBounds.Ints level, FoodPredicate food, GameTypePredicate gameType, List<StatMatcher<?>> stats, Object2BooleanMap<ResourceKey<Recipe<?>>> recipes, Map<Identifier, AdvancementPredicate> advancements, Optional<EntityPredicate> lookingAt, Optional<InputPredicate> input) implements EntitySubPredicate
{
    public static final int LOOKING_AT_RANGE = 100;
    public static final MapCodec<PlayerPredicate> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(MinMaxBounds.Ints.CODEC.optionalFieldOf("level", MinMaxBounds.Ints.ANY).forGetter(PlayerPredicate::level), FoodPredicate.CODEC.optionalFieldOf("food", FoodPredicate.ANY).forGetter(PlayerPredicate::food), GameTypePredicate.CODEC.optionalFieldOf("gamemode", GameTypePredicate.ANY).forGetter(PlayerPredicate::gameType), StatMatcher.CODEC.listOf().optionalFieldOf("stats", List.of()).forGetter(PlayerPredicate::stats), ExtraCodecs.object2BooleanMap(Recipe.KEY_CODEC).optionalFieldOf("recipes", Object2BooleanMaps.emptyMap()).forGetter(PlayerPredicate::recipes), Codec.unboundedMap(Identifier.CODEC, AdvancementPredicate.CODEC).optionalFieldOf("advancements", Map.of()).forGetter(PlayerPredicate::advancements), EntityPredicate.CODEC.optionalFieldOf("looking_at").forGetter(PlayerPredicate::lookingAt), InputPredicate.CODEC.optionalFieldOf("input").forGetter(PlayerPredicate::input)).apply((Applicative<PlayerPredicate, ?>)i, PlayerPredicate::new));

    @Override
    public boolean matches(Entity entity, ServerLevel level, @Nullable Vec3 position) {
        if (!(entity instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer player = (ServerPlayer)entity;
        if (!this.level.matches(player.experienceLevel)) {
            return false;
        }
        if (!this.food.matches(player.getFoodData())) {
            return false;
        }
        if (!this.gameType.matches(player.gameMode())) {
            return false;
        }
        ServerStatsCounter stats = player.getStats();
        for (StatMatcher<?> stat : this.stats) {
            if (stat.matches(stats)) continue;
            return false;
        }
        ServerRecipeBook recipes = player.getRecipeBook();
        for (Object2BooleanMap.Entry entry : this.recipes.object2BooleanEntrySet()) {
            if (recipes.contains((ResourceKey)entry.getKey()) == entry.getBooleanValue()) continue;
            return false;
        }
        if (!this.advancements.isEmpty()) {
            PlayerAdvancements advancements = player.getAdvancements();
            ServerAdvancementManager serverAdvancementManager = player.level().getServer().getAdvancements();
            for (Map.Entry<Identifier, AdvancementPredicate> entry : this.advancements.entrySet()) {
                AdvancementHolder advancement = serverAdvancementManager.get(entry.getKey());
                if (advancement != null && entry.getValue().test(advancements.getOrStartProgress(advancement))) continue;
                return false;
            }
        }
        if (this.lookingAt.isPresent()) {
            Vec3 from = player.getEyePosition();
            Vec3 vec3 = player.getViewVector(1.0f);
            Vec3 to = from.add(vec3.x * 100.0, vec3.y * 100.0, vec3.z * 100.0);
            EntityHitResult lookingAtResult = ProjectileUtil.getEntityHitResult(player.level(), player, from, to, new AABB(from, to).inflate(1.0), e -> !e.isSpectator(), 0.0f);
            if (lookingAtResult == null || lookingAtResult.getType() != HitResult.Type.ENTITY) {
                return false;
            }
            Entity lookingAtEntity = lookingAtResult.getEntity();
            if (!this.lookingAt.get().matches(player, lookingAtEntity) || !player.hasLineOfSight(lookingAtEntity)) {
                return false;
            }
        }
        return !this.input.isPresent() || this.input.get().matches(player.getLastClientInput());
    }

    public MapCodec<PlayerPredicate> codec() {
        return EntitySubPredicates.PLAYER;
    }

    private record StatMatcher<T>(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range, Supplier<Stat<T>> stat) {
        public static final Codec<StatMatcher<?>> CODEC = BuiltInRegistries.STAT_TYPE.byNameCodec().dispatch(StatMatcher::type, StatMatcher::createTypedCodec);

        public StatMatcher(StatType<T> type, Holder<T> value, MinMaxBounds.Ints range) {
            this(type, value, range, Suppliers.memoize(() -> type.get(value.value())));
        }

        private static <T> MapCodec<StatMatcher<T>> createTypedCodec(StatType<T> type) {
            return RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)type.getRegistry().holderByNameCodec().fieldOf("stat")).forGetter(StatMatcher::value), MinMaxBounds.Ints.CODEC.optionalFieldOf("value", MinMaxBounds.Ints.ANY).forGetter(StatMatcher::range)).apply((Applicative<StatMatcher, ?>)i, (value, range) -> new StatMatcher(type, value, (MinMaxBounds.Ints)range)));
        }

        public boolean matches(StatsCounter counter) {
            return this.range.matches(counter.getValue(this.stat.get()));
        }
    }

    private static interface AdvancementPredicate
    extends Predicate<AdvancementProgress> {
        public static final Codec<AdvancementPredicate> CODEC = Codec.either(AdvancementDonePredicate.CODEC, AdvancementCriterionsPredicate.CODEC).xmap(Either::unwrap, predicate -> {
            if (predicate instanceof AdvancementDonePredicate) {
                AdvancementDonePredicate done = (AdvancementDonePredicate)predicate;
                return Either.left(done);
            }
            if (predicate instanceof AdvancementCriterionsPredicate) {
                AdvancementCriterionsPredicate criterions = (AdvancementCriterionsPredicate)predicate;
                return Either.right(criterions);
            }
            throw new UnsupportedOperationException();
        });
    }

    public static class Builder {
        private MinMaxBounds.Ints level = MinMaxBounds.Ints.ANY;
        private FoodPredicate food = FoodPredicate.ANY;
        private GameTypePredicate gameType = GameTypePredicate.ANY;
        private final ImmutableList.Builder<StatMatcher<?>> stats = ImmutableList.builder();
        private final Object2BooleanMap<ResourceKey<Recipe<?>>> recipes = new Object2BooleanOpenHashMap();
        private final Map<Identifier, AdvancementPredicate> advancements = Maps.newHashMap();
        private Optional<EntityPredicate> lookingAt = Optional.empty();
        private Optional<InputPredicate> input = Optional.empty();

        public static Builder player() {
            return new Builder();
        }

        public Builder setLevel(MinMaxBounds.Ints level) {
            this.level = level;
            return this;
        }

        public Builder setFood(FoodPredicate food) {
            this.food = food;
            return this;
        }

        public <T> Builder addStat(StatType<T> type, Holder.Reference<T> value, MinMaxBounds.Ints range) {
            this.stats.add((Object)new StatMatcher<T>(type, value, range));
            return this;
        }

        public Builder addRecipe(ResourceKey<Recipe<?>> recipe, boolean present) {
            this.recipes.put(recipe, present);
            return this;
        }

        public Builder setGameType(GameTypePredicate gameType) {
            this.gameType = gameType;
            return this;
        }

        public Builder setLookingAt(EntityPredicate.Builder lookingAt) {
            this.lookingAt = Optional.of(lookingAt.build());
            return this;
        }

        public Builder checkAdvancementDone(Identifier advancement, boolean isDone) {
            this.advancements.put(advancement, new AdvancementDonePredicate(isDone));
            return this;
        }

        public Builder checkAdvancementCriterions(Identifier advancement, Map<String, Boolean> criterions) {
            this.advancements.put(advancement, new AdvancementCriterionsPredicate(new Object2BooleanOpenHashMap<String>(criterions)));
            return this;
        }

        public Builder hasInput(InputPredicate input) {
            this.input = Optional.of(input);
            return this;
        }

        public PlayerPredicate build() {
            return new PlayerPredicate(this.level, this.food, this.gameType, (List<StatMatcher<?>>)((Object)this.stats.build()), this.recipes, this.advancements, this.lookingAt, this.input);
        }
    }

    private record AdvancementCriterionsPredicate(Object2BooleanMap<String> criterions) implements AdvancementPredicate
    {
        public static final Codec<AdvancementCriterionsPredicate> CODEC = ExtraCodecs.object2BooleanMap(Codec.STRING).xmap(AdvancementCriterionsPredicate::new, AdvancementCriterionsPredicate::criterions);

        @Override
        public boolean test(AdvancementProgress progress) {
            for (Object2BooleanMap.Entry entry : this.criterions.object2BooleanEntrySet()) {
                CriterionProgress criterion = progress.getCriterion((String)entry.getKey());
                if (criterion != null && criterion.isDone() == entry.getBooleanValue()) continue;
                return false;
            }
            return true;
        }
    }

    private record AdvancementDonePredicate(boolean state) implements AdvancementPredicate
    {
        public static final Codec<AdvancementDonePredicate> CODEC = Codec.BOOL.xmap(AdvancementDonePredicate::new, AdvancementDonePredicate::state);

        @Override
        public boolean test(AdvancementProgress progress) {
            return progress.isDone() == this.state;
        }
    }
}

