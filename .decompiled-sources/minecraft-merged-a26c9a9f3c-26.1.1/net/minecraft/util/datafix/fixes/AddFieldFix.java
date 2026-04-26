/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

public class AddFieldFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;
    private final String fieldName;
    private final String[] path;
    private final Function<Dynamic<?>, Dynamic<?>> fieldGenerator;

    public AddFieldFix(Schema outputSchema, DSL.TypeReference type, String fieldName, Function<Dynamic<?>, Dynamic<?>> fieldGenerator, String ... path) {
        super(outputSchema, false);
        this.name = "Adding field `" + fieldName + "` to type `" + type.typeName().toLowerCase(Locale.ROOT) + "`";
        this.type = type;
        this.fieldName = fieldName;
        this.path = path;
        this.fieldGenerator = fieldGenerator;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped(this.name, this.getInputSchema().getType(this.type), this.getOutputSchema().getType(this.type), (Typed<?> input) -> input.update(DSL.remainderFinder(), dynamic -> this.addField((Dynamic<?>)dynamic, 0)));
    }

    private Dynamic<?> addField(Dynamic<?> dynamic, int pathIndex) {
        if (pathIndex >= this.path.length) {
            return dynamic.set(this.fieldName, this.fieldGenerator.apply(dynamic));
        }
        Optional<Dynamic<?>> field = dynamic.get(this.path[pathIndex]).result();
        if (field.isEmpty()) {
            return dynamic;
        }
        return this.addField(field.get(), pathIndex + 1);
    }
}

