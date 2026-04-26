/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import com.mojang.datafixers.util.Pair;
import java.io.File;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={CreateWorldScreen.class})
public abstract class CreateWorldScreenMixin
extends Screen {
    @Shadow
    private PackRepository tempDataPackRepository;

    private CreateWorldScreenMixin() {
        super(null);
    }

    @ModifyVariable(method={"openCreateWorldScreen(Lnet/minecraft/client/Minecraft;Ljava/lang/Runnable;Ljava/util/function/Function;Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContextMapper;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldCallback;)V"}, at=@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;createDefaultLoadConfig(Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/world/level/WorldDataConfiguration;)Lnet/minecraft/server/WorldLoader$InitConfig;"), name={"vanillaOnlyPackRepository"})
    private static PackRepository onCreateResManagerInit(PackRepository manager) {
        manager.sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
        return manager;
    }

    @Redirect(method={"openCreateWorldScreen(Lnet/minecraft/client/Minecraft;Ljava/lang/Runnable;Ljava/util/function/Function;Lnet/minecraft/client/gui/screens/worldselection/WorldCreationContextMapper;Lnet/minecraft/resources/ResourceKey;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldCallback;)V"}, at=@At(value="FIELD", target="Lnet/minecraft/world/level/WorldDataConfiguration;DEFAULT:Lnet/minecraft/world/level/WorldDataConfiguration;", ordinal=0, opcode=178))
    private static WorldDataConfiguration replaceDefaultSettings() {
        return ModPackResourcesUtil.createDefaultDataConfiguration();
    }

    @Inject(method={"getDataPackSelectionSettings"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/packs/repository/PackRepository;reload()V")})
    private void onScanPacks(CallbackInfoReturnable<Pair<File, PackRepository>> cir) {
        this.tempDataPackRepository.sources.add(new ModResourcePackCreator(PackType.SERVER_DATA));
    }
}

