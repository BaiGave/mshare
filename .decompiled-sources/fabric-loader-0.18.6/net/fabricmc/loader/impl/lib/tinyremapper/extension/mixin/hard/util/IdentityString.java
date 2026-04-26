/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util;

import java.util.Objects;
import net.fabricmc.loader.impl.lib.tinyremapper.extension.mixin.hard.util.IConvertibleString;

public class IdentityString
implements IConvertibleString {
    private final String text;

    public IdentityString(String text) {
        this.text = Objects.requireNonNull(text);
    }

    @Override
    public String getConverted() {
        return this.text;
    }

    @Override
    public String getReverted(String newText) {
        return newText;
    }
}

