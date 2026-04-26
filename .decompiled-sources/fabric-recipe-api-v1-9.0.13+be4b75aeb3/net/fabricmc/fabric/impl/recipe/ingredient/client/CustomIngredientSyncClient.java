/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.impl.recipe.ingredient.ClientboundCustomIngredientPayload;
import net.fabricmc.fabric.impl.recipe.ingredient.CustomIngredientSync;

public class CustomIngredientSyncClient
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientConfigurationNetworking.registerGlobalReceiver(ClientboundCustomIngredientPayload.TYPE, (payload, context) -> context.responseSender().sendPacket(CustomIngredientSync.createResponsePayload(payload.protocolVersion())));
    }
}

