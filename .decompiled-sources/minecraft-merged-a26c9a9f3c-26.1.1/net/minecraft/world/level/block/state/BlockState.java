/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block.state;

import com.mojang.serialization.Codec;
import net.fabricmc.fabric.api.block.v1.FabricBlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;

public class BlockState
extends BlockBehaviour.BlockStateBase
implements FabricBlockState {
    public static final Codec<BlockState> CODEC = BlockState.codec(BuiltInRegistries.BLOCK.byNameCodec(), Block::defaultBlockState, Block::getStateDefinition).stable();

    public BlockState(Block owner, Property<?>[] propertyKeys, Comparable<?>[] propertyValues) {
        super(owner, propertyKeys, propertyValues);
    }

    @Override
    protected BlockState asState() {
        return this;
    }
}

