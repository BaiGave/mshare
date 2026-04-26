/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.Streams;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class HorseBodyArmorItemFix
extends NamedEntityWriteReadFix {
    private final String previousBodyArmorTag;
    private final boolean clearArmorItems;

    public HorseBodyArmorItemFix(Schema outputSchema, String entityName, String previousBodyArmorTag, boolean clearArmorItems) {
        super(outputSchema, true, "Horse armor fix for " + entityName, References.ENTITY, entityName);
        this.previousBodyArmorTag = previousBodyArmorTag;
        this.clearArmorItems = clearArmorItems;
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> input) {
        Optional<Dynamic<T>> previousBodyArmor = input.get(this.previousBodyArmorTag).result();
        if (previousBodyArmor.isPresent()) {
            Dynamic<T> bodyArmorItem = previousBodyArmor.get();
            Dynamic<T> output = input.remove(this.previousBodyArmorTag);
            if (this.clearArmorItems) {
                output = output.update("ArmorItems", armorItems -> armorItems.createList(Streams.mapWithIndex(armorItems.asStream(), (entry, index) -> index == 2L ? entry.emptyMap() : entry)));
                output = output.update("ArmorDropChances", armorDropChances -> armorDropChances.createList(Streams.mapWithIndex(armorDropChances.asStream(), (entry, index) -> index == 2L ? entry.createFloat(0.085f) : entry)));
            }
            output = output.set("body_armor_item", bodyArmorItem);
            output = output.set("body_armor_drop_chance", input.createFloat(2.0f));
            return output;
        }
        return input;
    }
}

