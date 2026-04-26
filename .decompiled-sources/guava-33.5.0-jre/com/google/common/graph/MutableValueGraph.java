/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.ValueGraph;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import org.jspecify.annotations.Nullable;

@Beta
public interface MutableValueGraph<N, V>
extends ValueGraph<N, V> {
    @CanIgnoreReturnValue
    public boolean addNode(N var1);

    @CanIgnoreReturnValue
    public @Nullable V putEdgeValue(N var1, N var2, V var3);

    @CanIgnoreReturnValue
    public @Nullable V putEdgeValue(EndpointPair<N> var1, V var2);

    @CanIgnoreReturnValue
    public boolean removeNode(N var1);

    @CanIgnoreReturnValue
    public @Nullable V removeEdge(N var1, N var2);

    @CanIgnoreReturnValue
    public @Nullable V removeEdge(EndpointPair<N> var1);
}

