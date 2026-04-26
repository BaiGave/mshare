/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.util.datafix.fixes.DataComponentRemainderFix;
import org.jspecify.annotations.Nullable;

public class InvalidLockComponentFix
extends DataComponentRemainderFix {
    private static final Optional<String> INVALID_LOCK_CUSTOM_NAME = Optional.of("\"\"");

    public InvalidLockComponentFix(Schema outputSchema) {
        super(outputSchema, "InvalidLockComponentPredicateFix", "minecraft:lock");
    }

    @Override
    protected <T> @Nullable Dynamic<T> fixComponent(Dynamic<T> input) {
        return InvalidLockComponentFix.fixLock(input);
    }

    public static <T> @Nullable Dynamic<T> fixLock(Dynamic<T> input) {
        return InvalidLockComponentFix.isBrokenLock(input) ? null : input;
    }

    private static <T> boolean isBrokenLock(Dynamic<T> input) {
        return InvalidLockComponentFix.isMapWithOneField(input, "components", components -> InvalidLockComponentFix.isMapWithOneField(components, "minecraft:custom_name", customName -> customName.asString().result().equals(INVALID_LOCK_CUSTOM_NAME)));
    }

    private static <T> boolean isMapWithOneField(Dynamic<T> input, String fieldName, Predicate<Dynamic<T>> predicate) {
        Optional<Map<Dynamic<T>, Dynamic<T>>> map = input.getMapValues().result();
        if (map.isEmpty() || map.get().size() != 1) {
            return false;
        }
        return input.get(fieldName).result().filter(predicate).isPresent();
    }
}

