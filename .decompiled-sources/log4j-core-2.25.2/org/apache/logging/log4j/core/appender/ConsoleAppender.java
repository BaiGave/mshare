/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.appender;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AbstractOutputStreamAppender;
import org.apache.logging.log4j.core.appender.ManagerFactory;
import org.apache.logging.log4j.core.appender.OutputStreamManager;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;
import org.apache.logging.log4j.core.util.Booleans;
import org.apache.logging.log4j.core.util.CloseShieldOutputStream;
import org.apache.logging.log4j.util.PropertiesUtil;

@Plugin(name="Console", category="Core", elementType="appender", printObject=true)
public final class ConsoleAppender
extends AbstractOutputStreamAppender<OutputStreamManager> {
    public static final String PLUGIN_NAME = "Console";
    private static final ConsoleManagerFactory factory = new ConsoleManagerFactory();
    private static final Target DEFAULT_TARGET = Target.SYSTEM_OUT;
    private static final AtomicInteger COUNT = new AtomicInteger();
    private final Target target;

    private ConsoleAppender(String name, Layout<? extends Serializable> layout, Filter filter, OutputStreamManager manager, boolean ignoreExceptions, Target target, Property[] properties) {
        super(name, layout, filter, ignoreExceptions, true, properties, manager);
        this.target = target;
    }

    @Deprecated
    public static ConsoleAppender createAppender(Layout<? extends Serializable> layout, Filter filter, String target, String name, String follow, String ignoreExceptions) {
        return ((Builder)((AbstractAppender.Builder)((Builder)((AbstractAppender.Builder)((Builder)((Builder)((AbstractAppender.Builder)ConsoleAppender.newBuilder()).setLayout(layout)).setFilter(filter)).setTarget(target == null ? DEFAULT_TARGET : Target.valueOf(target))).setName(name)).setFollow(Boolean.parseBoolean(follow))).setIgnoreExceptions(Booleans.parseBoolean(ignoreExceptions, true))).build();
    }

    @Deprecated
    public static ConsoleAppender createAppender(Layout<? extends Serializable> layout, Filter filter, Target target, String name, boolean follow, boolean direct, boolean ignoreExceptions) {
        return ((Builder)((AbstractAppender.Builder)((Builder)((Builder)((AbstractAppender.Builder)((Builder)((Builder)((AbstractAppender.Builder)ConsoleAppender.newBuilder()).setLayout(layout)).setFilter(filter)).setTarget(target == null ? DEFAULT_TARGET : target)).setName(name)).setFollow(follow)).setDirect(direct)).setIgnoreExceptions(ignoreExceptions)).build();
    }

    public static ConsoleAppender createDefaultAppenderForLayout(Layout<? extends Serializable> layout) {
        return new ConsoleAppender("DefaultConsole-" + COUNT.incrementAndGet(), layout, null, ConsoleAppender.getDefaultManager(layout), true, DEFAULT_TARGET, null);
    }

    @PluginBuilderFactory
    public static <B extends Builder<B>> B newBuilder() {
        return (B)((Builder)new Builder().asBuilder());
    }

    private static OutputStreamManager getDefaultManager(Layout<? extends Serializable> layout) {
        OutputStream os = ConsoleAppender.getDefaultOutputStream(DEFAULT_TARGET);
        String managerName = DEFAULT_TARGET.name() + ".false.false-" + COUNT.get();
        return OutputStreamManager.getManager(managerName, new FactoryData(os, managerName, layout), factory);
    }

    private static OutputStream getDefaultOutputStream(Target target) {
        return new CloseShieldOutputStream(target == Target.SYSTEM_OUT ? System.out : System.err);
    }

    private static OutputStream getDirectOutputStream(Target target) {
        return new CloseShieldOutputStream(new FileOutputStream(target == Target.SYSTEM_OUT ? FileDescriptor.out : FileDescriptor.err));
    }

    private static OutputStream getFollowOutputStream(Target target) {
        return target == Target.SYSTEM_OUT ? new SystemOutStream() : new SystemErrStream();
    }

    public Target getTarget() {
        return this.target;
    }

    static /* synthetic */ Target access$200() {
        return DEFAULT_TARGET;
    }

    public static enum Target {
        SYSTEM_OUT{

            @Override
            public Charset getDefaultCharset() {
                return this.getCharset("sun.stdout.encoding", Charset.defaultCharset());
            }
        }
        ,
        SYSTEM_ERR{

            @Override
            public Charset getDefaultCharset() {
                return this.getCharset("sun.stderr.encoding", Charset.defaultCharset());
            }
        };


        public abstract Charset getDefaultCharset();

        protected Charset getCharset(String property, Charset defaultCharset) {
            return new PropertiesUtil(PropertiesUtil.getSystemProperties()).getCharsetProperty(property, defaultCharset);
        }
    }

    private static class ConsoleManagerFactory
    implements ManagerFactory<OutputStreamManager, FactoryData> {
        private ConsoleManagerFactory() {
        }

        @Override
        public OutputStreamManager createManager(String name, FactoryData data) {
            return new OutputStreamManager(data.os, data.name, data.layout, true);
        }
    }

    public static class Builder<B extends Builder<B>>
    extends AbstractOutputStreamAppender.Builder<B>
    implements org.apache.logging.log4j.core.util.Builder<ConsoleAppender> {
        @PluginBuilderAttribute
        @Required
        private Target target = ConsoleAppender.access$200();
        @PluginBuilderAttribute
        private boolean follow;
        @PluginBuilderAttribute
        private boolean direct;

        public B setTarget(Target aTarget) {
            this.target = aTarget;
            return (B)((Builder)this.asBuilder());
        }

        public B setFollow(boolean shouldFollow) {
            this.follow = shouldFollow;
            return (B)((Builder)this.asBuilder());
        }

        public B setDirect(boolean shouldDirect) {
            this.direct = shouldDirect;
            return (B)((Builder)this.asBuilder());
        }

        @Override
        public ConsoleAppender build() {
            if (!this.isValid()) {
                return null;
            }
            if (this.direct && this.follow) {
                LOGGER.error("Cannot use both `direct` and `follow` on ConsoleAppender.");
                return null;
            }
            Layout<Serializable> layout = this.getOrCreateLayout(this.target.getDefaultCharset());
            OutputStream stream = this.direct ? ConsoleAppender.getDirectOutputStream(this.target) : (this.follow ? ConsoleAppender.getFollowOutputStream(this.target) : ConsoleAppender.getDefaultOutputStream(this.target));
            String managerName = this.target.name() + '.' + this.follow + '.' + this.direct;
            OutputStreamManager manager = OutputStreamManager.getManager(managerName, new FactoryData(stream, managerName, layout), factory);
            return new ConsoleAppender(this.getName(), layout, this.getFilter(), manager, this.isIgnoreExceptions(), this.target, this.getPropertyArray());
        }
    }

    private static class FactoryData {
        private final OutputStream os;
        private final String name;
        private final Layout<? extends Serializable> layout;

        public FactoryData(OutputStream os, String type, Layout<? extends Serializable> layout) {
            this.os = os;
            this.name = type;
            this.layout = layout;
        }
    }

    private static class SystemOutStream
    extends OutputStream {
        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.out.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            System.out.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            System.out.write(b, off, len);
        }

        @Override
        public void write(int b) throws IOException {
            System.out.write(b);
        }
    }

    private static class SystemErrStream
    extends OutputStream {
        @Override
        public void close() {
        }

        @Override
        public void flush() {
            System.err.flush();
        }

        @Override
        public void write(byte[] b) throws IOException {
            System.err.write(b);
        }

        @Override
        public void write(byte[] b, int off, int len) throws IOException {
            System.err.write(b, off, len);
        }

        @Override
        public void write(int b) {
            System.err.write(b);
        }
    }
}

