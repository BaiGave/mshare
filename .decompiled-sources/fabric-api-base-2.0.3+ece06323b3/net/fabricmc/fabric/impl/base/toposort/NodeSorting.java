/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.base.toposort;

import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import net.fabricmc.fabric.impl.base.toposort.SortableNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeSorting {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-api-base");
    @VisibleForTesting
    public static boolean ENABLE_CYCLE_WARNING = true;

    public static <N extends SortableNode<N>> boolean sort(List<N> sortedNodes, String elementDescription, Comparator<N> comparator) {
        ArrayList toposort = new ArrayList(sortedNodes.size());
        for (Iterator node : sortedNodes) {
            NodeSorting.forwardVisit(node, null, toposort);
        }
        NodeSorting.clearStatus(toposort);
        Collections.reverse(toposort);
        IdentityHashMap nodeToScc = new IdentityHashMap();
        for (SortableNode node : toposort) {
            if (node.visited) continue;
            ArrayList<N> sccNodes = new ArrayList<N>();
            NodeSorting.backwardVisit(node, sccNodes);
            sccNodes.sort(comparator);
            NodeScc scc2 = new NodeScc(sccNodes);
            Iterator<Object> iterator = sccNodes.iterator();
            while (iterator.hasNext()) {
                SortableNode nodeInScc = (SortableNode)iterator.next();
                nodeToScc.put(nodeInScc, scc2);
            }
        }
        NodeSorting.clearStatus(toposort);
        for (Object scc3 : nodeToScc.values()) {
            for (SortableNode node : ((NodeScc)scc3).nodes) {
                for (SortableNode subsequentNode : node.subsequentNodes) {
                    NodeScc subsequentScc = (NodeScc)nodeToScc.get(subsequentNode);
                    if (subsequentScc == scc3) continue;
                    ((NodeScc)scc3).subsequentSccs.add(subsequentScc);
                    ++subsequentScc.inDegree;
                }
            }
        }
        PriorityQueue<NodeScc> pq = new PriorityQueue<NodeScc>(Comparator.comparing(scc -> (SortableNode)scc.nodes.get(0), comparator));
        sortedNodes.clear();
        for (NodeScc scc4 : nodeToScc.values()) {
            if (scc4.inDegree != 0) continue;
            pq.add(scc4);
            scc4.inDegree = -1;
        }
        boolean noCycle = true;
        while (!pq.isEmpty()) {
            NodeScc scc4;
            scc4 = pq.poll();
            sortedNodes.addAll(scc4.nodes);
            if (scc4.nodes.size() > 1) {
                noCycle = false;
                if (ENABLE_CYCLE_WARNING) {
                    StringBuilder builder = new StringBuilder();
                    builder.append("Found cycle while sorting ").append(elementDescription).append(":\n");
                    for (SortableNode node : scc4.nodes) {
                        builder.append("\t").append(node.getDescription()).append("\n");
                    }
                    LOGGER.warn(builder.toString());
                }
            }
            for (NodeScc subsequentScc : scc4.subsequentSccs) {
                --subsequentScc.inDegree;
                if (subsequentScc.inDegree != 0) continue;
                pq.add(subsequentScc);
            }
        }
        return noCycle;
    }

    private static <N extends SortableNode<N>> void forwardVisit(N node, N parent, List<N> toposort) {
        if (!node.visited) {
            node.visited = true;
            for (SortableNode data : node.subsequentNodes) {
                NodeSorting.forwardVisit(data, node, toposort);
            }
            toposort.add(node);
        }
    }

    private static <N extends SortableNode<N>> void clearStatus(List<N> nodes) {
        for (SortableNode node : nodes) {
            node.visited = false;
        }
    }

    private static <N extends SortableNode<N>> void backwardVisit(N node, List<N> sccNodes) {
        if (!node.visited) {
            node.visited = true;
            sccNodes.add(node);
            for (SortableNode data : node.previousNodes) {
                NodeSorting.backwardVisit(data, sccNodes);
            }
        }
    }

    private static class NodeScc<N extends SortableNode<N>> {
        final List<N> nodes;
        final List<NodeScc<N>> subsequentSccs = new ArrayList<NodeScc<N>>();
        int inDegree = 0;

        private NodeScc(List<N> nodes) {
            this.nodes = nodes;
        }
    }
}

