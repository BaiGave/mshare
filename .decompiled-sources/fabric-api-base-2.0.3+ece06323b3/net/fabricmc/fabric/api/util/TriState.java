/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.util;

import com.mojang.serialization.Codec;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.fabricmc.fabric.api.util.BooleanFunction;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

public enum TriState implements StringRepresentable
{
    FALSE("false"),
    DEFAULT("default"),
    TRUE("true");

    public static final Codec<TriState> CODEC;
    private final String name;

    public static TriState of(boolean bool) {
        return bool ? TRUE : FALSE;
    }

    public static TriState of(@Nullable Boolean bool) {
        return bool == null ? DEFAULT : TriState.of((boolean)bool);
    }

    public boolean get() {
        return this == TRUE;
    }

    public @Nullable Boolean getBoxed() {
        return this == DEFAULT ? null : Boolean.valueOf(this.get());
    }

    public boolean orElse(boolean value) {
        return this == DEFAULT ? value : this.get();
    }

    public boolean orElseGet(BooleanSupplier supplier) {
        return this == DEFAULT ? supplier.getAsBoolean() : this.get();
    }

    public <T> Optional<T> map(BooleanFunction<@Nullable ? extends T> mapper) {
        Objects.requireNonNull(mapper, "Mapper function cannot be null");
        if (this == DEFAULT) {
            return Optional.empty();
        }
        return Optional.ofNullable(mapper.apply(this.get()));
    }

    public <X extends Throwable> boolean orElseThrow(Supplier<X> exceptionSupplier) throws X {
        if (this != DEFAULT) {
            return this.get();
        }
        throw (Throwable)exceptionSupplier.get();
    }

    public static TriState fromSystemProperty(String property) {
        String value = System.getProperty(property);
        if (value != null) {
            return Boolean.parseBoolean(value) ? TRUE : FALSE;
        }
        return DEFAULT;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    private TriState(String name) {
        this.name = name;
    }

    static {
        CODEC = StringRepresentable.fromEnum(TriState::values);
    }
}

