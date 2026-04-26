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

public class V1470
extends NamespacedSchema {
    public V1470(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    protected static void registerMob(Schema schema, Map<String, Supplier<TypeTemplate>> map, String name) {
        schema.registerSimple(map, name);
    }

    @Override
    public Map<String, Supplier<TypeTemplate>> registerEntities(Schema schema) {
        Map<String, Supplier<TypeTemplate>> map = super.registerEntities(schema);
        V1470.registerMob(schema, map, "minecraft:turtle");
        V1470.registerMob(schema, map, "minecraft:cod_mob");
        V1470.registerMob(schema, map, "minecraft:tropical_fish");
        V1470.registerMob(schema, map, "minecraft:salmon_mob");
        V1470.registerMob(schema, map, "minecraft:puffer_fish");
        V1470.registerMob(schema, map, "minecraft:phantom");
        V1470.registerMob(schema, map, "minecraft:dolphin");
        V1470.registerMob(schema, map, "minecraft:drowned");
        schema.register(map, "minecraft:trident", (String name) -> DSL.optionalFields("inBlockState", References.BLOCK_STATE.in(schema), "Trident", References.ITEM_STACK.in(schema)));
        return map;
    }
}

