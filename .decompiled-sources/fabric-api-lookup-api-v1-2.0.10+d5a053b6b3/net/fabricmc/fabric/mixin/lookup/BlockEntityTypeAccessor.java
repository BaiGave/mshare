/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.lookup;

import java.util.Set;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={BlockEntityType.class})
public interface BlockEntityTypeAccessor {
    @Accessor(value="validBlocks")
    public Set<Block> getBlocks();
}

