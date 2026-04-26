/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapLike;
import java.util.function.Function;
import net.minecraft.util.datafix.fixes.References;

public class ChunkRenamesFix
extends DataFix {
    public ChunkRenamesFix(Schema outputSchema) {
        super(outputSchema, true);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> chunkType = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> levelFinder = chunkType.findField("Level");
        OpticFinder<?> structureFinder = levelFinder.type().findField("Structures");
        Type<?> newChunkType = this.getOutputSchema().getType(References.CHUNK);
        Type<?> newStructuresType = newChunkType.findFieldType("structures");
        return this.fixTypeEverywhereTyped("Chunk Renames; purge Level-tag", chunkType, newChunkType, (Typed<?> chunk) -> {
            Typed<Dynamic<?>> level = chunk.getTyped(levelFinder);
            Typed<Pair<String, Object>> chunkTyped = ChunkRenamesFix.appendChunkName(level);
            chunkTyped = chunkTyped.set(DSL.remainderFinder(), ChunkRenamesFix.mergeRemainders(chunk, level.get(DSL.remainderFinder())));
            chunkTyped = ChunkRenamesFix.renameField(chunkTyped, "TileEntities", "block_entities");
            chunkTyped = ChunkRenamesFix.renameField(chunkTyped, "TileTicks", "block_ticks");
            chunkTyped = ChunkRenamesFix.renameField(chunkTyped, "Entities", "entities");
            chunkTyped = ChunkRenamesFix.renameField(chunkTyped, "Sections", "sections");
            chunkTyped = chunkTyped.updateTyped(structureFinder, newStructuresType, structure -> ChunkRenamesFix.renameField(structure, "Starts", "starts"));
            chunkTyped = ChunkRenamesFix.renameField(chunkTyped, "Structures", "structures");
            return chunkTyped.update(DSL.remainderFinder(), remainder -> remainder.remove("Level"));
        });
    }

    private static Typed<?> renameField(Typed<?> input, String oldName, String newName) {
        return ChunkRenamesFix.renameFieldHelper(input, oldName, newName, input.getType().findFieldType(oldName)).update(DSL.remainderFinder(), tag -> tag.remove(oldName));
    }

    private static <A> Typed<?> renameFieldHelper(Typed<?> input, String oldName, String newName, Type<A> fieldType) {
        Type<Either<A, Unit>> oldType = DSL.optional(DSL.field(oldName, fieldType));
        Type<Either<A, Unit>> newType = DSL.optional(DSL.field(newName, fieldType));
        return input.update(oldType.finder(), newType, Function.identity());
    }

    private static <A> Typed<Pair<String, A>> appendChunkName(Typed<A> input) {
        return new Typed<Pair<String, A>>(DSL.named("chunk", input.getType()), input.getOps(), Pair.of("chunk", input.getValue()));
    }

    private static <T> Dynamic<T> mergeRemainders(Typed<?> chunk, Dynamic<T> levelRemainder) {
        DynamicOps ops = levelRemainder.getOps();
        Dynamic chunkRemainder = chunk.get(DSL.remainderFinder()).convert(ops);
        DataResult toMap = ops.getMap(levelRemainder.getValue()).flatMap(map -> ops.mergeToMap(chunkRemainder.getValue(), (MapLike)map));
        return toMap.result().map(v -> new Dynamic<Object>(ops, v)).orElse(levelRemainder);
    }
}

