/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import java.util.List;
import net.fabricmc.fabric.impl.resource.pack.ModPackResourcesUtil;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.client.multiplayer.KnownPacksManager;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.PackRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value={KnownPacksManager.class})
public class KnownPacksManagerMixin {
    @Unique
    private static final Logger LOGGER = LoggerFactory.getLogger("KnownPacksManagerMixin");

    @Redirect(method={"<init>"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/repository/ServerPacksSource;createVanillaTrustedRepository()Lnet/minecraft/server/packs/repository/PackRepository;"))
    public PackRepository createClientManager() {
        return ModPackResourcesUtil.createModdedRepository();
    }

    @ModifyReturnValue(method={"trySelectingPacks"}, at={@At(value="RETURN")})
    List<KnownPack> getCommonKnownPacksReturn(List<KnownPack> original) {
        if (original.size() > ModResourcePackCreator.MAX_KNOWN_PACKS) {
            LOGGER.warn("Too many knownPacks: Found {}; max {}", (Object)original.size(), (Object)ModResourcePackCreator.MAX_KNOWN_PACKS);
            return original.subList(0, ModResourcePackCreator.MAX_KNOWN_PACKS);
        }
        return original;
    }
}

