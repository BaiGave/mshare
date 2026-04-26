/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.message;

import java.io.Serializable;

public interface Message
extends Serializable {
    public String getFormattedMessage();

    @Deprecated
    default public String getFormat() {
        return null;
    }

    public Object[] getParameters();

    public Throwable getThrowable();
}

