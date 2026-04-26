/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;

public interface FabricResourceReloader
extends PreparableReloadListener {
    public Identifier fabric$getId();

    @Override
    default public String getName() {
        return String.valueOf(this.fabric$getId()) + " (" + this.getClass().getSimpleName() + ")";
    }
}

