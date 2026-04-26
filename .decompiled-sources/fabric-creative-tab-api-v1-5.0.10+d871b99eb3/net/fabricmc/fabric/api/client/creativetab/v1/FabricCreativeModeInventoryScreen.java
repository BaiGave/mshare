/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.creativetab.v1;

import java.util.List;
import net.minecraft.world.item.CreativeModeTab;

public interface FabricCreativeModeInventoryScreen {
    default public boolean switchToPage(int page) {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public boolean switchToNextPage() {
        return this.switchToPage(this.getCurrentPage() + 1);
    }

    default public boolean switchToPreviousPage() {
        return this.switchToPage(this.getCurrentPage() - 1);
    }

    default public int getCurrentPage() {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public int getPageCount() {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public List<CreativeModeTab> getTabsOnPage(int page) {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public int getPage(CreativeModeTab creativeModeTab) {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public boolean hasAdditionalPages() {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public CreativeModeTab getSelectedTab() {
        throw new AssertionError((Object)"Implemented by mixin");
    }

    default public boolean setSelectedTab(CreativeModeTab creativeModeTab) {
        throw new AssertionError((Object)"Implemented by mixin");
    }
}

