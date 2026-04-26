/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.recipe.v1.sync;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.api.recipe.v1.sync.SynchronizedRecipes;
import net.minecraft.client.Minecraft;

public interface ClientRecipeSynchronizedEvent {
    public static final Event<ClientRecipeSynchronizedEvent> EVENT = EventFactory.createArrayBacked(ClientRecipeSynchronizedEvent.class, callbacks -> (client, recipes) -> {
        for (ClientRecipeSynchronizedEvent callback : callbacks) {
            callback.onRecipesSynchronized(client, recipes);
        }
    });

    public void onRecipesSynchronized(Minecraft var1, SynchronizedRecipes var2);
}

