/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

public class InternalException
extends RuntimeException {
    private static final long serialVersionUID = 6366395965071580537L;

    public InternalException(String message) {
        super(message);
    }

    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }

    public InternalException(Throwable cause) {
        super(cause);
    }
}

