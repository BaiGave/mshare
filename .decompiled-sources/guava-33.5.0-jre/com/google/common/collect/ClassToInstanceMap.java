/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.collect.ParametricNullness;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.DoNotMock;
import java.util.Map;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

@DoNotMock(value="Use ImmutableClassToInstanceMap or MutableClassToInstanceMap")
@GwtCompatible
public interface ClassToInstanceMap<B>
extends Map<Class<? extends B>, B> {
    public <T extends B> @Nullable T getInstance(Class<T> var1);

    @CanIgnoreReturnValue
    public <T extends B> @Nullable T putInstance(Class<@NonNull T> var1, @ParametricNullness T var2);
}

