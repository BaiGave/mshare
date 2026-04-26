/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.advancement;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

@ApiStatus.NonExtendable
public sealed interface AdvancementRenderContext {
    public GuiGraphicsExtractor graphics();

    public AdvancementHolder holder();

    public @Nullable AdvancementProgress progress();

    default public Advancement advancement() {
        return this.holder().value();
    }

    default public DisplayInfo display() {
        return this.advancement().display().orElseThrow();
    }

    default public boolean isObtained() {
        AdvancementProgress progress = this.progress();
        return progress != null && progress.getPercent() >= 1.0f;
    }

    @ApiStatus.NonExtendable
    public static non-sealed interface Background
    extends AdvancementRenderContext {
        public ScreenRectangle bounds();

        public double scrollX();

        public double scrollY();
    }

    @ApiStatus.NonExtendable
    public static non-sealed interface Frame
    extends AdvancementRenderContext {
        public int x();

        public int y();

        public boolean isHovered();
    }

    @ApiStatus.NonExtendable
    public static non-sealed interface Icon
    extends AdvancementRenderContext {
        public int x();

        public int y();

        public boolean isHovered();

        public boolean isSelected();
    }
}

