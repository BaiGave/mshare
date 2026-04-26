/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface UnitClauseProvider {
    public static final UnitClauseProvider VOID = new UnitClauseProvider(){

        @Override
        public void provideUnitClauses(UnitPropagationListener upl) {
        }
    };

    public void provideUnitClauses(UnitPropagationListener var1);
}

