/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CrafterBlock.class})
public class CrafterBlockMixin {
    @Inject(method={"dispenseItem"}, at={@At(value="INVOKE", target="Lnet/minecraft/world/item/ItemStack;isEmpty()Z")})
    private void transferOrSpawnStack(ServerLevel level, BlockPos pos, CrafterBlockEntity blockEntity, ItemStack inputStack, BlockState state, RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci, @Local(name={"direction"}) Direction direction, @Local(name={"into"}) Container into, @Local(name={"remaining"}) ItemStack remaining) {
        if (into != null) {
            return;
        }
        if (remaining.isEmpty()) {
            return;
        }
        Storage<ItemVariant> target = ItemStorage.SIDED.find(level, pos.relative(direction), direction.getOpposite());
        if (target != null) {
            try (Transaction transaction = Transaction.openOuter();){
                long moved = target.insert(ItemVariant.of(remaining), inputStack.getCount(), transaction);
                if (moved > 0L) {
                    remaining.shrink((int)moved);
                    transaction.commit();
                }
            }
        }
    }
}

