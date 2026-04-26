/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import com.mojang.datafixers.DataFixer;
import java.net.Proxy;
import java.util.List;
import java.util.Optional;
import net.fabricmc.fabric.api.resource.v1.DataResourceStore;
import net.fabricmc.fabric.impl.resource.FabricDataResourceStoreHolder;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.FabricOriginalKnownPacksGetter;
import net.fabricmc.fabric.impl.resource.pack.ModNioPackResources;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.Services;
import net.minecraft.server.WorldStem;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.KnownPack;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={MinecraftServer.class})
public class MinecraftServerMixin
implements DataResourceStore,
FabricOriginalKnownPacksGetter {
    @Unique
    private List<KnownPack> originalKnownPacks;
    @Shadow
    private MinecraftServer.ReloadableResources resources;

    @Override
    public <T> T getOrThrow(DataResourceStore.Key<T> key) {
        return ((FabricDataResourceStoreHolder)((Object)this.resources.managers())).fabric$getDataResourceStore().getOrThrow(key);
    }

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(Thread serverThread, LevelStorageSource.LevelStorageAccess storageAccess, PackRepository dataPackManager, WorldStem worldStem, Optional<GameRules> gameRules, Proxy proxy, DataFixer dataFixer, Services apiServices, LevelLoadListener chunkLoadProgress, boolean propagatesCrashes, CallbackInfo ci) {
        this.originalKnownPacks = worldStem.resourceManager().listPacks().flatMap(pack -> pack.location().knownPackInfo().stream()).toList();
    }

    @Redirect(method={"configurePackRepository(Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/world/level/WorldDataConfiguration;ZZ)Lnet/minecraft/world/level/WorldDataConfiguration;"}, at=@At(value="INVOKE", target="Ljava/util/List;contains(Ljava/lang/Object;)Z"))
    private static boolean onCheckDisabled(List<String> list, Object o, PackRepository resourcePackManager) {
        String profileId = (String)o;
        boolean contains = list.contains(profileId);
        if (contains) {
            return true;
        }
        Pack profile = resourcePackManager.getPack(profileId);
        if (profile.getPackSource() instanceof BuiltinModPackSource) {
            try (PackResources pack = profile.open();){
                ModNioPackResources modPack;
                boolean bl = pack instanceof ModNioPackResources && !(modPack = (ModNioPackResources)pack).getActivationType().isEnabledByDefault();
                return bl;
            }
        }
        return false;
    }

    @Override
    public List<KnownPack> fabric$getOriginalKnownPacks() {
        return this.originalKnownPacks;
    }
}

