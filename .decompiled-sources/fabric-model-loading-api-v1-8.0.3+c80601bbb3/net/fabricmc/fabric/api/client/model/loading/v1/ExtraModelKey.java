/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import java.util.function.Supplier;
import org.jetbrains.annotations.Contract;

public final class ExtraModelKey<T> {
    private final Supplier<String> name;

    private ExtraModelKey(Supplier<String> debugName) {
        this.name = debugName;
    }

    @Contract(value="-> new")
    public static <T> ExtraModelKey<T> create() {
        return new ExtraModelKey<T>(() -> "unnamed");
    }

    @Contract(value="_ -> new")
    public static <T> ExtraModelKey<T> create(Supplier<String> name) {
        return new ExtraModelKey<T>(name);
    }

    public String toString() {
        return "ExtraModelKey(" + this.name.get() + ")";
    }
}

