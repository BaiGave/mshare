/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.sync;

import java.util.Set;
import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwareConnection;
import net.minecraft.network.Connection;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={Connection.class})
public abstract class ConnectionMixin
implements SyncedSerializerAwareConnection {
    @Unique
    private Set<RecipeSerializer<?>> syncedRecipeSerializers = Set.of();

    @Override
    public void fabric_setSyncedRecipeSerializers(Set<RecipeSerializer<?>> syncedRecipeSerializers) {
        this.syncedRecipeSerializers = syncedRecipeSerializers;
    }

    @Override
    public Set<RecipeSerializer<?>> fabric_getSyncedRecipeSerializers() {
        return this.syncedRecipeSerializers;
    }
}

