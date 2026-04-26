/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.advancement;

import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderContext;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRendererRegistryImpl;
import net.minecraft.resources.Identifier;

public final class AdvancementRenderer {
    public static void registerIcon(IconRenderer iconRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerIcon(iconRenderer, advancementIds);
    }

    public static void registerFrame(FrameRenderer frameRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerFrame(frameRenderer, advancementIds);
    }

    public static void registerBackground(BackgroundRenderer backgroundRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerBackground(backgroundRenderer, advancementIds);
    }

    private AdvancementRenderer() {
    }

    @FunctionalInterface
    public static interface IconRenderer {
        public void extractAdvancementIcon(AdvancementRenderContext.Icon var1);

        default public boolean shouldRenderOriginalIcon() {
            return false;
        }
    }

    @FunctionalInterface
    public static interface FrameRenderer {
        public void extractAdvancementFrame(AdvancementRenderContext.Frame var1);

        default public boolean shouldRenderOriginalFrame() {
            return false;
        }

        default public boolean shouldRenderTooltip() {
            return true;
        }
    }

    @FunctionalInterface
    public static interface BackgroundRenderer {
        public void extractAdvancementBackground(AdvancementRenderContext.Background var1);

        default public boolean shouldRenderOriginalBackground() {
            return false;
        }
    }
}

