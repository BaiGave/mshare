/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.render;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;

public interface FabricLayerRenderState {
    default public QuadEmitter emitter() {
        throw new AssertionError((Object)"Implemented in mixin.");
    }
}

