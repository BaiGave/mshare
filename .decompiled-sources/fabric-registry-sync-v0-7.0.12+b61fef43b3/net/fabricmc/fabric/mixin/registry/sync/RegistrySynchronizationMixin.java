/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.registry.sync;

import com.mojang.serialization.DynamicOps;
import java.util.Set;
import java.util.function.BiConsumer;
import net.fabricmc.fabric.impl.registry.sync.DynamicRegistriesImpl;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistrySynchronization;
import net.minecraft.resources.RegistryDataLoader;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={RegistrySynchronization.class})
abstract class RegistrySynchronizationMixin {
    RegistrySynchronizationMixin() {
    }

    @Inject(method={"lambda$ownedNetworkableRegistries$0"}, at={@At(value="HEAD")}, cancellable=true)
    @Dynamic(value="lambda$ownedNetworkableRegistries$0: Stream.filter in ownedNetworkableRegistries")
    private static void filterNonSyncedEntries(RegistryAccess.RegistryEntry<?> entry, CallbackInfoReturnable<Boolean> cir) {
        boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(entry.key());
        if (canSkip && entry.value().size() == 0) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method={"lambda$packRegistry$0"}, at={@At(value="HEAD")}, cancellable=true)
    @Dynamic(value="lambda$packRegistry$0: Optional.ifPresent in packRegistry")
    private static void filterNonSyncedEntriesAgain(Set set, RegistryDataLoader.RegistryData entry, DynamicOps dynamicOps, BiConsumer biConsumer, Registry registry, CallbackInfo ci) {
        boolean canSkip = DynamicRegistriesImpl.SKIP_EMPTY_SYNC_REGISTRIES.contains(registry.key());
        if (canSkip && registry.size() == 0) {
            ci.cancel();
        }
    }
}

