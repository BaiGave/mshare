/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@GwtCompatible
public interface BiMap<K, V>
extends Map<K, V> {
    @Override
    @CanIgnoreReturnValue
    public @Nullable V put(@ParametricNullness K var1, @ParametricNullness V var2);

    @CanIgnoreReturnValue
    public @Nullable V forcePut(@ParametricNullness K var1, @ParametricNullness V var2);

    @Override
    public void putAll(Map<? extends K, ? extends V> var1);

    @Override
    public Set<V> values();

    public BiMap<V, K> inverse();
}

