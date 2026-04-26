/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationFactory;
import org.apache.logging.log4j.core.net.UrlConnectionFactory;
import org.apache.logging.log4j.core.util.FileUtils;
import org.apache.logging.log4j.core.util.Loader;
import org.apache.logging.log4j.core.util.Source;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Constants;
import org.apache.logging.log4j.util.LoaderUtil;

public class ConfigurationSource {
    private static final Logger LOGGER = StatusLogger.getLogger();
    public static final ConfigurationSource NULL_SOURCE = new ConfigurationSource(Constants.EMPTY_BYTE_ARRAY, null, 0L);
    public static final ConfigurationSource COMPOSITE_SOURCE = new ConfigurationSource(Constants.EMPTY_BYTE_ARRAY, null, 0L);
    private final InputStream stream;
    private volatile byte[] data;
    private final Source source;
    private final long initialLastModified;
    private volatile long currentLastModified;

    public ConfigurationSource(InputStream stream, File file) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.data = null;
        this.source = new Source(file);
        long modified = 0L;
        try {
            modified = file.lastModified();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.currentLastModified = this.initialLastModified = modified;
    }

    public ConfigurationSource(InputStream stream, Path path) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.data = null;
        this.source = new Source(path);
        long modified = 0L;
        try {
            modified = Files.getLastModifiedTime(path, new LinkOption[0]).toMillis();
        }
        catch (Exception exception) {
            // empty catch block
        }
        this.currentLastModified = this.initialLastModified = modified;
    }

    public ConfigurationSource(InputStream stream, URL url) {
        this(stream, url, 0L);
    }

    public ConfigurationSource(InputStream stream, URL url, long lastModified) {
        this.stream = Objects.requireNonNull(stream, "stream is null");
        this.data = null;
        this.currentLastModified = this.initialLastModified = lastModified;
        this.source = new Source(url);
    }

    public ConfigurationSource(InputStream stream) throws IOException {
        this(ConfigurationSource.toByteArray(stream), null, 0L);
    }

    public ConfigurationSource(Source source, byte[] data, long lastModified) {
        Objects.requireNonNull(source, "source is null");
        this.data = Objects.requireNonNull(data, "data is null");
        this.stream = new ByteArrayInputStream(data);
        this.currentLastModified = this.initialLastModified = lastModified;
        this.source = source;
    }

    private ConfigurationSource(byte[] data, URL url, long lastModified) {
        this.data = Objects.requireNonNull(data, "data is null");
        this.stream = new ByteArrayInputStream(data);
        this.currentLastModified = this.initialLastModified = lastModified;
        this.source = url == null ? null : new Source(url);
    }

    private static byte[] toByteArray(InputStream inputStream) throws IOException {
        int buffSize = Math.max(4096, inputStream.available());
        ByteArrayOutputStream contents = new ByteArrayOutputStream(buffSize);
        byte[] buff = new byte[buffSize];
        int length = inputStream.read(buff);
        while (length > 0) {
            contents.write(buff, 0, length);
            length = inputStream.read(buff);
        }
        return contents.toByteArray();
    }

    public File getFile() {
        return this.source == null ? null : this.source.getFile();
    }

    private boolean isLocation() {
        return this.source != null && this.source.getLocation() != null;
    }

    public URL getURL() {
        return this.source == null ? null : this.source.getURL();
    }

    @Deprecated
    public void setSource(Source ignored) {
        LOGGER.warn("Ignoring call of deprecated method `ConfigurationSource#setSource()`.");
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public void setModifiedMillis(long currentLastModified) {
        this.currentLastModified = currentLastModified;
    }

    public URI getURI() {
        return this.source == null ? null : this.source.getURI();
    }

    public long getLastModified() {
        return this.initialLastModified;
    }

    public String getLocation() {
        return this.source == null ? null : this.source.getLocation();
    }

    public InputStream getInputStream() {
        return this.stream;
    }

    public ConfigurationSource resetInputStream() throws IOException {
        byte[] data = this.data;
        if (this.source != null && data != null) {
            return new ConfigurationSource(this.source, data, this.currentLastModified);
        }
        File file = this.getFile();
        if (file != null) {
            return new ConfigurationSource(Files.newInputStream(file.toPath(), new OpenOption[0]), this.getFile());
        }
        URL url = this.getURL();
        if (url != null && data != null) {
            return new ConfigurationSource(data, url, this.currentLastModified);
        }
        URI uri = this.getURI();
        if (uri != null) {
            return ConfigurationSource.fromUri(uri);
        }
        return data != null ? new ConfigurationSource(data, null, this.currentLastModified) : null;
    }

    public String toString() {
        if (this.source != null) {
            return this.source.getLocation();
        }
        if (this == NULL_SOURCE) {
            return "NULL_SOURCE";
        }
        byte[] data = this.data;
        int length = data == null ? -1 : data.length;
        return "stream (" + length + " bytes, unknown location)";
    }

    public static ConfigurationSource fromUri(URI configLocation) {
        File configFile = FileUtils.fileFromUri(configLocation);
        if (configFile != null && configFile.exists() && configFile.canRead()) {
            try {
                return new ConfigurationSource((InputStream)new FileInputStream(configFile), configFile);
            }
            catch (FileNotFoundException ex) {
                ConfigurationFactory.LOGGER.error("Cannot locate file {}", (Object)configLocation.getPath(), (Object)ex);
            }
        }
        if (ConfigurationFactory.isClassLoaderUri(configLocation)) {
            ClassLoader loader = LoaderUtil.getThreadContextClassLoader();
            String path = ConfigurationFactory.extractClassLoaderUriPath(configLocation);
            return ConfigurationSource.fromResource(path, loader);
        }
        if (!configLocation.isAbsolute()) {
            ConfigurationFactory.LOGGER.error("File not found in file system or classpath: {}", (Object)configLocation.toString());
            return null;
        }
        try {
            return ConfigurationSource.getConfigurationSource(configLocation.toURL());
        }
        catch (MalformedURLException ex) {
            ConfigurationFactory.LOGGER.error("Invalid URL {}", (Object)configLocation.toString(), (Object)ex);
            return null;
        }
    }

    public static ConfigurationSource fromResource(String resource, ClassLoader loader) {
        URL url = Loader.getResource(resource, loader);
        return url == null ? null : ConfigurationSource.getConfigurationSource(url);
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="The name of the accessed files is based on a configuration value.")
    private static ConfigurationSource getConfigurationSource(URL url) {
        try {
            File file = FileUtils.fileFromUri(url.toURI());
            URLConnection urlConnection = UrlConnectionFactory.createConnection(url);
            try {
                if (file != null) {
                    return new ConfigurationSource(urlConnection.getInputStream(), FileUtils.fileFromUri(url.toURI()));
                }
                if (urlConnection instanceof JarURLConnection) {
                    URL jarFileUrl = ((JarURLConnection)urlConnection).getJarFileURL();
                    File jarFile = new File(jarFileUrl.getFile());
                    long lastModified = jarFile.lastModified();
                    return new ConfigurationSource(urlConnection.getInputStream(), url, lastModified);
                }
                return new ConfigurationSource(urlConnection.getInputStream(), url, urlConnection.getLastModified());
            }
            catch (FileNotFoundException ex) {
                ConfigurationFactory.LOGGER.info("Unable to locate file {}, ignoring.", (Object)url.toString());
                return null;
            }
        }
        catch (IOException | URISyntaxException ex) {
            ConfigurationFactory.LOGGER.warn("Error accessing {} due to {}, ignoring.", (Object)url.toString(), (Object)ex.getMessage());
            return null;
        }
    }
}

