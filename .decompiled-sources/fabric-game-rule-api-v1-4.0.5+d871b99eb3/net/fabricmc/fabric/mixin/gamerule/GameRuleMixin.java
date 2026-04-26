/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gamerule;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.world.level.gamerules.GameRule;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={GameRule.class})
public abstract class GameRuleMixin<T>
implements RuleTypeExtensions {
    @Unique
    private @Nullable FabricGameRuleType fabricGameRuleType;
    @Unique
    private final List<T> enumSupportedValues = new ArrayList<T>();

    @Shadow
    public abstract Class<T> valueClass();

    @Override
    public @Nullable FabricGameRuleType fabric_getType() {
        return this.fabricGameRuleType;
    }

    @Override
    public void fabric_setType(FabricGameRuleType type) {
        this.fabricGameRuleType = type;
    }

    @Override
    public <E extends Enum<E>> E fabric_enumCycle(E currentValue) {
        if (this.fabric_getType() != FabricGameRuleType.ENUM) {
            return RuleTypeExtensions.super.fabric_enumCycle(currentValue);
        }
        int index = this.enumSupportedValues.indexOf(currentValue);
        if (index < 0) {
            throw new IllegalArgumentException(String.format("Invalid value: %s", currentValue));
        }
        return (E)((Enum)this.enumSupportedValues.get((index + 1) % this.enumSupportedValues.size()));
    }

    @Override
    public <E extends Enum<E>> Iterable<E> fabric_getSupportedEnumValues() {
        if (this.fabric_getType() != FabricGameRuleType.ENUM) {
            return RuleTypeExtensions.super.fabric_getSupportedEnumValues();
        }
        return this.enumSupportedValues;
    }

    @Override
    public <E extends Enum<E>> void fabric_setSupportedEnumValues(E[] supportedValues) {
        if (this.fabric_getType() != FabricGameRuleType.ENUM) {
            RuleTypeExtensions.super.fabric_setSupportedEnumValues(supportedValues);
            return;
        }
        this.enumSupportedValues.clear();
        Collections.addAll(this.enumSupportedValues, supportedValues);
    }

    @WrapMethod(method={"deserialize"})
    private <E extends Enum<E>> DataResult<T> deserializeEnum(String value, Operation<DataResult<T>> original) {
        if (this.fabric_getType() != FabricGameRuleType.ENUM) {
            return original.call(value);
        }
        try {
            Class classType = this.valueClass();
            T deserialized = Enum.valueOf(classType, value);
            if (!this.enumSupportedValues.contains(deserialized)) {
                return DataResult.error(() -> "Failed to parse rule of value " + value + " for rule of type " + String.valueOf(classType) + " because the value is unsupported.");
            }
            return DataResult.success(deserialized);
        }
        catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Failed to parse rule of value " + value + " for rule of type " + String.valueOf(this.valueClass()));
        }
    }
}

