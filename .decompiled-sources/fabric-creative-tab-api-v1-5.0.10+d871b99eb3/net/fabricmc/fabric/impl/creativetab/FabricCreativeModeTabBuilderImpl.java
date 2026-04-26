/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.creativetab;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public final class FabricCreativeModeTabBuilderImpl
extends CreativeModeTab.Builder {
    private boolean hasDisplayName = false;

    public FabricCreativeModeTabBuilderImpl() {
        super(null, -1);
    }

    @Override
    public CreativeModeTab.Builder title(Component displayName) {
        this.hasDisplayName = true;
        return super.title(displayName);
    }

    @Override
    public CreativeModeTab build() {
        if (!this.hasDisplayName) {
            throw new IllegalStateException("No display name set for CreativeModeTab");
        }
        return super.build();
    }
}

