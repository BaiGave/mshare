/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item.component;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.List;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import org.jspecify.annotations.Nullable;

public record CustomModelData(List<Float> floats, List<Boolean> flags, List<String> strings, List<Integer> colors) {
    public static final CustomModelData EMPTY = new CustomModelData(List.of(), List.of(), List.of(), List.of());
    public static final Codec<CustomModelData> CODEC = RecordCodecBuilder.create(i -> i.group(Codec.FLOAT.listOf().optionalFieldOf("floats", List.of()).forGetter(CustomModelData::floats), Codec.BOOL.listOf().optionalFieldOf("flags", List.of()).forGetter(CustomModelData::flags), Codec.STRING.listOf().optionalFieldOf("strings", List.of()).forGetter(CustomModelData::strings), ExtraCodecs.RGB_COLOR_CODEC.listOf().optionalFieldOf("colors", List.of()).forGetter(CustomModelData::colors)).apply((Applicative<CustomModelData, ?>)i, CustomModelData::new));
    public static final StreamCodec<ByteBuf, CustomModelData> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()), CustomModelData::floats, ByteBufCodecs.BOOL.apply(ByteBufCodecs.list()), CustomModelData::flags, ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), CustomModelData::strings, ByteBufCodecs.INT.apply(ByteBufCodecs.list()), CustomModelData::colors, CustomModelData::new);

    private static <T> @Nullable T getSafe(List<T> values, int index) {
        if (index < 0 || index >= values.size()) {
            return null;
        }
        return values.get(index);
    }

    public @Nullable Float getFloat(int index) {
        return CustomModelData.getSafe(this.floats, index);
    }

    public @Nullable Boolean getBoolean(int index) {
        return CustomModelData.getSafe(this.flags, index);
    }

    public @Nullable String getString(int index) {
        return CustomModelData.getSafe(this.strings, index);
    }

    public @Nullable Integer getColor(int index) {
        return CustomModelData.getSafe(this.colors, index);
    }
}

