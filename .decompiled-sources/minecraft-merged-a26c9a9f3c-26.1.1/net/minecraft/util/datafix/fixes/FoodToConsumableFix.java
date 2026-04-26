/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class FoodToConsumableFix
extends DataFix {
    public FoodToConsumableFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return this.writeFixAndRead("Food to consumable fix", this.getInputSchema().getType(References.DATA_COMPONENTS), this.getOutputSchema().getType(References.DATA_COMPONENTS), components -> {
            Optional foodComponent = components.get("minecraft:food").result();
            if (foodComponent.isPresent()) {
                float eatSeconds = foodComponent.get().get("eat_seconds").asFloat(1.6f);
                Stream<Dynamic<Dynamic>> effects = foodComponent.get().get("effects").asStream();
                Stream<Dynamic> onConsumeEffects = effects.map(effect -> effect.emptyMap().set("type", effect.createString("minecraft:apply_effects")).set("effects", effect.createList(effect.get("effect").result().stream())).set("probability", effect.createFloat(effect.get("probability").asFloat(1.0f))));
                components = Dynamic.copyField(foodComponent.get(), "using_converts_to", components, "minecraft:use_remainder");
                components = components.set("minecraft:food", foodComponent.get().remove("eat_seconds").remove("effects").remove("using_converts_to"));
                components = components.set("minecraft:consumable", components.emptyMap().set("consume_seconds", components.createFloat(eatSeconds)).set("on_consume_effects", components.createList(onConsumeEffects)));
                return components;
            }
            return components;
        });
    }
}

