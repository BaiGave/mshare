/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.sync;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.recipe.sync.ClientboundRecipeSyncPayload;
import net.fabricmc.fabric.impl.recipe.sync.ServerboundSupportedRecipeSerializersPayload;
import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwareConnection;
import net.fabricmc.fabric.impl.recipe.sync.SyncedSerializerAwarePreparedRecipe;
import net.fabricmc.fabric.mixin.recipe.sync.RecipeManagerAccessor;
import net.fabricmc.fabric.mixin.recipe.sync.ServerCommonPacketListenerImplAccessor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;

public class RecipeSyncImpl
implements ModInitializer {
    private static final int RECIPE_PAYLOAD_MAX_SIZE = 0x4000000;
    private static final Set<RecipeSerializer<?>> SYNCED_SERIALIZERS = new ReferenceOpenHashSet();
    public static final Identifier RECIPE_SYNC_EVENT_PHASE = Identifier.fromNamespaceAndPath("fabric", "recipe_sync");

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundConfiguration().register(ServerboundSupportedRecipeSerializersPayload.TYPE, ServerboundSupportedRecipeSerializersPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().registerLarge(ClientboundRecipeSyncPayload.TYPE, ClientboundRecipeSyncPayload.CODEC, 0x4000000);
        ServerConfigurationNetworking.registerGlobalReceiver(ServerboundSupportedRecipeSerializersPayload.TYPE, RecipeSyncImpl::onRecipeSyncRequest);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.addPhaseOrdering(Event.DEFAULT_PHASE, RECIPE_SYNC_EVENT_PHASE);
        ServerLifecycleEvents.SYNC_DATA_PACK_CONTENTS.register(RECIPE_SYNC_EVENT_PHASE, RecipeSyncImpl::sendRecipes);
    }

    private static void onRecipeSyncRequest(ServerboundSupportedRecipeSerializersPayload payload, ServerConfigurationNetworking.Context context) {
        ReferenceOpenHashSet set = new ReferenceOpenHashSet();
        for (Identifier identifier : payload.synchronizedSerializers()) {
            BuiltInRegistries.RECIPE_SERIALIZER.getOptional(identifier).ifPresent(set::add);
        }
        ((SyncedSerializerAwareConnection)((Object)((ServerCommonPacketListenerImplAccessor)((Object)context.packetListener())).getConnection())).fabric_setSyncedRecipeSerializers(set);
    }

    private static void sendRecipes(ServerPlayer player, boolean exist) {
        if (!ServerPlayNetworking.canSend(player, ClientboundRecipeSyncPayload.TYPE)) {
            return;
        }
        Set<RecipeSerializer<?>> serializers = ((SyncedSerializerAwareConnection)((Object)((ServerCommonPacketListenerImplAccessor)((Object)player.connection)).getConnection())).fabric_getSyncedRecipeSerializers();
        SyncedSerializerAwarePreparedRecipe accessor = (SyncedSerializerAwarePreparedRecipe)((Object)((RecipeManagerAccessor)((Object)player.level().recipeAccess())).getRecipes());
        ArrayList<ClientboundRecipeSyncPayload.Entry> list = new ArrayList<ClientboundRecipeSyncPayload.Entry>();
        for (RecipeSerializer<?> serializer : serializers) {
            List<RecipeHolder<?>> recipes = accessor.fabric_getRecipesBySyncedSerializer(serializer);
            if (recipes == null || recipes.isEmpty()) continue;
            list.add(new ClientboundRecipeSyncPayload.Entry(serializer, recipes));
        }
        if (list.isEmpty()) {
            return;
        }
        ServerPlayNetworking.send(player, new ClientboundRecipeSyncPayload(list));
    }

    public static void addSynchronizedSerializer(RecipeSerializer<?> serializer) {
        SYNCED_SERIALIZERS.add(serializer);
    }

    public static boolean isSynced(RecipeSerializer<?> serializer) {
        return SYNCED_SERIALIZERS.contains(serializer);
    }

    public static Set<RecipeSerializer<?>> getSyncedSerializers() {
        return Collections.unmodifiableSet(SYNCED_SERIALIZERS);
    }
}

