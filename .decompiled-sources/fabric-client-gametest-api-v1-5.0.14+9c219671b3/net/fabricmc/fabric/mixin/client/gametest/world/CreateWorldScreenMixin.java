/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.gametest.world;

import com.llamalad7.mixinextras.sugar.Local;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import net.fabricmc.fabric.impl.client.gametest.util.ClientGameTestImpl;
import net.fabricmc.fabric.impl.client.gametest.util.DedicatedServerImplUtil;
import net.fabricmc.fabric.mixin.client.gametest.world.GameRulesAccessor;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.RegistryLayer;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.level.gamerules.GameRuleMap;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.storage.PrimaryLevelData;
import net.minecraft.world.level.storage.SavedDataStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CreateWorldScreen.class})
public class CreateWorldScreenMixin {
    @Inject(method={"onCreate"}, at={@At(value="INVOKE", target="Lnet/minecraft/client/gui/screens/worldselection/WorldOpenFlows;confirmWorldCreation(Lnet/minecraft/client/Minecraft;Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;Lcom/mojang/serialization/Lifecycle;Ljava/lang/Runnable;Z)V")}, cancellable=true)
    private void createLevelDataForServers(CallbackInfo ci, @Local(name={"finalLayers"}) LayeredRegistryAccess<RegistryLayer> finalLayers, @Local(name={"worldData"}) PrimaryLevelData worldData, @Local(name={"worldGenSettings"}) WorldGenSettings worldGenSettings, @Local(name={"gameRules"}) GameRules gameRules) {
        if (DedicatedServerImplUtil.saveLevelDataTo != null) {
            try {
                Path worldPath = DedicatedServerImplUtil.saveLevelDataTo;
                CompoundTag levelDatInner = worldData.createTag(null);
                CompoundTag levelDat = new CompoundTag();
                levelDat.put("Data", levelDatInner);
                Files.createDirectories(worldPath, new FileAttribute[0]);
                NbtIo.writeCompressed(levelDat, worldPath.resolve("level.dat"));
                try (SavedDataStorage savedDataStorage = new SavedDataStorage(worldPath.resolve("data"), DataFixers.getDataFixer(), finalLayers.compositeAccess());){
                    savedDataStorage.set(WorldGenSettings.TYPE, worldGenSettings);
                    savedDataStorage.set(GameRuleMap.TYPE, ((GameRulesAccessor)((Object)gameRules)).getRules());
                    savedDataStorage.saveAndJoin();
                }
            }
            catch (IOException e) {
                ClientGameTestImpl.LOGGER.error("Failed to save dedicated server level data", e);
            }
            ci.cancel();
        }
    }
}

