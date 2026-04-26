/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.NamedEntityWriteReadFix;
import net.minecraft.util.datafix.fixes.References;

public class PrimedTntBlockStateFixer
extends NamedEntityWriteReadFix {
    public PrimedTntBlockStateFixer(Schema outputSchema) {
        super(outputSchema, true, "PrimedTnt BlockState fixer", References.ENTITY, "minecraft:tnt");
    }

    private static <T> Dynamic<T> renameFuse(Dynamic<T> input) {
        Optional<Dynamic<T>> fuseValue = input.get("Fuse").get().result();
        if (fuseValue.isPresent()) {
            return input.set("fuse", fuseValue.get());
        }
        return input;
    }

    private static <T> Dynamic<T> insertBlockState(Dynamic<T> input) {
        return input.set("block_state", input.createMap(Map.of(input.createString("Name"), input.createString("minecraft:tnt"))));
    }

    @Override
    protected <T> Dynamic<T> fix(Dynamic<T> input) {
        return PrimedTntBlockStateFixer.renameFuse(PrimedTntBlockStateFixer.insertBlockState(input));
    }
}

