/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.common.collect.SetMultimap;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedSet;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface SortedSetMultimap<K, V>
extends SetMultimap<K, V> {
    @Override
    public SortedSet<V> get(@ParametricNullness K var1);

    @Override
    @CanIgnoreReturnValue
    public SortedSet<V> removeAll(@Nullable Object var1);

    @Override
    @CanIgnoreReturnValue
    public SortedSet<V> replaceValues(@ParametricNullness K var1, Iterable<? extends V> var2);

    @Override
    public Map<K, Collection<V>> asMap();

    public @Nullable Comparator<? super V> valueComparator();
}

