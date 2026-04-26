/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix;

import com.mojang.datafixers.DataFixUtils;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.RewriteResult;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.View;
import com.mojang.datafixers.functions.PointFreeRule;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.BitSet;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Util;

public class ExtraDataFixUtils {
    public static Dynamic<?> fixBlockPos(Dynamic<?> pos) {
        Optional<Number> x = pos.get("X").asNumber().result();
        Optional<Number> y = pos.get("Y").asNumber().result();
        Optional<Number> z = pos.get("Z").asNumber().result();
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return pos;
        }
        return ExtraDataFixUtils.createBlockPos(pos, x.get().intValue(), y.get().intValue(), z.get().intValue());
    }

    public static Dynamic<?> fixInlineBlockPos(Dynamic<?> input, String fieldX, String fieldY, String fieldZ, String newField) {
        Optional<Number> x = input.get(fieldX).asNumber().result();
        Optional<Number> y = input.get(fieldY).asNumber().result();
        Optional<Number> z = input.get(fieldZ).asNumber().result();
        if (x.isEmpty() || y.isEmpty() || z.isEmpty()) {
            return input;
        }
        return input.remove(fieldX).remove(fieldY).remove(fieldZ).set(newField, ExtraDataFixUtils.createBlockPos(input, x.get().intValue(), y.get().intValue(), z.get().intValue()));
    }

    public static Dynamic<?> createBlockPos(Dynamic<?> dynamic, int x, int y, int z) {
        return dynamic.createIntList(IntStream.of(x, y, z));
    }

    public static <T, R> Typed<R> cast(Type<R> type, Typed<T> typed) {
        return new Typed<R>(type, typed.getOps(), typed.getValue());
    }

    public static <T> Typed<T> cast(Type<T> type, Object value, DynamicOps<?> ops) {
        return new Typed<Object>(type, ops, value);
    }

    public static Type<?> patchSubType(Type<?> type, Type<?> find, Type<?> replace) {
        return type.all(ExtraDataFixUtils.typePatcher(find, replace), true, false).view().newType();
    }

    private static <A, B> TypeRewriteRule typePatcher(Type<A> inputEntityType, Type<B> outputEntityType) {
        RewriteResult<A, B> view = RewriteResult.create(View.create("Patcher", inputEntityType, outputEntityType, ops -> a -> {
            throw new UnsupportedOperationException();
        }), new BitSet());
        return TypeRewriteRule.everywhere(TypeRewriteRule.ifSame(inputEntityType, view), PointFreeRule.nop(), true, true);
    }

    @SafeVarargs
    public static <T> Function<Typed<?>, Typed<?>> chainAllFilters(Function<Typed<?>, Typed<?>> ... fixers) {
        return typed -> {
            for (Function fixer : fixers) {
                typed = (Typed)fixer.apply(typed);
            }
            return typed;
        };
    }

    public static Dynamic<?> blockState(String id, Map<String, String> properties) {
        Dynamic<CompoundTag> dynamic = new Dynamic<CompoundTag>(NbtOps.INSTANCE, new CompoundTag());
        Dynamic<CompoundTag> blockState = dynamic.set("Name", dynamic.createString(id));
        if (!properties.isEmpty()) {
            blockState = blockState.set("Properties", dynamic.createMap(properties.entrySet().stream().collect(Collectors.toMap(entry -> dynamic.createString((String)entry.getKey()), entry -> dynamic.createString((String)entry.getValue())))));
        }
        return blockState;
    }

    public static Dynamic<?> blockState(String id) {
        return ExtraDataFixUtils.blockState(id, Map.of());
    }

    public static Dynamic<?> fixStringField(Dynamic<?> dynamic, String fieldName, UnaryOperator<String> fix) {
        return dynamic.update(fieldName, field -> DataFixUtils.orElse(field.asString().map(fix).map(dynamic::createString).result(), field));
    }

    public static String dyeColorIdToName(int id) {
        return switch (id) {
            default -> "white";
            case 1 -> "orange";
            case 2 -> "magenta";
            case 3 -> "light_blue";
            case 4 -> "yellow";
            case 5 -> "lime";
            case 6 -> "pink";
            case 7 -> "gray";
            case 8 -> "light_gray";
            case 9 -> "cyan";
            case 10 -> "purple";
            case 11 -> "blue";
            case 12 -> "brown";
            case 13 -> "green";
            case 14 -> "red";
            case 15 -> "black";
        };
    }

    public static <T> Typed<?> readAndSet(Typed<?> target, OpticFinder<T> optic, Dynamic<?> value) {
        return target.set(optic, Util.readTypedOrThrow(optic.type(), value, true));
    }
}

