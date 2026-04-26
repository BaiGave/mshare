/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.Modifier;
import com.ibm.icu.impl.number.ModifierStore;

public class AdoptingModifierStore
implements ModifierStore {
    private final Modifier positive;
    private final Modifier posZero;
    private final Modifier negZero;
    private final Modifier negative;
    final Modifier[] mods;
    boolean frozen;

    public AdoptingModifierStore(Modifier positive, Modifier posZero, Modifier negZero, Modifier negative) {
        this.positive = positive;
        this.posZero = posZero;
        this.negZero = negZero;
        this.negative = negative;
        this.mods = null;
        this.frozen = true;
    }

    public AdoptingModifierStore() {
        this.positive = null;
        this.posZero = null;
        this.negZero = null;
        this.negative = null;
        this.mods = new Modifier[4 * StandardPlural.COUNT];
        this.frozen = false;
    }

    public void setModifier(Modifier.Signum signum, StandardPlural plural, Modifier mod) {
        assert (!this.frozen);
        this.mods[AdoptingModifierStore.getModIndex((Modifier.Signum)signum, (StandardPlural)plural)] = mod;
    }

    public void freeze() {
        this.frozen = true;
    }

    public Modifier getModifierWithoutPlural(Modifier.Signum signum) {
        assert (this.frozen);
        assert (this.mods == null);
        assert (signum != null);
        switch (signum) {
            case POS: {
                return this.positive;
            }
            case POS_ZERO: {
                return this.posZero;
            }
            case NEG_ZERO: {
                return this.negZero;
            }
            case NEG: {
                return this.negative;
            }
        }
        throw new AssertionError((Object)"Unreachable");
    }

    @Override
    public Modifier getModifier(Modifier.Signum signum, StandardPlural plural) {
        assert (this.frozen);
        assert (this.positive == null);
        return this.mods[AdoptingModifierStore.getModIndex(signum, plural)];
    }

    private static int getModIndex(Modifier.Signum signum, StandardPlural plural) {
        assert (signum != null);
        assert (plural != null);
        return plural.ordinal() * Modifier.Signum.COUNT + signum.ordinal();
    }
}

