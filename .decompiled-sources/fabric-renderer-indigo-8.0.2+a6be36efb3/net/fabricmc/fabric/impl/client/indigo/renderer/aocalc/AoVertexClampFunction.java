/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.fabricmc.fabric.impl.client.indigo.Indigo;

@FunctionalInterface
interface AoVertexClampFunction {
    public static final AoVertexClampFunction CLAMP_FUNC = Indigo.FIX_EXTERIOR_VERTEX_LIGHTING ? x -> x < 0.0f ? 0.0f : (x > 1.0f ? 1.0f : x) : x -> x;

    public float clamp(float var1);
}

