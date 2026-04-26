/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1.wrapper;

import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.resources.model.geometry.UnbakedGeometry;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public abstract class WrapperUnbakedModel
implements UnbakedModel {
    protected UnbakedModel wrapped;

    protected WrapperUnbakedModel() {
    }

    protected WrapperUnbakedModel(UnbakedModel wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public @Nullable Boolean ambientOcclusion() {
        return this.wrapped.ambientOcclusion();
    }

    @Override
    public @Nullable UnbakedModel.GuiLight guiLight() {
        return this.wrapped.guiLight();
    }

    @Override
    public @Nullable ItemTransforms transforms() {
        return this.wrapped.transforms();
    }

    @Override
    public TextureSlots.Data textureSlots() {
        return this.wrapped.textureSlots();
    }

    @Override
    public @Nullable UnbakedGeometry geometry() {
        return this.wrapped.geometry();
    }

    @Override
    public @Nullable Identifier parent() {
        return this.wrapped.parent();
    }
}

