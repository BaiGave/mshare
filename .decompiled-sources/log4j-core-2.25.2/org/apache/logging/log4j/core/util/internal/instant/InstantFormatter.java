/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util.internal.instant;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.apache.logging.log4j.core.time.Instant;

public interface InstantFormatter {
    public ChronoUnit getPrecision();

    default public String format(Instant instant) {
        Objects.requireNonNull(instant, "instant");
        StringBuilder buffer = new StringBuilder();
        this.formatTo(buffer, instant);
        return buffer.toString();
    }

    public void formatTo(StringBuilder var1, Instant var2);
}

