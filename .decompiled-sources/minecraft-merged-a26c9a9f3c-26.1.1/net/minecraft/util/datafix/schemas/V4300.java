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

public class V4300
extends NamespacedSchema {
    public V4300(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        schema.register(map, "minecraft:llama", (String name) -> V4300.entityWithInventory(schema));
        schema.register(map, "minecraft:trader_llama", (String name) -> V4300.entityWithInventory(schema));
        schema.register(map, "minecraft:donkey", (String name) -> V4300.entityWithInventory(schema));
        schema.register(map, "minecraft:mule", (String name) -> V4300.entityWithInventory(schema));
        schema.registerSimple(map, "minecraft:horse");
        schema.registerSimple(map, "minecraft:skeleton_horse");
        schema.registerSimple(map, "minecraft:zombie_horse");
        return map;
    }

    private static TypeTemplate entityWithInventory(Schema schema) {
        return DSL.optionalFields("Items", DSL.list(References.ITEM_STACK.in(schema)));
    }
}

