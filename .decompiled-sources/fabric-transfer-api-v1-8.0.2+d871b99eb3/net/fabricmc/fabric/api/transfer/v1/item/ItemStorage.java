/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.transfer.v1.item;

import java.util.List;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.item.ItemApiLookup;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.CombinedSlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.impl.transfer.item.BundleContentsStorage;
import net.fabricmc.fabric.impl.transfer.item.ComposterWrapper;
import net.fabricmc.fabric.impl.transfer.item.ItemContainerContentsStorage;
import net.fabricmc.fabric.mixin.transfer.CompoundContainerAccessor;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.WorldlyContainerHolder;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import org.jspecify.annotations.Nullable;

public final class ItemStorage {
    public static final BlockApiLookup<Storage<ItemVariant>, @Nullable Direction> SIDED = BlockApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "sided_item_storage"), Storage.asClass(), Direction.class);
    public static final ItemApiLookup<Storage<ItemVariant>, ContainerItemContext> ITEM = ItemApiLookup.get(Identifier.fromNamespaceAndPath("fabric", "item_storage"), Storage.asClass(), ContainerItemContext.class);

    private ItemStorage() {
    }

    static {
        SIDED.registerForBlocks((level, pos, state, blockEntity, direction) -> ComposterWrapper.get(level, pos, direction), Blocks.COMPOSTER);
        SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            if (blockEntity instanceof SidedStorageBlockEntity) {
                SidedStorageBlockEntity sidedStorageBlockEntity = (SidedStorageBlockEntity)((Object)blockEntity);
                return sidedStorageBlockEntity.getItemStorage((Direction)direction);
            }
            return null;
        });
        SIDED.registerFallback((level, pos, state, blockEntity, direction) -> {
            WorldlyContainer second;
            WorldlyContainerHolder provider;
            WorldlyContainer first;
            Container containerToWrap = null;
            Block patt0$temp = state.getBlock();
            if (patt0$temp instanceof WorldlyContainerHolder && (first = (provider = (WorldlyContainerHolder)((Object)patt0$temp)).getContainer(state, level, pos)) == (second = provider.getContainer(state, level, pos)) && first != null) {
                return ContainerStorage.of(first, direction);
            }
            if (blockEntity instanceof Container) {
                Block patt1$temp;
                Container container = (Container)((Object)blockEntity);
                if (blockEntity instanceof ChestBlockEntity && (patt1$temp = state.getBlock()) instanceof ChestBlock) {
                    ChestBlock chestBlock = (ChestBlock)patt1$temp;
                    containerToWrap = ChestBlock.getContainer(chestBlock, state, level, pos, true);
                    if (containerToWrap instanceof CompoundContainerAccessor) {
                        CompoundContainerAccessor accessor = (CompoundContainerAccessor)((Object)containerToWrap);
                        ContainerStorage first2 = ContainerStorage.of(accessor.fabric_getContainer1(), direction);
                        ContainerStorage second2 = ContainerStorage.of(accessor.fabric_getContainer2(), direction);
                        return new CombinedSlottedStorage(List.of(first2, second2));
                    }
                } else {
                    containerToWrap = container;
                }
            }
            return containerToWrap != null ? ContainerStorage.of(containerToWrap, direction) : null;
        });
        ITEM.registerForItems((itemStack, context) -> new ItemContainerContentsStorage((ContainerItemContext)context, 27), Items.SHULKER_BOX, Items.WHITE_SHULKER_BOX, Items.ORANGE_SHULKER_BOX, Items.MAGENTA_SHULKER_BOX, Items.LIGHT_BLUE_SHULKER_BOX, Items.YELLOW_SHULKER_BOX, Items.LIME_SHULKER_BOX, Items.PINK_SHULKER_BOX, Items.GRAY_SHULKER_BOX, Items.LIGHT_GRAY_SHULKER_BOX, Items.CYAN_SHULKER_BOX, Items.PURPLE_SHULKER_BOX, Items.BLUE_SHULKER_BOX, Items.BROWN_SHULKER_BOX, Items.GREEN_SHULKER_BOX, Items.RED_SHULKER_BOX, Items.BLACK_SHULKER_BOX);
        ITEM.registerForItems((itemStack, context) -> new BundleContentsStorage((ContainerItemContext)context), Items.BUNDLE, Items.WHITE_BUNDLE, Items.ORANGE_BUNDLE, Items.MAGENTA_BUNDLE, Items.LIGHT_BLUE_BUNDLE, Items.YELLOW_BUNDLE, Items.LIME_BUNDLE, Items.PINK_BUNDLE, Items.GRAY_BUNDLE, Items.LIGHT_GRAY_BUNDLE, Items.CYAN_BUNDLE, Items.PURPLE_BUNDLE, Items.BLUE_BUNDLE, Items.BROWN_BUNDLE, Items.GREEN_BUNDLE, Items.RED_BUNDLE, Items.BLACK_BUNDLE);
    }
}

