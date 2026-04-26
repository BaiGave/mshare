/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;

public interface UnitPropagationListener {
    public boolean enqueue(int var1);

    public boolean enqueue(int var1, Constr var2);

    public void unset(int var1);

    public int getPropagationLevel();
}

