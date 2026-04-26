/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.graph.Graph;
import com.google.common.graph.Graphs;
import java.util.Set;

@Beta
abstract class GraphsBridgeMethods {
    GraphsBridgeMethods() {
    }

    public static <N> Graph<N> transitiveClosure(Graph<N> graph) {
        return Graphs.transitiveClosure(graph);
    }

    public static <N> Set<N> reachableNodes(Graph<N> graph, N node) {
        return Graphs.reachableNodes(graph, node);
    }
}

