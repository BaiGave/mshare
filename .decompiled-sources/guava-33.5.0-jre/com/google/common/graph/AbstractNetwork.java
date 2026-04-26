/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterators;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.graph.AbstractGraph;
import com.google.common.graph.ElementOrder;
import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.InvalidatableSet;
import com.google.common.graph.Network;
import com.google.common.math.IntMath;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jspecify.annotations.Nullable;

@Beta
public abstract class AbstractNetwork<N, E>
implements Network<N, E> {
    @Override
    public Graph<N> asGraph() {
        return new AbstractGraph<N>(){

            @Override
            public Set<N> nodes() {
                return AbstractNetwork.this.nodes();
            }

            @Override
            public Set<EndpointPair<N>> edges() {
                if (AbstractNetwork.this.allowsParallelEdges()) {
                    return super.edges();
                }
                return new AbstractSet<EndpointPair<N>>(){

                    @Override
                    public Iterator<EndpointPair<N>> iterator() {
                        return Iterators.transform(AbstractNetwork.this.edges().iterator(), edge -> AbstractNetwork.this.incidentNodes(edge));
                    }

                    @Override
                    public int size() {
                        return AbstractNetwork.this.edges().size();
                    }

                    @Override
                    public boolean contains(@Nullable Object obj) {
                        if (!(obj instanceof EndpointPair)) {
                            return false;
                        }
                        EndpointPair endpointPair = (EndpointPair)obj;
                        return this.isOrderingCompatible(endpointPair) && this.nodes().contains(endpointPair.nodeU()) && this.successors(endpointPair.nodeU()).contains(endpointPair.nodeV());
                    }
                };
            }

            @Override
            public ElementOrder<N> nodeOrder() {
                return AbstractNetwork.this.nodeOrder();
            }

            @Override
            public ElementOrder<N> incidentEdgeOrder() {
                return ElementOrder.unordered();
            }

            @Override
            public boolean isDirected() {
                return AbstractNetwork.this.isDirected();
            }

            @Override
            public boolean allowsSelfLoops() {
                return AbstractNetwork.this.allowsSelfLoops();
            }

            @Override
            public Set<N> adjacentNodes(N node) {
                return AbstractNetwork.this.adjacentNodes(node);
            }

            @Override
            public Set<N> predecessors(N node) {
                return AbstractNetwork.this.predecessors(node);
            }

            @Override
            public Set<N> successors(N node) {
                return AbstractNetwork.this.successors(node);
            }
        };
    }

    @Override
    public int degree(N node) {
        if (this.isDirected()) {
            return IntMath.saturatedAdd(this.inEdges(node).size(), this.outEdges(node).size());
        }
        return IntMath.saturatedAdd(this.incidentEdges(node).size(), this.edgesConnecting(node, node).size());
    }

    @Override
    public int inDegree(N node) {
        return this.isDirected() ? this.inEdges(node).size() : this.degree(node);
    }

    @Override
    public int outDegree(N node) {
        return this.isDirected() ? this.outEdges(node).size() : this.degree(node);
    }

    @Override
    public Set<E> adjacentEdges(E edge) {
        EndpointPair endpointPair = this.incidentNodes(edge);
        Sets.SetView endpointPairIncidentEdges = Sets.union(this.incidentEdges(endpointPair.nodeU()), this.incidentEdges(endpointPair.nodeV()));
        return this.edgeInvalidatableSet(Sets.difference(endpointPairIncidentEdges, ImmutableSet.of(edge)), edge);
    }

    @Override
    public Set<E> edgesConnecting(N nodeU, N nodeV) {
        Set outEdgesU = this.outEdges(nodeU);
        Set inEdgesV = this.inEdges(nodeV);
        return this.nodePairInvalidatableSet(outEdgesU.size() <= inEdgesV.size() ? Collections.unmodifiableSet(Sets.filter(outEdgesU, this.connectedPredicate(nodeU, nodeV))) : Collections.unmodifiableSet(Sets.filter(inEdgesV, this.connectedPredicate(nodeV, nodeU))), nodeU, nodeV);
    }

    @Override
    public Set<E> edgesConnecting(EndpointPair<N> endpoints) {
        this.validateEndpoints(endpoints);
        return this.edgesConnecting(endpoints.nodeU(), endpoints.nodeV());
    }

    private Predicate<E> connectedPredicate(N nodePresent, N nodeToCheck) {
        return edge -> this.incidentNodes(edge).adjacentNode(nodePresent).equals(nodeToCheck);
    }

    @Override
    public Optional<E> edgeConnecting(N nodeU, N nodeV) {
        return Optional.ofNullable(this.edgeConnectingOrNull(nodeU, nodeV));
    }

    @Override
    public Optional<E> edgeConnecting(EndpointPair<N> endpoints) {
        this.validateEndpoints(endpoints);
        return this.edgeConnecting(endpoints.nodeU(), endpoints.nodeV());
    }

    @Override
    public @Nullable E edgeConnectingOrNull(N nodeU, N nodeV) {
        Set<E> edgesConnecting = this.edgesConnecting(nodeU, nodeV);
        switch (edgesConnecting.size()) {
            case 0: {
                return null;
            }
            case 1: {
                return edgesConnecting.iterator().next();
            }
        }
        throw new IllegalArgumentException(String.format("Cannot call edgeConnecting() when parallel edges exist between %s and %s. Consider calling edgesConnecting() instead.", nodeU, nodeV));
    }

    @Override
    public @Nullable E edgeConnectingOrNull(EndpointPair<N> endpoints) {
        this.validateEndpoints(endpoints);
        return this.edgeConnectingOrNull(endpoints.nodeU(), endpoints.nodeV());
    }

    @Override
    public boolean hasEdgeConnecting(N nodeU, N nodeV) {
        Preconditions.checkNotNull(nodeU);
        Preconditions.checkNotNull(nodeV);
        return this.nodes().contains(nodeU) && this.successors((Object)nodeU).contains(nodeV);
    }

    @Override
    public boolean hasEdgeConnecting(EndpointPair<N> endpoints) {
        Preconditions.checkNotNull(endpoints);
        if (!this.isOrderingCompatible(endpoints)) {
            return false;
        }
        return this.hasEdgeConnecting(endpoints.nodeU(), endpoints.nodeV());
    }

    protected final void validateEndpoints(EndpointPair<?> endpoints) {
        Preconditions.checkNotNull(endpoints);
        Preconditions.checkArgument(this.isOrderingCompatible(endpoints), "Mismatch: endpoints' ordering is not compatible with directionality of the graph");
    }

    protected final boolean isOrderingCompatible(EndpointPair<?> endpoints) {
        return endpoints.isOrdered() == this.isDirected();
    }

    @Override
    public final boolean equals(@Nullable Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof Network)) {
            return false;
        }
        Network other = (Network)obj;
        return this.isDirected() == other.isDirected() && this.nodes().equals(other.nodes()) && AbstractNetwork.edgeIncidentNodesMap(this).equals(AbstractNetwork.edgeIncidentNodesMap(other));
    }

    @Override
    public final int hashCode() {
        return AbstractNetwork.edgeIncidentNodesMap(this).hashCode();
    }

    public String toString() {
        return "isDirected: " + this.isDirected() + ", allowsParallelEdges: " + this.allowsParallelEdges() + ", allowsSelfLoops: " + this.allowsSelfLoops() + ", nodes: " + this.nodes() + ", edges: " + AbstractNetwork.edgeIncidentNodesMap(this);
    }

    protected final <T> Set<T> edgeInvalidatableSet(Set<T> set, E edge) {
        return InvalidatableSet.of(set, () -> this.edges().contains(edge), () -> String.format("Edge %s that was used to generate this set is no longer in the graph.", edge));
    }

    protected final <T> Set<T> nodeInvalidatableSet(Set<T> set, N node) {
        return InvalidatableSet.of(set, () -> this.nodes().contains(node), () -> String.format("Node %s that was used to generate this set is no longer in the graph.", node));
    }

    protected final <T> Set<T> nodePairInvalidatableSet(Set<T> set, N nodeU, N nodeV) {
        return InvalidatableSet.of(set, () -> this.nodes().contains(nodeU) && this.nodes().contains(nodeV), () -> String.format("Node %s or node %s that were used to generate this set are no longer in the graph.", nodeU, nodeV));
    }

    private static <N, E> Map<E, EndpointPair<N>> edgeIncidentNodesMap(Network<N, E> network) {
        return Maps.asMap(network.edges(), network::incidentNodes);
    }
}

