/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import java.util.Map;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={AxeItem.class})
public interface AxeItemAccessor {
    @Accessor(value="STRIPPABLES")
    public static Map<Block, Block> getStrippables() {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }

    @Accessor(value="STRIPPABLES")
    @Mutable
    public static void setStrippables(Map<Block, Block> strippedBlocks) {
        throw new AssertionError((Object)"Untransformed @Accessor");
    }
}

