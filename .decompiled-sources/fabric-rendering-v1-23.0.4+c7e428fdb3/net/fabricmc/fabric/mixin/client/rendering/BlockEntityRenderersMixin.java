/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import java.util.Map;
import net.fabricmc.fabric.impl.client.rendering.BlockEntityRendererRegistryImpl;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={BlockEntityRenderers.class})
public abstract class BlockEntityRenderersMixin {
    @Shadow
    @Final
    private static Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> PROVIDERS;

    @Inject(at={@At(value="RETURN")}, method={"<clinit>*"})
    private static void init(CallbackInfo ci) {
        BlockEntityRendererRegistryImpl.setup((t, factory) -> PROVIDERS.put((BlockEntityType<?>)t, (BlockEntityRendererProvider<?, ?>)factory));
    }
}

