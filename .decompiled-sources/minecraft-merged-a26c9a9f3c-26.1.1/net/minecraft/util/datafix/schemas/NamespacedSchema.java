/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.Const;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import net.minecraft.resources.Identifier;

public class NamespacedSchema
extends Schema {
    public static final PrimitiveCodec<String> NAMESPACED_STRING_CODEC = new PrimitiveCodec<String>(){

        @Override
        public <T> DataResult<String> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).map(NamespacedSchema::ensureNamespaced);
        }

        @Override
        public <T> T write(DynamicOps<T> ops, String value) {
            return ops.createString(value);
        }

        public String toString() {
            return "NamespacedString";
        }
    };
    private static final Type<String> NAMESPACED_STRING = new Const.PrimitiveType<String>(NAMESPACED_STRING_CODEC);

    public NamespacedSchema(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    public static String ensureNamespaced(String input) {
        Identifier identifier = Identifier.tryParse(input);
        if (identifier != null) {
            return identifier.toString();
        }
        return input;
    }

    public static Type<String> namespacedString() {
        return NAMESPACED_STRING;
    }

    @Override
    public Type<?> getChoiceType(DSL.TypeReference type, String choiceName) {
        return super.getChoiceType(type, NamespacedSchema.ensureNamespaced(choiceName));
    }
}

