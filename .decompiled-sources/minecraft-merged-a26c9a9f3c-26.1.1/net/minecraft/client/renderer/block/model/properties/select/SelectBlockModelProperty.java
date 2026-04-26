/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.renderer.block.model.properties.select;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public interface SelectBlockModelProperty<T> {
    public @Nullable T get(BlockState var1, BlockDisplayContext var2);
}

