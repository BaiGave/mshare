/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.net.URI;
import java.util.Objects;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

@Plugin(name="MonitorResource", category="Core", printObject=true)
public final class MonitorResource {
    private final URI uri;

    @PluginBuilderFactory
    public static Builder newBuilder() {
        return new Builder();
    }

    private MonitorResource(URI uri) {
        this.uri = Objects.requireNonNull(uri, "uri");
        if (!"file".equals(uri.getScheme())) {
            String message = String.format("Only `file` scheme is supported in monitor resource URIs! Illegal URI: `%s`", uri);
            throw new IllegalArgumentException(message);
        }
    }

    public URI getUri() {
        return this.uri;
    }

    public int hashCode() {
        return this.uri.hashCode();
    }

    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof MonitorResource)) {
            return false;
        }
        MonitorResource other = (MonitorResource)object;
        return this.uri == other.uri;
    }

    public String toString() {
        return String.format("MonitorResource{%s}", this.uri);
    }

    public static final class Builder
    implements org.apache.logging.log4j.core.util.Builder<MonitorResource> {
        @PluginBuilderAttribute
        @Required(message="No URI provided")
        private URI uri;

        public Builder setUri(URI uri) {
            this.uri = uri;
            return this;
        }

        @Override
        public MonitorResource build() {
            return new MonitorResource(this.uri);
        }
    }
}

