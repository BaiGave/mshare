/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ContainerStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.impl.transfer.TransferApiImpl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DropperBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={DropperBlock.class})
public class DropperBlockMixin {
    @Inject(at={@At(value="INVOKE", target="Lnet/minecraft/core/dispenser/DispenseItemBehavior;dispense(Lnet/minecraft/core/dispenser/BlockSource;Lnet/minecraft/world/item/ItemStack;)Lnet/minecraft/world/item/ItemStack;")}, method={"dispenseFrom"}, cancellable=true, allow=1)
    public void hookDispense(ServerLevel level, BlockState blockState, BlockPos pos, CallbackInfo ci) {
        DispenserBlockEntity dispenser = (DispenserBlockEntity)level.getBlockEntity(pos);
        Direction direction = dispenser.getBlockState().getValue(DispenserBlock.FACING);
        Storage<ItemVariant> target = ItemStorage.SIDED.find(level, pos.relative(direction), direction.getOpposite());
        if (target != null) {
            ci.cancel();
            int slot = dispenser.getRandomSlot(level.getRandom());
            if (slot == -1) {
                TransferApiImpl.LOGGER.warn("Skipping dropper transfer because the empty slot is unexpectedly -1.");
                return;
            }
            StorageUtil.move(ContainerStorage.of(dispenser, null).getSlot(slot), target, k -> true, 1L, null);
        }
    }
}

