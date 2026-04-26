/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TaggedChoice;
import java.util.Locale;

public class AddNewChoices
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;

    public AddNewChoices(Schema outputSchema, String name, DSL.TypeReference type) {
        super(outputSchema, true);
        this.name = name;
        this.type = type;
    }

    @Override
    public TypeRewriteRule makeRule() {
        TaggedChoice.TaggedChoiceType<?> inputType = this.getInputSchema().findChoiceType(this.type);
        TaggedChoice.TaggedChoiceType<?> outputType = this.getOutputSchema().findChoiceType(this.type);
        return this.cap(inputType, outputType);
    }

    private <K> TypeRewriteRule cap(TaggedChoice.TaggedChoiceType<K> inputType, TaggedChoice.TaggedChoiceType<?> outputType) {
        if (inputType.getKeyType() != outputType.getKeyType()) {
            throw new IllegalStateException("Could not inject: key type is not the same");
        }
        TaggedChoice.TaggedChoiceType<?> outputChoiceType = outputType;
        return this.fixTypeEverywhere(this.name, inputType, outputChoiceType, ops -> input -> {
            if (!outputChoiceType.hasType(input.getFirst())) {
                throw new IllegalArgumentException(String.format(Locale.ROOT, "%s: Unknown type %s in '%s'", this.name, input.getFirst(), this.type.typeName()));
            }
            return input;
        });
    }
}

