/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync.client;

import java.util.ArrayList;
import java.util.Comparator;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.impl.recipe.sync.ClientboundRecipeSyncPayload;
import net.fabricmc.fabric.impl.recipe.sync.SynchronizedRecipesImpl;
import net.fabricmc.fabric.impl.recipe.sync.client.SynchronizedClientRecipesSetter;

public class RecipeSyncImplClient
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ClientboundRecipeSyncPayload.TYPE, RecipeSyncImplClient::onRecipeSyncPacket);
    }

    private static void onRecipeSyncPacket(ClientboundRecipeSyncPayload payload, ClientPlayNetworking.Context context) {
        SynchronizedRecipesImpl recipes;
        if (!payload.entries().isEmpty()) {
            ArrayList collectedRecipes = new ArrayList();
            for (ClientboundRecipeSyncPayload.Entry entry2 : payload.entries()) {
                collectedRecipes.addAll(entry2.recipes());
            }
            collectedRecipes.sort(Comparator.comparing(entry -> entry.id().identifier()));
            recipes = SynchronizedRecipesImpl.of(collectedRecipes);
        } else {
            recipes = SynchronizedRecipesImpl.EMPTY;
        }
        ((SynchronizedClientRecipesSetter)((Object)context.player().connection.recipes())).fabric_setSynchronizedClientRecipes(recipes);
        ClientRecipeSynchronizedEvent.EVENT.invoker().onRecipesSynchronized(context.client(), recipes);
    }
}

