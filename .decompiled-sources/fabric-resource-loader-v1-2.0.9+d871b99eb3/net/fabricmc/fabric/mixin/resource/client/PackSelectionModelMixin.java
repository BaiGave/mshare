/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={PackSelectionModel.class})
public class PackSelectionModelMixin {
    @Shadow
    @Final
    private List<Pack> selected;
    @Shadow
    @Final
    private List<Pack> unselected;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void removeHiddenPacksInit(Consumer<PackSelectionModel.EntryBase> updateCallback, Function iconIdSupplier, PackRepository packRepository, Consumer applier, CallbackInfo ci) {
        this.selected.removeIf(profile -> ((FabricPack)((Object)profile)).fabric$isHidden());
        this.unselected.removeIf(profile -> ((FabricPack)((Object)profile)).fabric$isHidden());
    }

    @Inject(method={"findNewPacks"}, at={@At(value="TAIL")})
    private void removeHiddenPacksRefresh(CallbackInfo ci) {
        this.selected.removeIf(profile -> ((FabricPack)((Object)profile)).fabric$isHidden());
        this.unselected.removeIf(profile -> ((FabricPack)((Object)profile)).fabric$isHidden());
    }
}

