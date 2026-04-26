/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.List;
import net.fabricmc.fabric.impl.resource.client.DefaultResourcePackStorage;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.minecraft.client.Options;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Options.class})
public class OptionsMixin {
    @Shadow
    public List<String> resourcePacks;

    @Inject(method={"load"}, at={@At(value="RETURN")})
    private void onLoad(CallbackInfo ci) {
        this.resourcePacks = DefaultResourcePackStorage.process(this.resourcePacks);
    }

    @WrapOperation(method={"updateResourcePacks"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/packs/repository/Pack;isFixedPosition()Z")})
    private boolean excludeInternalResourcePacksFromRefreshCheck(Pack instance, Operation<Boolean> original) {
        return original.call(instance) != false || ((FabricPack)((Object)instance)).fabric$isHidden();
    }
}

