/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.function.Function;
import java.util.function.IntFunction;
import net.minecraft.util.datafix.fixes.NamedEntityFix;

public class EntityVariantFix
extends NamedEntityFix {
    private final String fieldName;
    private final IntFunction<String> idConversions;

    public EntityVariantFix(Schema outputSchema, String name, DSL.TypeReference type, String entityName, String fieldName, IntFunction<String> idConversions) {
        super(outputSchema, false, name, type, entityName);
        this.fieldName = fieldName;
        this.idConversions = idConversions;
    }

    private static <T> Dynamic<T> updateAndRename(Dynamic<T> input, String oldKey, String newKey, Function<Dynamic<T>, Dynamic<T>> function) {
        return input.map(v -> {
            DynamicOps<Object> ops = input.getOps();
            Function<Object, Object> liftedFunction = value -> ((Dynamic)function.apply(new Dynamic<Object>(ops, value))).getValue();
            return ops.get(v, oldKey).map(fieldValue -> ops.set(v, newKey, liftedFunction.apply(fieldValue))).result().orElse(v);
        });
    }

    @Override
    protected Typed<?> fix(Typed<?> typed) {
        return typed.update(DSL.remainderFinder(), remainder -> EntityVariantFix.updateAndRename(remainder, this.fieldName, "variant", catType -> DataFixUtils.orElse(catType.asNumber().map(e -> catType.createString(this.idConversions.apply(e.intValue()))).result(), catType)));
    }
}

