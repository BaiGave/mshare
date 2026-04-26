/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.WaitStrategy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDefaultExceptionHandler;
import org.apache.logging.log4j.core.async.AsyncLoggerConfigDisruptor;
import org.apache.logging.log4j.core.async.AsyncLoggerDefaultExceptionHandler;
import org.apache.logging.log4j.core.async.AsyncWaitStrategyFactory;
import org.apache.logging.log4j.core.async.DefaultAsyncWaitStrategyFactory;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.util.Constants;
import org.apache.logging.log4j.core.util.Integers;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesUtil;

final class DisruptorUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int RINGBUFFER_MIN_SIZE = 128;
    private static final int RINGBUFFER_DEFAULT_SIZE = 262144;
    private static final int RINGBUFFER_NO_GC_DEFAULT_SIZE = 4096;
    public static final String LOGGER_EXCEPTION_HANDLER_PROPERTY = "AsyncLogger.ExceptionHandler";
    public static final String LOGGER_CONFIG_EXCEPTION_HANDLER_PROPERTY = "AsyncLoggerConfig.ExceptionHandler";
    static final boolean ASYNC_LOGGER_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL = PropertiesUtil.getProperties().getBooleanProperty("AsyncLogger.SynchronizeEnqueueWhenQueueFull", true);
    static final boolean ASYNC_CONFIG_SYNCHRONIZE_ENQUEUE_WHEN_QUEUE_FULL = PropertiesUtil.getProperties().getBooleanProperty("AsyncLoggerConfig.SynchronizeEnqueueWhenQueueFull", true);
    static final int DISRUPTOR_MAJOR_VERSION = DisruptorUtil.detectDisruptorMajorVersion();

    private static int detectDisruptorMajorVersion() {
        try {
            Class.forName("com.lmax.disruptor.SequenceReportingEventHandler", true, DisruptorUtil.class.getClassLoader());
            return 3;
        }
        catch (ClassNotFoundException e) {
            return 4;
        }
    }

    private DisruptorUtil() {
    }

    static WaitStrategy createWaitStrategy(String propertyName, AsyncWaitStrategyFactory asyncWaitStrategyFactory) {
        if (asyncWaitStrategyFactory == null) {
            LOGGER.debug("No AsyncWaitStrategyFactory was configured in the configuration, using default factory...");
            return new DefaultAsyncWaitStrategyFactory(propertyName).createWaitStrategy();
        }
        LOGGER.debug("Using configured AsyncWaitStrategyFactory {}", (Object)asyncWaitStrategyFactory.getClass().getName());
        return asyncWaitStrategyFactory.createWaitStrategy();
    }

    static int calculateRingBufferSize(String propertyName) {
        int ringBufferSize = Constants.ENABLE_THREADLOCALS ? 4096 : 262144;
        String userPreferredRBSize = PropertiesUtil.getProperties().getStringProperty(propertyName, String.valueOf(ringBufferSize));
        try {
            int size = Integers.parseInt(userPreferredRBSize);
            if (size < 128) {
                size = 128;
                LOGGER.warn("Invalid RingBufferSize {}, using minimum size {}.", (Object)userPreferredRBSize, (Object)128);
            }
            ringBufferSize = size;
        }
        catch (Exception ex) {
            LOGGER.warn("Invalid RingBufferSize {}, using default size {}.", (Object)userPreferredRBSize, (Object)ringBufferSize);
        }
        return Integers.ceilingNextPowerOfTwo(ringBufferSize);
    }

    static ExceptionHandler<RingBufferLogEvent> getAsyncLoggerExceptionHandler() {
        try {
            return LoaderUtil.newCheckedInstanceOfProperty(LOGGER_EXCEPTION_HANDLER_PROPERTY, ExceptionHandler.class, AsyncLoggerDefaultExceptionHandler::new);
        }
        catch (ReflectiveOperationException e) {
            LOGGER.debug("Invalid AsyncLogger.ExceptionHandler value: {}", (Object)e.getMessage(), (Object)e);
            return new AsyncLoggerDefaultExceptionHandler();
        }
    }

    static ExceptionHandler<AsyncLoggerConfigDisruptor.Log4jEventWrapper> getAsyncLoggerConfigExceptionHandler() {
        try {
            return LoaderUtil.newCheckedInstanceOfProperty(LOGGER_CONFIG_EXCEPTION_HANDLER_PROPERTY, ExceptionHandler.class, AsyncLoggerConfigDefaultExceptionHandler::new);
        }
        catch (ReflectiveOperationException e) {
            LOGGER.debug("Invalid AsyncLogger.ExceptionHandler value: {}", (Object)e.getMessage(), (Object)e);
            return new AsyncLoggerConfigDefaultExceptionHandler();
        }
    }

    public static long getExecutorThreadId(ExecutorService executor) {
        Future<Long> result = executor.submit(() -> Thread.currentThread().getId());
        try {
            return result.get();
        }
        catch (Exception ex) {
            String msg = "Could not obtain executor thread Id. Giving up to avoid the risk of application deadlock.";
            throw new IllegalStateException("Could not obtain executor thread Id. Giving up to avoid the risk of application deadlock.", ex);
        }
    }
}

