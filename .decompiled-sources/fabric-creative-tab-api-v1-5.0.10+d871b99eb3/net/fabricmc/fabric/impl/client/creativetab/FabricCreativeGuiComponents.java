/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.creativetab;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.fabricmc.fabric.api.client.creativetab.v1.FabricCreativeModeInventoryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;

public class FabricCreativeGuiComponents {
    private static final Identifier BUTTON_TEX = Identifier.fromNamespaceAndPath("fabric", "textures/gui/creative_buttons.png");
    private static final double TABS_PER_PAGE = 10.0;
    public static final Set<CreativeModeTab> COMMON_TABS = Set.of(CreativeModeTabs.SEARCH, CreativeModeTabs.INVENTORY, CreativeModeTabs.HOTBAR, CreativeModeTabs.OP_BLOCKS).stream().map(BuiltInRegistries.CREATIVE_MODE_TAB::getValueOrThrow).collect(Collectors.toSet());

    public static int getPageCount() {
        return (int)Math.ceil((double)((long)CreativeModeTabs.tabs().size() - COMMON_TABS.stream().filter(CreativeModeTab::shouldDisplay).count()) / 10.0);
    }

    public static enum Type {
        NEXT(Component.literal(">"), FabricCreativeModeInventoryScreen::switchToNextPage, screen -> screen.getCurrentPage() + 1 < screen.getPageCount()),
        PREVIOUS(Component.literal("<"), FabricCreativeModeInventoryScreen::switchToPreviousPage, screen -> screen.getCurrentPage() != 0);

        final Component component;
        final Consumer<CreativeModeInventoryScreen> clickConsumer;
        final Predicate<CreativeModeInventoryScreen> isEnabled;

        private Type(Component component, Consumer<CreativeModeInventoryScreen> clickConsumer, Predicate<CreativeModeInventoryScreen> isEnabled) {
            this.component = component;
            this.clickConsumer = clickConsumer;
            this.isEnabled = isEnabled;
        }
    }

    public static class CreativeModeTabButton
    extends Button {
        final CreativeModeInventoryScreen screen;
        final Type type;

        public CreativeModeTabButton(int x, int y, Type type, CreativeModeInventoryScreen screen) {
            super(x, y, 10, 12, type.component, bw -> type.clickConsumer.accept(screen), Button.DEFAULT_NARRATION);
            this.type = type;
            this.screen = screen;
        }

        @Override
        protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            this.active = this.type.isEnabled.test(this.screen);
            this.visible = this.screen.hasAdditionalPages();
            if (!this.visible) {
                return;
            }
            int u = this.active && this.isHovered() ? 20 : 0;
            int v = this.active ? 0 : 12;
            graphics.blit(RenderPipelines.GUI_TEXTURED, BUTTON_TEX, this.getX(), this.getY(), (float)(u + (this.type == Type.NEXT ? 10 : 0)), (float)v, 10, 12, 256, 256);
            if (this.isHovered()) {
                graphics.setTooltipForNextFrame(Minecraft.getInstance().font, Component.translatable("fabric.gui.creativeTabPage", this.screen.getCurrentPage() + 1, FabricCreativeGuiComponents.getPageCount()), mouseX, mouseY);
            }
        }
    }
}

