/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.threading;

import com.google.common.collect.ConcurrentHashMultiset;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.minecraft.util.Unit;
import net.minecraft.util.thread.BlockableEventLoop;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NetworkSynchronizer {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
    public static final NetworkSynchronizer CLIENTBOUND = new NetworkSynchronizer();
    public static final NetworkSynchronizer SERVERBOUND = new NetworkSynchronizer();
    private final ThreadLocal<Unit> isNettyThread = new ThreadLocal();
    private final AtomicInteger inFlightPackets = new AtomicInteger();
    private final ConcurrentHashMultiset<RunnableBox> mainThreadPacketHandlers = ConcurrentHashMultiset.create();
    private final Lock morePacketsLock = new ReentrantLock();
    private final Condition morePacketsCondition = this.morePacketsLock.newCondition();
    private final AtomicBoolean invalid = new AtomicBoolean();
    private boolean isRunningNetworkTasks = false;

    public void preSendPacket() {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        this.inFlightPackets.incrementAndGet();
    }

    public void preNettyHandlePacket() {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        this.isNettyThread.set(Unit.INSTANCE);
    }

    public void postNettyHandlePacket() {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        int remainingInFlightPackets = this.inFlightPackets.decrementAndGet();
        if (remainingInFlightPackets < 0) {
            this.markInvalid();
            return;
        }
        this.isNettyThread.remove();
        if (remainingInFlightPackets == 0) {
            this.signalMorePackets();
        }
    }

    public void preTaskAdded(Runnable task) {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        if (this.isNettyThread.get() != null) {
            this.mainThreadPacketHandlers.add(new RunnableBox(task));
            this.signalMorePackets();
        }
    }

    public void postTaskRun(Runnable task) {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        this.checkInvalid();
        this.mainThreadPacketHandlers.remove(new RunnableBox(task));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void waitForPacketHandlers(BlockableEventLoop<?> executor) {
        if (TestSystemProperties.DISABLE_NETWORK_SYNCHRONIZER) {
            return;
        }
        while (this.inFlightPackets.get() > 0 || !this.mainThreadPacketHandlers.isEmpty()) {
            while (this.inFlightPackets.get() > 0 && this.mainThreadPacketHandlers.isEmpty()) {
                this.morePacketsLock.lock();
                try {
                    if (this.morePacketsCondition.await(10L, TimeUnit.SECONDS)) continue;
                    this.markInvalid();
                    this.checkInvalid();
                }
                catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    this.morePacketsLock.unlock();
                }
            }
            this.isRunningNetworkTasks = true;
            long startTime = System.nanoTime();
            try {
                executor.managedBlock(() -> {
                    if (System.nanoTime() - startTime > 10000000000L) {
                        this.markInvalid();
                        this.checkInvalid();
                    }
                    return this.mainThreadPacketHandlers.isEmpty();
                });
            }
            finally {
                this.isRunningNetworkTasks = false;
            }
        }
    }

    public void reset() {
        this.inFlightPackets.set(0);
        this.mainThreadPacketHandlers.clear();
        this.signalMorePackets();
    }

    public boolean isRunningNetworkTasks() {
        return this.isRunningNetworkTasks;
    }

    private void signalMorePackets() {
        this.morePacketsLock.lock();
        this.morePacketsCondition.signal();
        this.morePacketsLock.unlock();
    }

    private void markInvalid() {
        if (!this.invalid.getAndSet(true)) {
            LOGGER.error("Detected interfacing with packets at a lower level. Please disable network synchronization by setting the fabric.client.gametest.disableNetworkSynchronizer system property");
            this.signalMorePackets();
        }
    }

    private void checkInvalid() {
        if (this.invalid.get()) {
            throw new AssertionError((Object)"Network synchronizer in invalid state, see earlier log messages");
        }
    }

    private record RunnableBox(Runnable runnable) {
        /*
         * Enabled force condition propagation
         * Lifted jumps to return sites
         */
        @Override
        public boolean equals(Object other) {
            Runnable otherRunnable;
            if (!(other instanceof RunnableBox)) return false;
            RunnableBox runnableBox = (RunnableBox)other;
            try {
                Runnable runnable;
                otherRunnable = runnable = runnableBox.runnable();
            }
            catch (Throwable throwable) {
                throw new MatchException(throwable.toString(), throwable);
            }
            if (otherRunnable != this.runnable) return false;
            return true;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this.runnable);
        }
    }
}

