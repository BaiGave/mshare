/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.schemas;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.templates.Hook;
import com.mojang.datafixers.types.templates.TypeTemplate;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class V1451_6
extends NamespacedSchema {
    public static final String SPECIAL_OBJECTIVE_MARKER = "_special";
    protected static final Hook.HookFunction UNPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> ops, T value) {
            Dynamic input = new Dynamic(ops, value);
            return DataFixUtils.orElse(input.get("CriteriaName").asString().result().map(name -> {
                int colonPos = name.indexOf(58);
                if (colonPos < 0) {
                    return Pair.of(V1451_6.SPECIAL_OBJECTIVE_MARKER, name);
                }
                try {
                    Identifier statType = Identifier.bySeparator(name.substring(0, colonPos), '.');
                    Identifier statId = Identifier.bySeparator(name.substring(colonPos + 1), '.');
                    return Pair.of(statType.toString(), statId.toString());
                }
                catch (Exception e) {
                    return Pair.of(V1451_6.SPECIAL_OBJECTIVE_MARKER, name);
                }
            }).map(explodedId -> input.set("CriteriaType", input.createMap(ImmutableMap.of(input.createString("type"), input.createString((String)explodedId.getFirst()), input.createString("id"), input.createString((String)explodedId.getSecond()))))), input).getValue();
        }
    };
    protected static final Hook.HookFunction REPACK_OBJECTIVE_ID = new Hook.HookFunction(){

        @Override
        public <T> T apply(DynamicOps<T> ops, T value) {
            Dynamic input = new Dynamic(ops, value);
            Optional<Dynamic> repackedId = input.get("CriteriaType").get().result().flatMap(type -> {
                Optional<String> statType = type.get("type").asString().result();
                Optional<String> statId = type.get("id").asString().result();
                if (statType.isPresent() && statId.isPresent()) {
                    String unpackedType = statType.get();
                    if (unpackedType.equals(V1451_6.SPECIAL_OBJECTIVE_MARKER)) {
                        return Optional.of(input.createString(statId.get()));
                    }
                    return Optional.of(type.createString(V1451_6.packNamespacedWithDot(unpackedType) + ":" + V1451_6.packNamespacedWithDot(statId.get())));
                }
                return Optional.empty();
            });
            return DataFixUtils.orElse(repackedId.map(id -> input.set("CriteriaName", (Dynamic<?>)id).remove("CriteriaType")), input).getValue();
        }
    };

    public V1451_6(int versionKey, Schema parent) {
        super(versionKey, parent);
    }

    @Override
    public void registerTypes(Schema schema, Map<String, Supplier<TypeTemplate>> entityTypes, Map<String, Supplier<TypeTemplate>> blockEntityTypes) {
        super.registerTypes(schema, entityTypes, blockEntityTypes);
        Supplier<TypeTemplate> ITEM_STATS = () -> DSL.compoundList(References.ITEM_NAME.in(schema), DSL.constType(DSL.intType()));
        schema.registerType(false, References.STATS, () -> DSL.optionalFields("stats", DSL.optionalFields(Pair.of("minecraft:mined", DSL.compoundList(References.BLOCK_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:crafted", (TypeTemplate)ITEM_STATS.get()), Pair.of("minecraft:used", (TypeTemplate)ITEM_STATS.get()), Pair.of("minecraft:broken", (TypeTemplate)ITEM_STATS.get()), Pair.of("minecraft:picked_up", (TypeTemplate)ITEM_STATS.get()), Pair.of("minecraft:dropped", (TypeTemplate)ITEM_STATS.get()), Pair.of("minecraft:killed", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:killed_by", DSL.compoundList(References.ENTITY_NAME.in(schema), DSL.constType(DSL.intType()))), Pair.of("minecraft:custom", DSL.compoundList(DSL.constType(V1451_6.namespacedString()), DSL.constType(DSL.intType()))))));
        Map<String, Supplier<TypeTemplate>> criterionTypes = V1451_6.createCriterionTypes(schema);
        schema.registerType(false, References.OBJECTIVE, () -> DSL.hook(DSL.optionalFields("CriteriaType", DSL.taggedChoiceLazy("type", DSL.string(), criterionTypes), "DisplayName", References.TEXT_COMPONENT.in(schema)), UNPACK_OBJECTIVE_ID, REPACK_OBJECTIVE_ID));
    }

    protected static Map<String, Supplier<TypeTemplate>> createCriterionTypes(Schema schema) {
        Supplier<TypeTemplate> itemCriterion = () -> DSL.optionalFields("id", References.ITEM_NAME.in(schema));
        Supplier<TypeTemplate> blockCriterion = () -> DSL.optionalFields("id", References.BLOCK_NAME.in(schema));
        Supplier<TypeTemplate> entityCriterion = () -> DSL.optionalFields("id", References.ENTITY_NAME.in(schema));
        HashMap<String, Supplier<TypeTemplate>> criterionTypes = Maps.newHashMap();
        criterionTypes.put("minecraft:mined", blockCriterion);
        criterionTypes.put("minecraft:crafted", itemCriterion);
        criterionTypes.put("minecraft:used", itemCriterion);
        criterionTypes.put("minecraft:broken", itemCriterion);
        criterionTypes.put("minecraft:picked_up", itemCriterion);
        criterionTypes.put("minecraft:dropped", itemCriterion);
        criterionTypes.put("minecraft:killed", entityCriterion);
        criterionTypes.put("minecraft:killed_by", entityCriterion);
        criterionTypes.put("minecraft:custom", () -> DSL.optionalFields("id", DSL.constType(V1451_6.namespacedString())));
        criterionTypes.put(SPECIAL_OBJECTIVE_MARKER, () -> DSL.optionalFields("id", DSL.constType(DSL.string())));
        return criterionTypes;
    }

    public static String packNamespacedWithDot(String location) {
        Identifier parsedLoc = Identifier.tryParse(location);
        return parsedLoc != null ? parsedLoc.getNamespace() + "." + parsedLoc.getPath() : location;
    }
}

