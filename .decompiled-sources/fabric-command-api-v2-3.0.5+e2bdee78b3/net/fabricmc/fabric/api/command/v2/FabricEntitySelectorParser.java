/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.command.v2;

import net.minecraft.resources.Identifier;

public interface FabricEntitySelectorParser {
    default public void setCustomFlag(Identifier key, boolean value) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    default public boolean getCustomFlag(Identifier key) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }
}

