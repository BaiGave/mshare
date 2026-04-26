/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.include.com.google.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import org.spongepowered.include.com.google.common.base.Preconditions;
import org.spongepowered.include.com.google.common.io.ByteSource;

public final class Resources {
    public static ByteSource asByteSource(URL url) {
        return new UrlByteSource(url);
    }

    private static final class UrlByteSource
    extends ByteSource {
        private final URL url;

        private UrlByteSource(URL url) {
            this.url = Preconditions.checkNotNull(url);
        }

        @Override
        public InputStream openStream() throws IOException {
            return this.url.openStream();
        }

        public String toString() {
            return "Resources.asByteSource(" + this.url + ")";
        }
    }
}

