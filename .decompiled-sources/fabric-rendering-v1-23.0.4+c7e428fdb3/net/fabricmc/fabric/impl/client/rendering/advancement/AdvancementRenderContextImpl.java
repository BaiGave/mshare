/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.advancement;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderContext;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jspecify.annotations.Nullable;

public final class AdvancementRenderContextImpl {
    private AdvancementRenderContextImpl() {
    }

    public record BackgroundImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, ScreenRectangle bounds, double scrollX, double scrollY) implements AdvancementRenderContext.Background
    {
    }

    public record FrameImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, int x, int y, boolean isHovered) implements AdvancementRenderContext.Frame
    {
    }

    public static final class IconImpl
    implements AdvancementRenderContext.Icon {
        private final GuiGraphicsExtractor graphics;
        private final AdvancementHolder holder;
        private final @Nullable AdvancementProgress progress;
        private int x;
        private int y;
        private final boolean hovered;
        private final boolean selected;

        public IconImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, int x, int y, boolean hovered, boolean selected) {
            this.graphics = graphics;
            this.holder = holder;
            this.progress = progress;
            this.x = x;
            this.y = y;
            this.hovered = hovered;
            this.selected = selected;
        }

        public IconImpl(GuiGraphicsExtractor graphics, AdvancementHolder holder, @Nullable AdvancementProgress progress, boolean hovered, boolean selected) {
            this(graphics, holder, progress, 0, 0, hovered, selected);
        }

        @Override
        public GuiGraphicsExtractor graphics() {
            return this.graphics;
        }

        @Override
        public AdvancementHolder holder() {
            return this.holder;
        }

        @Override
        public @Nullable AdvancementProgress progress() {
            return this.progress;
        }

        @Override
        public int x() {
            return this.x;
        }

        @Override
        public int y() {
            return this.y;
        }

        @Override
        public boolean isHovered() {
            return this.hovered;
        }

        @Override
        public boolean isSelected() {
            return this.selected;
        }

        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }
}

