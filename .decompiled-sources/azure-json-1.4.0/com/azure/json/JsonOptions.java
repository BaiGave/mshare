/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json;

public final class JsonOptions {
    private boolean nonNumericNumbersSupported = true;
    private boolean isJsoncSupported;

    public boolean isNonNumericNumbersSupported() {
        return this.nonNumericNumbersSupported;
    }

    public JsonOptions setNonNumericNumbersSupported(boolean nonNumericNumbersSupported) {
        this.nonNumericNumbersSupported = nonNumericNumbersSupported;
        return this;
    }

    public boolean isJsoncSupported() {
        return this.isJsoncSupported;
    }

    public JsonOptions setJsoncSupported(boolean jsoncSupported) {
        this.isJsoncSupported = jsoncSupported;
        return this;
    }
}

