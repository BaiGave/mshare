/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.loot.v3.FabricLootPoolBuilder;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.FunctionUserBuilder;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.ConditionUserBuilder;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;
import org.apache.commons.lang3.mutable.MutableInt;

public class LootPool
implements Validatable {
    public static final Codec<LootPool> CODEC = RecordCodecBuilder.create(i -> i.group(((MapCodec)LootPoolEntries.CODEC.listOf().fieldOf("entries")).forGetter(p -> p.entries), LootItemCondition.DIRECT_CODEC.listOf().optionalFieldOf("conditions", List.of()).forGetter(p -> p.conditions), LootItemFunctions.ROOT_CODEC.listOf().optionalFieldOf("functions", List.of()).forGetter(p -> p.functions), ((MapCodec)NumberProviders.CODEC.fieldOf("rolls")).forGetter(p -> p.rolls), ((MapCodec)NumberProviders.CODEC.fieldOf("bonus_rolls")).orElse(ConstantValue.exactly(0.0f)).forGetter(p -> p.bonusRolls)).apply((Applicative<LootPool, ?>)i, LootPool::new));
    public final List<LootPoolEntryContainer> entries;
    public final List<LootItemCondition> conditions;
    private final Predicate<LootContext> compositeCondition;
    public final List<LootItemFunction> functions;
    private final BiFunction<ItemStack, LootContext, ItemStack> compositeFunction;
    public final NumberProvider rolls;
    public final NumberProvider bonusRolls;

    private LootPool(List<LootPoolEntryContainer> entries, List<LootItemCondition> conditions, List<LootItemFunction> functions, NumberProvider rolls, NumberProvider bonusRolls) {
        this.entries = entries;
        this.conditions = conditions;
        this.compositeCondition = Util.allOf(conditions);
        this.functions = functions;
        this.compositeFunction = LootItemFunctions.compose(functions);
        this.rolls = rolls;
        this.bonusRolls = bonusRolls;
    }

    private void addRandomItem(Consumer<ItemStack> result, LootContext context) {
        RandomSource random = context.getRandom();
        ArrayList<LootPoolEntry> validEntries = Lists.newArrayList();
        MutableInt totalWeight = new MutableInt();
        for (LootPoolEntryContainer entry : this.entries) {
            entry.expand(context, e -> {
                int weight = e.getWeight(context.getLuck());
                if (weight > 0) {
                    validEntries.add(e);
                    totalWeight.add(weight);
                }
            });
        }
        int entryCount = validEntries.size();
        if (totalWeight.intValue() == 0 || entryCount == 0) {
            return;
        }
        if (entryCount == 1) {
            ((LootPoolEntry)validEntries.get(0)).createItemStack(result, context);
            return;
        }
        int index = random.nextInt(totalWeight.intValue());
        for (LootPoolEntry entry : validEntries) {
            if ((index -= entry.getWeight(context.getLuck())) >= 0) continue;
            entry.createItemStack(result, context);
            return;
        }
    }

    public void addRandomItems(Consumer<ItemStack> result, LootContext context) {
        if (!this.compositeCondition.test(context)) {
            return;
        }
        Consumer<ItemStack> decoratedConsumer = LootItemFunction.decorate(this.compositeFunction, result, context);
        int count = this.rolls.getInt(context) + Mth.floor(this.bonusRolls.getFloat(context) * context.getLuck());
        for (int i = 0; i < count; ++i) {
            this.addRandomItem(decoratedConsumer, context);
        }
    }

    @Override
    public void validate(ValidationContext output) {
        Validatable.validate(output, "conditions", this.conditions);
        Validatable.validate(output, "functions", this.functions);
        Validatable.validate(output, "entries", this.entries);
        Validatable.validate(output, "rolls", this.rolls);
        Validatable.validate(output, "bonus_rolls", this.bonusRolls);
    }

    public static Builder lootPool() {
        return new Builder();
    }

    public static class Builder
    implements FunctionUserBuilder<Builder>,
    ConditionUserBuilder<Builder>,
    FabricLootPoolBuilder {
        private final ImmutableList.Builder<LootPoolEntryContainer> entries = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemCondition> conditions = ImmutableList.builder();
        private final ImmutableList.Builder<LootItemFunction> functions = ImmutableList.builder();
        private NumberProvider rolls = ConstantValue.exactly(1.0f);
        private NumberProvider bonusRolls = ConstantValue.exactly(0.0f);

        public Builder setRolls(NumberProvider rolls) {
            this.rolls = rolls;
            return this;
        }

        @Override
        public Builder unwrap() {
            return this;
        }

        public Builder setBonusRolls(NumberProvider bonusRolls) {
            this.bonusRolls = bonusRolls;
            return this;
        }

        public Builder add(LootPoolEntryContainer.Builder<?> entry) {
            this.entries.add((Object)entry.build());
            return this;
        }

        @Override
        public Builder when(LootItemCondition.Builder condition) {
            this.conditions.add((Object)condition.build());
            return this;
        }

        @Override
        public Builder apply(LootItemFunction.Builder function) {
            this.functions.add((Object)function.build());
            return this;
        }

        public LootPool build() {
            return new LootPool((List<LootPoolEntryContainer>)((Object)this.entries.build()), (List<LootItemCondition>)((Object)this.conditions.build()), (List<LootItemFunction>)((Object)this.functions.build()), this.rolls, this.bonusRolls);
        }
    }
}

