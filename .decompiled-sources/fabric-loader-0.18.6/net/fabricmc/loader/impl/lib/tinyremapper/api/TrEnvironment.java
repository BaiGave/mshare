/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.api;

import net.fabricmc.loader.impl.lib.tinyremapper.api.TrClass;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrLogger;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrMember;
import net.fabricmc.loader.impl.lib.tinyremapper.api.TrRemapper;

public interface TrEnvironment {
    public int getMrjVersion();

    public TrRemapper getRemapper();

    public TrLogger getLogger();

    public TrClass getClass(String var1);

    public void propagate(TrMember var1, String var2);
}

