/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.block;

import net.fabricmc.fabric.api.block.v1.FabricBlock;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={Block.class})
public class BlockMixin
implements FabricBlock {
}

