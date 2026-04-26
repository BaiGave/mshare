/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.schemas;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.TypeTemplate;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;
import net.minecraft.util.datafix.schemas.V1458;

public class V1906
extends NamespacedSchema {
    public V1906(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerBlockEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerBlockEntities(schema);
        V1906.registerInventory(schema, map, "minecraft:barrel");
        V1906.registerInventory(schema, map, "minecraft:smoker");
        V1906.registerInventory(schema, map, "minecraft:blast_furnace");
        schema.register(map, "minecraft:lectern", (String name) -> DSL.optionalFields("Book", References.ITEM_STACK.in(schema)));
        schema.registerSimple(map, "minecraft:bell");
        return map;
    }

    protected static void registerInventory(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.register(map, name, () -> V1458.nameableInventory(schema));
    }
}

