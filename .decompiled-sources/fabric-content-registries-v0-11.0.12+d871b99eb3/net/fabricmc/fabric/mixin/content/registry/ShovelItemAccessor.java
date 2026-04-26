/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ShovelItem.class})
public interface ShovelItemAccessor {
    @Accessor(value="FLATTENABLES")
    public static Map<Block, BlockState> getFlattenables() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

