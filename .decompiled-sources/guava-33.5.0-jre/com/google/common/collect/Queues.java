/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.collect;

import com.google.common.annotations.GwtCompatible;
import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Synchronized;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

@GwtCompatible
public final class Queues {
    private Queues() {
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> ArrayBlockingQueue<E> newArrayBlockingQueue(int capacity) {
        return new ArrayBlockingQueue(capacity);
    }

    public static <E> ArrayDeque<E> newArrayDeque() {
        return new ArrayDeque();
    }

    public static <E> ArrayDeque<E> newArrayDeque(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new ArrayDeque((Collection)elements);
        }
        ArrayDeque deque = new ArrayDeque();
        Iterables.addAll(deque, elements);
        return deque;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue() {
        return new ConcurrentLinkedQueue();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> ConcurrentLinkedQueue<E> newConcurrentLinkedQueue(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new ConcurrentLinkedQueue((Collection)elements);
        }
        ConcurrentLinkedQueue queue = new ConcurrentLinkedQueue();
        Iterables.addAll(queue, elements);
        return queue;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingDeque<E> newLinkedBlockingDeque() {
        return new LinkedBlockingDeque();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingDeque<E> newLinkedBlockingDeque(int capacity) {
        return new LinkedBlockingDeque(capacity);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingDeque<E> newLinkedBlockingDeque(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new LinkedBlockingDeque((Collection)elements);
        }
        LinkedBlockingDeque deque = new LinkedBlockingDeque();
        Iterables.addAll(deque, elements);
        return deque;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue() {
        return new LinkedBlockingQueue();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(int capacity) {
        return new LinkedBlockingQueue(capacity);
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> LinkedBlockingQueue<E> newLinkedBlockingQueue(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new LinkedBlockingQueue((Collection)elements);
        }
        LinkedBlockingQueue queue = new LinkedBlockingQueue();
        Iterables.addAll(queue, elements);
        return queue;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E extends Comparable> PriorityBlockingQueue<E> newPriorityBlockingQueue() {
        return new PriorityBlockingQueue();
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E extends Comparable> PriorityBlockingQueue<E> newPriorityBlockingQueue(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new PriorityBlockingQueue((Collection)elements);
        }
        PriorityBlockingQueue queue = new PriorityBlockingQueue();
        Iterables.addAll(queue, elements);
        return queue;
    }

    public static <E extends Comparable> PriorityQueue<E> newPriorityQueue() {
        return new PriorityQueue();
    }

    public static <E extends Comparable> PriorityQueue<E> newPriorityQueue(Iterable<? extends E> elements) {
        if (elements instanceof Collection) {
            return new PriorityQueue((Collection)elements);
        }
        PriorityQueue queue = new PriorityQueue();
        Iterables.addAll(queue, elements);
        return queue;
    }

    @J2ktIncompatible
    @GwtIncompatible
    public static <E> SynchronousQueue<E> newSynchronousQueue() {
        return new SynchronousQueue();
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static <E> int drain(BlockingQueue<E> q, Collection<? super E> buffer, int numElements, Duration timeout) throws InterruptedException {
        return Queues.drain(q, buffer, numElements, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static <E> int drain(BlockingQueue<E> q, Collection<? super E> buffer, int numElements, long timeout, TimeUnit unit) throws InterruptedException {
        Preconditions.checkNotNull(buffer);
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        int added = 0;
        while (added < numElements) {
            if ((added += q.drainTo(buffer, numElements - added)) >= numElements) continue;
            E e = q.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
            if (e == null) break;
            buffer.add(e);
            ++added;
        }
        return added;
    }

    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static <E> int drainUninterruptibly(BlockingQueue<E> q, Collection<? super E> buffer, int numElements, Duration timeout) {
        return Queues.drainUninterruptibly(q, buffer, numElements, timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @CanIgnoreReturnValue
    @J2ktIncompatible
    @GwtIncompatible
    public static <E> int drainUninterruptibly(BlockingQueue<E> q, Collection<? super E> buffer, int numElements, long timeout, TimeUnit unit) {
        Preconditions.checkNotNull(buffer);
        long deadline = System.nanoTime() + unit.toNanos(timeout);
        int added = 0;
        boolean interrupted = false;
        try {
            while (added < numElements) {
                E e;
                if ((added += q.drainTo(buffer, numElements - added)) >= numElements) continue;
                while (true) {
                    try {
                        e = q.poll(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                    }
                    catch (InterruptedException ex) {
                        interrupted = true;
                        continue;
                    }
                    break;
                }
                if (e == null) {
                    break;
                }
                buffer.add(e);
                ++added;
            }
        }
        finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return added;
    }

    @J2ktIncompatible
    public static <E> Queue<E> synchronizedQueue(Queue<E> queue) {
        return Synchronized.queue(queue, null);
    }

    @J2ktIncompatible
    public static <E> Deque<E> synchronizedDeque(Deque<E> deque) {
        return Synchronized.deque(deque, null);
    }
}

