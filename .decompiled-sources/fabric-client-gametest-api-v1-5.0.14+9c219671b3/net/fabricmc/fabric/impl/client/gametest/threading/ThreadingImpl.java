/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.gametest.threading;

import com.google.common.base.Preconditions;
import java.util.concurrent.Phaser;
import java.util.concurrent.Semaphore;
import net.fabricmc.fabric.impl.client.gametest.TestSystemProperties;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.function.FailableRunnable;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ThreadingImpl {
    private static final Logger LOGGER = LoggerFactory.getLogger("fabric-client-gametest-api-v1");
    private static final String THREAD_IMPL_CLASS_NAME = ThreadingImpl.class.getName();
    private static final String TASK_ON_THIS_THREAD_METHOD_NAME = "runTaskOnThisThread";
    private static final String TASK_ON_OTHER_THREAD_METHOD_NAME = "runTaskOnOtherThread";
    public static final int PHASE_TICK = 0;
    public static final int PHASE_SERVER_TASKS = 1;
    public static final int PHASE_CLIENT_TASKS = 2;
    public static final int PHASE_TEST = 3;
    private static final int PHASE_MASK = 3;
    public static final Phaser PHASER = new Phaser();
    private static volatile boolean enablePhases = true;
    public static volatile boolean isClientRunning = false;
    public static volatile boolean clientCanAcceptTasks = false;
    public static final Semaphore CLIENT_SEMAPHORE = new Semaphore(0);
    public static volatile boolean isServerRunning = false;
    public static volatile boolean serverCanAcceptTasks = false;
    public static final Semaphore SERVER_SEMAPHORE = new Semaphore(0);
    public static @Nullable Thread testThread = null;
    public static final Semaphore TEST_SEMAPHORE = new Semaphore(0);
    public static @Nullable Throwable testFailureException = null;
    public static @Nullable Runnable taskToRun = null;
    private static volatile boolean gameCrashed = false;

    private ThreadingImpl() {
    }

    public static void enterPhase(int phase) {
        while (enablePhases && ThreadingImpl.getNextPhase() != phase) {
            PHASER.arriveAndAwaitAdvance();
        }
        if (enablePhases) {
            PHASER.arriveAndAwaitAdvance();
        }
    }

    public static int getCurrentPhase() {
        return ThreadingImpl.getNextPhase() - 1 & 3;
    }

    private static int getNextPhase() {
        return PHASER.getPhase() & 3;
    }

    public static boolean isGameCrashed() {
        return gameCrashed;
    }

    public static void setGameCrashed() {
        enablePhases = false;
        gameCrashed = true;
    }

    public static void runTestThread(Runnable testRunner) {
        Preconditions.checkState(testThread == null, "There is already a test thread running");
        testThread = new Thread(() -> {
            PHASER.register();
            ThreadingImpl.enterPhase(3);
            try {
                testRunner.run();
            }
            catch (Throwable e) {
                testFailureException = e;
            }
            finally {
                if (clientCanAcceptTasks) {
                    ThreadingImpl.runOnClient(() -> Minecraft.getInstance().stop());
                }
                if (testFailureException != null) {
                    LOGGER.error("Client gametests failed with an exception", testFailureException);
                }
                ThreadingImpl.deregisterTestThread();
            }
        });
        testThread.setName("Test thread");
        testThread.setDaemon(true);
        testThread.start();
    }

    private static void deregisterTestThread() {
        testThread = null;
        enablePhases = false;
        PHASER.arriveAndDeregister();
        if (clientCanAcceptTasks) {
            CLIENT_SEMAPHORE.release();
        }
        if (serverCanAcceptTasks) {
            SERVER_SEMAPHORE.release();
        }
    }

    public static void checkOnGametestThread(String methodName) {
        Preconditions.checkState(Thread.currentThread() == testThread, "%s can only be called from the client gametest thread", (Object)methodName);
    }

    public static <E extends Throwable> void runOnClient(FailableRunnable<E> action) throws E {
        Preconditions.checkNotNull(action, "action");
        ThreadingImpl.checkOnGametestThread("runOnClient");
        Preconditions.checkState(clientCanAcceptTasks, "runOnClient called when no client is running");
        ThreadingImpl.runTaskOnOtherThread(action, CLIENT_SEMAPHORE);
    }

    public static <E extends Throwable> void runOnServer(FailableRunnable<E> action) throws E {
        Preconditions.checkNotNull(action, "action");
        ThreadingImpl.checkOnGametestThread("runOnServer");
        Preconditions.checkState(serverCanAcceptTasks, "runOnServer called when no server is running");
        ThreadingImpl.runTaskOnOtherThread(action, SERVER_SEMAPHORE);
    }

    private static <E extends Throwable> void runTaskOnOtherThread(FailableRunnable<E> action, Semaphore clientOrServerSemaphore) throws E {
        MutableObject thrown = new MutableObject();
        taskToRun = () -> ThreadingImpl.runTaskOnThisThread(action, thrown);
        clientOrServerSemaphore.release();
        try {
            TEST_SEMAPHORE.acquire();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (thrown.getValue() != null) {
            ThreadingImpl.joinAsyncStackTrace((Throwable)thrown.getValue());
            throw (Throwable)thrown.getValue();
        }
    }

    private static <E extends Throwable> void runTaskOnThisThread(FailableRunnable<E> action, MutableObject<E> thrown) {
        try {
            action.run();
        }
        catch (Throwable e) {
            thrown.setValue(e);
        }
        finally {
            taskToRun = null;
            TEST_SEMAPHORE.release();
        }
    }

    private static void joinAsyncStackTrace(Throwable e) {
        StackTraceElement element;
        int thisThreadIndex;
        StackTraceElement element2;
        int otherThreadIndex;
        if (TestSystemProperties.DISABLE_JOIN_ASYNC_STACK_TRACES) {
            return;
        }
        StackTraceElement[] otherThreadStackTrace = e.getStackTrace();
        if (otherThreadStackTrace == null) {
            return;
        }
        for (otherThreadIndex = otherThreadStackTrace.length - 1; !(otherThreadIndex < 0 || THREAD_IMPL_CLASS_NAME.equals((element2 = otherThreadStackTrace[otherThreadIndex]).getClassName()) && TASK_ON_THIS_THREAD_METHOD_NAME.equals(element2.getMethodName())); --otherThreadIndex) {
        }
        if (otherThreadIndex == -1) {
            return;
        }
        StackTraceElement[] thisThreadStackTrace = Thread.currentThread().getStackTrace();
        for (thisThreadIndex = 0; !(thisThreadIndex >= thisThreadStackTrace.length || THREAD_IMPL_CLASS_NAME.equals((element = thisThreadStackTrace[thisThreadIndex]).getClassName()) && TASK_ON_OTHER_THREAD_METHOD_NAME.equals(element.getMethodName())); ++thisThreadIndex) {
        }
        if (thisThreadIndex == thisThreadStackTrace.length) {
            return;
        }
        StackTraceElement[] joinedStackTrace = new StackTraceElement[otherThreadIndex + 1 + 1 + (thisThreadStackTrace.length - thisThreadIndex)];
        System.arraycopy(otherThreadStackTrace, 0, joinedStackTrace, 0, otherThreadIndex + 1);
        joinedStackTrace[otherThreadIndex + 1] = new StackTraceElement("Async Stack Trace", ".", null, 1);
        System.arraycopy(thisThreadStackTrace, thisThreadIndex, joinedStackTrace, otherThreadIndex + 2, thisThreadStackTrace.length - thisThreadIndex);
        e.setStackTrace(joinedStackTrace);
    }

    public static void runTick() {
        ThreadingImpl.checkOnGametestThread("runTick");
        if (clientCanAcceptTasks) {
            CLIENT_SEMAPHORE.release();
        }
        if (serverCanAcceptTasks) {
            SERVER_SEMAPHORE.release();
        }
        ThreadingImpl.enterPhase(3);
        if (gameCrashed) {
            ThreadingImpl.deregisterTestThread();
            try {
                new Semaphore(0).acquire();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}

