/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup.custom;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.lookup.v1.custom.ApiLookupMap;
import net.minecraft.resources.Identifier;

public final class ApiLookupMapImpl<L>
implements ApiLookupMap<L> {
    private final Map<Identifier, StoredLookup<L>> lookups = new HashMap<Identifier, StoredLookup<L>>();
    private final ApiLookupMap.LookupConstructor<L> lookupConstructor;

    public ApiLookupMapImpl(ApiLookupMap.LookupConstructor<L> lookupConstructor) {
        this.lookupConstructor = lookupConstructor;
    }

    @Override
    public synchronized L getLookup(Identifier lookupId, Class<?> apiClass, Class<?> contextClass) {
        Objects.requireNonNull(lookupId, "Lookup Identifier may not be null.");
        Objects.requireNonNull(apiClass, "API class may not be null.");
        Objects.requireNonNull(contextClass, "Context class may not be null.");
        StoredLookup storedLookup = this.lookups.computeIfAbsent(lookupId, id -> new StoredLookup<L>(this.lookupConstructor.get((Identifier)id, apiClass, contextClass), apiClass, contextClass));
        if (storedLookup.apiClass == apiClass && storedLookup.contextClass == contextClass) {
            return storedLookup.lookup;
        }
        String errorMessage = String.format("Lookup with id %s is already registered with api class %s and context class %s. It can't be registered with api class %s and context class %s.", lookupId, storedLookup.apiClass.getCanonicalName(), storedLookup.contextClass.getCanonicalName(), apiClass.getCanonicalName(), contextClass.getCanonicalName());
        throw new IllegalArgumentException(errorMessage);
    }

    @Override
    public synchronized Iterator<L> iterator() {
        return this.lookups.values().stream().map(storedLookup -> storedLookup.lookup).collect(Collectors.toList()).iterator();
    }

    private static final class StoredLookup<L> {
        final L lookup;
        final Class<?> apiClass;
        final Class<?> contextClass;

        StoredLookup(L lookup, Class<?> apiClass, Class<?> contextClass) {
            this.lookup = lookup;
            this.apiClass = apiClass;
            this.contextClass = contextClass;
        }
    }
}

