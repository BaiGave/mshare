/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.ChatFormatting;
import org.jspecify.annotations.Nullable;

public final class TextColor {
    private static final String CUSTOM_COLOR_PREFIX = "#";
    public static final Codec<TextColor> CODEC = Codec.STRING.comapFlatMap(TextColor::parseColor, TextColor::serialize);
    private static final Map<ChatFormatting, TextColor> LEGACY_FORMAT_TO_COLOR = Stream.of(ChatFormatting.values()).filter(ChatFormatting::isColor).collect(ImmutableMap.toImmutableMap(Function.identity(), f -> new TextColor(f.getColor(), f.getName())));
    private static final Map<String, TextColor> NAMED_COLORS = LEGACY_FORMAT_TO_COLOR.values().stream().collect(ImmutableMap.toImmutableMap(e -> e.name, Function.identity()));
    private final int value;
    private final @Nullable String name;

    private TextColor(int value, String name) {
        this.value = value & 0xFFFFFF;
        this.name = name;
    }

    private TextColor(int value) {
        this.value = value & 0xFFFFFF;
        this.name = null;
    }

    public int getValue() {
        return this.value;
    }

    public String serialize() {
        return this.name != null ? this.name : this.formatValue();
    }

    public final String formatValue() {
        return String.format(Locale.ROOT, "#%06X", this.value);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        TextColor other = (TextColor)o;
        return this.value == other.value;
    }

    public int hashCode() {
        return Objects.hash(this.value, this.name);
    }

    public String toString() {
        return this.serialize();
    }

    public static @Nullable TextColor fromLegacyFormat(ChatFormatting format) {
        return LEGACY_FORMAT_TO_COLOR.get(format);
    }

    public static TextColor fromRgb(int rgb) {
        return new TextColor(rgb);
    }

    public static DataResult<TextColor> parseColor(String color) {
        if (color.startsWith(CUSTOM_COLOR_PREFIX)) {
            try {
                int value = Integer.parseInt(color.substring(1), 16);
                if (value < 0 || value > 0xFFFFFF) {
                    return DataResult.error(() -> "Color value out of range: " + color);
                }
                return DataResult.success(TextColor.fromRgb(value), Lifecycle.stable());
            }
            catch (NumberFormatException e) {
                return DataResult.error(() -> "Invalid color value: " + color);
            }
        }
        TextColor predefinedColor = NAMED_COLORS.get(color);
        if (predefinedColor == null) {
            return DataResult.error(() -> "Invalid color name: " + color);
        }
        return DataResult.success(predefinedColor, Lifecycle.stable());
    }
}

