/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.components;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.MultiLineTextWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ARGB;

@Environment(value=EnvType.CLIENT)
public class FocusableTextWidget
extends MultiLineTextWidget {
    public static final int DEFAULT_PADDING = 4;
    private final int padding;
    private final int maxWidth;
    private final boolean alwaysShowBorder;
    private final BackgroundFill backgroundFill;

    private FocusableTextWidget(Component message, Font font, int padding, int maxWidth, BackgroundFill backgroundFill, boolean alwaysShowBorder) {
        super(message, font);
        this.active = true;
        this.padding = padding;
        this.maxWidth = maxWidth;
        this.alwaysShowBorder = alwaysShowBorder;
        this.backgroundFill = backgroundFill;
        this.updateWidth();
        this.updateHeight();
        this.setCentered(true);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, this.getMessage());
    }

    @Override
    public void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        int borderColor = this.alwaysShowBorder && !this.isFocused() ? ARGB.color(this.alpha, -6250336) : ARGB.white(this.alpha);
        switch (this.backgroundFill.ordinal()) {
            case 0: {
                graphics.fill(this.getX() + 1, this.getY(), this.getRight(), this.getBottom(), ARGB.black(this.alpha));
                break;
            }
            case 1: {
                if (!this.isFocused()) break;
                graphics.fill(this.getX() + 1, this.getY(), this.getRight(), this.getBottom(), ARGB.black(this.alpha));
                break;
            }
        }
        if (this.isFocused() || this.alwaysShowBorder) {
            graphics.outline(this.getX(), this.getY(), this.getWidth(), this.getHeight(), borderColor);
        }
        super.extractWidgetRenderState(graphics, mouseX, mouseY, a);
    }

    @Override
    protected int getTextX() {
        return this.getX() + this.padding;
    }

    @Override
    protected int getTextY() {
        return super.getTextY() + this.padding;
    }

    @Override
    public MultiLineTextWidget setMaxWidth(int maxWidth) {
        return super.setMaxWidth(maxWidth - this.padding * 2);
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    public int getPadding() {
        return this.padding;
    }

    public void updateWidth() {
        if (this.maxWidth != -1) {
            this.setWidth(this.maxWidth);
            this.setMaxWidth(this.maxWidth);
        } else {
            this.setWidth(this.getFont().width(this.getMessage()) + this.padding * 2);
        }
    }

    public void updateHeight() {
        int textHeight = this.getFont().lineHeight * this.getFont().split(this.getMessage(), super.getWidth()).size();
        this.setHeight(textHeight + this.padding * 2);
    }

    @Override
    public void setMessage(Component message) {
        this.message = message;
        int width = this.maxWidth != -1 ? this.maxWidth : this.getFont().width(message) + this.padding * 2;
        this.setWidth(width);
        this.updateHeight();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
    }

    public static Builder builder(Component message, Font font) {
        return new Builder(message, font);
    }

    public static Builder builder(Component message, Font font, int padding) {
        return new Builder(message, font, padding);
    }

    @Environment(value=EnvType.CLIENT)
    public static enum BackgroundFill {
        ALWAYS,
        ON_FOCUS,
        NEVER;

    }

    @Environment(value=EnvType.CLIENT)
    public static class Builder {
        private final Component message;
        private final Font font;
        private final int padding;
        private int maxWidth = -1;
        private boolean alwaysShowBorder = true;
        private BackgroundFill backgroundFill = BackgroundFill.ALWAYS;

        private Builder(Component message, Font font) {
            this(message, font, 4);
        }

        private Builder(Component message, Font font, int padding) {
            this.message = message;
            this.font = font;
            this.padding = padding;
        }

        public Builder maxWidth(int maxWidth) {
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder textWidth(int textWidth) {
            this.maxWidth = textWidth + this.padding * 2;
            return this;
        }

        public Builder alwaysShowBorder(boolean alwaysShowBorder) {
            this.alwaysShowBorder = alwaysShowBorder;
            return this;
        }

        public Builder backgroundFill(BackgroundFill backgroundFill) {
            this.backgroundFill = backgroundFill;
            return this;
        }

        public FocusableTextWidget build() {
            return new FocusableTextWidget(this.message, this.font, this.padding, this.maxWidth, this.backgroundFill, this.alwaysShowBorder);
        }
    }
}

