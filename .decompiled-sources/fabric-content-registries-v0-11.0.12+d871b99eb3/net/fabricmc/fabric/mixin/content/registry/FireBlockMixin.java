/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.content.registry;

import net.fabricmc.fabric.api.registry.FlammableBlockRegistry;
import net.fabricmc.fabric.impl.content.registry.FireBlockHooks;
import net.fabricmc.fabric.impl.content.registry.FlammableBlockRegistryImpl;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={FireBlock.class})
public class FireBlockMixin
implements FireBlockHooks {
    @Unique
    private FlammableBlockRegistryImpl registry;

    @Shadow
    private int getBurnOdds(BlockState block_1) {
        return 0;
    }

    @Shadow
    private int getIgniteOdds(BlockState block_1) {
        return 0;
    }

    @Inject(at={@At(value="RETURN")}, method={"<init>"})
    private void afterConstruct(BlockBehaviour.Properties properties, CallbackInfo info) {
        this.registry = FlammableBlockRegistryImpl.getInstance((Block)((Object)this));
    }

    @Inject(at={@At(value="HEAD")}, method={"getIgniteOdds(Lnet/minecraft/world/level/block/state/BlockState;)I"}, cancellable=true)
    private void getFabricBurnChance(BlockState block, CallbackInfoReturnable info) {
        FlammableBlockRegistry.Entry entry = this.registry.getFabric(block.getBlock());
        if (entry != null) {
            if (block.hasProperty(BlockStateProperties.WATERLOGGED) && block.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
                info.setReturnValue(0);
            } else {
                info.setReturnValue(entry.getIgniteOdds());
            }
        }
    }

    @Inject(at={@At(value="HEAD")}, method={"getBurnOdds"}, cancellable=true)
    private void getFabricSpreadChance(BlockState block, CallbackInfoReturnable info) {
        FlammableBlockRegistry.Entry entry = this.registry.getFabric(block.getBlock());
        if (entry != null) {
            if (block.hasProperty(BlockStateProperties.WATERLOGGED) && block.getValue(BlockStateProperties.WATERLOGGED).booleanValue()) {
                info.setReturnValue(0);
            } else {
                info.setReturnValue(entry.getBurnOdds());
            }
        }
    }

    @Override
    public FlammableBlockRegistry.Entry fabric_getVanillaEntry(BlockState block) {
        return new FlammableBlockRegistry.Entry(this.getIgniteOdds(block), this.getBurnOdds(block));
    }
}

