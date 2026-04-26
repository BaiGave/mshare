/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering.advancement;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.fabricmc.fabric.api.client.rendering.v1.advancement.AdvancementRenderer;
import net.fabricmc.fabric.impl.client.rendering.advancement.AdvancementRenderContextImpl;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public final class AdvancementRendererRegistryImpl {
    public static final ScopedValue<AdvancementRenderContextImpl.IconImpl> TAB_ICON_RENDER_CONTEXT = ScopedValue.newInstance();
    private static final Map<Identifier, AdvancementRenderer.IconRenderer> ICONS = new HashMap<Identifier, AdvancementRenderer.IconRenderer>();
    private static final Map<Identifier, AdvancementRenderer.FrameRenderer> FRAMES = new HashMap<Identifier, AdvancementRenderer.FrameRenderer>();
    private static final Map<Identifier, AdvancementRenderer.BackgroundRenderer> BACKGROUNDS = new HashMap<Identifier, AdvancementRenderer.BackgroundRenderer>();

    public static void registerIcon(AdvancementRenderer.IconRenderer iconRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerRenderer("Icon", ICONS, iconRenderer, advancementIds);
    }

    public static void registerFrame(AdvancementRenderer.FrameRenderer frameRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerRenderer("Frame", FRAMES, frameRenderer, advancementIds);
    }

    public static void registerBackground(AdvancementRenderer.BackgroundRenderer backgroundRenderer, Identifier ... advancementIds) {
        AdvancementRendererRegistryImpl.registerRenderer("Background", BACKGROUNDS, backgroundRenderer, advancementIds);
    }

    public static @Nullable AdvancementRenderer.IconRenderer getIconRenderer(Identifier advancementId) {
        return ICONS.get(advancementId);
    }

    public static @Nullable AdvancementRenderer.FrameRenderer getFrameRenderer(Identifier advancementId) {
        return FRAMES.get(advancementId);
    }

    public static @Nullable AdvancementRenderer.BackgroundRenderer getBackgroundRenderer(Identifier advancementId) {
        return BACKGROUNDS.get(advancementId);
    }

    private static <T> void registerRenderer(String type, Map<Identifier, T> renderers, T renderer, Identifier ... advancementIds) {
        Objects.requireNonNull(renderer, type + " renderer is null");
        if (advancementIds.length == 0) {
            throw new IllegalArgumentException(type + " advancement renderer registered for no advancements");
        }
        for (Identifier advancementId : advancementIds) {
            Objects.requireNonNull(advancementId, " advancement id is null");
            if (renderers.putIfAbsent(advancementId, renderer) == null) continue;
            throw new IllegalArgumentException(type + " advancement renderer already exists for " + String.valueOf(advancementId));
        }
    }

    private AdvancementRendererRegistryImpl() {
    }
}

