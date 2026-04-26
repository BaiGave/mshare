/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class PlayerHeadBlock
extends SkullBlock {
    public static final MapCodec<PlayerHeadBlock> CODEC = PlayerHeadBlock.simpleCodec(PlayerHeadBlock::new);

    public MapCodec<PlayerHeadBlock> codec() {
        return CODEC;
    }

    public PlayerHeadBlock(BlockBehaviour.Properties properties) {
        super(SkullBlock.Types.PLAYER, properties);
    }
}

