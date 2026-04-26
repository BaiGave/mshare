/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import java.util.function.BiConsumer;

public interface ObjectShare {
    public Object get(String var1);

    public void whenAvailable(String var1, BiConsumer<String, Object> var2);

    public Object put(String var1, Object var2);

    public Object putIfAbsent(String var1, Object var2);

    public Object remove(String var1);
}

