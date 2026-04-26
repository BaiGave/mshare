/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.References;

public class IglooMetadataRemovalFix
extends DataFix {
    public IglooMetadataRemovalFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(References.STRUCTURE_FEATURE);
        return this.fixTypeEverywhereTyped("IglooMetadataRemovalFix", type, typed -> typed.update(DSL.remainderFinder(), IglooMetadataRemovalFix::fixTag));
    }

    private static <T> Dynamic<T> fixTag(Dynamic<T> input) {
        boolean isIglooOnly = input.get("Children").asStreamOpt().map(s -> s.allMatch(IglooMetadataRemovalFix::isIglooPiece)).result().orElse(false);
        if (isIglooOnly) {
            return input.set("id", input.createString("Igloo")).remove("Children");
        }
        return input.update("Children", IglooMetadataRemovalFix::removeIglooPieces);
    }

    private static <T> Dynamic<T> removeIglooPieces(Dynamic<T> children) {
        return children.asStreamOpt().map(s -> s.filter(v -> !IglooMetadataRemovalFix.isIglooPiece(v))).map(children::createList).result().orElse(children);
    }

    private static boolean isIglooPiece(Dynamic<?> tag) {
        return tag.get("id").asString("").equals("Iglu");
    }
}

