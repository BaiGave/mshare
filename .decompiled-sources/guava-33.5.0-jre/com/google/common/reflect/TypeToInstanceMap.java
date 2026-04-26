/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.reflect;

import com.google.common.reflect.ParametricNullness;
import com.google.common.reflect.TypeToken;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@DoNotMock(value="Use ImmutableTypeToInstanceMap or MutableTypeToInstanceMap")
public interface TypeToInstanceMap<B>
extends Map<TypeToken<? extends B>, B> {
    public <T extends B> @Nullable T getInstance(Class<T> var1);

    public <T extends B> @Nullable T getInstance(TypeToken<T> var1);

    @CanIgnoreReturnValue
    public <T extends B> @Nullable T putInstance(Class<@NonNull T> var1, @ParametricNullness T var2);

    @CanIgnoreReturnValue
    public <T extends B> @Nullable T putInstance(TypeToken<@NonNull T> var1, @ParametricNullness T var2);
}

