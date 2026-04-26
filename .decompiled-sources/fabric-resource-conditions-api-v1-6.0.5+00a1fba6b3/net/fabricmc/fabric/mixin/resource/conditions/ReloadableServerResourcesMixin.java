/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.conditions;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.fabricmc.fabric.impl.resource.conditions.ResourceConditionsImpl;
import net.minecraft.commands.Commands;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.Registry;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.flag.FeatureFlagSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={ReloadableServerResources.class})
public class ReloadableServerResourcesMixin {
    @Inject(method={"loadResources"}, at={@At(value="HEAD")})
    private static void hookReload(ResourceManager resourceManager, LayeredRegistryAccess<RegistryLayer> dynamicRegistries, List<Registry.PendingTags<?>> pendingTagLoads, FeatureFlagSet enabledFeatures, Commands.CommandSelection environment, PermissionSet permissionPredicate, Executor prepareExecutor, Executor applyExecutor, CallbackInfoReturnable<CompletableFuture<ReloadableServerResources>> cir) {
        ResourceConditionsImpl.currentFeatures = enabledFeatures;
    }
}

