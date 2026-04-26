/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.block;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public class DropExperienceBlock
extends Block {
    public static final MapCodec<DropExperienceBlock> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)IntProviders.codec(0, 10).fieldOf("experience")).forGetter(b -> b.xpRange), DropExperienceBlock.propertiesCodec()).apply((Applicative<DropExperienceBlock, ?>)i, DropExperienceBlock::new));
    private final IntProvider xpRange;

    public MapCodec<? extends DropExperienceBlock> codec() {
        return CODEC;
    }

    public DropExperienceBlock(IntProvider xpRange, BlockBehaviour.Properties properties) {
        super(properties);
        this.xpRange = xpRange;
    }

    @Override
    protected void spawnAfterBreak(BlockState state, ServerLevel level, BlockPos pos, ItemStack tool, boolean dropExperience) {
        super.spawnAfterBreak(state, level, pos, tool, dropExperience);
        if (dropExperience) {
            this.tryDropExperience(level, pos, tool, this.xpRange);
        }
    }
}

