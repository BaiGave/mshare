/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.Constr;
import net.fabricmc.loader.impl.lib.sat4j.specs.MandatoryLiteralListener;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface Propagatable {
    public boolean propagate(UnitPropagationListener var1, int var2);

    public boolean propagatePI(MandatoryLiteralListener var1, int var2);

    public Constr toConstraint();
}

