/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.lookup.v1.custom;

import java.util.Objects;
import net.fabricmc.fabric.impl.lookup.custom.ApiLookupMapImpl;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface ApiLookupMap<L>
extends Iterable<L> {
    public static <L> ApiLookupMap<L> create(LookupConstructor<L> lookupConstructor) {
        Objects.requireNonNull(lookupConstructor, "Lookup factory may not be null.");
        return new ApiLookupMapImpl<L>(lookupConstructor);
    }

    public L getLookup(Identifier var1, Class<?> var2, Class<?> var3);

    @Deprecated(forRemoval=true)
    public static <L> ApiLookupMap<L> create(LookupFactory<L> lookupFactory) {
        return ApiLookupMap.create((Identifier id, Class<?> apiClass, Class<?> contextClass) -> lookupFactory.get(apiClass, contextClass));
    }

    @FunctionalInterface
    public static interface LookupConstructor<L> {
        public L get(Identifier var1, Class<?> var2, Class<?> var3);
    }

    @Deprecated(forRemoval=true)
    public static interface LookupFactory<L> {
        public L get(Class<?> var1, Class<?> var2);
    }
}

