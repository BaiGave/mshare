/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.renderer;

import java.util.function.Consumer;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

public class QuadToPosPipe
implements Consumer<QuadView> {
    private final Consumer<Vector3fc> posConsumer;
    private final Vector3f vec;
    public Matrix4fc matrix;

    public QuadToPosPipe(Consumer<Vector3fc> posConsumer, Vector3f vec) {
        this.posConsumer = posConsumer;
        this.vec = vec;
    }

    @Override
    public void accept(QuadView quad) {
        for (int i = 0; i < 4; ++i) {
            this.posConsumer.accept(quad.copyPos(i, this.vec).mulPosition(this.matrix));
        }
    }
}

