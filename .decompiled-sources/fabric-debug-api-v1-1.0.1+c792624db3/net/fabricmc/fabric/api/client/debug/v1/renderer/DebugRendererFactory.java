/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.debug.v1.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.debug.DebugRenderer;

@FunctionalInterface
public interface DebugRendererFactory {
    public DebugRenderer.SimpleDebugRenderer create(Minecraft var1);
}

