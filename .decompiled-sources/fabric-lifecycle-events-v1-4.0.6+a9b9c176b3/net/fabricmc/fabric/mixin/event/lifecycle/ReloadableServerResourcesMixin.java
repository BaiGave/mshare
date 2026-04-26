/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.event.lifecycle;

import java.util.List;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ReloadableServerResources.class})
public class ReloadableServerResourcesMixin {
    @Unique
    private RegistryAccess layeredRegistries;

    @Inject(method={"<init>"}, at={@At(value="TAIL")})
    private void init(LayeredRegistryAccess<RegistryLayer> layeredRegistries, HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List postponedTags, PermissionSet functionCompilationPermissions, List newComponents, CallbackInfo ci) {
        this.layeredRegistries = layeredRegistries.compositeAccess();
    }

    @Inject(method={"updateComponentsAndStaticRegistryTags"}, at={@At(value="TAIL")})
    private void hookRefresh(CallbackInfo ci) {
        CommonLifecycleEvents.TAGS_LOADED.invoker().onTagsLoaded(this.layeredRegistries, false);
    }
}

