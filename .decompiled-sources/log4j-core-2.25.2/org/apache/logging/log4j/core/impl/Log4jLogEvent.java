/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.async.InternalAsyncUtil;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.impl.ContextDataFactory;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.impl.ThrowableProxy;
import org.apache.logging.log4j.core.time.Instant;
import org.apache.logging.log4j.core.time.MutableInstant;
import org.apache.logging.log4j.core.util.Clock;
import org.apache.logging.log4j.core.util.ClockFactory;
import org.apache.logging.log4j.core.util.DummyNanoClock;
import org.apache.logging.log4j.core.util.NanoClock;
import org.apache.logging.log4j.message.LoggerNameAwareMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.message.TimestampMessage;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.ReadOnlyStringMap;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.StringMap;

public class Log4jLogEvent
implements LogEvent {
    private static final long serialVersionUID = -8393305700508709443L;
    private static Clock CLOCK = ClockFactory.getClock();
    private static volatile NanoClock nanoClock = new DummyNanoClock();
    private static final ContextDataInjector CONTEXT_DATA_INJECTOR = ContextDataInjectorFactory.createInjector();
    private final String loggerFqcn;
    private final Level level;
    private final String loggerName;
    private final Marker marker;
    private final transient Throwable thrown;
    private final transient long nanoTime;
    private final MutableInstant instant = new MutableInstant();
    private boolean endOfBatch;
    private boolean includeLocation;
    private StackTraceElement source;
    private String threadName;
    private long threadId;
    private int threadPriority;
    private Message message;
    private final StringMap contextData;
    private final ThreadContext.ContextStack contextStack;
    private ThrowableProxy thrownProxy;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Log4jLogEvent() {
        this("", null, "", null, null, null, null, null, 0L, null, 0, null, CLOCK, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(long timestamp) {
        this("", null, "", null, null, null, null, null, 0L, null, 0, null, timestamp, 0, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable t) {
        this(loggerName, marker, loggerFQCN, level, message, null, t);
    }

    public Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, List<Property> properties, Throwable t) {
        this(loggerName, marker, loggerFQCN, level, message, t, Log4jLogEvent.createContextData(properties), ThreadContext.getDepth() == 0 ? null : ThreadContext.cloneStack(), 0L, null, 0, null, CLOCK, nanoClock.nanoTime());
    }

    public Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, StackTraceElement source, Level level, Message message, List<Property> properties, Throwable t) {
        this(loggerName, marker, loggerFQCN, level, message, t, Log4jLogEvent.createContextData(properties), ThreadContext.getDepth() == 0 ? null : ThreadContext.cloneStack(), 0L, null, 0, source, CLOCK, nanoClock.nanoTime());
    }

    @Deprecated
    public Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable t, Map<String, String> mdc, ThreadContext.ContextStack ndc, String threadName, StackTraceElement location, long timestampMillis) {
        this(loggerName, marker, loggerFQCN, level, message, t, Log4jLogEvent.createContextData(mdc), ndc, 0L, threadName, 0, location, timestampMillis, 0, nanoClock.nanoTime());
    }

    @Deprecated
    public static Log4jLogEvent createEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable thrown, ThrowableProxy ignoredThrownProxy, Map<String, String> mdc, ThreadContext.ContextStack ndc, String threadName, StackTraceElement location, long timestamp) {
        Log4jLogEvent result = new Log4jLogEvent(loggerName, marker, loggerFQCN, level, message, thrown, Log4jLogEvent.createContextData(mdc), ndc, 0L, threadName, 0, location, timestamp, 0, nanoClock.nanoTime());
        return result;
    }

    private Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable thrown, StringMap contextData, ThreadContext.ContextStack contextStack, long threadId, String threadName, int threadPriority, StackTraceElement source, long timestampMillis, int nanoOfMillisecond, long nanoTime) {
        this(loggerName, marker, loggerFQCN, level, message, thrown, contextData, contextStack, threadId, threadName, threadPriority, source, nanoTime);
        long millis = message instanceof TimestampMessage ? ((TimestampMessage)((Object)message)).getTimestamp() : timestampMillis;
        this.instant.initFromEpochMilli(millis, nanoOfMillisecond);
    }

    private Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable thrown, StringMap contextData, ThreadContext.ContextStack contextStack, long threadId, String threadName, int threadPriority, StackTraceElement source, Clock clock, long nanoTime) {
        this(loggerName, marker, loggerFQCN, level, message, thrown, contextData, contextStack, threadId, threadName, threadPriority, source, nanoTime);
        if (message instanceof TimestampMessage) {
            this.instant.initFromEpochMilli(((TimestampMessage)((Object)message)).getTimestamp(), 0);
        } else {
            this.instant.initFrom(clock);
        }
    }

    private Log4jLogEvent(String loggerName, Marker marker, String loggerFQCN, Level level, Message message, Throwable thrown, StringMap contextData, ThreadContext.ContextStack contextStack, long threadId, String threadName, int threadPriority, StackTraceElement source, long nanoTime) {
        this.loggerName = loggerName;
        this.marker = marker;
        this.loggerFqcn = loggerFQCN;
        this.level = level == null ? Level.OFF : level;
        this.message = message;
        this.thrown = thrown;
        this.contextData = contextData == null ? ContextDataFactory.createContextData() : contextData;
        this.contextStack = contextStack == null ? ThreadContext.EMPTY_STACK : contextStack;
        this.threadId = threadId;
        this.threadName = threadName;
        this.threadPriority = threadPriority;
        this.source = source;
        if (message instanceof LoggerNameAwareMessage) {
            ((LoggerNameAwareMessage)((Object)message)).setLoggerName(loggerName);
        }
        this.nanoTime = nanoTime;
    }

    private static StringMap createContextData(Map<String, String> contextMap) {
        StringMap result = ContextDataFactory.createContextData();
        if (contextMap != null) {
            for (Map.Entry<String, String> entry : contextMap.entrySet()) {
                result.putValue(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    private static StringMap createContextData(List<Property> properties) {
        StringMap reusable = ContextDataFactory.createContextData();
        return CONTEXT_DATA_INJECTOR.injectContextData(properties, reusable);
    }

    public static NanoClock getNanoClock() {
        return nanoClock;
    }

    public static void setNanoClock(NanoClock nanoClock) {
        Log4jLogEvent.nanoClock = Objects.requireNonNull(nanoClock, "NanoClock must be non-null");
        StatusLogger.getLogger().trace("Using {} for nanosecond timestamps.", (Object)nanoClock.getClass().getSimpleName());
    }

    public Builder asBuilder() {
        return new Builder(this);
    }

    @Override
    public Log4jLogEvent toImmutable() {
        if (this.getMessage() instanceof ReusableMessage) {
            this.makeMessageImmutable();
        }
        this.populateLazilyInitializedFields();
        return this;
    }

    private void populateLazilyInitializedFields() {
        this.getSource();
        this.getThreadId();
        this.getThreadPriority();
        this.getThreadName();
    }

    @Override
    public Level getLevel() {
        return this.level;
    }

    @Override
    public String getLoggerName() {
        return this.loggerName;
    }

    @Override
    public Message getMessage() {
        return this.message;
    }

    public void makeMessageImmutable() {
        this.message = this.message instanceof ReusableMessage ? ((ReusableMessage)this.message).memento() : InternalAsyncUtil.makeMessageImmutable(this.message);
    }

    @Override
    public long getThreadId() {
        if (this.threadId == 0L) {
            this.threadId = Thread.currentThread().getId();
        }
        return this.threadId;
    }

    @Override
    public String getThreadName() {
        if (this.threadName == null) {
            this.threadName = Thread.currentThread().getName();
        }
        return this.threadName;
    }

    @Override
    public int getThreadPriority() {
        if (this.threadPriority == 0) {
            this.threadPriority = Thread.currentThread().getPriority();
        }
        return this.threadPriority;
    }

    @Override
    public long getTimeMillis() {
        return this.instant.getEpochMillisecond();
    }

    @Override
    public Instant getInstant() {
        return this.instant;
    }

    @Override
    public Throwable getThrown() {
        return this.thrown;
    }

    @Override
    public ThrowableProxy getThrownProxy() {
        return this.thrownProxy != null ? this.thrownProxy : (this.thrown != null ? new ThrowableProxy(this.thrown) : null);
    }

    @Override
    public Marker getMarker() {
        return this.marker;
    }

    @Override
    public String getLoggerFqcn() {
        return this.loggerFqcn;
    }

    @Override
    public ReadOnlyStringMap getContextData() {
        return this.contextData;
    }

    @Override
    public Map<String, String> getContextMap() {
        return this.contextData.toMap();
    }

    @Override
    public ThreadContext.ContextStack getContextStack() {
        return this.contextStack;
    }

    @Override
    public StackTraceElement getSource() {
        if (this.source != null) {
            return this.source;
        }
        if (this.loggerFqcn == null || !this.includeLocation) {
            return null;
        }
        this.source = StackLocatorUtil.calcLocation(this.loggerFqcn);
        return this.source;
    }

    @Override
    public boolean isIncludeLocation() {
        return this.includeLocation;
    }

    @Override
    public void setIncludeLocation(boolean includeLocation) {
        this.includeLocation = includeLocation;
    }

    @Override
    public boolean isEndOfBatch() {
        return this.endOfBatch;
    }

    @Override
    public void setEndOfBatch(boolean endOfBatch) {
        this.endOfBatch = endOfBatch;
    }

    @Override
    public long getNanoTime() {
        return this.nanoTime;
    }

    protected Object writeReplace() {
        return new LogEventProxy(this, this.includeLocation);
    }

    public static Serializable serialize(LogEvent event, boolean includeLocation) {
        if (event instanceof Log4jLogEvent) {
            return new LogEventProxy((Log4jLogEvent)event, includeLocation);
        }
        return new LogEventProxy(event, includeLocation);
    }

    public static Serializable serialize(Log4jLogEvent event, boolean includeLocation) {
        return new LogEventProxy(event, includeLocation);
    }

    public static boolean canDeserialize(Serializable event) {
        return event instanceof LogEventProxy;
    }

    public static Log4jLogEvent deserialize(Serializable event) {
        Objects.requireNonNull(event, "Event cannot be null");
        if (event instanceof LogEventProxy) {
            LogEventProxy proxy = (LogEventProxy)event;
            Log4jLogEvent result = new Log4jLogEvent(proxy.loggerName, proxy.marker, proxy.loggerFQCN, proxy.level, proxy.message, proxy.thrown, proxy.contextData, proxy.contextStack, proxy.threadId, proxy.threadName, proxy.threadPriority, proxy.source, proxy.timeMillis, proxy.nanoOfMillisecond, proxy.nanoTime);
            result.setEndOfBatch(proxy.isEndOfBatch);
            result.setIncludeLocation(proxy.isLocationRequired);
            return result;
        }
        throw new IllegalArgumentException("Event is not a serialized LogEvent: " + event.toString());
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    public static LogEvent createMemento(LogEvent logEvent) {
        return new Builder(logEvent).build();
    }

    public static Log4jLogEvent createMemento(LogEvent event, boolean includeLocation) {
        if (event.isIncludeLocation() && !includeLocation) {
            event.setIncludeLocation(false);
            Log4jLogEvent memento = (Log4jLogEvent)Log4jLogEvent.createMemento(event);
            event.setIncludeLocation(true);
            return memento;
        }
        return (Log4jLogEvent)Log4jLogEvent.createMemento(event);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        String n = this.loggerName.isEmpty() ? "root" : this.loggerName;
        sb.append("Logger=").append(n);
        sb.append(" Level=").append(this.level.name());
        sb.append(" Message=").append(this.message == null ? null : this.message.getFormattedMessage());
        return sb.toString();
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Log4jLogEvent that = (Log4jLogEvent)o;
        if (this.endOfBatch != that.endOfBatch) {
            return false;
        }
        if (this.includeLocation != that.includeLocation) {
            return false;
        }
        if (!this.instant.equals(that.instant)) {
            return false;
        }
        if (this.nanoTime != that.nanoTime) {
            return false;
        }
        if (this.loggerFqcn != null ? !this.loggerFqcn.equals(that.loggerFqcn) : that.loggerFqcn != null) {
            return false;
        }
        if (this.level != null ? !this.level.equals(that.level) : that.level != null) {
            return false;
        }
        if (this.source != null ? !this.source.equals(that.source) : that.source != null) {
            return false;
        }
        if (this.marker != null ? !this.marker.equals(that.marker) : that.marker != null) {
            return false;
        }
        if (this.contextData != null ? !this.contextData.equals(that.contextData) : that.contextData != null) {
            return false;
        }
        if (!this.message.equals(that.message)) {
            return false;
        }
        if (!this.loggerName.equals(that.loggerName)) {
            return false;
        }
        if (this.contextStack != null ? !this.contextStack.equals(that.contextStack) : that.contextStack != null) {
            return false;
        }
        if (this.threadId != that.threadId) {
            return false;
        }
        if (this.threadName != null ? !this.threadName.equals(that.threadName) : that.threadName != null) {
            return false;
        }
        if (this.threadPriority != that.threadPriority) {
            return false;
        }
        return !(this.thrown != null ? !this.thrown.equals(that.thrown) : that.thrown != null);
    }

    public int hashCode() {
        int result = this.loggerFqcn != null ? this.loggerFqcn.hashCode() : 0;
        result = 31 * result + (this.marker != null ? this.marker.hashCode() : 0);
        result = 31 * result + (this.level != null ? this.level.hashCode() : 0);
        result = 31 * result + this.loggerName.hashCode();
        result = 31 * result + this.message.hashCode();
        result = 31 * result + this.instant.hashCode();
        result = 31 * result + (int)(this.nanoTime ^ this.nanoTime >>> 32);
        result = 31 * result + (this.thrown != null ? this.thrown.hashCode() : 0);
        result = 31 * result + (this.contextData != null ? this.contextData.hashCode() : 0);
        result = 31 * result + (this.contextStack != null ? this.contextStack.hashCode() : 0);
        result = 31 * result + (int)(this.threadId ^ this.threadId >>> 32);
        result = 31 * result + (this.threadName != null ? this.threadName.hashCode() : 0);
        result = 31 * result + this.threadPriority;
        result = 31 * result + (this.source != null ? this.source.hashCode() : 0);
        result = 31 * result + (this.includeLocation ? 1 : 0);
        result = 31 * result + (this.endOfBatch ? 1 : 0);
        return result;
    }

    public static class Builder
    implements org.apache.logging.log4j.core.util.Builder<LogEvent> {
        private String loggerFqcn;
        private Level level;
        private String loggerName;
        private Marker marker;
        private Throwable thrown;
        private boolean endOfBatch;
        private boolean includeLocation;
        private long nanoTime;
        private final MutableInstant instant = new MutableInstant();
        private StackTraceElement source;
        private String threadName;
        private long threadId;
        private int threadPriority;
        private Message message;
        private StringMap contextData;
        private ThreadContext.ContextStack contextStack;

        public Builder() {
            this.contextData = Log4jLogEvent.createContextData(null);
            this.contextStack = ThreadContext.getImmutableStack();
        }

        public Builder(LogEvent other) {
            Objects.requireNonNull(other);
            this.loggerFqcn = other.getLoggerFqcn();
            this.level = other.getLevel();
            this.loggerName = other.getLoggerName();
            this.marker = other.getMarker();
            this.thrown = other.getThrown();
            this.endOfBatch = other.isEndOfBatch();
            this.includeLocation = other.isIncludeLocation();
            this.nanoTime = other.getNanoTime();
            this.instant.initFrom(other.getInstant());
            this.threadId = other.getThreadId();
            this.threadPriority = other.getThreadPriority();
            this.threadName = other.getThreadName();
            this.source = other.getSource();
            Message message = other.getMessage();
            this.message = message instanceof ReusableMessage ? ((ReusableMessage)message).memento() : InternalAsyncUtil.makeMessageImmutable(message);
            ReadOnlyStringMap contextData = other.getContextData();
            this.contextData = contextData instanceof StringMap && ((StringMap)contextData).isFrozen() ? (StringMap)contextData : (contextData != null ? ContextDataFactory.createContextData(contextData) : ContextDataFactory.emptyFrozenContextData());
            this.contextStack = other.getContextStack();
        }

        public Builder setLevel(Level level) {
            this.level = level;
            return this;
        }

        public Builder setLoggerFqcn(String loggerFqcn) {
            this.loggerFqcn = loggerFqcn;
            return this;
        }

        public Builder setLoggerName(String loggerName) {
            this.loggerName = loggerName;
            return this;
        }

        public Builder setMarker(Marker marker) {
            this.marker = marker;
            return this;
        }

        public Builder setMessage(Message message) {
            this.message = message;
            return this;
        }

        public Builder setThrown(Throwable thrown) {
            this.thrown = thrown;
            return this;
        }

        public Builder setTimeMillis(long timeMillis) {
            this.instant.initFromEpochMilli(timeMillis, 0);
            return this;
        }

        public Builder setInstant(Instant instant) {
            this.instant.initFrom(instant);
            return this;
        }

        @Deprecated
        public Builder setThrownProxy(ThrowableProxy thrownProxy) {
            return this;
        }

        @Deprecated
        public Builder setContextMap(Map<String, String> contextMap) {
            this.contextData = ContextDataFactory.createContextData();
            if (contextMap != null) {
                for (Map.Entry<String, String> entry : contextMap.entrySet()) {
                    this.contextData.putValue(entry.getKey(), entry.getValue());
                }
            }
            return this;
        }

        public Builder setContextData(StringMap contextData) {
            this.contextData = contextData;
            return this;
        }

        public Builder setContextStack(ThreadContext.ContextStack contextStack) {
            this.contextStack = contextStack;
            return this;
        }

        public Builder setThreadId(long threadId) {
            this.threadId = threadId;
            return this;
        }

        public Builder setThreadName(String threadName) {
            this.threadName = threadName;
            return this;
        }

        public Builder setThreadPriority(int threadPriority) {
            this.threadPriority = threadPriority;
            return this;
        }

        public Builder setSource(StackTraceElement source) {
            this.source = source;
            return this;
        }

        public Builder setIncludeLocation(boolean includeLocation) {
            this.includeLocation = includeLocation;
            return this;
        }

        public Builder setEndOfBatch(boolean endOfBatch) {
            this.endOfBatch = endOfBatch;
            return this;
        }

        public Builder setNanoTime(long nanoTime) {
            this.nanoTime = nanoTime;
            return this;
        }

        @Override
        public Log4jLogEvent build() {
            this.initTimeFields();
            Log4jLogEvent result = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFqcn, this.level, this.message, this.thrown, this.contextData, this.contextStack, this.threadId, this.threadName, this.threadPriority, this.source, this.instant.getEpochMillisecond(), this.instant.getNanoOfMillisecond(), this.nanoTime);
            result.setIncludeLocation(this.includeLocation);
            result.setEndOfBatch(this.endOfBatch);
            return result;
        }

        private void initTimeFields() {
            if (this.instant.getEpochMillisecond() == 0L) {
                this.instant.initFrom(CLOCK);
            }
        }
    }

    static class LogEventProxy
    implements Serializable {
        private static final long serialVersionUID = -8634075037355293699L;
        private final String loggerFQCN;
        private final Marker marker;
        private final Level level;
        private final String loggerName;
        private final transient Message message;
        private MarshalledObject<Message> marshalledMessage;
        private String messageString;
        private final long timeMillis;
        private final int nanoOfMillisecond;
        private final transient Throwable thrown;
        private final ThrowableProxy thrownProxy;
        private final StringMap contextData;
        private final ThreadContext.ContextStack contextStack;
        private final long threadId;
        private final String threadName;
        private final int threadPriority;
        private final StackTraceElement source;
        private final boolean isLocationRequired;
        private final boolean isEndOfBatch;
        private final transient long nanoTime;

        public LogEventProxy(Log4jLogEvent event, boolean includeLocation) {
            this.loggerFQCN = event.loggerFqcn;
            this.marker = event.marker;
            this.level = event.level;
            this.loggerName = event.loggerName;
            this.message = event.message instanceof ReusableMessage ? LogEventProxy.memento((ReusableMessage)event.message) : event.message;
            this.timeMillis = event.instant.getEpochMillisecond();
            this.nanoOfMillisecond = event.instant.getNanoOfMillisecond();
            this.thrown = event.thrown;
            this.thrownProxy = event.getThrownProxy();
            this.contextData = event.contextData;
            this.contextStack = event.contextStack;
            this.source = includeLocation ? event.getSource() : event.source;
            this.threadId = event.getThreadId();
            this.threadName = event.getThreadName();
            this.threadPriority = event.getThreadPriority();
            this.isLocationRequired = includeLocation;
            this.isEndOfBatch = event.endOfBatch;
            this.nanoTime = event.nanoTime;
        }

        public LogEventProxy(LogEvent event, boolean includeLocation) {
            this.loggerFQCN = event.getLoggerFqcn();
            this.marker = event.getMarker();
            this.level = event.getLevel();
            this.loggerName = event.getLoggerName();
            Message temp = event.getMessage();
            this.message = temp instanceof ReusableMessage ? LogEventProxy.memento((ReusableMessage)temp) : temp;
            this.timeMillis = event.getInstant().getEpochMillisecond();
            this.nanoOfMillisecond = event.getInstant().getNanoOfMillisecond();
            this.thrown = event.getThrown();
            this.thrownProxy = event.getThrownProxy();
            this.contextData = LogEventProxy.memento(event.getContextData());
            this.contextStack = event.getContextStack();
            if (event.isIncludeLocation() && !includeLocation) {
                event.setIncludeLocation(false);
                this.source = event.getSource();
                event.setIncludeLocation(true);
            } else {
                this.source = event.getSource();
            }
            this.threadId = event.getThreadId();
            this.threadName = event.getThreadName();
            this.threadPriority = event.getThreadPriority();
            this.isLocationRequired = includeLocation;
            this.isEndOfBatch = event.isEndOfBatch();
            this.nanoTime = event.getNanoTime();
        }

        private static Message memento(ReusableMessage message) {
            return message.memento();
        }

        private static StringMap memento(ReadOnlyStringMap data) {
            StringMap result = ContextDataFactory.createContextData();
            result.putAll(data);
            return result;
        }

        private static MarshalledObject<Message> marshall(Message msg) {
            try {
                return new MarshalledObject<Message>(msg);
            }
            catch (Exception ex) {
                return null;
            }
        }

        private void writeObject(ObjectOutputStream s) throws IOException {
            this.messageString = this.message.getFormattedMessage();
            this.marshalledMessage = LogEventProxy.marshall(this.message);
            s.defaultWriteObject();
        }

        protected Object readResolve() {
            Log4jLogEvent result = new Log4jLogEvent(this.loggerName, this.marker, this.loggerFQCN, this.level, this.message(), this.thrownProxy != null ? this.thrownProxy.getThrowable() : null, this.contextData, this.contextStack, this.threadId, this.threadName, this.threadPriority, this.source, this.timeMillis, this.nanoOfMillisecond, this.nanoTime);
            result.setEndOfBatch(this.isEndOfBatch);
            result.setIncludeLocation(this.isLocationRequired);
            result.thrownProxy = this.thrownProxy;
            return result;
        }

        private Message message() {
            if (this.marshalledMessage != null) {
                try {
                    return this.marshalledMessage.get();
                }
                catch (Exception exception) {
                    // empty catch block
                }
            }
            return new SimpleMessage(this.messageString);
        }
    }
}

