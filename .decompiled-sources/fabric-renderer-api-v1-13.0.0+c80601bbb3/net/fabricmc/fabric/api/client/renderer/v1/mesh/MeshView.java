/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.renderer.v1.mesh;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import org.jetbrains.annotations.Range;

public interface MeshView {
    public @Range(from=0L, to=0x7FFFFFFFL) int size();

    public void forEach(Consumer<? super QuadView> var1);

    public void outputTo(QuadEmitter var1);
}

