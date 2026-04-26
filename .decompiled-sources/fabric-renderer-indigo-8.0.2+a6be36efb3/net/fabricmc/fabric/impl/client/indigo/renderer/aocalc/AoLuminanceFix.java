/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.indigo.renderer.aocalc;

import net.fabricmc.fabric.impl.client.indigo.Indigo;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

@FunctionalInterface
public interface AoLuminanceFix {
    public static final AoLuminanceFix INSTANCE = Indigo.FIX_LUMINOUS_AO_SHADE ? AoLuminanceFix::fixed : AoLuminanceFix::vanilla;

    public float apply(BlockGetter var1, BlockPos var2, BlockState var3);

    public static float vanilla(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getShadeBrightness(level, pos);
    }

    public static float fixed(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getLightEmission() == 0 ? state.getShadeBrightness(level, pos) : 1.0f;
    }
}

