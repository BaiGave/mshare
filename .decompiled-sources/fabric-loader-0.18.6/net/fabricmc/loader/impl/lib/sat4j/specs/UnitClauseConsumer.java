/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

public interface UnitClauseConsumer {
    public static final UnitClauseConsumer VOID = new UnitClauseConsumer(){

        @Override
        public void learnUnit(int p) {
        }
    };

    public void learnUnit(int var1);
}

