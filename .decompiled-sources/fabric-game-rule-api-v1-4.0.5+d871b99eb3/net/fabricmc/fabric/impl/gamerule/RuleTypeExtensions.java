/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.gamerule;

import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import org.jspecify.annotations.Nullable;

public interface RuleTypeExtensions {
    public @Nullable FabricGameRuleType fabric_getType();

    public void fabric_setType(FabricGameRuleType var1);

    default public <E extends Enum<E>> E fabric_enumCycle(E currentValue) {
        throw new UnsupportedOperationException("Non-enum rules cannot be cycled!");
    }

    default public <E extends Enum<E>> Iterable<E> fabric_getSupportedEnumValues() {
        throw new UnsupportedOperationException("Non-enum rules cannot have supported enum values!");
    }

    default public <E extends Enum<E>> void fabric_setSupportedEnumValues(E[] supportedValues) {
        throw new UnsupportedOperationException("Non-enum rules cannot have supported enum values!");
    }
}

