/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.zip;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.compress.archivers.zip.NioZipEncoding;
import org.apache.commons.compress.archivers.zip.ZipEncoding;
import org.apache.commons.io.Charsets;

public abstract class ZipEncodingHelper {
    static final ZipEncoding ZIP_ENCODING_UTF_8 = ZipEncodingHelper.getZipEncoding(StandardCharsets.UTF_8);

    public static ZipEncoding getZipEncoding(Charset charset) {
        return new NioZipEncoding(Charsets.toCharset(charset));
    }

    public static ZipEncoding getZipEncoding(String name) {
        return new NioZipEncoding(ZipEncodingHelper.toSafeCharset(name));
    }

    static ByteBuffer growBufferBy(ByteBuffer buffer, int increment) {
        buffer.limit(buffer.position());
        buffer.rewind();
        ByteBuffer on = ByteBuffer.allocate(buffer.capacity() + increment);
        on.put(buffer);
        return on;
    }

    static boolean isUTF8(Charset charset) {
        return ZipEncodingHelper.isUTF8Alias(Charsets.toCharset(charset).name());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    private static boolean isUTF8Alias(String name) {
        if (StandardCharsets.UTF_8.name().equalsIgnoreCase(name)) return true;
        if (!StandardCharsets.UTF_8.aliases().stream().anyMatch(name::equalsIgnoreCase)) return false;
        return true;
    }

    private static Charset toSafeCharset(String name) {
        try {
            return Charsets.toCharset(name);
        }
        catch (IllegalArgumentException | NullPointerException ignored) {
            return Charset.defaultCharset();
        }
    }
}

