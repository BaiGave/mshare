/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import net.fabricmc.fabric.api.client.renderer.v1.mesh.MutableQuadView;

@FunctionalInterface
public interface QuadTransform {
    public boolean transform(MutableQuadView var1);
}

