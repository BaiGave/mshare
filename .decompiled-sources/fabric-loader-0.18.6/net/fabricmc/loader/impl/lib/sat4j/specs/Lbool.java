/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.sat4j.specs;

public final class Lbool {
    public static final Lbool FALSE = new Lbool("F");
    public static final Lbool TRUE = new Lbool("T");
    public static final Lbool UNDEFINED = new Lbool("U");
    private final String symbol;
    private Lbool opposite;

    private Lbool(String symbol) {
        this.symbol = symbol;
    }

    public String toString() {
        return this.symbol;
    }

    static {
        Lbool.FALSE.opposite = TRUE;
        Lbool.TRUE.opposite = FALSE;
        Lbool.UNDEFINED.opposite = UNDEFINED;
    }
}

