/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blocklist;

import java.util.function.Predicate;
import javax.annotation.Nullable;

public interface BlockListSupplier {
    @Nullable
    public Predicate<String> createBlockList();
}

