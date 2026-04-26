/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.gamerule.v1;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JavaOps;
import java.util.Objects;
import java.util.function.ToIntFunction;
import net.fabricmc.fabric.api.gamerule.v1.FabricGameRuleTypeVisitor;
import net.fabricmc.fabric.impl.gamerule.RuleTypeExtensions;
import net.fabricmc.fabric.impl.gamerule.rpc.FabricGameRuleType;
import net.minecraft.Optionull;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public class GameRuleBuilder<T> {
    protected final T defaultValue;
    protected GameRuleCategory category = GameRuleCategory.MISC;
    protected GameRuleType type = GameRuleType.INT;
    protected @Nullable FabricGameRuleType fabricType;
    protected @Nullable ArgumentType<T> argumentType;
    protected GameRules.VisitorCaller<T> acceptor;
    protected Codec<T> codec;
    protected ToIntFunction<T> commandResultSupplier;
    protected FeatureFlagSet requiredFeatures = FeatureFlagSet.of();

    protected GameRuleBuilder(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public static BooleanRuleBuilder forBoolean(boolean defaultValue) {
        return new BooleanRuleBuilder(defaultValue);
    }

    public static IntegerRuleBuilder forInteger(int defaultValue) {
        return new IntegerRuleBuilder(defaultValue);
    }

    public static DoubleRuleBuilder forDouble(double defaultValue) {
        return new DoubleRuleBuilder(defaultValue);
    }

    public static <E extends Enum<E>> EnumRuleBuilder<E> forEnum(E defaultValue) {
        return new EnumRuleBuilder<E>(defaultValue);
    }

    public GameRuleBuilder<T> category(GameRuleCategory category) {
        this.category = category;
        return this;
    }

    public GameRuleBuilder<T> codec(Codec<T> codec) {
        this.codec = codec;
        return this;
    }

    public GameRuleBuilder<T> argumentType(ArgumentType<T> argumentType) {
        this.argumentType = argumentType;
        return this;
    }

    public GameRuleBuilder<T> commandResultSupplier(ToIntFunction<T> commandResultSupplier) {
        this.commandResultSupplier = commandResultSupplier;
        return this;
    }

    public GameRuleBuilder<T> requiredFeatures(FeatureFlagSet requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
        return this;
    }

    public GameRule<T> build() {
        Objects.requireNonNull(this.category, "GameRule category cannot be null! Consider using GameRuleCategory.MISC instead.");
        Objects.requireNonNull(this.type, "GameRule type cannot be null! Consider using GameRuleType.INT instead.");
        if (this.fabricType != FabricGameRuleType.ENUM) {
            Objects.requireNonNull(this.argumentType, "GameRule argumentType cannot be null for non-enum rules!");
        }
        Objects.requireNonNull(this.acceptor, "GameRule acceptor cannot be null!");
        Objects.requireNonNull(this.codec, "GameRule codec cannot be null!");
        Objects.requireNonNull(this.commandResultSupplier, "GameRule commandResultSupplier cannot be null!");
        Objects.requireNonNull(this.defaultValue, "GameRule defaultValue cannot be null!");
        Objects.requireNonNull(this.requiredFeatures, "GameRule requiredFeatures cannot be null! Consider using FeatureSet.empty() instead.");
        this.codec.encodeStart(JavaOps.INSTANCE, this.defaultValue).getOrThrow(error -> new IllegalStateException("Failed to serialize default value: " + error));
        GameRule<T> rule = new GameRule<T>(this.category, this.type, this.argumentType, this.acceptor, this.codec, this.commandResultSupplier, this.defaultValue, this.requiredFeatures);
        if (this.fabricType != null) {
            ((RuleTypeExtensions)((Object)rule)).fabric_setType(this.fabricType);
        }
        return rule;
    }

    public GameRule<T> buildAndRegister(Identifier id) {
        GameRule<T> rule = this.build();
        return Registry.register(BuiltInRegistries.GAME_RULE, id, rule);
    }

    private static void visitDouble(GameRuleTypeVisitor visitor, GameRule<Double> rule) {
        if (visitor instanceof FabricGameRuleTypeVisitor) {
            ((FabricGameRuleTypeVisitor)visitor).visitDouble(rule);
        }
    }

    private static <E extends Enum<E>> void visitEnum(GameRuleTypeVisitor visitor, GameRule<E> rule) {
        if (visitor instanceof FabricGameRuleTypeVisitor) {
            ((FabricGameRuleTypeVisitor)visitor).visitEnum(rule);
        }
    }

    public static final class BooleanRuleBuilder
    extends GameRuleBuilder<Boolean> {
        BooleanRuleBuilder(boolean defaultValue) {
            super(defaultValue);
            this.type = GameRuleType.BOOL;
            this.acceptor = GameRuleTypeVisitor::visitBoolean;
            this.argumentType = BoolArgumentType.bool();
            this.codec = Codec.BOOL;
            this.commandResultSupplier = bool -> bool != false ? 1 : 0;
        }

        public BooleanRuleBuilder category(GameRuleCategory category) {
            super.category(category);
            return this;
        }

        public BooleanRuleBuilder codec(Codec<Boolean> codec) {
            super.codec(codec);
            return this;
        }

        public BooleanRuleBuilder argumentType(ArgumentType<Boolean> argumentType) {
            super.argumentType(argumentType);
            return this;
        }

        public BooleanRuleBuilder commandResultSupplier(ToIntFunction<Boolean> commandResultSupplier) {
            super.commandResultSupplier(commandResultSupplier);
            return this;
        }

        public BooleanRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
            super.requiredFeatures(requiredFeatures);
            return this;
        }
    }

    public static final class IntegerRuleBuilder
    extends NumberRuleBuilder<Integer> {
        IntegerRuleBuilder(int defaultValue) {
            super(defaultValue);
            this.type = GameRuleType.INT;
            this.acceptor = GameRuleTypeVisitor::visitInteger;
            this.argumentType = IntegerArgumentType.integer();
            this.codec = Codec.INT;
            this.commandResultSupplier = integer -> integer;
        }

        public IntegerRuleBuilder category(GameRuleCategory category) {
            super.category(category);
            return this;
        }

        public IntegerRuleBuilder codec(Codec<Integer> codec) {
            super.codec(codec);
            return this;
        }

        public IntegerRuleBuilder argumentType(ArgumentType<Integer> argumentType) {
            super.argumentType(argumentType);
            return this;
        }

        public IntegerRuleBuilder commandResultSupplier(ToIntFunction<Integer> commandResultSupplier) {
            super.commandResultSupplier(commandResultSupplier);
            return this;
        }

        public IntegerRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
            super.requiredFeatures(requiredFeatures);
            return this;
        }

        public IntegerRuleBuilder minValue(Integer minValue) {
            return this.range(minValue, Integer.MAX_VALUE);
        }

        public IntegerRuleBuilder range(Integer minValue, Integer maxValue) {
            if ((Integer)this.defaultValue < minValue || (Integer)this.defaultValue > maxValue) {
                throw new IllegalArgumentException("Default value is out-of-bounds: " + String.valueOf(this.defaultValue));
            }
            return ((IntegerRuleBuilder)this.argumentType((ArgumentType)IntegerArgumentType.integer(minValue, maxValue))).codec((Codec)Codec.intRange(minValue, maxValue));
        }
    }

    public static final class DoubleRuleBuilder
    extends NumberRuleBuilder<Double> {
        DoubleRuleBuilder(double defaultValue) {
            super(defaultValue);
            this.fabricType = FabricGameRuleType.DOUBLE;
            this.acceptor = GameRuleBuilder::visitDouble;
            this.argumentType = DoubleArgumentType.doubleArg();
            this.codec = Codec.DOUBLE;
            this.commandResultSupplier = value -> Double.compare(value, 0.0);
        }

        public DoubleRuleBuilder category(GameRuleCategory category) {
            super.category(category);
            return this;
        }

        public DoubleRuleBuilder codec(Codec<Double> codec) {
            super.codec(codec);
            return this;
        }

        public DoubleRuleBuilder argumentType(ArgumentType<Double> argumentType) {
            super.argumentType(argumentType);
            return this;
        }

        public DoubleRuleBuilder commandResultSupplier(ToIntFunction<Double> commandResultSupplier) {
            super.commandResultSupplier(commandResultSupplier);
            return this;
        }

        public DoubleRuleBuilder requiredFeatures(FeatureFlagSet requiredFeatures) {
            super.requiredFeatures(requiredFeatures);
            return this;
        }

        public DoubleRuleBuilder minValue(Double minValue) {
            return this.range(minValue, (Double)Double.MAX_VALUE);
        }

        public DoubleRuleBuilder range(Double minValue, Double maxValue) {
            if ((Double)this.defaultValue < minValue || (Double)this.defaultValue > maxValue) {
                throw new IllegalArgumentException("Default value is out-of-bounds: " + String.valueOf(this.defaultValue));
            }
            return ((DoubleRuleBuilder)this.argumentType((ArgumentType)DoubleArgumentType.doubleArg(minValue, maxValue))).codec((Codec)Codec.doubleRange(minValue, maxValue));
        }
    }

    public static final class EnumRuleBuilder<E extends Enum<E>>
    extends GameRuleBuilder<E> {
        private E[] supportedValues;

        EnumRuleBuilder(E defaultValue) {
            super(defaultValue);
            this.fabricType = FabricGameRuleType.ENUM;
            this.acceptor = GameRuleBuilder::visitEnum;
            this.argumentType = null;
            this.codec = EnumRuleBuilder.createEnumCodec(((Enum)defaultValue).getDeclaringClass());
            this.commandResultSupplier = value -> value.ordinal();
            this.supportedValues = (Enum[])((Enum)defaultValue).getDeclaringClass().getEnumConstants();
        }

        @Override
        public EnumRuleBuilder<E> category(GameRuleCategory category) {
            super.category(category);
            return this;
        }

        @Override
        public EnumRuleBuilder<E> codec(Codec<E> codec) {
            super.codec(codec);
            return this;
        }

        @Override
        public EnumRuleBuilder<E> argumentType(ArgumentType<E> argumentType) {
            super.argumentType(argumentType);
            return this;
        }

        @Override
        public EnumRuleBuilder<E> commandResultSupplier(ToIntFunction<E> commandResultSupplier) {
            super.commandResultSupplier(commandResultSupplier);
            return this;
        }

        @Override
        public EnumRuleBuilder<E> requiredFeatures(FeatureFlagSet requiredFeatures) {
            super.requiredFeatures(requiredFeatures);
            return this;
        }

        @SafeVarargs
        public final EnumRuleBuilder<E> supportedValues(E ... supportedValues) {
            if (Optionull.isNullOrEmpty(supportedValues)) {
                throw new IllegalArgumentException("No values are supported!");
            }
            if (!ArrayUtils.contains(supportedValues, this.defaultValue)) {
                throw new IllegalArgumentException("Supported enum value must include the default " + String.valueOf(this.defaultValue));
            }
            this.supportedValues = supportedValues;
            return this;
        }

        @Override
        public GameRule<E> build() {
            GameRule rule = super.build();
            ((RuleTypeExtensions)((Object)rule)).fabric_setSupportedEnumValues((Enum[])this.supportedValues);
            return rule;
        }

        private static <E extends Enum<E>> Codec<E> createEnumCodec(Class<E> clazz) {
            return Codec.STRING.comapFlatMap(string -> {
                try {
                    return DataResult.success(Enum.valueOf(clazz, string));
                }
                catch (IllegalArgumentException exception) {
                    return DataResult.error(() -> string + " is not a valid value for enum + " + String.valueOf(clazz));
                }
            }, Enum::name);
        }
    }

    public static abstract class NumberRuleBuilder<T extends Number>
    extends GameRuleBuilder<T> {
        NumberRuleBuilder(T defaultValue) {
            super(defaultValue);
        }

        public abstract NumberRuleBuilder<T> minValue(T var1);

        public abstract NumberRuleBuilder<T> range(T var1, T var2);
    }
}

