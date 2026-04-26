/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.pipeline;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class TextureTarget
extends RenderTarget {
    public TextureTarget(@Nullable String label, int width, int height, boolean useDepth) {
        super(label, useDepth);
        RenderSystem.assertOnRenderThread();
        this.resize(width, height);
    }
}

