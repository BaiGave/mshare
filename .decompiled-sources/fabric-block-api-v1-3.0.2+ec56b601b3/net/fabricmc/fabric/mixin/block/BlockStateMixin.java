/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.block;

import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value={BlockState.class})
public class BlockStateMixin
implements FabricBlockState {
}

