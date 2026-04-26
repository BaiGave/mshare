/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.functions;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctions;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class FilteredFunction
extends LootItemConditionalFunction {
    public static final MapCodec<FilteredFunction> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> FilteredFunction.commonFields(i).and(i.group(((MapCodec)ItemPredicate.CODEC.fieldOf("item_filter")).forGetter(f -> f.filter), LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_pass").forGetter(f -> f.onPass), LootItemFunctions.ROOT_CODEC.optionalFieldOf("on_fail").forGetter(f -> f.onFail))).apply((Applicative<FilteredFunction, ?>)i, FilteredFunction::new));
    private final ItemPredicate filter;
    private final Optional<LootItemFunction> onPass;
    private final Optional<LootItemFunction> onFail;

    private FilteredFunction(List<LootItemCondition> predicates, ItemPredicate filter, Optional<LootItemFunction> onPass, Optional<LootItemFunction> onFail) {
        super(predicates);
        this.filter = filter;
        this.onPass = onPass;
        this.onFail = onFail;
    }

    public MapCodec<FilteredFunction> codec() {
        return MAP_CODEC;
    }

    @Override
    public ItemStack run(ItemStack itemStack, LootContext context) {
        Optional<LootItemFunction> function;
        Optional<LootItemFunction> optional = function = this.filter.test(itemStack) ? this.onPass : this.onFail;
        if (function.isPresent()) {
            return (ItemStack)function.get().apply(itemStack, context);
        }
        return itemStack;
    }

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        Validatable.validate(context, "on_pass", this.onPass);
        Validatable.validate(context, "on_fail", this.onFail);
    }

    public static Builder filtered(ItemPredicate predicate) {
        return new Builder(predicate);
    }

    public static class Builder
    extends LootItemConditionalFunction.Builder<Builder> {
        private final ItemPredicate itemPredicate;
        private Optional<LootItemFunction> onPass = Optional.empty();
        private Optional<LootItemFunction> onFail = Optional.empty();

        private Builder(ItemPredicate itemPredicate) {
            this.itemPredicate = itemPredicate;
        }

        @Override
        protected Builder getThis() {
            return this;
        }

        public Builder onPass(Optional<LootItemFunction> onPass) {
            this.onPass = onPass;
            return this;
        }

        public Builder onFail(Optional<LootItemFunction> onFail) {
            this.onFail = onFail;
            return this;
        }

        @Override
        public LootItemFunction build() {
            return new FilteredFunction(this.getConditions(), this.itemPredicate, this.onPass, this.onFail);
        }
    }
}

