/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.HashMap;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class DebugProfileLookingAtSplitFix
extends DataFix {
    public DebugProfileLookingAtSplitFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    public TypeRewriteRule makeRule() {
        return this.fixTypeEverywhereTyped("DebugProfileLookingAtSplitFix", this.getInputSchema().getType(References.DEBUG_PROFILE), input -> input.update(DSL.remainderFinder(), tag -> tag.update("custom", DebugProfileLookingAtSplitFix::updateOptions)));
    }

    private static <T> Dynamic<T> updateOptions(Dynamic<T> custom) {
        return DataFixUtils.orElse(custom.getMapValues().map(map -> {
            HashMap newOptions = new HashMap();
            map.forEach((key, value) -> DebugProfileLookingAtSplitFix.renamedKey(key).ifPresentOrElse(newKey -> newOptions.putIfAbsent(newKey, value), () -> newOptions.put(key, value)));
            return custom.createMap(newOptions);
        }).result(), custom);
    }

    private static <T> Optional<Dynamic<T>> renamedKey(Dynamic<T> keyDynamic) {
        return keyDynamic.asString().result().flatMap(key -> switch (key) {
            case "minecraft:looking_at_block" -> Optional.of("minecraft:looking_at_block_state");
            case "minecraft:looking_at_fluid" -> Optional.of("minecraft:looking_at_fluid_state");
            default -> Optional.empty();
        }).map(keyDynamic::createString);
    }
}

