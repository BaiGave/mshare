/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import com.google.common.collect.Lists;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.MapLike;
import com.mojang.serialization.RecordBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.lang.runtime.SwitchBootstraps;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.SingleVariant;
import net.minecraft.client.renderer.block.dispatch.Variant;
import net.minecraft.client.renderer.block.dispatch.WeightedVariants;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.Weighted;
import net.minecraft.util.random.WeightedList;

public class CustomUnbakedBlockStateModelRegistry {
    private static final String TYPE_KEY = "fabric:type";
    private static final ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends CustomUnbakedBlockStateModel>> ID_MAPPER = new ExtraCodecs.LateBoundIdMapper();
    private static final MapCodec<CustomUnbakedBlockStateModel> CUSTOM_MODEL_MAP_CODEC = ID_MAPPER.codec(Identifier.CODEC).dispatchMap("fabric:type", CustomUnbakedBlockStateModel::codec, codec -> codec);
    private static final MapCodec<SingleVariant.Unbaked> SIMPLE_MODEL_MAP_CODEC = Variant.MAP_CODEC.xmap(SingleVariant.Unbaked::new, SingleVariant.Unbaked::variant);
    private static final MapCodec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> VARIANT_MAP_CODEC = new KeyExistsCodec<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>("fabric:type", CUSTOM_MODEL_MAP_CODEC, SIMPLE_MODEL_MAP_CODEC);
    private static final Codec<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>> VARIANT_CODEC = VARIANT_MAP_CODEC.codec();
    private static final Codec<Weighted<Either<CustomUnbakedBlockStateModel, SingleVariant.Unbaked>>> WEIGHTED_VARIANT_CODEC = RecordCodecBuilder.create(instance -> instance.group(VARIANT_MAP_CODEC.forGetter(Weighted::value), ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(Weighted::weight)).apply((Applicative<Weighted, ?>)instance, Weighted::new));
    public static final Codec<WeightedVariants.Unbaked> WEIGHTED_MODEL_CODEC = ExtraCodecs.nonEmptyList(WEIGHTED_VARIANT_CODEC.listOf()).flatComapMap(weightedVariants -> new WeightedVariants.Unbaked(WeightedList.of(Lists.transform(weightedVariants, weighted -> weighted.map(either -> (BlockStateModel.Unbaked)either.map(Function.identity(), Function.identity()))))), model -> {
        List<Weighted<BlockStateModel.Unbaked>> entries = model.entries().unwrap();
        ArrayList<Weighted<Either<CustomUnbakedBlockStateModel, Object>>> weightedVariants = new ArrayList<Weighted<Either<CustomUnbakedBlockStateModel, Object>>>(entries.size());
        block4: for (Weighted<BlockStateModel.Unbaked> weighted : entries) {
            BlockStateModel.Unbaked selector0$temp;
            Objects.requireNonNull(weighted.value());
            int index$1 = 0;
            switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{CustomUnbakedBlockStateModel.class, SingleVariant.Unbaked.class}, (BlockStateModel.Unbaked)selector0$temp, index$1)) {
                case 0: {
                    CustomUnbakedBlockStateModel custom = (CustomUnbakedBlockStateModel)selector0$temp;
                    weightedVariants.add(new Weighted(Either.left(custom), weighted.weight()));
                    continue block4;
                }
                case 1: {
                    SingleVariant.Unbaked simple = (SingleVariant.Unbaked)selector0$temp;
                    weightedVariants.add(new Weighted(Either.right(simple), weighted.weight()));
                    continue block4;
                }
            }
            return DataResult.error(() -> "Only custom models or single variants are supported");
        }
        return DataResult.success(weightedVariants);
    });
    public static final Codec<BlockStateModel.Unbaked> MODEL_CODEC = Codec.either(WEIGHTED_MODEL_CODEC, VARIANT_CODEC).flatComapMap(either -> either.map(Function.identity(), right -> (BlockStateModel.Unbaked)right.map(Function.identity(), Function.identity())), model -> {
        Objects.requireNonNull(model);
        BlockStateModel.Unbaked unbaked = model;
        Objects.requireNonNull(unbaked);
        BlockStateModel.Unbaked selector0$temp = unbaked;
        int index$1 = 0;
        return switch (SwitchBootstraps.typeSwitch("typeSwitch", new Object[]{CustomUnbakedBlockStateModel.class, SingleVariant.Unbaked.class, WeightedVariants.Unbaked.class}, (BlockStateModel.Unbaked)selector0$temp, index$1)) {
            case 0 -> {
                CustomUnbakedBlockStateModel custom = (CustomUnbakedBlockStateModel)selector0$temp;
                yield DataResult.success(Either.right(Either.left(custom)));
            }
            case 1 -> {
                SingleVariant.Unbaked simple = (SingleVariant.Unbaked)selector0$temp;
                yield DataResult.success(Either.right(Either.right(simple)));
            }
            case 2 -> {
                WeightedVariants.Unbaked weighted = (WeightedVariants.Unbaked)selector0$temp;
                yield DataResult.success(Either.left(weighted));
            }
            default -> DataResult.error(() -> "Only a custom model or a single variant or a list of variants are supported");
        };
    });

    public static void register(Identifier id, MapCodec<? extends CustomUnbakedBlockStateModel> codec) {
        ID_MAPPER.put(id, codec);
    }

    private static class KeyExistsCodec<E, N>
    extends MapCodec<Either<E, N>> {
        private final String key;
        private final MapCodec<E> exists;
        private final MapCodec<N> notExists;

        KeyExistsCodec(String key, MapCodec<E> exists, MapCodec<N> notExists) {
            this.key = key;
            this.exists = exists;
            this.notExists = notExists;
        }

        @Override
        public <T> Stream<T> keys(DynamicOps<T> ops) {
            return Stream.concat(this.exists.keys(ops), this.notExists.keys(ops));
        }

        @Override
        public <T> DataResult<Either<E, N>> decode(DynamicOps<T> ops, MapLike<T> input) {
            if (input.get(this.key) != null) {
                return this.exists.decode(ops, input).map(Either::left);
            }
            return this.notExists.decode(ops, input).map(Either::right);
        }

        @Override
        public <T> RecordBuilder<T> encode(Either<E, N> input, DynamicOps<T> ops, RecordBuilder<T> prefix) {
            return input.map(left -> this.exists.encode(left, ops, prefix), right -> this.notExists.encode(right, ops, prefix));
        }

        public String toString() {
            return "KeyExistsCodec[" + this.key + " " + String.valueOf(this.exists) + " " + String.valueOf(this.notExists) + "]";
        }
    }
}

