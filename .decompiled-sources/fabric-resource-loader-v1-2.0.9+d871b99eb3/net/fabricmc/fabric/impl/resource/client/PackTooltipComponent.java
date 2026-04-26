/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.resource.client;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record PackTooltipComponent(Optional<Component> name, Optional<List<FormattedCharSequence>> description) implements TooltipComponent,
ClientTooltipComponent
{
    @Override
    public int getHeight(Font font) {
        int height = 0;
        if (this.name.isPresent()) {
            height += font.lineHeight + 2;
        }
        if (this.description.isPresent()) {
            height += this.description.get().size() * font.lineHeight + 3;
        }
        if (this.name.isPresent() && this.description.isPresent()) {
            height += font.lineHeight;
        }
        return height;
    }

    @Override
    public int getWidth(Font font) {
        return Math.max(this.name.map(font::width).orElse(0), this.description.map(description -> description.stream().mapToInt(font::width).max().orElse(0)).orElse(0));
    }

    @Override
    public void extractText(GuiGraphicsExtractor graphics, Font font, int x, int y) {
        if (this.name.isPresent()) {
            graphics.text(font, this.name.get(), x, y, -1, true);
            y += font.lineHeight + 1;
            if (this.description.isPresent()) {
                y += font.lineHeight;
            }
        }
        if (this.description.isPresent()) {
            for (FormattedCharSequence line : this.description.get()) {
                graphics.text(font, line, x, y, -1, true);
                y += font.lineHeight + 1;
            }
        }
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics) {
        if (this.name.isPresent() && this.description.isPresent()) {
            graphics.fill(x, y + font.lineHeight + 4, x + this.getWidth(font), y + font.lineHeight + 5, 0xFF000000 | ChatFormatting.GRAY.getColor());
        }
    }
}

