/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import java.util.Objects;
import net.fabricmc.fabric.impl.client.rendering.PictureInPictureRendererRegistryImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.ApiStatus;

public final class PictureInPictureRendererRegistry {
    public static void register(Factory factory) {
        Objects.requireNonNull(factory, "factory");
        PictureInPictureRendererRegistryImpl.register(factory);
    }

    @FunctionalInterface
    public static interface Factory {
        public PictureInPictureRenderer<?> createRenderer(Context var1);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public MultiBufferSource.BufferSource bufferSource();

        public Minecraft minecraft();

        public SubmitNodeCollector submitNodeCollector();
    }
}

