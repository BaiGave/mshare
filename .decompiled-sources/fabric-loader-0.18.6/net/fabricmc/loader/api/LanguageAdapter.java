/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.api;

import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.util.DefaultLanguageAdapter;

public interface LanguageAdapter {
    public static LanguageAdapter getDefault() {
        return DefaultLanguageAdapter.INSTANCE;
    }

    public <T> T create(ModContainer var1, String var2, Class<T> var3) throws LanguageAdapterException;
}

