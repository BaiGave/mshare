/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.internal;

import java.util.Arrays;
import org.apache.logging.log4j.BridgeAware;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogBuilder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LambdaUtil;
import org.apache.logging.log4j.util.StackLocatorUtil;
import org.apache.logging.log4j.util.Supplier;

public class DefaultLogBuilder
implements BridgeAware,
LogBuilder {
    private static Message EMPTY_MESSAGE = new SimpleMessage("");
    private static final String FQCN = DefaultLogBuilder.class.getName();
    private static final Logger LOGGER = StatusLogger.getLogger();
    private ExtendedLogger logger;
    private Level level;
    private Marker marker;
    private Throwable throwable;
    private StackTraceElement location;
    private volatile boolean inUse;
    private long threadId;
    private String fqcn = FQCN;

    public DefaultLogBuilder(ExtendedLogger logger, Level level) {
        this.logger = logger;
        this.level = level;
        this.threadId = Thread.currentThread().getId();
        this.inUse = level != null;
    }

    public DefaultLogBuilder() {
        this(null, null);
    }

    @Override
    public void setEntryPoint(String fqcn) {
        this.fqcn = fqcn;
    }

    public LogBuilder reset(ExtendedLogger logger, Level level) {
        this.logger = logger;
        this.level = level;
        this.marker = null;
        this.throwable = null;
        this.location = null;
        this.inUse = true;
        return this;
    }

    @Override
    public LogBuilder withMarker(Marker marker) {
        this.marker = marker;
        return this;
    }

    @Override
    public LogBuilder withThrowable(Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public LogBuilder withLocation() {
        this.location = StackLocatorUtil.getStackTraceElement(2);
        return this;
    }

    @Override
    public LogBuilder withLocation(StackTraceElement location) {
        this.location = location;
        return this;
    }

    public boolean isInUse() {
        return this.inUse;
    }

    @Override
    public void log(Message message) {
        if (this.isValid() && this.isEnabled(message)) {
            this.logMessage(message);
        }
    }

    @Override
    public Message logAndGet(Supplier<Message> messageSupplier) {
        Message message = null;
        if (this.isValid() && this.isEnabled(message = messageSupplier.get())) {
            this.logMessage(message);
        }
        return message;
    }

    @Override
    public void log(CharSequence message) {
        if (this.isValid() && this.isEnabled(message)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message));
        }
    }

    @Override
    public void log(String message) {
        if (this.isValid() && this.isEnabled(message)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message));
        }
    }

    @Override
    public void log(String message, Object ... params) {
        if (this.isValid() && this.isEnabled(message, params)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, params));
        }
    }

    @Override
    public void log(String message, Supplier<?> ... params) {
        Object[] objs;
        if (this.isValid() && this.isEnabled(message, objs = LambdaUtil.getAll(params))) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, objs));
        }
    }

    @Override
    public void log(Supplier<Message> messageSupplier) {
        this.logAndGet(messageSupplier);
    }

    @Override
    public void log(Object message) {
        if (this.isValid() && this.isEnabled(message)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message));
        }
    }

    @Override
    public void log(String message, Object p0) {
        if (this.isValid() && this.isEnabled(message, p0)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1) {
        if (this.isValid() && this.isEnabled(message, p0, p1)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4, p5)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4, p5));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4, p5, p6)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4, p5, p6));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4, p5, p6, p7)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4, p5, p6, p7, p8)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8));
        }
    }

    @Override
    public void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        if (this.isValid() && this.isEnabled(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9)) {
            this.logMessage(this.logger.getMessageFactory().newMessage(message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
        }
    }

    @Override
    public void log() {
        if (this.isValid() && this.isEnabled(EMPTY_MESSAGE)) {
            this.logMessage(EMPTY_MESSAGE);
        }
    }

    private void logMessage(Message message) {
        try {
            this.logger.logMessage(this.level, this.marker, this.fqcn, this.location, message, this.throwable);
        }
        finally {
            this.inUse = false;
        }
    }

    private boolean isValid() {
        if (!this.inUse) {
            LOGGER.warn("Attempt to reuse LogBuilder was ignored. {}", (Object)StackLocatorUtil.getCallerClass(2));
            return false;
        }
        if (this.threadId != Thread.currentThread().getId()) {
            LOGGER.warn("LogBuilder can only be used on the owning thread. {}", (Object)StackLocatorUtil.getCallerClass(2));
            return false;
        }
        return true;
    }

    protected boolean isEnabled(Message message) {
        return this.logger.isEnabled(this.level, this.marker, message, this.throwable);
    }

    protected boolean isEnabled(CharSequence message) {
        return this.logger.isEnabled(this.level, this.marker, message, this.throwable);
    }

    protected boolean isEnabled(String message) {
        return this.logger.isEnabled(this.level, this.marker, message, this.throwable);
    }

    protected boolean isEnabled(String message, Object ... params) {
        Object[] newParams;
        if (this.throwable != null) {
            newParams = Arrays.copyOf(params, params.length + 1);
            newParams[params.length] = this.throwable;
        } else {
            newParams = params;
        }
        return this.logger.isEnabled(this.level, this.marker, message, newParams);
    }

    protected boolean isEnabled(Object message) {
        return this.logger.isEnabled(this.level, this.marker, message, this.throwable);
    }

    protected boolean isEnabled(String message, Object p0) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0);
    }

    protected boolean isEnabled(String message, Object p0, Object p1) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, (Object)this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    protected boolean isEnabled(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.throwable != null ? this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9, this.throwable) : this.logger.isEnabled(this.level, this.marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }
}

