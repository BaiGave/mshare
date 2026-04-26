/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.creativetab.v1;

import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabBuilderImpl;
import net.minecraft.world.item.CreativeModeTab;

public final class FabricCreativeModeTab {
    private FabricCreativeModeTab() {
    }

    public static CreativeModeTab.Builder builder() {
        return new FabricCreativeModeTabBuilderImpl();
    }
}

