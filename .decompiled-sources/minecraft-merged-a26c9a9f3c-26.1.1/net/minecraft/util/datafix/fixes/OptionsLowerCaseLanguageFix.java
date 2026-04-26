/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class OptionsLowerCaseLanguageFix
extends DataFix {
    public OptionsLowerCaseLanguageFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("OptionsLowerCaseLanguageFix", this.getInputSchema().getType(References.OPTIONS), input -> input.update(DSL.remainderFinder(), tag -> {
            Optional<String> lang = tag.get("lang").asString().result();
            if (lang.isPresent()) {
                return tag.set("lang", tag.createString(lang.get().toLowerCase(Locale.ROOT)));
            }
            return tag;
        }));
    }
}

