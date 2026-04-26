/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;

public class MobSpawnerEntityIdentifiersFix
extends DataFix {
    public MobSpawnerEntityIdentifiersFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    private Dynamic<?> fix(Dynamic<?> input) {
        Optional<Stream<Dynamic<?>>> spawnPotentials;
        if (!"MobSpawner".equals(input.get("id").asString(""))) {
            return input;
        }
        Optional<String> entityId = input.get("EntityId").asString().result();
        if (entityId.isPresent()) {
            Dynamic spawnData = DataFixUtils.orElse(input.get("SpawnData").result(), input.emptyMap());
            spawnData = spawnData.set("id", spawnData.createString(entityId.get().isEmpty() ? "Pig" : entityId.get()));
            input = input.set("SpawnData", spawnData);
            input = input.remove("EntityId");
        }
        if ((spawnPotentials = input.get("SpawnPotentials").asStreamOpt().result()).isPresent()) {
            input = input.set("SpawnPotentials", input.createList(spawnPotentials.get().map(spawnPotential -> {
                Optional<String> type = spawnPotential.get("Type").asString().result();
                if (type.isPresent()) {
                    Dynamic spawnData = DataFixUtils.orElse(spawnPotential.get("Properties").result(), spawnPotential.emptyMap()).set("id", spawnPotential.createString(type.get()));
                    return spawnPotential.set("Entity", spawnData).remove("Type").remove("Properties");
                }
                return spawnPotential;
            })));
        }
        return input;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> newType = this.getOutputSchema().getType(References.UNTAGGED_SPAWNER);
        return this.fixTypeEverywhereTyped("MobSpawnerEntityIdentifiersFix", this.getInputSchema().getType(References.UNTAGGED_SPAWNER), newType, (Typed<?> input) -> {
            Dynamic tag = input.get(DSL.remainderFinder());
            DataResult fixed = newType.readTyped(this.fix(tag = tag.set("id", tag.createString("MobSpawner"))));
            if (fixed.result().isEmpty()) {
                return input;
            }
            return fixed.result().get().getFirst();
        });
    }
}

