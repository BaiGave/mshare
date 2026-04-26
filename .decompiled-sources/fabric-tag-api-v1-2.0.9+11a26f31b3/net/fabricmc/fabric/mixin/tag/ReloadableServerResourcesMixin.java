/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.tag;

import java.util.List;
import net.fabricmc.fabric.impl.tag.TagAliasLoader;
import net.minecraft.commands.Commands;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ReloadableServerResources.class}, priority=999)
abstract class ReloadableServerResourcesMixin {
    @Unique
    private LayeredRegistryAccess<RegistryLayer> dynamicRegistriesByType;

    ReloadableServerResourcesMixin() {
    }

    @Inject(method={"<init>"}, at={@At(value="RETURN")})
    private void storeDynamicRegistries(LayeredRegistryAccess<RegistryLayer> dynamicRegistries, HolderLookup.Provider loadingContext, FeatureFlagSet enabledFeatures, Commands.CommandSelection commandSelection, List postponedTags, PermissionSet functionCompilationPermissions, List newComponents, CallbackInfo ci) {
        this.dynamicRegistriesByType = dynamicRegistries;
    }

    @Inject(method={"updateComponentsAndStaticRegistryTags"}, at={@At(value="RETURN")})
    private void applyDynamicTagAliases(CallbackInfo info) {
        TagAliasLoader.applyToDynamicRegistries(this.dynamicRegistriesByType, RegistryLayer.WORLDGEN);
        TagAliasLoader.applyToDynamicRegistries(this.dynamicRegistriesByType, RegistryLayer.RELOADABLE);
    }
}

