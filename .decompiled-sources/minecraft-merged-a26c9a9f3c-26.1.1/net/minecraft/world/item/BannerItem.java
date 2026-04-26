/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.item;

import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.Validate;

public class BannerItem
extends StandingAndWallBlockItem {
    public BannerItem(Block block, Block wallBlock, Item.Properties properties) {
        super(block, wallBlock, Direction.DOWN, properties);
        Validate.isInstanceOf(AbstractBannerBlock.class, block);
        Validate.isInstanceOf(AbstractBannerBlock.class, wallBlock);
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }
}

