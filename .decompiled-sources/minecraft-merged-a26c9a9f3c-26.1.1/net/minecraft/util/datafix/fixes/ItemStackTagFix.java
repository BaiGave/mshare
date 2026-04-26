/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public abstract class ItemStackTagFix
extends DataFix {
    private final String name;
    private final Predicate<String> idFilter;

    public ItemStackTagFix(Schema outputSchema, String name, Predicate<String> idFilter) {
        super(outputSchema, false);
        this.name = name;
        this.idFilter = idFilter;
    }

    @Override
    public final TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);
        return this.fixTypeEverywhereTyped(this.name, itemStackType, ItemStackTagFix.createFixer(itemStackType, this.idFilter, this::fixItemStackTag));
    }

    public static UnaryOperator<Typed<?>> createFixer(Type<?> itemStackType, Predicate<String> idFilter, UnaryOperator<Typed<?>> fixer) {
        OpticFinder<Pair<String, String>> idF = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> tagF = itemStackType.findField("tag");
        return input -> {
            Optional idOpt = input.getOptional(idF);
            if (idOpt.isPresent() && idFilter.test((String)((Pair)idOpt.get()).getSecond())) {
                return input.updateTyped(tagF, fixer);
            }
            return input;
        };
    }

    protected abstract Typed<?> fixItemStackTag(Typed<?> var1);
}

