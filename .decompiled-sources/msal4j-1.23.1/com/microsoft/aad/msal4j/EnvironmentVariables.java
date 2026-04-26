/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.IEnvironmentVariables;

class EnvironmentVariables
implements IEnvironmentVariables {
    EnvironmentVariables() {
    }

    @Override
    public String getEnvironmentVariable(String envVariable) {
        return System.getenv(envVariable);
    }
}

