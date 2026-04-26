/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.annotation.Nullable;

public class LogQueues {
    private static final Map<String, BlockingQueue<String>> QUEUES = new HashMap<String, BlockingQueue<String>>();
    private static final ReentrantReadWriteLock QUEUE_LOCK = new ReentrantReadWriteLock();

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static BlockingQueue<String> getOrCreateQueue(String target) {
        try {
            QUEUE_LOCK.readLock().lock();
            BlockingQueue<String> queue = QUEUES.get(target);
            if (queue != null) {
                BlockingQueue<String> blockingQueue = queue;
                return blockingQueue;
            }
        }
        finally {
            QUEUE_LOCK.readLock().unlock();
        }
        try {
            QUEUE_LOCK.writeLock().lock();
            BlockingQueue blockingQueue = QUEUES.computeIfAbsent(target, k -> new LinkedBlockingQueue());
            return blockingQueue;
        }
        finally {
            QUEUE_LOCK.writeLock().unlock();
        }
    }

    @Nullable
    public static String getNextLogEvent(String queueName) {
        QUEUE_LOCK.readLock().lock();
        BlockingQueue<String> queue = QUEUES.get(queueName);
        QUEUE_LOCK.readLock().unlock();
        if (queue != null) {
            try {
                return queue.take();
            }
            catch (InterruptedException interruptedException) {
                // empty catch block
            }
        }
        return null;
    }
}

