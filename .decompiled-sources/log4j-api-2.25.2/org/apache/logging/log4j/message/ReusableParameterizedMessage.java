/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.util.Arrays;
import org.apache.logging.log4j.message.Clearable;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterConsumer;
import org.apache.logging.log4j.message.ParameterFormatter;
import org.apache.logging.log4j.message.ParameterVisitable;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.ReusableMessage;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.PerformanceSensitive;
import org.apache.logging.log4j.util.StringBuilders;

@PerformanceSensitive(value={"allocation"})
public class ReusableParameterizedMessage
implements ReusableMessage,
ParameterVisitable,
Clearable {
    private static final int MAX_PARAMS = 10;
    private static final long serialVersionUID = 7800075879295123856L;
    private String messagePattern;
    private final ParameterFormatter.MessagePatternAnalysis patternAnalysis = new ParameterFormatter.MessagePatternAnalysis();
    private final StringBuilder formatBuffer = new StringBuilder(Constants.MAX_REUSABLE_MESSAGE_SIZE);
    private int argCount;
    private transient Object[] varargs;
    private transient Object[] params = new Object[10];
    private transient Throwable throwable;
    transient boolean reserved = false;

    private Object[] getTrimmedParams() {
        return this.varargs == null ? Arrays.copyOf(this.params, this.argCount) : this.varargs;
    }

    private Object[] getParams() {
        return this.varargs == null ? this.params : this.varargs;
    }

    @Override
    public Object[] swapParameters(Object[] emptyReplacement) {
        Object[] result;
        if (this.varargs == null) {
            result = this.params;
            if (emptyReplacement.length >= 10) {
                this.params = emptyReplacement;
            } else if (this.argCount <= emptyReplacement.length) {
                System.arraycopy(this.params, 0, emptyReplacement, 0, this.argCount);
                for (int i = 0; i < this.argCount; ++i) {
                    this.params[i] = null;
                }
                result = emptyReplacement;
            } else {
                this.params = new Object[10];
            }
        } else {
            result = this.argCount <= emptyReplacement.length ? emptyReplacement : new Object[this.argCount];
            System.arraycopy(this.varargs, 0, result, 0, this.argCount);
        }
        return result;
    }

    @Override
    public short getParameterCount() {
        return (short)this.argCount;
    }

    @Override
    public <S> void forEachParameter(ParameterConsumer<S> action, S state) {
        Object[] parameters = this.getParams();
        for (int i = 0; i < this.argCount; i = (int)((short)(i + 1))) {
            action.accept(parameters[i], i, state);
        }
    }

    @Override
    public Message memento() {
        ParameterizedMessage message = new ParameterizedMessage(this.messagePattern, this.getTrimmedParams());
        message.getFormattedMessage();
        return message;
    }

    private void init(String messagePattern, int argCount, Object[] args) {
        this.varargs = null;
        this.messagePattern = messagePattern;
        this.argCount = argCount;
        ParameterFormatter.analyzePattern(messagePattern, argCount, this.patternAnalysis);
        this.throwable = ReusableParameterizedMessage.determineThrowable(args, argCount, this.patternAnalysis.placeholderCount);
    }

    private static Throwable determineThrowable(Object[] args, int argCount, int placeholderCount) {
        Object lastArg;
        if (placeholderCount < argCount && (lastArg = args[argCount - 1]) instanceof Throwable) {
            return (Throwable)lastArg;
        }
        return null;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object ... arguments) {
        this.init(messagePattern, arguments == null ? 0 : arguments.length, arguments);
        this.varargs = arguments;
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0) {
        this.params[0] = p0;
        this.init(messagePattern, 1, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.init(messagePattern, 2, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.init(messagePattern, 3, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.init(messagePattern, 4, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.init(messagePattern, 5, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.params[5] = p5;
        this.init(messagePattern, 6, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.params[5] = p5;
        this.params[6] = p6;
        this.init(messagePattern, 7, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.params[5] = p5;
        this.params[6] = p6;
        this.params[7] = p7;
        this.init(messagePattern, 8, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.params[5] = p5;
        this.params[6] = p6;
        this.params[7] = p7;
        this.params[8] = p8;
        this.init(messagePattern, 9, this.params);
        return this;
    }

    public ReusableParameterizedMessage set(String messagePattern, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        this.params[0] = p0;
        this.params[1] = p1;
        this.params[2] = p2;
        this.params[3] = p3;
        this.params[4] = p4;
        this.params[5] = p5;
        this.params[6] = p6;
        this.params[7] = p7;
        this.params[8] = p8;
        this.params[9] = p9;
        this.init(messagePattern, 10, this.params);
        return this;
    }

    @Override
    public String getFormat() {
        return this.messagePattern;
    }

    @Override
    public Object[] getParameters() {
        return this.getTrimmedParams();
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String getFormattedMessage() {
        try {
            this.formatTo(this.formatBuffer);
            String string = this.formatBuffer.toString();
            return string;
        }
        finally {
            StringBuilders.trimToMaxSize(this.formatBuffer, Constants.MAX_REUSABLE_MESSAGE_SIZE);
            this.formatBuffer.setLength(0);
        }
    }

    @Override
    public void formatTo(StringBuilder builder) {
        ParameterFormatter.formatMessage(builder, this.messagePattern, this.getParams(), this.argCount, this.patternAnalysis);
    }

    ReusableParameterizedMessage reserve() {
        this.reserved = true;
        return this;
    }

    public String toString() {
        return "ReusableParameterizedMessage[messagePattern=" + this.getFormat() + ", argCount=" + this.getParameterCount() + ", throwableProvided=" + (this.getThrowable() != null) + ']';
    }

    @Override
    public void clear() {
        this.reserved = false;
        this.varargs = null;
        this.messagePattern = null;
        this.throwable = null;
        int placeholderCharIndicesMaxLength = 16;
        if (this.patternAnalysis.placeholderCharIndices != null && this.patternAnalysis.placeholderCharIndices.length > 16) {
            this.patternAnalysis.placeholderCharIndices = new int[16];
        }
    }

    private Object writeReplace() {
        return this.memento();
    }
}

