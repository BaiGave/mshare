/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.archivers.dump;

import org.apache.commons.compress.archivers.ArchiveException;

public class DumpArchiveException
extends ArchiveException {
    private static final long serialVersionUID = 1L;

    public DumpArchiveException() {
    }

    public DumpArchiveException(String message) {
        super(message);
    }

    public DumpArchiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public DumpArchiveException(Throwable cause) {
        super(cause);
    }
}

