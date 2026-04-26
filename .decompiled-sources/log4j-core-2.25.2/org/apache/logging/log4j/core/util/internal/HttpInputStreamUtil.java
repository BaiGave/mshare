/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.time.Instant;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationException;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationFactory;
import org.apache.logging.log4j.core.util.AuthorizationProvider;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.core.util.internal.LastModifiedSource;
import org.apache.logging.log4j.core.util.internal.Status;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;
import org.apache.logging.log4j.util.Supplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

public final class HttpInputStreamUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final int NOT_MODIFIED = 304;
    private static final int NOT_AUTHORIZED = 401;
    private static final int FORBIDDEN = 403;
    private static final int NOT_FOUND = 404;
    private static final int OK = 200;
    private static final int BUF_SIZE = 1024;

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static Result getInputStream(LastModifiedSource source, AuthorizationProvider authorizationProvider) {
        Result result = new Result();
        try {
            long lastModified = source.getLastModified();
            HttpURLConnection connection = (HttpURLConnection)UrlConnectionFactory.createConnection(source.getURI().toURL(), lastModified, SslConfigurationFactory.getSslConfiguration(), authorizationProvider);
            connection.connect();
            try {
                int code = connection.getResponseCode();
                switch (code) {
                    case 304: {
                        LOGGER.debug("{} resource {}: not modified since {}", HttpInputStreamUtil.formatProtocol(source), () -> source, () -> Instant.ofEpochMilli(lastModified));
                        result.status = Status.NOT_MODIFIED;
                        Result result2 = result;
                        return result2;
                    }
                    case 404: {
                        LOGGER.debug("{} resource {}: not found", HttpInputStreamUtil.formatProtocol(source), () -> source);
                        result.status = Status.NOT_FOUND;
                        Result result3 = result;
                        return result3;
                    }
                    case 200: {
                        Result result4;
                        InputStream is = connection.getInputStream();
                        try {
                            source.setLastModified(connection.getLastModified());
                            LOGGER.debug("{} resource {}: last modified on {}", HttpInputStreamUtil.formatProtocol(source), () -> source, () -> Instant.ofEpochMilli(connection.getLastModified()));
                            result.status = Status.SUCCESS;
                            Result.access$102(result, HttpInputStreamUtil.readStream(is));
                            result4 = result;
                            if (is == null) return result4;
                        }
                        catch (Throwable throwable) {
                            try {
                                if (is == null) throw throwable;
                                try {
                                    is.close();
                                    throw throwable;
                                }
                                catch (Throwable throwable2) {
                                    throwable.addSuppressed(throwable2);
                                }
                                throw throwable;
                            }
                            catch (IOException e) {
                                try (InputStream es = connection.getErrorStream();){
                                    if (!LOGGER.isDebugEnabled()) throw new ConfigurationException("Unable to access " + source, e);
                                    LOGGER.debug("Error accessing {} resource at {}: {}", (Object)HttpInputStreamUtil.formatProtocol(source).get(), (Object)source, (Object)HttpInputStreamUtil.readStream(es), (Object)e);
                                    throw new ConfigurationException("Unable to access " + source, e);
                                }
                                catch (IOException ioe) {
                                    LOGGER.debug("Error accessing {} resource at {}", HttpInputStreamUtil.formatProtocol(source), () -> source, () -> e);
                                }
                                throw new ConfigurationException("Unable to access " + source, e);
                            }
                        }
                        is.close();
                        return result4;
                    }
                    case 401: {
                        throw new ConfigurationException("Authentication required for " + source);
                    }
                    case 403: {
                        throw new ConfigurationException("Access denied to " + source);
                    }
                }
                if (code < 0) {
                    LOGGER.debug("{} resource {}: invalid response code", (Object)HttpInputStreamUtil.formatProtocol(source), (Object)source);
                    throw new ConfigurationException("Unable to access " + source);
                }
                LOGGER.debug("{} resource {}: unexpected response code {}", (Object)HttpInputStreamUtil.formatProtocol(source), (Object)source, (Object)code);
                throw new ConfigurationException("Unable to access " + source);
            }
            finally {
                connection.disconnect();
            }
        }
        catch (IOException e) {
            LOGGER.debug("Error accessing {} resource at {}", (Object)HttpInputStreamUtil.formatProtocol(source), (Object)source, (Object)e);
            throw new ConfigurationException("Unable to access " + source, e);
        }
    }

    private static Supplier<String> formatProtocol(Source source) {
        return () -> Strings.toRootUpperCase(source.getURI().getScheme());
    }

    public static byte[] readStream(InputStream is) throws IOException {
        int length;
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        while ((length = is.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toByteArray();
    }

    @NullMarked
    public static class Result {
        private byte @Nullable [] bytes = null;
        private Status status;

        public Result() {
            this(Status.ERROR);
        }

        public Result(Status status) {
            this.status = status;
        }

        public @Nullable InputStream getInputStream() {
            return this.bytes != null ? new ByteArrayInputStream(this.bytes) : null;
        }

        public Status getStatus() {
            return this.status;
        }

        static /* synthetic */ byte[] access$102(Result x0, byte[] x1) {
            x0.bytes = x1;
            return x1;
        }
    }
}

