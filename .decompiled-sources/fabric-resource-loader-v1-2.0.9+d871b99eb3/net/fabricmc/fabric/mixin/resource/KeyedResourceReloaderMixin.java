/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import java.util.Locale;
import net.fabricmc.fabric.api.resource.v1.reloader.ResourceReloaderKeys;
import net.fabricmc.fabric.impl.resource.FabricResourceReloader;
import net.minecraft.resources.Identifier;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={RecipeManager.class, ServerAdvancementManager.class, ServerFunctionLibrary.class})
public abstract class KeyedResourceReloaderMixin
implements FabricResourceReloader {
    @Unique
    private Identifier id;

    @Override
    public Identifier fabric$getId() {
        if (this.id == null) {
            KeyedResourceReloaderMixin self = this;
            this.id = self instanceof RecipeManager ? ResourceReloaderKeys.Server.RECIPES : (self instanceof ServerAdvancementManager ? ResourceReloaderKeys.Server.ADVANCEMENTS : (self instanceof ServerFunctionLibrary ? ResourceReloaderKeys.Server.FUNCTIONS : Identifier.withDefaultNamespace("private/" + self.getClass().getSimpleName().toLowerCase(Locale.ROOT))));
        }
        return this.id;
    }
}

