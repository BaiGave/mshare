/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.client.sync;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.fabricmc.fabric.impl.recipe.sync.client.SynchronizedClientRecipesSetter;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.ClientRecipeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value={ClientPacketListener.class})
public class ClientPacketListenerMixin {
    @Shadow
    private ClientRecipeContainer recipes;

    @WrapOperation(method={"handleUpdateRecipes"}, at={@At(value="FIELD", target="Lnet/minecraft/client/multiplayer/ClientPacketListener;recipes:Lnet/minecraft/client/multiplayer/ClientRecipeContainer;", opcode=181)})
    private void copyPreviousRecipes(ClientPacketListener instance, ClientRecipeContainer value, Operation<Void> original) {
        ((SynchronizedClientRecipesSetter)((Object)value)).fabric_setSynchronizedClientRecipes(this.recipes.getSynchronizedRecipes());
        original.call(instance, value);
    }
}

