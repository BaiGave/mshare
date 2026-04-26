/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import aQute.bnd.annotation.spi.ServiceConsumer;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import org.apache.logging.log4j.message.AsynchronouslyFormattable;
import org.apache.logging.log4j.message.BasicThreadInformation;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.ThreadInformation;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Lazy;
import org.apache.logging.log4j.util.ServiceLoaderUtil;
import org.apache.logging.log4j.util.StringBuilderFormattable;

@AsynchronouslyFormattable
@ServiceConsumer(value=ThreadInfoFactory.class, resolution="optional", cardinality="single")
public class ThreadDumpMessage
implements Message,
StringBuilderFormattable {
    private static final long serialVersionUID = -1103400781608841088L;
    private static final Lazy<ThreadInfoFactory> FACTORY = Lazy.lazy(ThreadDumpMessage::initFactory);
    private volatile Map<ThreadInformation, StackTraceElement[]> threads;
    private final String title;
    private String formattedMessage;

    public ThreadDumpMessage(String title) {
        this.title = title == null ? "" : title;
        this.threads = FACTORY.get().createThreadInfo();
    }

    private ThreadDumpMessage(String formattedMsg, String title) {
        this.formattedMessage = formattedMsg;
        this.title = title == null ? "" : title;
    }

    private static ThreadInfoFactory initFactory() {
        return ServiceLoaderUtil.safeStream(ThreadInfoFactory.class, ServiceLoader.load(ThreadInfoFactory.class, ThreadDumpMessage.class.getClassLoader()), StatusLogger.getLogger()).findFirst().orElseGet(() -> new BasicThreadInfoFactory());
    }

    public String toString() {
        return this.getFormattedMessage();
    }

    @Override
    public String getFormattedMessage() {
        if (this.formattedMessage != null) {
            return this.formattedMessage;
        }
        StringBuilder sb = new StringBuilder(255);
        this.formatTo(sb);
        return sb.toString();
    }

    @Override
    public void formatTo(StringBuilder sb) {
        sb.append(this.title);
        if (this.title.length() > 0) {
            sb.append('\n');
        }
        for (Map.Entry<ThreadInformation, StackTraceElement[]> entry : this.threads.entrySet()) {
            ThreadInformation info = entry.getKey();
            info.printThreadInfo(sb);
            info.printStack(sb, entry.getValue());
            sb.append('\n');
        }
    }

    @Override
    public String getFormat() {
        return this.title == null ? "" : this.title;
    }

    @Override
    public Object[] getParameters() {
        return null;
    }

    protected Object writeReplace() {
        return new ThreadDumpMessageProxy(this);
    }

    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    @Override
    public Throwable getThrowable() {
        return null;
    }

    public static interface ThreadInfoFactory {
        public Map<ThreadInformation, StackTraceElement[]> createThreadInfo();
    }

    private static class ThreadDumpMessageProxy
    implements Serializable {
        private static final long serialVersionUID = -3476620450287648269L;
        private final String formattedMsg;
        private final String title;

        ThreadDumpMessageProxy(ThreadDumpMessage msg) {
            this.formattedMsg = msg.getFormattedMessage();
            this.title = msg.title;
        }

        protected Object readResolve() {
            return new ThreadDumpMessage(this.formattedMsg, this.title);
        }
    }

    private static class BasicThreadInfoFactory
    implements ThreadInfoFactory {
        private BasicThreadInfoFactory() {
        }

        @Override
        public Map<ThreadInformation, StackTraceElement[]> createThreadInfo() {
            Map<Thread, StackTraceElement[]> map = Thread.getAllStackTraces();
            HashMap<ThreadInformation, StackTraceElement[]> threads = new HashMap<ThreadInformation, StackTraceElement[]>(map.size());
            for (Map.Entry<Thread, StackTraceElement[]> entry : map.entrySet()) {
                threads.put(new BasicThreadInformation(entry.getKey()), entry.getValue());
            }
            return threads;
        }
    }
}

