/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

import net.fabricmc.loader.impl.lib.sat4j.specs.IConstr;
import net.fabricmc.loader.impl.lib.sat4j.specs.IVecInt;
import net.fabricmc.loader.impl.lib.sat4j.specs.UnitPropagationListener;

public interface Constr
extends IConstr {
    public static final Constr TAUTOLOGY = new Constr(){

        @Override
        public boolean learnt() {
            return false;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public int get(int i) {
            throw new UnsupportedOperationException("No elements in a tautology");
        }

        @Override
        public double getActivity() {
            return 0.0;
        }

        @Override
        public boolean canBePropagatedMultipleTimes() {
            return false;
        }

        @Override
        public void remove(UnitPropagationListener upl) {
        }

        @Override
        public boolean simplify() {
            return false;
        }

        @Override
        public void calcReason(int p, IVecInt outReason) {
            throw new UnsupportedOperationException("A tautology cannot be a reason");
        }

        @Override
        public void incActivity(double claInc) {
        }

        @Override
        public boolean locked() {
            return false;
        }

        @Override
        public void setLearnt() {
        }

        @Override
        public void register() {
        }

        @Override
        public void rescaleBy(double d) {
        }

        @Override
        public void setActivity(double d) {
        }

        @Override
        public void assertConstraint(UnitPropagationListener s) {
        }

        @Override
        public boolean canBeSatisfiedByCountingLiterals() {
            return false;
        }

        @Override
        public int requiredNumberOfSatisfiedLiterals() {
            return 0;
        }

        @Override
        public boolean isSatisfied() {
            return true;
        }

        @Override
        public int getAssertionLevel(IVecInt trail, int decisionLevel) {
            return 0;
        }
    };

    public void remove(UnitPropagationListener var1);

    public boolean simplify();

    public void calcReason(int var1, IVecInt var2);

    public void incActivity(double var1);

    public boolean locked();

    public void setLearnt();

    public void register();

    public void rescaleBy(double var1);

    public void setActivity(double var1);

    public void assertConstraint(UnitPropagationListener var1);

    public boolean canBeSatisfiedByCountingLiterals();

    public int requiredNumberOfSatisfiedLiterals();

    public boolean isSatisfied();

    public int getAssertionLevel(IVecInt var1, int var2);
}

