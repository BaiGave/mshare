/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.FieldFinder;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.CompoundList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Dynamic;
import java.util.List;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.fixes.WorldGenSettingsFix;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class MissingDimensionFix
extends DataFix {
    public MissingDimensionFix(Schema schema, boolean changesType) {
        super(schema, changesType);
    }

    protected static <A> Type<Pair<A, Dynamic<?>>> fields(String name, Type<A> type) {
        return DSL.and(DSL.field(name, type), DSL.remainderType());
    }

    protected static <A> Type<Pair<Either<A, Unit>, Dynamic<?>>> optionalFields(String name, Type<A> type) {
        return DSL.and(DSL.optional(DSL.field(name, type)), DSL.remainderType());
    }

    protected static <A1, A2> Type<Pair<Either<A1, Unit>, Pair<Either<A2, Unit>, Dynamic<?>>>> optionalFields(String name1, Type<A1> type1, String name2, Type<A2> type2) {
        return DSL.and(DSL.optional(DSL.field(name1, type1)), DSL.optional(DSL.field(name2, type2)), DSL.remainderType());
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Schema schema = this.getInputSchema();
        Type<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>> generatorType = DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:debug", DSL.remainderType(), "minecraft:flat", MissingDimensionFix.flatType(schema), "minecraft:noise", MissingDimensionFix.optionalFields("biome_source", DSL.taggedChoiceType("type", DSL.string(), ImmutableMap.of("minecraft:fixed", MissingDimensionFix.fields("biome", schema.getType(References.BIOME)), "minecraft:multi_noise", DSL.list(MissingDimensionFix.fields("biome", schema.getType(References.BIOME))), "minecraft:checkerboard", MissingDimensionFix.fields("biomes", DSL.list(schema.getType(References.BIOME))), "minecraft:vanilla_layered", DSL.remainderType(), "minecraft:the_end", DSL.remainderType())), "settings", DSL.or(DSL.string(), MissingDimensionFix.optionalFields("default_block", schema.getType(References.BLOCK_NAME), "default_fluid", schema.getType(References.BLOCK_NAME))))));
        CompoundList.CompoundListType<String, Pair<Pair<String, Pair<Either<Pair<String, Dynamic<?>>, Unit>, Pair<Either<Either<String, Pair<Either<?, Unit>, Pair<Either<?, Unit>, Dynamic<?>>>>, Unit>, Dynamic<?>>>>, Dynamic<?>>> dimensionsType = DSL.compoundList(NamespacedSchema.namespacedString(), MissingDimensionFix.fields("generator", generatorType));
        Type expectedDimensionsType = DSL.and(dimensionsType, DSL.remainderType());
        Type<?> settings = schema.getType(References.WORLD_GEN_SETTINGS);
        FieldFinder dimensionsFinder = new FieldFinder("dimensions", expectedDimensionsType);
        if (!settings.findFieldType("dimensions").equals(expectedDimensionsType)) {
            throw new IllegalStateException();
        }
        OpticFinder dimensionListFinder = dimensionsType.finder();
        return this.fixTypeEverywhereTyped("MissingDimensionFix", settings, input -> input.updateTyped(dimensionsFinder, dimensions -> dimensions.updateTyped(dimensionListFinder, generators -> {
            if (!(generators.getValue() instanceof List)) {
                throw new IllegalStateException("List exptected");
            }
            if (((List)generators.getValue()).isEmpty()) {
                Dynamic<?> tag = input.get(DSL.remainderFinder());
                Dynamic<?> newDimensions = this.recreateSettings(tag);
                return DataFixUtils.orElse(dimensionsType.readTyped(newDimensions).result().map(Pair::getFirst), generators);
            }
            return generators;
        })));
    }

    protected static Type<? extends Pair<? extends Either<? extends Pair<? extends Either<?, Unit>, ? extends Pair<? extends Either<? extends List<? extends Pair<? extends Either<?, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>>, Unit>, Dynamic<?>>> flatType(Schema schema) {
        return MissingDimensionFix.optionalFields("settings", MissingDimensionFix.optionalFields("biome", schema.getType(References.BIOME), "layers", DSL.list(MissingDimensionFix.optionalFields("block", schema.getType(References.BLOCK_NAME)))));
    }

    private <T> Dynamic<T> recreateSettings(Dynamic<T> tag) {
        long seed = tag.get("seed").asLong(0L);
        return new Dynamic(tag.getOps(), WorldGenSettingsFix.vanillaLevels(tag, seed, WorldGenSettingsFix.defaultOverworld(tag, seed), false));
    }
}

