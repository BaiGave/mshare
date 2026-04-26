/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.renderer.block.model;

import java.util.List;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.block.dispatch.multipart.MultiPartModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={MultiPartModel.SharedBakedState.class})
public interface MultiPartModelSharedBakedStateAccessor {
    @Accessor(value="selectors")
    public List<MultiPartModel.Selector<BlockStateModel>> getSelectors();
}

