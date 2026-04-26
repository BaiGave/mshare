/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.trackers.StateIdTracker;
import net.fabricmc.fabric.impl.registry.sync.trackers.vanilla.BlockItemTracker;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Bootstrap.class})
public class BootstrapMixin {
    @Inject(method={"bootStrap"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/Bootstrap;wrapStreams()V")})
    private static void afterInitialize(CallbackInfo info) {
        Block oBlock = Blocks.AIR;
        Fluid oFluid = Fluids.EMPTY;
        Item oItem = Items.AIR;
        StateIdTracker.register(BuiltInRegistries.BLOCK, Block.BLOCK_STATE_REGISTRY, block -> block.getStateDefinition().getPossibleStates());
        StateIdTracker.register(BuiltInRegistries.FLUID, Fluid.FLUID_STATE_REGISTRY, fluid -> fluid.getStateDefinition().getPossibleStates());
        BlockItemTracker.register(BuiltInRegistries.ITEM);
        RegistrySyncManager.bootstrapRegistries();
    }

    @Redirect(method={"bootStrap"}, at=@At(value="INVOKE", target="Lnet/minecraft/core/registries/BuiltInRegistries;bootStrap()V"))
    private static void delayRegistryFreeze() {
        BuiltInRegistries.createContents();
    }
}

