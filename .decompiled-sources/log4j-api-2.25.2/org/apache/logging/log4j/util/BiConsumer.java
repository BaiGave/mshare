/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

@FunctionalInterface
public interface BiConsumer<K, V>
extends java.util.function.BiConsumer<K, V> {
    @Override
    public void accept(K var1, V var2);
}

