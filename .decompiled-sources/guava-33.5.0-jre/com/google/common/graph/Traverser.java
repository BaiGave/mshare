/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.graph;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableSet;
import com.google.common.graph.BaseGraph;
import com.google.common.graph.Network;
import com.google.common.graph.SuccessorsFunction;
import com.google.errorprone.annotations.DoNotMock;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

@DoNotMock(value="Call forGraph or forTree, passing a lambda or a Graph with the desired edges (built with GraphBuilder)")
@Beta
public abstract class Traverser<N> {
    private final SuccessorsFunction<N> successorFunction;

    private Traverser(SuccessorsFunction<N> successorFunction) {
        this.successorFunction = Preconditions.checkNotNull(successorFunction);
    }

    public static <N> Traverser<N> forGraph(final SuccessorsFunction<N> graph) {
        return new Traverser<N>(graph){

            @Override
            Traversal<N> newTraversal() {
                return Traversal.inGraph(graph);
            }
        };
    }

    public static <N> Traverser<N> forTree(final SuccessorsFunction<N> tree) {
        if (tree instanceof BaseGraph) {
            Preconditions.checkArgument(((BaseGraph)tree).isDirected(), "Undirected graphs can never be trees.");
        }
        if (tree instanceof Network) {
            Preconditions.checkArgument(((Network)tree).isDirected(), "Undirected networks can never be trees.");
        }
        return new Traverser<N>(tree){

            @Override
            Traversal<N> newTraversal() {
                return Traversal.inTree(tree);
            }
        };
    }

    public final Iterable<N> breadthFirst(N startNode) {
        return this.breadthFirst((Iterable<? extends N>)ImmutableSet.of(startNode));
    }

    public final Iterable<N> breadthFirst(Iterable<? extends N> startNodes) {
        ImmutableSet<? extends N> validated = this.validate(startNodes);
        return () -> this.newTraversal().breadthFirst(validated.iterator());
    }

    public final Iterable<N> depthFirstPreOrder(N startNode) {
        return this.depthFirstPreOrder((Iterable<? extends N>)ImmutableSet.of(startNode));
    }

    public final Iterable<N> depthFirstPreOrder(Iterable<? extends N> startNodes) {
        ImmutableSet<? extends N> validated = this.validate(startNodes);
        return () -> this.newTraversal().preOrder(validated.iterator());
    }

    public final Iterable<N> depthFirstPostOrder(N startNode) {
        return this.depthFirstPostOrder((Iterable<? extends N>)ImmutableSet.of(startNode));
    }

    public final Iterable<N> depthFirstPostOrder(Iterable<? extends N> startNodes) {
        ImmutableSet<? extends N> validated = this.validate(startNodes);
        return () -> this.newTraversal().postOrder(validated.iterator());
    }

    abstract Traversal<N> newTraversal();

    private ImmutableSet<N> validate(Iterable<? extends N> startNodes) {
        ImmutableSet copy = ImmutableSet.copyOf(startNodes);
        for (Object node : copy) {
            this.successorFunction.successors(node);
        }
        return copy;
    }

    private static abstract class Traversal<N> {
        final SuccessorsFunction<N> successorFunction;

        Traversal(SuccessorsFunction<N> successorFunction) {
            this.successorFunction = successorFunction;
        }

        static <N> Traversal<N> inGraph(SuccessorsFunction<N> graph) {
            final HashSet visited = new HashSet();
            return new Traversal<N>(graph){

                @Override
                @Nullable N visitNext(Deque<Iterator<? extends N>> horizon) {
                    Iterator top = horizon.getFirst();
                    while (top.hasNext()) {
                        Object element = top.next();
                        Objects.requireNonNull(element);
                        if (!visited.add(element)) continue;
                        return element;
                    }
                    horizon.removeFirst();
                    return null;
                }
            };
        }

        static <N> Traversal<N> inTree(SuccessorsFunction<N> tree) {
            return new Traversal<N>((SuccessorsFunction)tree){

                @Override
                @Nullable N visitNext(Deque<Iterator<? extends N>> horizon) {
                    Iterator top = horizon.getFirst();
                    if (top.hasNext()) {
                        return Preconditions.checkNotNull(top.next());
                    }
                    horizon.removeFirst();
                    return null;
                }
            };
        }

        final Iterator<N> breadthFirst(Iterator<? extends N> startNodes) {
            return this.topDown(startNodes, InsertionOrder.BACK);
        }

        final Iterator<N> preOrder(Iterator<? extends N> startNodes) {
            return this.topDown(startNodes, InsertionOrder.FRONT);
        }

        private Iterator<N> topDown(Iterator<? extends N> startNodes, final InsertionOrder order) {
            final ArrayDeque<Iterator<? extends N>> horizon = new ArrayDeque<Iterator<? extends N>>();
            horizon.add(startNodes);
            return new AbstractIterator<N>(this){
                final /* synthetic */ Traversal this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                protected @Nullable N computeNext() {
                    do {
                        Object next;
                        if ((next = this.this$0.visitNext(horizon)) == null) continue;
                        Iterator successors = this.this$0.successorFunction.successors(next).iterator();
                        if (successors.hasNext()) {
                            order.insertInto(horizon, successors);
                        }
                        return next;
                    } while (!horizon.isEmpty());
                    return this.endOfData();
                }
            };
        }

        final Iterator<N> postOrder(Iterator<? extends N> startNodes) {
            final ArrayDeque ancestorStack = new ArrayDeque();
            final ArrayDeque<Iterator<? extends N>> horizon = new ArrayDeque<Iterator<? extends N>>();
            horizon.add(startNodes);
            return new AbstractIterator<N>(this){
                final /* synthetic */ Traversal this$0;
                {
                    this.this$0 = this$0;
                }

                @Override
                protected @Nullable N computeNext() {
                    Object next = this.this$0.visitNext(horizon);
                    while (next != null) {
                        Iterator successors = this.this$0.successorFunction.successors(next).iterator();
                        if (!successors.hasNext()) {
                            return next;
                        }
                        horizon.addFirst(successors);
                        ancestorStack.push(next);
                        next = this.this$0.visitNext(horizon);
                    }
                    if (!ancestorStack.isEmpty()) {
                        return ancestorStack.pop();
                    }
                    return this.endOfData();
                }
            };
        }

        abstract @Nullable N visitNext(Deque<Iterator<? extends N>> var1);
    }

    private static enum InsertionOrder {
        FRONT{

            @Override
            <T> void insertInto(Deque<T> deque, T value) {
                deque.addFirst(value);
            }
        }
        ,
        BACK{

            @Override
            <T> void insertInto(Deque<T> deque, T value) {
                deque.addLast(value);
            }
        };


        abstract <T> void insertInto(Deque<T> var1, T var2);
    }
}

