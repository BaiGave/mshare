/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.creativetab.client;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.fabricmc.fabric.impl.client.creativetab.FabricCreativeGuiComponents;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabImpl;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={CreativeModeInventoryScreen.class})
public abstract class CreativeModeInventoryScreenMixin
extends AbstractContainerScreen<CreativeModeInventoryScreen.ItemPickerMenu>
implements FabricCreativeModeInventoryScreen {
    @Shadow
    private static CreativeModeTab selectedTab;
    @Unique
    private static int currentPage;

    public CreativeModeInventoryScreenMixin(CreativeModeInventoryScreen.ItemPickerMenu menu, Inventory playerInventory, Component component) {
        super(menu, playerInventory, component);
    }

    @Shadow
    protected abstract void selectTab(CreativeModeTab var1);

    @Unique
    private void updateSelection() {
        if (!this.isTabVisible(selectedTab)) {
            CreativeModeTabs.allTabs().stream().filter(this::isTabVisible).min((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight())).ifPresent(this::selectTab);
        }
    }

    @Inject(method={"init"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/components/EditBox;setTextColor(I)V", shift=At.Shift.AFTER)})
    private void init(CallbackInfo info) {
        currentPage = this.getPage(selectedTab);
        int xpos = this.leftPos + 171;
        int ypos = this.topPos + 4;
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen)((Object)this);
        this.addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos + 10, ypos, FabricCreativeGuiComponents.Type.NEXT, self));
        this.addRenderableWidget(new FabricCreativeGuiComponents.CreativeModeTabButton(xpos, ypos, FabricCreativeGuiComponents.Type.PREVIOUS, self));
    }

    @Inject(method={"selectTab"}, at={@At(value="HEAD")}, cancellable=true)
    private void setSelectedTab(CreativeModeTab creativeModeTab, CallbackInfo info) {
        if (!this.isTabVisible(creativeModeTab)) {
            info.cancel();
        }
    }

    @Inject(method={"checkTabHovering"}, at={@At(value="HEAD")}, cancellable=true)
    private void renderTabTooltipIfHovered(GuiGraphicsExtractor graphics, CreativeModeTab creativeModeTab, int mx, int my, CallbackInfoReturnable<Boolean> info) {
        if (!this.isTabVisible(creativeModeTab)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method={"checkTabClicked"}, at={@At(value="HEAD")}, cancellable=true)
    private void isClickInTab(CreativeModeTab creativeModeTab, double mx, double my, CallbackInfoReturnable<Boolean> info) {
        if (!this.isTabVisible(creativeModeTab)) {
            info.setReturnValue(false);
        }
    }

    @Inject(method={"extractTabButton"}, at={@At(value="HEAD")}, cancellable=true)
    private void extractTabButton(GuiGraphicsExtractor guiGraphics, int i, int j, CreativeModeTab creativeModeTab, CallbackInfo info) {
        if (!this.isTabVisible(creativeModeTab)) {
            info.cancel();
        }
    }

    @Inject(method={"keyPressed"}, at={@At(value="HEAD")}, cancellable=true)
    private void keyPressed(KeyEvent context, CallbackInfoReturnable<Boolean> cir) {
        if (context.key() == 266) {
            if (this.switchToPreviousPage()) {
                cir.setReturnValue(true);
            }
        } else if (context.key() == 267 && this.switchToNextPage()) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean isTabVisible(CreativeModeTab creativeModeTab) {
        return creativeModeTab.shouldDisplay() && currentPage == this.getPage(creativeModeTab);
    }

    @Override
    public int getPage(CreativeModeTab creativeModeTab) {
        if (FabricCreativeGuiComponents.COMMON_TABS.contains(creativeModeTab)) {
            return currentPage;
        }
        FabricCreativeModeTabImpl fabriccreativeModeTab = (FabricCreativeModeTabImpl)((Object)creativeModeTab);
        return fabriccreativeModeTab.fabric_getPage();
    }

    @Unique
    private boolean hasGroupForPage(int page) {
        return CreativeModeTabs.tabs().stream().anyMatch(creativeModeTab -> this.getPage((CreativeModeTab)creativeModeTab) == page);
    }

    @Override
    public boolean switchToPage(int page) {
        if (!this.hasGroupForPage(page)) {
            return false;
        }
        if (currentPage == page) {
            return false;
        }
        currentPage = page;
        this.updateSelection();
        return true;
    }

    @Override
    public int getCurrentPage() {
        return currentPage;
    }

    @Override
    public int getPageCount() {
        return FabricCreativeGuiComponents.getPageCount();
    }

    @Override
    public List<CreativeModeTab> getTabsOnPage(int page) {
        return CreativeModeTabs.tabs().stream().filter(creativeModeTab -> this.getPage((CreativeModeTab)creativeModeTab) == page).sorted(Comparator.comparing(CreativeModeTab::row).thenComparingInt(CreativeModeTab::column)).sorted((a, b) -> Boolean.compare(a.isAlignedRight(), b.isAlignedRight())).toList();
    }

    @Override
    public boolean hasAdditionalPages() {
        return CreativeModeTabs.tabs().size() > (Objects.requireNonNull(CreativeModeTabs.CACHED_PARAMETERS).hasPermissions() ? 14 : 13);
    }

    @Override
    public CreativeModeTab getSelectedTab() {
        return selectedTab;
    }

    @Override
    public boolean setSelectedTab(CreativeModeTab creativeModeTab) {
        Objects.requireNonNull(creativeModeTab, "creativeModeTab");
        if (selectedTab == creativeModeTab) {
            return false;
        }
        if (currentPage != this.getPage(creativeModeTab) && !this.switchToPage(this.getPage(creativeModeTab))) {
            return false;
        }
        this.selectTab(creativeModeTab);
        return true;
    }

    static {
        currentPage = 0;
    }
}

