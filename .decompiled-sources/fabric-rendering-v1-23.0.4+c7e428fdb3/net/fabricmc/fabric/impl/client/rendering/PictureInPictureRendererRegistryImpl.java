/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.rendering;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.fabric.api.client.rendering.v1.PictureInPictureRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.GuiBannerResultRenderer;
import net.minecraft.client.gui.render.pip.GuiBookModelRenderer;
import net.minecraft.client.gui.render.pip.GuiEntityRenderer;
import net.minecraft.client.gui.render.pip.GuiProfilerChartRenderer;
import net.minecraft.client.gui.render.pip.GuiSignRenderer;
import net.minecraft.client.gui.render.pip.GuiSkinRenderer;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.gui.pip.GuiBannerResultRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiBookModelRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiEntityRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiProfilerChartRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiSignRenderState;
import net.minecraft.client.renderer.state.gui.pip.GuiSkinRenderState;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.jetbrains.annotations.VisibleForTesting;
import org.jspecify.annotations.Nullable;

public final class PictureInPictureRendererRegistryImpl {
    private static final List<PictureInPictureRendererRegistry.Factory> FACTORIES = new ArrayList<PictureInPictureRendererRegistry.Factory>();
    private static final Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererRegistry.Factory> REGISTERED_FACTORIES = new HashMap<Class<? extends PictureInPictureRenderState>, PictureInPictureRendererRegistry.Factory>();
    private static boolean frozen;

    private PictureInPictureRendererRegistryImpl() {
    }

    public static void register(PictureInPictureRendererRegistry.Factory factory) {
        if (frozen) {
            throw new IllegalStateException("Too late to register, GuiRenderer has already been initialized.");
        }
        FACTORIES.add(factory);
    }

    public static void onReady(Minecraft client, MultiBufferSource.BufferSource immediate, SubmitNodeCollector submitNodeCollector, Map<Class<? extends PictureInPictureRenderState>, PictureInPictureRenderer<?>> specialElementRenderers) {
        frozen = true;
        PictureInPictureRendererRegistryImpl.registerVanillaFactories();
        ContextImpl context = new ContextImpl(client, immediate, submitNodeCollector);
        for (PictureInPictureRendererRegistry.Factory factory : FACTORIES) {
            PictureInPictureRenderer<?> elementRenderer = factory.createRenderer(context);
            specialElementRenderers.put(elementRenderer.getRenderStateClass(), elementRenderer);
            REGISTERED_FACTORIES.put(elementRenderer.getRenderStateClass(), factory);
        }
    }

    public static <S extends PictureInPictureRenderState> @Nullable PictureInPictureRenderer<S> createNewRenderer(S state, Minecraft client, MultiBufferSource.BufferSource immediate, SubmitNodeCollector submitNodeCollector) {
        PictureInPictureRendererRegistry.Factory factory = REGISTERED_FACTORIES.get(state.getClass());
        return factory == null ? null : factory.createRenderer(new ContextImpl(client, immediate, submitNodeCollector));
    }

    private static void registerVanillaFactories() {
        REGISTERED_FACTORIES.put(GuiEntityRenderState.class, context -> new GuiEntityRenderer(context.bufferSource(), context.minecraft().getEntityRenderDispatcher()));
        REGISTERED_FACTORIES.put(GuiSkinRenderState.class, context -> new GuiSkinRenderer(context.bufferSource()));
        REGISTERED_FACTORIES.put(GuiBookModelRenderState.class, context -> new GuiBookModelRenderer(context.bufferSource()));
        REGISTERED_FACTORIES.put(GuiBannerResultRenderState.class, context -> new GuiBannerResultRenderer(context.bufferSource(), context.minecraft().getAtlasManager()));
        REGISTERED_FACTORIES.put(GuiSignRenderState.class, context -> new GuiSignRenderer(context.bufferSource(), context.minecraft().getAtlasManager()));
        REGISTERED_FACTORIES.put(GuiProfilerChartRenderState.class, context -> new GuiProfilerChartRenderer(context.bufferSource()));
    }

    @VisibleForTesting
    public static Collection<Class<? extends PictureInPictureRenderState>> getRegisteredFactoryStateClasses() {
        return REGISTERED_FACTORIES.keySet();
    }

    record ContextImpl(Minecraft minecraft, MultiBufferSource.BufferSource bufferSource, SubmitNodeCollector submitNodeCollector) implements PictureInPictureRendererRegistry.Context
    {
    }
}

