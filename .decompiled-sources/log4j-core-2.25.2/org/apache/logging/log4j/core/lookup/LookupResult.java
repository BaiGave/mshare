/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

public interface LookupResult {
    public String value();

    default public boolean isLookupEvaluationAllowedInValue() {
        return false;
    }
}

