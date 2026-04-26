/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

public interface IConstr {
    public boolean learnt();

    public int size();

    public int get(int var1);

    public double getActivity();

    public boolean canBePropagatedMultipleTimes();
}

