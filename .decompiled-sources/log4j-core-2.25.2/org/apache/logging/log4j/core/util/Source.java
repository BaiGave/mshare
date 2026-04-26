/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class Source {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private final @Nullable File file;
    private final URI uri;
    private final String location;

    private static String normalize(File file) {
        try {
            return file.getCanonicalFile().getAbsolutePath();
        }
        catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static @Nullable File toFile(Path path) {
        try {
            return Objects.requireNonNull(path, "path").toFile();
        }
        catch (UnsupportedOperationException e) {
            return null;
        }
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="The URI should be specified in a configuration file.")
    private static @Nullable File toFile(URI uri) {
        try {
            String scheme = Objects.requireNonNull(uri, "uri").getScheme();
            if (Strings.isBlank(scheme) || scheme.equals("file")) {
                return new File(uri.getPath());
            }
            LOGGER.debug("uri does not represent a local file: " + uri);
            return null;
        }
        catch (Exception e) {
            LOGGER.debug("uri is malformed: " + uri);
            return null;
        }
    }

    private static URI toURI(URL url) {
        try {
            return Objects.requireNonNull(url, "url").toURI();
        }
        catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public Source(ConfigurationSource source) {
        this.file = source.getFile();
        this.uri = Objects.requireNonNull(source.getURI());
        this.location = Objects.requireNonNull(source.getLocation());
    }

    public Source(File file) {
        this.file = Objects.requireNonNull(file, "file");
        this.location = Source.normalize(file);
        this.uri = file.toURI();
    }

    public Source(Path path) {
        Path normPath = Objects.requireNonNull(path, "path").normalize();
        this.file = Source.toFile(normPath);
        this.uri = normPath.toUri();
        this.location = normPath.toString();
    }

    public Source(URI uri) {
        URI normUri;
        this.uri = normUri = Objects.requireNonNull(uri, "uri").normalize();
        this.location = normUri.toString();
        this.file = Source.toFile(normUri);
    }

    @Deprecated
    public Source(URI uri, long ignored) {
        this(uri);
    }

    public Source(URL url) {
        this.uri = Source.toURI(url);
        this.location = this.uri.toString();
        this.file = Source.toFile(this.uri);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Source)) {
            return false;
        }
        Source other = (Source)obj;
        return Objects.equals(this.location, other.location);
    }

    public @Nullable File getFile() {
        return this.file;
    }

    public String getLocation() {
        return this.location;
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="The `file`, `uri` and `location` fields come from Log4j properties.")
    public Path getPath() {
        return this.file != null ? this.file.toPath() : Paths.get(this.uri);
    }

    public URI getURI() {
        return this.uri;
    }

    public URL getURL() {
        try {
            return this.uri.toURL();
        }
        catch (MalformedURLException e) {
            throw new IllegalStateException(e);
        }
    }

    public int hashCode() {
        return Objects.hash(this.location);
    }

    public String toString() {
        return this.location;
    }
}

