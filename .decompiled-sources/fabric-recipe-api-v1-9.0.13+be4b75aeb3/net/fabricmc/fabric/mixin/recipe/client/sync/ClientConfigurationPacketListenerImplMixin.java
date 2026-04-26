/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.recipe.client.sync;

import java.util.HashSet;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.recipe.sync.RecipeSyncImpl;
import net.fabricmc.fabric.impl.recipe.sync.ServerboundSupportedRecipeSerializersPayload;
import net.minecraft.client.multiplayer.ClientConfigurationPacketListenerImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.protocol.configuration.ClientboundSelectKnownPacks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.crafting.RecipeSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={ClientConfigurationPacketListenerImpl.class})
public class ClientConfigurationPacketListenerImplMixin {
    @Inject(method={"handleSelectKnownPacks"}, at={@At(value="TAIL")})
    private void sendSupportedRecipeSerializers(ClientboundSelectKnownPacks packet, CallbackInfo ci) {
        if (!ClientConfigurationNetworking.canSend(ServerboundSupportedRecipeSerializersPayload.TYPE)) {
            return;
        }
        HashSet<Identifier> ids = new HashSet<Identifier>();
        for (RecipeSerializer<?> serializer : RecipeSyncImpl.getSyncedSerializers()) {
            ids.add(BuiltInRegistries.RECIPE_SERIALIZER.getKey(serializer));
        }
        if (ids.isEmpty()) {
            return;
        }
        ClientConfigurationNetworking.send(new ServerboundSupportedRecipeSerializersPayload(ids));
    }
}

