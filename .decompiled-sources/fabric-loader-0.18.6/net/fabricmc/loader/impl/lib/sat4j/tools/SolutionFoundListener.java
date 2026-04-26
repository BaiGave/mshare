/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.tools;

public interface SolutionFoundListener {
    public static final SolutionFoundListener VOID = new SolutionFoundListener(){

        @Override
        public void onSolutionFound(int[] model) {
        }

        @Override
        public void onUnsatTermination() {
        }
    };

    public void onSolutionFound(int[] var1);

    public void onUnsatTermination();
}

