/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class EmptyItemInHotbarFix
extends DataFix {
    public EmptyItemInHotbarFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<?> itemStackF = DSL.typeFinder(this.getInputSchema().getType(References.ITEM_STACK));
        return this.fixTypeEverywhereTyped("EmptyItemInHotbarFix", this.getInputSchema().getType(References.HOTBAR), input -> input.update(itemStackF, namedStack -> namedStack.mapSecond(itemStack -> {
            boolean isEmpty;
            Optional<String> id = ((Either)itemStack.getFirst()).left().map(Pair::getSecond);
            Dynamic remainder = (Dynamic)((Pair)itemStack.getSecond()).getSecond();
            boolean isAir = id.isEmpty() || id.get().equals("minecraft:air");
            boolean bl = isEmpty = remainder.get("Count").asInt(0) <= 0;
            if (isAir || isEmpty) {
                return Pair.of(Either.right(Unit.INSTANCE), Pair.of(Either.right(Unit.INSTANCE), remainder.emptyMap()));
            }
            return itemStack;
        })));
    }
}

