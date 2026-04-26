/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import java.util.List;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.object.builder.v1.entity.MinecartComparatorLogicRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DetectorRailBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DetectorRailBlock.class})
public abstract class DetectorRailBlockMixin {
    @Shadow
    protected abstract <T extends AbstractMinecart> List<T> getInteractingMinecartOfType(Level var1, BlockPos var2, Class<T> var3, Predicate<Entity> var4);

    @Inject(at={@At(value="HEAD")}, method={"getAnalogOutputSignal"}, cancellable=true)
    private void getCustomComparatorOutput(BlockState state, Level level, BlockPos pos, Direction direction, CallbackInfoReturnable<Integer> cir) {
        if (state.getValue(DetectorRailBlock.POWERED).booleanValue()) {
            List<AbstractMinecart> carts = this.getInteractingMinecartOfType(level, pos, AbstractMinecart.class, cart -> MinecartComparatorLogicRegistry.getCustomComparatorLogic(cart.getType()) != null);
            for (AbstractMinecart cart2 : carts) {
                int comparatorValue = MinecartComparatorLogicRegistry.getCustomComparatorLogic(cart2.getType()).getComparatorValue(cart2, state, pos);
                if (comparatorValue < 0) continue;
                cir.setReturnValue(comparatorValue);
                break;
            }
        }
    }
}

