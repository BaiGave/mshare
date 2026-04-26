/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.impl.number;

import com.ibm.icu.impl.FormattedStringBuilder;
import com.ibm.icu.impl.StandardPlural;
import com.ibm.icu.impl.number.ModifierStore;
import java.text.Format;

public interface Modifier {
    public int apply(FormattedStringBuilder var1, int var2, int var3);

    public int getPrefixLength();

    public int getCodePointCount();

    public boolean isStrong();

    public boolean containsField(Format.Field var1);

    public Parameters getParameters();

    public boolean strictEquals(Modifier var1);

    default public boolean semanticallyEquivalent(Modifier other) {
        Parameters paramsThis = this.getParameters();
        Parameters paramsOther = other.getParameters();
        if (paramsThis == null && paramsOther == null) {
            return this.strictEquals(other);
        }
        if (paramsThis == null || paramsOther == null) {
            return false;
        }
        if (paramsThis.obj == null && paramsOther.obj == null) {
            return this.strictEquals(other);
        }
        if (paramsThis.obj == null || paramsOther.obj == null) {
            return false;
        }
        for (Signum signum : Signum.VALUES) {
            for (StandardPlural plural : StandardPlural.VALUES) {
                Modifier mod2;
                Modifier mod1 = paramsThis.obj.getModifier(signum, plural);
                if (mod1 == (mod2 = paramsOther.obj.getModifier(signum, plural))) continue;
                if (mod1 == null || mod2 == null) {
                    return false;
                }
                if (mod1.strictEquals(mod2)) continue;
                return false;
            }
        }
        return true;
    }

    public static class Parameters {
        public ModifierStore obj;
        public Signum signum;
        public StandardPlural plural;
    }

    public static enum Signum {
        NEG,
        NEG_ZERO,
        POS_ZERO,
        POS;

        static final int COUNT;
        public static final Signum[] VALUES;

        static {
            COUNT = Signum.values().length;
            VALUES = Signum.values();
        }
    }
}

