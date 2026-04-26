/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import net.minecraft.util.datafix.LegacyComponentDataFixUtils;
import net.minecraft.util.datafix.fixes.References;

public class ScoreboardDisplayNameFix
extends DataFix {
    private final String name;
    private final DSL.TypeReference type;

    public ScoreboardDisplayNameFix(Schema outputSchema, String name, DSL.TypeReference type) {
        super(outputSchema, false);
        this.name = name;
        this.type = type;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> inputType = this.getInputSchema().getType(this.type);
        OpticFinder<?> displayNameF = inputType.findField("DisplayName");
        OpticFinder<?> textComponentF = DSL.typeFinder(this.getInputSchema().getType(References.TEXT_COMPONENT));
        return this.fixTypeEverywhereTyped(this.name, inputType, team -> team.updateTyped(displayNameF, displayName -> displayName.update(textComponentF, textComponent -> textComponent.mapSecond(LegacyComponentDataFixUtils::createTextComponentJson))));
    }
}

