/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import net.fabricmc.fabric.api.event.registry.RegistryEntryAddedCallback;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Blocks.class})
public class BlocksMixin {
    @Inject(method={"<clinit>"}, at={@At(value="TAIL")})
    private static void initShapeCache(CallbackInfo ci) {
        RegistryEntryAddedCallback.event(BuiltInRegistries.BLOCK).register((rawId, id, block) -> {
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                state.initCache();
            }
        });
    }
}

