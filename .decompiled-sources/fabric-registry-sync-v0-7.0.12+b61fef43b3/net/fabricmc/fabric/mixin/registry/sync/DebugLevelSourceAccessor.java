/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.DebugLevelSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={DebugLevelSource.class})
public interface DebugLevelSourceAccessor {
    @Accessor
    @Mutable
    public static void setALL_BLOCKS(List<BlockState> blockStates) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    @Mutable
    public static void setGRID_WIDTH(int length) {
        throw new UnsupportedOperationException();
    }

    @Accessor
    @Mutable
    public static void setGRID_HEIGHT(int length) {
        throw new UnsupportedOperationException();
    }
}

