/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1.level;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelTerrainRenderContext;
import net.fabricmc.fabric.impl.client.rendering.LevelRenderContextBackwardsCompatHack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface LevelRenderContext
extends LevelTerrainRenderContext,
LevelRenderContextBackwardsCompatHack {
    public SubmitNodeCollector submitNodeCollector();

    public PoseStack poseStack();

    @Override
    public MultiBufferSource.BufferSource bufferSource();
}

