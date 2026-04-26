/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.Validatable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.entries.ComposableEntryContainer;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntries;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntry;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public abstract class CompositeEntryBase
extends LootPoolEntryContainer {
    public static final ProblemReporter.Problem NO_CHILDREN_PROBLEM = new ProblemReporter.Problem(){

        @Override
        public String description() {
            return "Empty children list";
        }
    };
    protected final List<LootPoolEntryContainer> children;
    private final ComposableEntryContainer composedChildren;

    protected CompositeEntryBase(List<LootPoolEntryContainer> children, List<LootItemCondition> conditions) {
        super(conditions);
        this.children = children;
        this.composedChildren = this.compose(children);
    }

    public abstract MapCodec<? extends CompositeEntryBase> codec();

    @Override
    public void validate(ValidationContext context) {
        super.validate(context);
        if (this.children.isEmpty()) {
            context.reportProblem(NO_CHILDREN_PROBLEM);
        }
        Validatable.validate(context, "children", this.children);
    }

    protected abstract ComposableEntryContainer compose(List<? extends ComposableEntryContainer> var1);

    @Override
    public final boolean expand(LootContext context, Consumer<LootPoolEntry> output) {
        if (!this.canRun(context)) {
            return false;
        }
        return this.composedChildren.expand(context, output);
    }

    public static <T extends CompositeEntryBase> MapCodec<T> createCodec(CompositeEntryConstructor<T> constructor) {
        return RecordCodecBuilder.mapCodec(i -> i.group(LootPoolEntries.CODEC.listOf().optionalFieldOf("children", List.of()).forGetter(e -> e.children)).and(CompositeEntryBase.commonFields(i).t1()).apply(i, constructor::create));
    }

    @FunctionalInterface
    public static interface CompositeEntryConstructor<T extends CompositeEntryBase> {
        public T create(List<LootPoolEntryContainer> var1, List<LootItemCondition> var2);
    }
}

