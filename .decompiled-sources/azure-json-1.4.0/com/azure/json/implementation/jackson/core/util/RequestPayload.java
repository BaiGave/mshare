/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.util;

import java.io.IOException;
import java.io.Serializable;

public class RequestPayload
implements Serializable {
    private static final long serialVersionUID = 1L;
    protected byte[] _payloadAsBytes;
    protected CharSequence _payloadAsText;
    protected String _charset;

    public RequestPayload(CharSequence str) {
        if (str == null) {
            throw new IllegalArgumentException();
        }
        this._payloadAsText = str;
    }

    public String toString() {
        if (this._payloadAsBytes != null) {
            try {
                return new String(this._payloadAsBytes, this._charset);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this._payloadAsText.toString();
    }
}

