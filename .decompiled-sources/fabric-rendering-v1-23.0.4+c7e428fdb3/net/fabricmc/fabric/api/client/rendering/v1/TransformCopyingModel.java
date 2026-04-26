/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.rendering.v1;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.impl.client.rendering.ModelExtensions;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import org.jspecify.annotations.Nullable;

public final class TransformCopyingModel<S, D>
extends Model<Pair<S, D>>
implements ModelExtensions {
    private final Model<? super S> source;
    private final Model<? super D> delegate;
    private final boolean setDelegateAngles;

    public static <S, D> TransformCopyingModel<S, D> create(Model<? super S> source, Model<? super D> delegate, boolean setDelegateAngles) {
        return new TransformCopyingModel<S, D>(source, delegate, setDelegateAngles);
    }

    private TransformCopyingModel(Model<? super S> source, Model<? super D> delegate, boolean setDelegateAngles) {
        super(delegate.root(), delegate::renderType);
        this.source = source;
        this.delegate = delegate;
        this.setDelegateAngles = setDelegateAngles;
    }

    @Override
    public void setupAnim(Pair<S, D> state) {
        this.resetPose();
        this.source.setupAnim(state.getFirst());
        this.delegate.copyTransforms(this.source);
        if (this.setDelegateAngles) {
            this.delegate.setupAnim(state.getSecond());
        }
    }

    @Override
    public void fabric$calculateChildParts(ModelPart root) {
    }

    @Override
    public @Nullable ModelPart getChildPart(String name) {
        return this.delegate.getChildPart(name);
    }

    @Override
    public void copyTransforms(Model<?> model) {
        this.delegate.copyTransforms(model);
    }
}

