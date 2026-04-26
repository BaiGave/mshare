/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import java.util.List;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.server.WorldLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={WorldLoader.class})
abstract class WorldLoaderMixin {
    WorldLoaderMixin() {
    }

    @ModifyArg(method={"lambda$load$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/resources/RegistryDataLoader;load(Lnet/minecraft/server/packs/resources/ResourceManager;Ljava/util/List;Ljava/util/List;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;", ordinal=0), index=2, allow=1)
    private static List<RegistryDataLoader.RegistryData<?>> modifyLoadedEntries(List<RegistryDataLoader.RegistryData<?>> entries) {
        return DynamicRegistries.getDynamicRegistries();
    }
}

