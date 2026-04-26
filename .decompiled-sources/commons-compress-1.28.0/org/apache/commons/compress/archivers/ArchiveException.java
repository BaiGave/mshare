/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers;

import java.util.function.Supplier;
import org.apache.commons.compress.CompressException;

public class ArchiveException
extends CompressException {
    private static final long serialVersionUID = 2772690708123267100L;

    public static <T> T requireNonNull(T obj, Supplier<String> messageSupplier) throws ArchiveException {
        return CompressException.requireNonNull(ArchiveException.class, obj, messageSupplier);
    }

    public ArchiveException() {
    }

    public ArchiveException(String message) {
        super(message);
    }

    @Deprecated
    public ArchiveException(String message, Exception cause) {
        super(message, cause);
    }

    public ArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArchiveException(Throwable cause) {
        super(cause);
    }
}

