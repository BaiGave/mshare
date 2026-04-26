/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress;

import org.apache.commons.compress.CompressException;

public class PasswordRequiredException
extends CompressException {
    private static final long serialVersionUID = 1391070005491684483L;

    public PasswordRequiredException(String name) {
        super("Cannot read encrypted content from " + name + " without a password.");
    }
}

