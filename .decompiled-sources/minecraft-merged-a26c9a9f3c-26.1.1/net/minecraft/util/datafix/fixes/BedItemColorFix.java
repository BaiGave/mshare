/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class BedItemColorFix
extends DataFix {
    public BedItemColorFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        OpticFinder<Pair<String, String>> idF = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        return this.fixTypeEverywhereTyped("BedItemColorFix", this.getInputSchema().getType(References.ITEM_STACK), input -> {
            Dynamic tag;
            Optional idOpt = input.getOptional(idF);
            if (idOpt.isPresent() && Objects.equals(((Pair)idOpt.get()).getSecond(), "minecraft:bed") && (tag = input.get(DSL.remainderFinder())).get("Damage").asInt(0) == 0) {
                return input.set(DSL.remainderFinder(), tag.set("Damage", tag.createShort((short)14)));
            }
            return input;
        });
    }
}

