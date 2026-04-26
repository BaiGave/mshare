/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.graph.DirectedGraphConnections;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.GraphConnections;
import com.google.common.graph.ImmutableGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.StandardValueGraph;
import com.google.common.graph.UndirectedGraphConnections;
import com.google.common.graph.ValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.Immutable;
import com.google.errorprone.annotations.InlineMe;
import java.util.Objects;

@Immutable(containerOf={"N", "V"})
@Beta
public final class ImmutableValueGraph<N, V>
extends StandardValueGraph<N, V> {
    private ImmutableValueGraph(ValueGraph<N, V> graph) {
        super(ValueGraphBuilder.from(graph), ImmutableValueGraph.getNodeConnections(graph), graph.edges().size());
    }

    public static <N, V> ImmutableValueGraph<N, V> copyOf(ValueGraph<N, V> graph) {
        return graph instanceof ImmutableValueGraph ? (ImmutableValueGraph<N, V>)graph : new ImmutableValueGraph<N, V>(graph);
    }

    @Deprecated
    @InlineMe(replacement="checkNotNull(graph)", staticImports={"com.google.common.base.Preconditions.checkNotNull"})
    public static <N, V> ImmutableValueGraph<N, V> copyOf(ImmutableValueGraph<N, V> graph) {
        return Preconditions.checkNotNull(graph);
    }

    @Override
    public ElementOrder<N> incidentEdgeOrder() {
        return ElementOrder.stable();
    }

    @Override
    public ImmutableGraph<N> asGraph() {
        return new ImmutableGraph(this);
    }

    private static <N, V> ImmutableMap<N, GraphConnections<N, V>> getNodeConnections(ValueGraph<N, V> graph) {
        ImmutableMap.Builder<N, GraphConnections<N, V>> nodeConnections = ImmutableMap.builder();
        for (N node : graph.nodes()) {
            nodeConnections.put(node, ImmutableValueGraph.connectionsOf(graph, node));
        }
        return nodeConnections.buildOrThrow();
    }

    private static <N, V> GraphConnections<N, V> connectionsOf(ValueGraph<N, V> graph, N node) {
        Function successorNodeToValueFn = successorNode -> Objects.requireNonNull(graph.edgeValueOrDefault(node, successorNode, null));
        return graph.isDirected() ? DirectedGraphConnections.ofImmutable(node, graph.incidentEdges(node), successorNodeToValueFn) : UndirectedGraphConnections.ofImmutable(Maps.asMap(graph.adjacentNodes(node), successorNodeToValueFn));
    }

    public static class Builder<N, V> {
        private final MutableValueGraph<N, V> mutableValueGraph;

        Builder(ValueGraphBuilder<N, V> graphBuilder) {
            this.mutableValueGraph = graphBuilder.copy().incidentEdgeOrder(ElementOrder.stable()).build();
        }

        @CanIgnoreReturnValue
        public Builder<N, V> addNode(N node) {
            this.mutableValueGraph.addNode(node);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<N, V> putEdgeValue(N nodeU, N nodeV, V value) {
            this.mutableValueGraph.putEdgeValue(nodeU, nodeV, value);
            return this;
        }

        @CanIgnoreReturnValue
        public Builder<N, V> putEdgeValue(EndpointPair<N> endpoints, V value) {
            this.mutableValueGraph.putEdgeValue(endpoints, value);
            return this;
        }

        public ImmutableValueGraph<N, V> build() {
            return ImmutableValueGraph.copyOf(this.mutableValueGraph);
        }
    }
}

