/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft;

import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

public enum ChatFormatting implements StringRepresentable
{
    BLACK("BLACK", '0', 0, 0),
    DARK_BLUE("DARK_BLUE", '1', 1, 170),
    DARK_GREEN("DARK_GREEN", '2', 2, 43520),
    DARK_AQUA("DARK_AQUA", '3', 3, 43690),
    DARK_RED("DARK_RED", '4', 4, 0xAA0000),
    DARK_PURPLE("DARK_PURPLE", '5', 5, 0xAA00AA),
    GOLD("GOLD", '6', 6, 0xFFAA00),
    GRAY("GRAY", '7', 7, 0xAAAAAA),
    DARK_GRAY("DARK_GRAY", '8', 8, 0x555555),
    BLUE("BLUE", '9', 9, 0x5555FF),
    GREEN("GREEN", 'a', 10, 0x55FF55),
    AQUA("AQUA", 'b', 11, 0x55FFFF),
    RED("RED", 'c', 12, 0xFF5555),
    LIGHT_PURPLE("LIGHT_PURPLE", 'd', 13, 0xFF55FF),
    YELLOW("YELLOW", 'e', 14, 0xFFFF55),
    WHITE("WHITE", 'f', 15, 0xFFFFFF),
    OBFUSCATED("OBFUSCATED", 'k', true),
    BOLD("BOLD", 'l', true),
    STRIKETHROUGH("STRIKETHROUGH", 'm', true),
    UNDERLINE("UNDERLINE", 'n', true),
    ITALIC("ITALIC", 'o', true),
    RESET("RESET", 'r', -1, null);

    public static final Codec<ChatFormatting> CODEC;
    public static final Codec<ChatFormatting> COLOR_CODEC;
    public static final char PREFIX_CODE = '\u00a7';
    private static final Map<String, ChatFormatting> FORMATTING_BY_NAME;
    private static final Pattern STRIP_FORMATTING_PATTERN;
    private final String name;
    private final char code;
    private final boolean isFormat;
    private final String toString;
    private final int id;
    private final @Nullable Integer color;

    private static String cleanName(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private ChatFormatting(String name, @Nullable char code, int id, Integer color) {
        this(name, code, false, id, color);
    }

    private ChatFormatting(String name, char code, boolean isFormat) {
        this(name, code, isFormat, -1, null);
    }

    private ChatFormatting(String name, char code, @Nullable boolean isFormat, int id, Integer color) {
        this.name = name;
        this.code = code;
        this.isFormat = isFormat;
        this.id = id;
        this.color = color;
        this.toString = "\u00a7" + String.valueOf(code);
    }

    public char getChar() {
        return this.code;
    }

    public int getId() {
        return this.id;
    }

    public boolean isFormat() {
        return this.isFormat;
    }

    public boolean isColor() {
        return !this.isFormat && this != RESET;
    }

    public @Nullable Integer getColor() {
        return this.color;
    }

    public String getName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    public String toString() {
        return this.toString;
    }

    @Contract(value="!null->!null;_->_")
    public static @Nullable String stripFormatting(@Nullable String input) {
        return input == null ? null : STRIP_FORMATTING_PATTERN.matcher(input).replaceAll("");
    }

    public static @Nullable ChatFormatting getByName(@Nullable String name) {
        if (name == null) {
            return null;
        }
        return FORMATTING_BY_NAME.get(ChatFormatting.cleanName(name));
    }

    public static @Nullable ChatFormatting getById(int id) {
        if (id < 0) {
            return RESET;
        }
        for (ChatFormatting format : ChatFormatting.values()) {
            if (format.getId() != id) continue;
            return format;
        }
        return null;
    }

    public static @Nullable ChatFormatting getByCode(char code) {
        char sanitized = Character.toLowerCase(code);
        for (ChatFormatting format : ChatFormatting.values()) {
            if (format.code != sanitized) continue;
            return format;
        }
        return null;
    }

    public static Collection<String> getNames(boolean getColors, boolean getFormats) {
        ArrayList<String> result = Lists.newArrayList();
        for (ChatFormatting format : ChatFormatting.values()) {
            if (format.isColor() && !getColors || format.isFormat() && !getFormats) continue;
            result.add(format.getName());
        }
        return result;
    }

    @Override
    public String getSerializedName() {
        return this.getName();
    }

    static {
        CODEC = StringRepresentable.fromEnum(ChatFormatting::values);
        COLOR_CODEC = CODEC.validate(color -> color.isFormat() ? DataResult.error(() -> "Formatting was not a valid color: " + String.valueOf(color)) : DataResult.success(color));
        FORMATTING_BY_NAME = Arrays.stream(ChatFormatting.values()).collect(Collectors.toMap(format -> ChatFormatting.cleanName(format.name), f -> f));
        STRIP_FORMATTING_PATTERN = Pattern.compile("(?i)\u00a7[0-9A-FK-OR]");
    }
}

