/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.primitives.UnsignedBytes;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Decoder;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JavaOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.BaseMapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.objects.Object2BooleanMap;
import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Base64;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.HexFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.ToIntFunction;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Stream;
import net.minecraft.core.HolderSet;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.FileUtil;
import net.minecraft.util.StringUtil;
import net.minecraft.util.Util;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.mutable.MutableObject;
import org.joml.AxisAngle4f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector2f;
import org.joml.Vector2fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector3i;
import org.joml.Vector3ic;
import org.joml.Vector4f;
import org.joml.Vector4fc;
import org.jspecify.annotations.Nullable;

public class ExtraCodecs {
    public static final Codec<JsonElement> JSON = ExtraCodecs.converter(JsonOps.INSTANCE);
    public static final Codec<Object> JAVA = ExtraCodecs.converter(JavaOps.INSTANCE);
    public static final Codec<Tag> NBT = ExtraCodecs.converter(NbtOps.INSTANCE);
    public static final Codec<Vector2fc> VECTOR2F = Codec.FLOAT.listOf().comapFlatMap(input -> Util.fixedSize(input, 2).map(d -> new Vector2f(((Float)d.get(0)).floatValue(), ((Float)d.get(1)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y())));
    public static final Codec<Vector3fc> VECTOR3F = Codec.FLOAT.listOf().comapFlatMap(input -> Util.fixedSize(input, 3).map(d -> new Vector3f(((Float)d.get(0)).floatValue(), ((Float)d.get(1)).floatValue(), ((Float)d.get(2)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y()), Float.valueOf(vec.z())));
    public static final Codec<Vector3ic> VECTOR3I = Codec.INT.listOf().comapFlatMap(input -> Util.fixedSize(input, 3).map(d -> new Vector3i((Integer)d.get(0), (Integer)d.get(1), (int)((Integer)d.get(2)))), vec -> List.of(Integer.valueOf(vec.x()), Integer.valueOf(vec.y()), Integer.valueOf(vec.z())));
    public static final Codec<Vector4fc> VECTOR4F = Codec.FLOAT.listOf().comapFlatMap(input -> Util.fixedSize(input, 4).map(d -> new Vector4f(((Float)d.get(0)).floatValue(), ((Float)d.get(1)).floatValue(), ((Float)d.get(2)).floatValue(), ((Float)d.get(3)).floatValue())), vec -> List.of(Float.valueOf(vec.x()), Float.valueOf(vec.y()), Float.valueOf(vec.z()), Float.valueOf(vec.w())));
    public static final Codec<Quaternionfc> QUATERNIONF_COMPONENTS = Codec.FLOAT.listOf().comapFlatMap(input -> Util.fixedSize(input, 4).map(d -> new Quaternionf(((Float)d.get(0)).floatValue(), ((Float)d.get(1)).floatValue(), ((Float)d.get(2)).floatValue(), ((Float)d.get(3)).floatValue()).normalize()), q -> List.of(Float.valueOf(q.x()), Float.valueOf(q.y()), Float.valueOf(q.z()), Float.valueOf(q.w())));
    public static final Codec<AxisAngle4f> AXISANGLE4F = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.FLOAT.fieldOf("angle")).forGetter(o -> Float.valueOf(o.angle)), ((MapCodec)VECTOR3F.fieldOf("axis")).forGetter(o -> new Vector3f(o.x, o.y, o.z))).apply((Applicative<AxisAngle4f, ?>)i, AxisAngle4f::new));
    public static final Codec<Quaternionfc> QUATERNIONF = Codec.withAlternative(QUATERNIONF_COMPONENTS, AXISANGLE4F.xmap(Quaternionf::new, AxisAngle4f::new));
    public static final Codec<Matrix4fc> MATRIX4F = Codec.FLOAT.listOf().comapFlatMap(input -> Util.fixedSize(input, 16).map(l -> {
        Matrix4f result = new Matrix4f();
        for (int i = 0; i < l.size(); ++i) {
            result.setRowColumn(i >> 2, i & 3, ((Float)l.get(i)).floatValue());
        }
        return result.determineProperties();
    }), m -> {
        FloatArrayList output = new FloatArrayList(16);
        for (int i = 0; i < 16; ++i) {
            output.add(m.getRowColumn(i >> 2, i & 3));
        }
        return output;
    });
    private static final String HEX_COLOR_PREFIX = "#";
    public static final Codec<Integer> RGB_COLOR_CODEC = Codec.withAlternative(Codec.INT, VECTOR3F, v -> ARGB.colorFromFloat(1.0f, v.x(), v.y(), v.z()));
    public static final Codec<Integer> ARGB_COLOR_CODEC = Codec.withAlternative(Codec.INT, VECTOR4F, v -> ARGB.colorFromFloat(v.w(), v.x(), v.y(), v.z()));
    public static final Codec<Integer> STRING_RGB_COLOR = Codec.withAlternative(ExtraCodecs.hexColor(6).xmap(ARGB::opaque, ARGB::transparent), RGB_COLOR_CODEC);
    public static final Codec<Integer> STRING_ARGB_COLOR = Codec.withAlternative(ExtraCodecs.hexColor(8), ARGB_COLOR_CODEC);
    public static final Codec<Integer> UNSIGNED_BYTE = Codec.BYTE.flatComapMap(UnsignedBytes::toInt, integer -> {
        if (integer > 255) {
            return DataResult.error(() -> "Unsigned byte was too large: " + integer + " > 255");
        }
        return DataResult.success(integer.byteValue());
    });
    public static final Codec<Integer> NON_NEGATIVE_INT = ExtraCodecs.intRangeWithMessage(0, Integer.MAX_VALUE, n -> "Value must be non-negative: " + n);
    public static final Codec<Integer> POSITIVE_INT = ExtraCodecs.intRangeWithMessage(1, Integer.MAX_VALUE, n -> "Value must be positive: " + n);
    public static final Codec<Long> NON_NEGATIVE_LONG = ExtraCodecs.longRangeWithMessage(0L, Long.MAX_VALUE, n -> "Value must be non-negative: " + n);
    public static final Codec<Long> POSITIVE_LONG = ExtraCodecs.longRangeWithMessage(1L, Long.MAX_VALUE, n -> "Value must be positive: " + n);
    public static final Codec<Float> NON_NEGATIVE_FLOAT = ExtraCodecs.floatRangeMinInclusiveWithMessage(0.0f, Float.MAX_VALUE, n -> "Value must be non-negative: " + n);
    public static final Codec<Float> POSITIVE_FLOAT = ExtraCodecs.floatRangeMinExclusiveWithMessage(0.0f, Float.MAX_VALUE, n -> "Value must be positive: " + n);
    public static final Codec<Pattern> PATTERN = Codec.STRING.comapFlatMap(pattern -> {
        try {
            return DataResult.success(Pattern.compile(pattern));
        }
        catch (PatternSyntaxException e) {
            return DataResult.error(() -> "Invalid regex pattern '" + pattern + "': " + e.getMessage());
        }
    }, Pattern::pattern);
    public static final Codec<Instant> INSTANT_ISO8601 = ExtraCodecs.temporalCodec(DateTimeFormatter.ISO_INSTANT).xmap(Instant::from, Function.identity());
    public static final Codec<byte[]> BASE64_STRING = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Base64.getDecoder().decode((String)string));
        }
        catch (IllegalArgumentException e) {
            return DataResult.error(() -> "Malformed base64 string");
        }
    }, bytes -> Base64.getEncoder().encodeToString((byte[])bytes));
    public static final Codec<String> ESCAPED_STRING = Codec.STRING.comapFlatMap(str -> DataResult.success(StringEscapeUtils.unescapeJava(str)), StringEscapeUtils::escapeJava);
    public static final Codec<TagOrElementLocation> TAG_OR_ELEMENT_ID = Codec.STRING.comapFlatMap(name -> name.startsWith(HEX_COLOR_PREFIX) ? Identifier.read(name.substring(1)).map(id -> new TagOrElementLocation((Identifier)id, true)) : Identifier.read(name).map(id -> new TagOrElementLocation((Identifier)id, false)), TagOrElementLocation::decoratedId);
    public static final Function<Optional<Long>, OptionalLong> toOptionalLong = o -> o.map(OptionalLong::of).orElseGet(OptionalLong::empty);
    public static final Function<OptionalLong, Optional<Long>> fromOptionalLong = l -> l.isPresent() ? Optional.of(l.getAsLong()) : Optional.empty();
    public static final Codec<BitSet> BIT_SET = Codec.LONG_STREAM.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> Arrays.stream(bitSet.toLongArray()));
    public static final int MAX_PROPERTY_NAME_LENGTH = 64;
    public static final int MAX_PROPERTY_VALUE_LENGTH = Short.MAX_VALUE;
    public static final int MAX_PROPERTY_SIGNATURE_LENGTH = 1024;
    public static final int MAX_PROPERTIES = 16;
    private static final Codec<Property> PROPERTY = RecordCodecBuilder.create(i -> i.group(((MapCodec)Codec.sizeLimitedString(64).fieldOf("name")).forGetter(Property::name), ((MapCodec)Codec.sizeLimitedString(Short.MAX_VALUE).fieldOf("value")).forGetter(Property::value), Codec.sizeLimitedString(1024).optionalFieldOf("signature").forGetter(property -> Optional.ofNullable(property.signature()))).apply((Applicative<Property, ?>)i, (name, value, signature) -> new Property((String)name, (String)value, signature.orElse(null))));
    public static final Codec<PropertyMap> PROPERTY_MAP = Codec.either(Codec.unboundedMap(Codec.STRING, Codec.STRING.listOf()).validate(map -> map.size() > 16 ? DataResult.error(() -> "Cannot have more than 16 properties, but was " + map.size()) : DataResult.success(map)), PROPERTY.sizeLimitedListOf(16)).xmap(mapListEither -> {
        ImmutableMultimap.Builder result = ImmutableMultimap.builder();
        mapListEither.ifLeft(s -> s.forEach((name, properties) -> {
            for (String property : properties) {
                result.put(name, new Property((String)name, property));
            }
        })).ifRight(properties -> {
            for (Property property : properties) {
                result.put(property.name(), property);
            }
        });
        return new PropertyMap(result.build());
    }, propertyMap -> Either.right(propertyMap.values().stream().toList()));
    public static final Codec<String> PLAYER_NAME = Codec.string(0, 16).validate(name -> {
        if (StringUtil.isValidPlayerName(name)) {
            return DataResult.success(name);
        }
        return DataResult.error(() -> "Player name contained disallowed characters: '" + name + "'");
    });
    public static final Codec<GameProfile> AUTHLIB_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.AUTHLIB_CODEC).codec();
    public static final MapCodec<GameProfile> STORED_GAME_PROFILE = ExtraCodecs.gameProfileCodec(UUIDUtil.CODEC);
    public static final Codec<String> NON_EMPTY_STRING = Codec.STRING.validate(value -> value.isEmpty() ? DataResult.error(() -> "Expected non-empty string") : DataResult.success(value));
    public static final Codec<Integer> CODEPOINT = Codec.STRING.comapFlatMap(s -> {
        int[] codepoint = s.codePoints().toArray();
        if (codepoint.length != 1) {
            return DataResult.error(() -> "Expected one codepoint, got: " + s);
        }
        return DataResult.success(codepoint[0]);
    }, Character::toString);
    public static final Codec<String> RESOURCE_PATH_CODEC = Codec.STRING.validate(s -> {
        if (!Identifier.isValidPath(s)) {
            return DataResult.error(() -> "Invalid string to use as a resource path element: " + s);
        }
        return DataResult.success(s);
    });
    public static final Codec<URI> UNTRUSTED_URI = Codec.STRING.comapFlatMap(string -> {
        try {
            return DataResult.success(Util.parseAndValidateUntrustedUri(string));
        }
        catch (URISyntaxException e) {
            return DataResult.error(e::getMessage);
        }
    }, URI::toString);
    public static final Codec<String> CHAT_STRING = Codec.STRING.validate(string -> {
        for (int i = 0; i < string.length(); ++i) {
            char c = string.charAt(i);
            if (StringUtil.isAllowedChatCharacter(c)) continue;
            return DataResult.error(() -> "Disallowed chat character: '" + c + "'");
        }
        return DataResult.success(string);
    });

    public static <T> Codec<T> converter(DynamicOps<T> ops) {
        return Codec.PASSTHROUGH.xmap(t -> t.convert(ops).getValue(), t -> new Dynamic<Object>(ops, t));
    }

    private static Codec<Integer> hexColor(int expectedDigits) {
        long maxValue = (1L << expectedDigits * 4) - 1L;
        return Codec.STRING.comapFlatMap(string -> {
            if (!string.startsWith(HEX_COLOR_PREFIX)) {
                return DataResult.error(() -> "Hex color must begin with #");
            }
            int digits = string.length() - HEX_COLOR_PREFIX.length();
            if (digits != expectedDigits) {
                return DataResult.error(() -> "Hex color is wrong size, expected " + expectedDigits + " digits but got " + digits);
            }
            try {
                long value = HexFormat.fromHexDigitsToLong(string, HEX_COLOR_PREFIX.length(), string.length());
                if (value < 0L || value > maxValue) {
                    return DataResult.error(() -> "Color value out of range: " + string);
                }
                return DataResult.success((int)value);
            }
            catch (NumberFormatException e) {
                return DataResult.error(() -> "Invalid color value: " + string);
            }
        }, value -> HEX_COLOR_PREFIX + HexFormat.of().toHexDigits(value.intValue(), expectedDigits));
    }

    public static <P, I> Codec<I> intervalCodec(Codec<P> pointCodec, String lowerBoundName, String upperBoundName, BiFunction<P, P, DataResult<I>> makeInterval, Function<I, P> getMin, Function<I, P> getMax) {
        Codec<Object> arrayCodec = Codec.list(pointCodec).comapFlatMap(list -> Util.fixedSize(list, 2).flatMap(l -> {
            Object min = l.get(0);
            Object max = l.get(1);
            return (DataResult)makeInterval.apply(min, max);
        }), p -> ImmutableList.of(getMin.apply(p), getMax.apply(p)));
        Codec<Object> objectCodec = RecordCodecBuilder.create(i -> i.group(((MapCodec)pointCodec.fieldOf(lowerBoundName)).forGetter(Pair::getFirst), ((MapCodec)pointCodec.fieldOf(upperBoundName)).forGetter(Pair::getSecond)).apply((Applicative<Pair, ?>)i, Pair::of)).comapFlatMap(p -> (DataResult)makeInterval.apply(p.getFirst(), p.getSecond()), i -> Pair.of(getMin.apply(i), getMax.apply(i)));
        Codec<Object> arrayOrObjectCodec = Codec.withAlternative(arrayCodec, objectCodec);
        return Codec.either(pointCodec, arrayOrObjectCodec).comapFlatMap(either -> either.map(min -> (DataResult)makeInterval.apply(min, min), DataResult::success), p -> {
            Object max;
            Object min = getMin.apply(p);
            if (Objects.equals(min, max = getMax.apply(p))) {
                return Either.left(min);
            }
            return Either.right(p);
        });
    }

    public static <A> Codec.ResultFunction<A> orElsePartial(final A value) {
        return new Codec.ResultFunction<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<A, T>> a) {
                MutableObject message = new MutableObject();
                Optional result = a.resultOrPartial(message::setValue);
                if (result.isPresent()) {
                    return a;
                }
                return DataResult.error(() -> "(" + (String)message.get() + " -> using default)", Pair.of(value, input));
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, A input, DataResult<T> t) {
                return t;
            }

            public String toString() {
                return "OrElsePartial[" + String.valueOf(value) + "]";
            }
        };
    }

    public static <E> Codec<E> idResolverCodec(ToIntFunction<E> toInt, IntFunction<@Nullable E> fromInt, int unknownId) {
        return Codec.INT.flatXmap(id -> Optional.ofNullable(fromInt.apply((int)id)).map(DataResult::success).orElseGet(() -> DataResult.error(() -> "Unknown element id: " + id)), e -> {
            int id = toInt.applyAsInt(e);
            return id == unknownId ? DataResult.error(() -> "Element with unknown id: " + String.valueOf(e)) : DataResult.success(id);
        });
    }

    public static <I, E> Codec<E> idResolverCodec(Codec<I> value, Function<I, @Nullable E> fromId, Function<E, @Nullable I> toId) {
        return value.flatXmap(id -> {
            Object element = fromId.apply(id);
            return element == null ? DataResult.error(() -> "Unknown element id: " + String.valueOf(id)) : DataResult.success(element);
        }, e -> {
            Object id = toId.apply(e);
            if (id == null) {
                return DataResult.error(() -> "Element with unknown id: " + String.valueOf(e));
            }
            return DataResult.success(id);
        });
    }

    public static <E> Codec<E> orCompressed(final Codec<E> normal, final Codec<E> compressed) {
        return new Codec<E>(){

            @Override
            public <T> DataResult<T> encode(E input, DynamicOps<T> ops, T prefix) {
                if (ops.compressMaps()) {
                    return compressed.encode(input, ops, prefix);
                }
                return normal.encode(input, ops, prefix);
            }

            @Override
            public <T> DataResult<Pair<E, T>> decode(DynamicOps<T> ops, T input) {
                if (ops.compressMaps()) {
                    return compressed.decode(ops, input);
                }
                return normal.decode(ops, input);
            }

            public String toString() {
                return String.valueOf(normal) + " orCompressed " + String.valueOf(compressed);
            }
        };
    }

    public static <E> MapCodec<E> orCompressed(final MapCodec<E> normal, final MapCodec<E> compressed) {
        return new MapCodec<E>(){

            @Override
            public <T> RecordBuilder<T> encode(E input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                if (ops.compressMaps()) {
                    return compressed.encode(input, ops, prefix);
                }
                return normal.encode(input, ops, prefix);
            }

            @Override
            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                if (ops.compressMaps()) {
                    return compressed.decode(ops, input);
                }
                return normal.decode(ops, input);
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return compressed.keys(ops);
            }

            public String toString() {
                return String.valueOf(normal) + " orCompressed " + String.valueOf(compressed);
            }
        };
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, final Function<E, Lifecycle> decodeLifecycle, final Function<E, Lifecycle> encodeLifecycle) {
        return codec.mapResult(new Codec.ResultFunction<E>(){

            @Override
            public <T> DataResult<Pair<E, T>> apply(DynamicOps<T> ops, T input, DataResult<Pair<E, T>> a) {
                return a.result().map(r -> a.setLifecycle((Lifecycle)decodeLifecycle.apply(r.getFirst()))).orElse(a);
            }

            @Override
            public <T> DataResult<T> coApply(DynamicOps<T> ops, E input, DataResult<T> t) {
                return t.setLifecycle((Lifecycle)encodeLifecycle.apply(input));
            }

            public String toString() {
                return "WithLifecycle[" + String.valueOf(decodeLifecycle) + " " + String.valueOf(encodeLifecycle) + "]";
            }
        });
    }

    public static <E> Codec<E> overrideLifecycle(Codec<E> codec, Function<E, Lifecycle> lifecycleGetter) {
        return ExtraCodecs.overrideLifecycle(codec, lifecycleGetter, lifecycleGetter);
    }

    public static <K, V> StrictUnboundedMapCodec<K, V> strictUnboundedMap(Codec<K> keyCodec, Codec<V> elementCodec) {
        return new StrictUnboundedMapCodec<K, V>(keyCodec, elementCodec);
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec) {
        return ExtraCodecs.compactListCodec(elementCodec, elementCodec.listOf());
    }

    public static <E> Codec<List<E>> compactListCodec(Codec<E> elementCodec, Codec<List<E>> listCodec) {
        return Codec.either(listCodec, elementCodec).xmap(e -> e.map(l -> l, List::of), v -> v.size() == 1 ? Either.right(v.getFirst()) : Either.left(v));
    }

    private static Codec<Integer> intRangeWithMessage(int minInclusive, int maxInclusive, Function<Integer, String> error) {
        return Codec.INT.validate(value -> {
            if (value.compareTo(minInclusive) >= 0 && value.compareTo(maxInclusive) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> (String)error.apply((Integer)value));
        });
    }

    public static Codec<Integer> intRange(int minInclusive, int maxInclusive) {
        return ExtraCodecs.intRangeWithMessage(minInclusive, maxInclusive, n -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + n);
    }

    private static Codec<Long> longRangeWithMessage(long minInclusive, long maxInclusive, Function<Long, String> error) {
        return Codec.LONG.validate(value -> {
            if ((long)value.compareTo(minInclusive) >= 0L && (long)value.compareTo(maxInclusive) <= 0L) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> (String)error.apply((Long)value));
        });
    }

    public static Codec<Long> longRange(int minInclusive, int maxInclusive) {
        return ExtraCodecs.longRangeWithMessage(minInclusive, maxInclusive, n -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + n);
    }

    private static Codec<Float> floatRangeMinInclusiveWithMessage(float minInclusive, float maxInclusive, Function<Float, String> error) {
        return Codec.FLOAT.validate(value -> {
            if (value.compareTo(Float.valueOf(minInclusive)) >= 0 && value.compareTo(Float.valueOf(maxInclusive)) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> (String)error.apply((Float)value));
        });
    }

    private static Codec<Float> floatRangeMinExclusiveWithMessage(float minExclusive, float maxInclusive, Function<Float, String> error) {
        return Codec.FLOAT.validate(value -> {
            if (value.compareTo(Float.valueOf(minExclusive)) > 0 && value.compareTo(Float.valueOf(maxInclusive)) <= 0) {
                return DataResult.success(value);
            }
            return DataResult.error(() -> (String)error.apply((Float)value));
        });
    }

    public static Codec<Float> floatRange(float minInclusive, float maxInclusive) {
        return ExtraCodecs.floatRangeMinInclusiveWithMessage(minInclusive, maxInclusive, n -> "Value must be within range [" + minInclusive + ";" + maxInclusive + "]: " + n);
    }

    public static <T> Codec<List<T>> nonEmptyList(Codec<List<T>> listCodec) {
        return listCodec.validate(list -> list.isEmpty() ? DataResult.error(() -> "List must have contents") : DataResult.success(list));
    }

    public static <T> Codec<HolderSet<T>> nonEmptyHolderSet(Codec<HolderSet<T>> listCodec) {
        return listCodec.validate(list -> {
            if (list.unwrap().right().filter(List::isEmpty).isPresent()) {
                return DataResult.error(() -> "List must have contents");
            }
            return DataResult.success(list);
        });
    }

    public static <M extends Map<?, ?>> Codec<M> nonEmptyMap(Codec<M> mapCodec) {
        return mapCodec.validate(map -> map.isEmpty() ? DataResult.error(() -> "Map must have contents") : DataResult.success(map));
    }

    public static <E> MapCodec<E> retrieveContext(Function<DynamicOps<?>, DataResult<E>> getter) {
        class ContextRetrievalCodec
        extends MapCodec<E> {
            final /* synthetic */ Function val$getter;

            ContextRetrievalCodec(Function function) {
                this.val$getter = function;
            }

            @Override
            public <T> RecordBuilder<T> encode(E input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
                return prefix;
            }

            @Override
            public <T> DataResult<E> decode(DynamicOps<T> ops, MapLike<T> input) {
                return (DataResult)this.val$getter.apply(ops);
            }

            public String toString() {
                return "ContextRetrievalCodec[" + String.valueOf(this.val$getter) + "]";
            }

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.empty();
            }
        }
        return new ContextRetrievalCodec(getter);
    }

    public static <E, L extends Collection<E>, T> Function<L, DataResult<L>> ensureHomogenous(Function<E, T> typeGetter) {
        return container -> {
            Iterator it = container.iterator();
            if (it.hasNext()) {
                Object firstType = typeGetter.apply(it.next());
                while (it.hasNext()) {
                    Object next = it.next();
                    Object nextType = typeGetter.apply(next);
                    if (nextType == firstType) continue;
                    return DataResult.error(() -> "Mixed type list: element " + String.valueOf(next) + " had type " + String.valueOf(nextType) + ", but list is of type " + String.valueOf(firstType));
                }
            }
            return DataResult.success(container, Lifecycle.stable());
        };
    }

    public static <A> Codec<A> catchDecoderException(final Codec<A> codec) {
        return Codec.of(codec, new Decoder<A>(){

            @Override
            public <T> DataResult<Pair<A, T>> decode(DynamicOps<T> ops, T input) {
                try {
                    return codec.decode(ops, input);
                }
                catch (Exception e) {
                    return DataResult.error(() -> "Caught exception decoding " + String.valueOf(input) + ": " + e.getMessage());
                }
            }
        });
    }

    public static Codec<TemporalAccessor> temporalCodec(DateTimeFormatter formatter) {
        return Codec.STRING.comapFlatMap(s -> {
            try {
                return DataResult.success(formatter.parse((CharSequence)s));
            }
            catch (Exception e) {
                return DataResult.error(e::getMessage);
            }
        }, formatter::format);
    }

    public static MapCodec<OptionalLong> asOptionalLong(MapCodec<Optional<Long>> fieldCodec) {
        return fieldCodec.xmap(toOptionalLong, fromOptionalLong);
    }

    private static MapCodec<GameProfile> gameProfileCodec(Codec<UUID> uuidCodec) {
        return RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)uuidCodec.fieldOf("id")).forGetter(GameProfile::id), ((MapCodec)PLAYER_NAME.fieldOf("name")).forGetter(GameProfile::name), PROPERTY_MAP.optionalFieldOf("properties", PropertyMap.EMPTY).forGetter(GameProfile::properties)).apply((Applicative<GameProfile, ?>)i, GameProfile::new));
    }

    public static <K, V> Codec<Map<K, V>> sizeLimitedMap(Codec<Map<K, V>> codec, int maxSizeInclusive) {
        return codec.validate(map -> {
            if (map.size() > maxSizeInclusive) {
                return DataResult.error(() -> "Map is too long: " + map.size() + ", expected range [0-" + maxSizeInclusive + "]");
            }
            return DataResult.success(map);
        });
    }

    public static <T> Codec<Object2BooleanMap<T>> object2BooleanMap(Codec<T> keyCodec) {
        return Codec.unboundedMap(keyCodec, Codec.BOOL).xmap(Object2BooleanOpenHashMap::new, Object2ObjectOpenHashMap::new);
    }

    @Deprecated
    public static <K, V> MapCodec<V> dispatchOptionalValue(final String typeKey, final String valueKey, final Codec<K> typeCodec, final Function<? super V, ? extends K> typeGetter, final Function<? super K, ? extends Codec<? extends V>> valueCodec) {
        return new MapCodec<V>(){

            @Override
            public <T> Stream<T> keys(DynamicOps<T> ops) {
                return Stream.of(ops.createString(typeKey), ops.createString(valueKey));
            }

            @Override
            public <T> DataResult<V> decode(DynamicOps<T> ops, MapLike<T> input) {
                T typeName = input.get(typeKey);
                if (typeName == null) {
                    return DataResult.error(() -> "Missing \"" + typeKey + "\" in: " + String.valueOf(input));
                }
                return typeCodec.decode(ops, typeName).flatMap((? super R type) -> {
                    Object value = Objects.requireNonNullElseGet(input.get(valueKey), ops::emptyMap);
                    return ((Codec)valueCodec.apply(type.getFirst())).decode(ops, value).map(Pair::getFirst);
                });
            }

            @Override
            public <T> RecordBuilder<T> encode(V input, DynamicOps<T> ops, RecordBuilder<T> builder) {
                Object type = typeGetter.apply(input);
                builder.add(typeKey, typeCodec.encodeStart(ops, type));
                DataResult<T> parameters = this.encode((Codec)valueCodec.apply(type), input, ops);
                if (parameters.result().isEmpty() || !Objects.equals(parameters.result().get(), ops.emptyMap())) {
                    builder.add(valueKey, parameters);
                }
                return builder;
            }

            private <T, V2 extends V> DataResult<T> encode(Codec<V2> codec, V input, DynamicOps<T> ops) {
                return codec.encodeStart(ops, input);
            }
        };
    }

    public static <A> Codec<Optional<A>> optionalEmptyMap(final Codec<A> codec) {
        return new Codec<Optional<A>>(){

            @Override
            public <T> DataResult<Pair<Optional<A>, T>> decode(DynamicOps<T> ops, T input) {
                if (7.isEmptyMap(ops, input)) {
                    return DataResult.success(Pair.of(Optional.empty(), input));
                }
                return codec.decode(ops, input).map((? super R pair) -> pair.mapFirst(Optional::of));
            }

            private static <T> boolean isEmptyMap(DynamicOps<T> ops, T input) {
                Optional<MapLike<T>> map = ops.getMap(input).result();
                return map.isPresent() && map.get().entries().findAny().isEmpty();
            }

            @Override
            public <T> DataResult<T> encode(Optional<A> input, DynamicOps<T> ops, T prefix) {
                if (input.isEmpty()) {
                    return DataResult.success(ops.emptyMap());
                }
                return codec.encode(input.get(), ops, prefix);
            }
        };
    }

    @Deprecated
    public static <E extends Enum<E>> Codec<E> legacyEnum(Function<String, E> valueOf) {
        return Codec.STRING.comapFlatMap(key -> {
            try {
                return DataResult.success((Enum)valueOf.apply((String)key));
            }
            catch (IllegalArgumentException ignored) {
                return DataResult.error(() -> "No value with id: " + key);
            }
        }, Enum::toString);
    }

    public static Codec<Path> pathCodec(Function<String, Path> pathFactory) {
        return Codec.STRING.xmap(pathFactory, path -> FilenameUtils.separatorsToUnix(path.toString()));
    }

    public static Codec<Path> relaiveNormalizedSubPathCodec(Function<String, Path> pathFactory) {
        return ExtraCodecs.pathCodec(pathFactory).xmap(Path::normalize, Path::normalize).validate(path -> {
            if (path.isAbsolute()) {
                return DataResult.error(() -> "Illegal absolute path: " + String.valueOf(path));
            }
            if (path.startsWith("..") || path.startsWith(".") || FileUtil.isEmptyPath(path)) {
                return DataResult.error(() -> "Illegal path traversal: " + String.valueOf(path));
            }
            return DataResult.success(path);
        });
    }

    public static Codec<Path> guardedPathCodec(Path baseFolder) {
        FileSystem fileSystem = baseFolder.getFileSystem();
        Objects.requireNonNull(fileSystem);
        FileSystem fileSystem2 = fileSystem;
        return ExtraCodecs.relaiveNormalizedSubPathCodec(x$0 -> fileSystem2.getPath((String)x$0, new String[0])).xmap(baseFolder::resolve, baseFolder::relativize);
    }

    public record StrictUnboundedMapCodec<K, V>(Codec<K> keyCodec, Codec<V> elementCodec) implements BaseMapCodec<K, V>,
    Codec<Map<K, V>>
    {
        @Override
        public <T> DataResult<Map<K, V>> decode(DynamicOps<T> ops, MapLike<T> input) {
            ImmutableMap.Builder read = ImmutableMap.builder();
            for (Pair<T, T> pair : input.entries().toList()) {
                DataResult v;
                DataResult k = this.keyCodec().parse(ops, pair.getFirst());
                DataResult<Pair> entry = k.apply2stable(Pair::of, v = this.elementCodec().parse(ops, pair.getSecond()));
                Optional<DataResult.Error<Pair>> error = entry.error();
                if (error.isPresent()) {
                    String errorMessage = error.get().message();
                    return DataResult.error(() -> {
                        if (k.result().isPresent()) {
                            return "Map entry '" + String.valueOf(k.result().get()) + "' : " + errorMessage;
                        }
                        return errorMessage;
                    });
                }
                if (entry.result().isPresent()) {
                    Pair kvPair = entry.result().get();
                    read.put(kvPair.getFirst(), kvPair.getSecond());
                    continue;
                }
                return DataResult.error(() -> "Empty or invalid map contents are not allowed");
            }
            ImmutableMap elements = read.build();
            return DataResult.success(elements);
        }

        @Override
        public <T> DataResult<Pair<Map<K, V>, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).setLifecycle(Lifecycle.stable()).flatMap((? super R map) -> this.decode(ops, (Object)map)).map((? super R r) -> Pair.of(r, input));
        }

        @Override
        public <T> DataResult<T> encode(Map<K, V> input, DynamicOps<T> ops, T prefix) {
            return this.encode(input, ops, ops.mapBuilder()).build(prefix);
        }

        @Override
        public String toString() {
            return "StrictUnboundedMapCodec[" + String.valueOf(this.keyCodec) + " -> " + String.valueOf(this.elementCodec) + "]";
        }
    }

    public record TagOrElementLocation(Identifier id, boolean tag) {
        @Override
        public String toString() {
            return this.decoratedId();
        }

        private String decoratedId() {
            return this.tag ? ExtraCodecs.HEX_COLOR_PREFIX + String.valueOf(this.id) : this.id.toString();
        }
    }

    public static class LateBoundIdMapper<I, V> {
        private final BiMap<I, V> idToValue = HashBiMap.create();

        public Codec<V> codec(Codec<I> idCodec) {
            BiMap<V, I> valueToId = this.idToValue.inverse();
            return ExtraCodecs.idResolverCodec(idCodec, this.idToValue::get, valueToId::get);
        }

        public LateBoundIdMapper<I, V> put(I id, V value) {
            Objects.requireNonNull(value, () -> "Value for " + String.valueOf(id) + " is null");
            this.idToValue.put(id, value);
            return this;
        }

        public Set<V> values() {
            return Collections.unmodifiableSet(this.idToValue.values());
        }
    }
}

