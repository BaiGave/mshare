/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.type;

public abstract class ResolvedType {
    public abstract boolean isAbstract();

    public abstract boolean isThrowable();

    public abstract boolean isInterface();

    public abstract boolean isFinal();

    @Deprecated
    public Class<?> getParameterSource() {
        return null;
    }
}

