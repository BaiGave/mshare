/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.blockgetter;

import net.fabricmc.fabric.api.blockgetter.v2.RenderDataBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockEntity.class})
public abstract class BlockEntityMixin
implements RenderDataBlockEntity {
}

