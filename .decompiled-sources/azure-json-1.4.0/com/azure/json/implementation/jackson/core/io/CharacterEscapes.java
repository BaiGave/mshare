/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.io;

import com.azure.json.implementation.jackson.core.SerializableString;
import java.io.Serializable;

public abstract class CharacterEscapes
implements Serializable {
    public static final int ESCAPE_STANDARD = -1;
    public static final int ESCAPE_CUSTOM = -2;

    public abstract int[] getEscapeCodesForAscii();

    public abstract SerializableString getEscapeSequence(int var1);
}

