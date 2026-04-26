/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.gametest.server;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.fabricmc.fabric.impl.gametest.FabricGameTestRunner;
import net.minecraft.server.Main;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={Main.class})
public class MainMixin {
    @ModifyExpressionValue(method={"main"}, at={@At(value="INVOKE", target="Lnet/minecraft/server/Eula;hasAgreedToEULA()Z")})
    private static boolean isEulaAgreedTo(boolean isEulaAgreedTo) {
        return FabricGameTestRunner.ENABLED || isEulaAgreedTo;
    }

    @Inject(method={"main"}, cancellable=true, at={@At(value="INVOKE_ASSIGN", target="Lnet/minecraft/server/packs/repository/ServerPacksSource;createPackRepository(Lnet/minecraft/world/level/storage/LevelStorageSource$LevelStorageAccess;)Lnet/minecraft/server/packs/repository/PackRepository;")})
    private static void main(String[] args, CallbackInfo info, @Local(name={"access"}) LevelStorageSource.LevelStorageAccess storageAccess, @Local(name={"packRepository"}) PackRepository packRepository) {
        if (FabricGameTestRunner.ENABLED) {
            FabricGameTestRunner.runHeadlessServer(storageAccess, packRepository);
            info.cancel();
        }
    }

    @Inject(method={"main"}, at={@At(value="INVOKE", target="Lorg/slf4j/Logger;error(Lorg/slf4j/Marker;Ljava/lang/String;Ljava/lang/Throwable;)V", shift=At.Shift.AFTER)})
    private static void exitOnError(CallbackInfo info) {
        if (FabricGameTestRunner.ENABLED) {
            System.exit(-1);
        }
    }
}

