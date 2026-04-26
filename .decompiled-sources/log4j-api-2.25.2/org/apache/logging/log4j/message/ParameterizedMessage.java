/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import com.google.errorprone.annotations.InlineMe;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ParameterFormatter;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.apache.logging.log4j.util.StringBuilders;
import org.apache.logging.log4j.util.internal.SerializationUtil;

public class ParameterizedMessage
implements Message,
StringBuilderFormattable {
    public static final String RECURSION_PREFIX = "[...";
    public static final String RECURSION_SUFFIX = "...]";
    public static final String ERROR_PREFIX = "[!!!";
    public static final String ERROR_SEPARATOR = "=>";
    public static final String ERROR_MSG_SEPARATOR = ":";
    public static final String ERROR_SUFFIX = "!!!]";
    private static final long serialVersionUID = -665975803997290697L;
    private static final ThreadLocal<FormatBufferHolder> FORMAT_BUFFER_HOLDER_REF = Constants.ENABLE_THREADLOCALS ? ThreadLocal.withInitial(() -> new FormatBufferHolder()) : null;
    private final String pattern;
    private transient Object[] args;
    private final transient Throwable throwable;
    private final ParameterFormatter.MessagePatternAnalysis patternAnalysis;
    private String formattedMessage;

    @Deprecated
    @InlineMe(replacement="this(pattern, Arrays.stream(args).toArray(Object[]::new), throwable)", imports={"java.util.Arrays"})
    public ParameterizedMessage(String pattern, String[] args, Throwable throwable) {
        this(pattern, (Object[])Arrays.stream(args).toArray(Object[]::new), throwable);
    }

    public ParameterizedMessage(String pattern, Object[] args, Throwable throwable) {
        this.args = args;
        this.pattern = pattern;
        this.patternAnalysis = ParameterFormatter.analyzePattern(pattern, args != null ? args.length : 0);
        this.throwable = ParameterizedMessage.determineThrowable(throwable, this.args, this.patternAnalysis);
    }

    private static Throwable determineThrowable(Throwable throwable, Object[] args, ParameterFormatter.MessagePatternAnalysis analysis) {
        Object lastArg;
        if (throwable != null) {
            return throwable;
        }
        if (args != null && args.length > analysis.placeholderCount && (lastArg = args[args.length - 1]) instanceof Throwable) {
            return (Throwable)lastArg;
        }
        return null;
    }

    public ParameterizedMessage(String pattern, Object ... args) {
        this(pattern, args, null);
    }

    public ParameterizedMessage(String pattern, Object arg) {
        this(pattern, new Object[]{arg});
    }

    public ParameterizedMessage(String pattern, Object arg0, Object arg1) {
        this(pattern, new Object[]{arg0, arg1});
    }

    @Override
    public String getFormat() {
        return this.pattern;
    }

    @Override
    public Object[] getParameters() {
        return this.args;
    }

    @Override
    public Throwable getThrowable() {
        return this.throwable;
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage == null) {
            FormatBufferHolder bufferHolder;
            if (FORMAT_BUFFER_HOLDER_REF == null || (bufferHolder = FORMAT_BUFFER_HOLDER_REF.get()).used) {
                StringBuilder buffer = new StringBuilder(Constants.MAX_REUSABLE_MESSAGE_SIZE);
                this.formatTo(buffer);
                this.formattedMessage = buffer.toString();
            } else {
                bufferHolder.used = true;
                StringBuilder buffer = bufferHolder.buffer;
                try {
                    this.formatTo(buffer);
                    this.formattedMessage = buffer.toString();
                }
                finally {
                    StringBuilders.trimToMaxSize(buffer, Constants.MAX_REUSABLE_MESSAGE_SIZE);
                    buffer.setLength(0);
                    bufferHolder.used = false;
                }
            }
        }
        return this.formattedMessage;
    }

    @Override
    public void formatTo(StringBuilder buffer) {
        if (this.formattedMessage != null) {
            buffer.append(this.formattedMessage);
        } else {
            int argCount = this.args != null ? this.args.length : 0;
            ParameterFormatter.formatMessage(buffer, this.pattern, this.args, argCount, this.patternAnalysis);
        }
    }

    public static String format(String pattern, Object[] args) {
        int argCount = args != null ? args.length : 0;
        return ParameterFormatter.format(pattern, args, argCount);
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof ParameterizedMessage)) {
            return false;
        }
        ParameterizedMessage that = (ParameterizedMessage)object;
        return Objects.equals(this.pattern, that.pattern) && Arrays.equals(this.args, that.args);
    }

    public int hashCode() {
        int result = this.pattern != null ? this.pattern.hashCode() : 0;
        result = 31 * result + (this.args != null ? Arrays.hashCode(this.args) : 0);
        return result;
    }

    public static int countArgumentPlaceholders(String pattern) {
        if (pattern == null) {
            return 0;
        }
        return ParameterFormatter.analyzePattern((String)pattern, (int)-1).placeholderCount;
    }

    public static String deepToString(Object o) {
        return ParameterFormatter.deepToString(o);
    }

    public static String identityToString(Object obj) {
        return ParameterFormatter.identityToString(obj);
    }

    public String toString() {
        return "ParameterizedMessage[messagePattern=" + this.pattern + ", argCount=" + this.args.length + ", throwableProvided=" + (this.throwable != null) + ']';
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeInt(this.args.length);
        for (Object arg : this.args) {
            Object serializableArg = arg instanceof Serializable ? (Serializable)arg : String.valueOf(arg);
            SerializationUtil.writeWrappedObject((Serializable)serializableArg, out);
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        SerializationUtil.assertFiltered(in);
        in.defaultReadObject();
        int argCount = in.readInt();
        this.args = new Object[argCount];
        for (int argIndex = 0; argIndex < this.args.length; ++argIndex) {
            this.args[argIndex] = SerializationUtil.readWrappedObject(in);
        }
    }

    private static final class FormatBufferHolder {
        private final StringBuilder buffer = new StringBuilder(Constants.MAX_REUSABLE_MESSAGE_SIZE);
        private boolean used = false;

        private FormatBufferHolder() {
        }
    }
}

