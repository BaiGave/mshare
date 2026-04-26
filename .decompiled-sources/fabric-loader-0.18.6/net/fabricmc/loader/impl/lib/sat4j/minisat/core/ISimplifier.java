/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.minisat.core;

import java.io.Serializable;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;

public interface ISimplifier
extends Serializable {
    public void simplify(IVecInt var1);
}

