/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.registry.sync;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.registry.RegistryAttribute;
import net.fabricmc.fabric.api.event.registry.RegistryAttributeHolder;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;
import net.fabricmc.fabric.impl.registry.sync.RegistrySyncManager;
import net.fabricmc.fabric.impl.registry.sync.SyncCompletePayload;
import net.fabricmc.fabric.impl.registry.sync.packet.RegistrySyncPayload;
import net.minecraft.core.registries.BuiltInRegistries;

public class FabricRegistryInit
implements ModInitializer {
    private static final int MAX_PACKET_SIZE = Integer.getInteger("fabric.registry.sync.max_packet_size", 0x8000000);

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.serverboundConfiguration().register(SyncCompletePayload.ID, SyncCompletePayload.CODEC);
        PayloadTypeRegistry.clientboundConfiguration().registerLarge(RegistrySyncPayload.ID, RegistrySyncPayload.CODEC, MAX_PACKET_SIZE);
        ServerConfigurationConnectionEvents.BEFORE_CONFIGURE.register(RegistrySyncManager::configureClient);
        ServerConfigurationNetworking.registerGlobalReceiver(SyncCompletePayload.ID, (payload, context) -> context.packetListener().completeTask(RegistrySyncManager.SyncConfigurationTask.KEY));
        RegistryAttributeHolder.get(BuiltInRegistries.SOUND_EVENT).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.FLUID).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.MOB_EFFECT).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.BLOCK).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.ENTITY_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.ITEM).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.POTION).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.CARVER);
        RegistryAttributeHolder.get(BuiltInRegistries.FEATURE);
        RegistryAttributeHolder.get(BuiltInRegistries.BLOCKSTATE_PROVIDER_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.FOLIAGE_PLACER_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.TRUNK_PLACER_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.TREE_DECORATOR_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.FEATURE_SIZE_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.PARTICLE_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.BIOME_SOURCE);
        RegistryAttributeHolder.get(BuiltInRegistries.BLOCK_ENTITY_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.CUSTOM_STAT).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.CHUNK_STATUS);
        RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PIECE);
        RegistryAttributeHolder.get(BuiltInRegistries.RULE_TEST);
        RegistryAttributeHolder.get(BuiltInRegistries.POS_RULE_TEST);
        RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_PROCESSOR);
        RegistryAttributeHolder.get(BuiltInRegistries.STRUCTURE_POOL_ELEMENT);
        RegistryAttributeHolder.get(BuiltInRegistries.COMMAND_ARGUMENT_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.MENU).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.ATTRIBUTE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.STAT_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.VILLAGER_PROFESSION).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.POINT_OF_INTEREST_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.MEMORY_MODULE_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.SENSOR_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.ACTIVITY);
        RegistryAttributeHolder.get(BuiltInRegistries.LOOT_POOL_ENTRY_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.LOOT_FUNCTION_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.LOOT_CONDITION_TYPE);
        RegistryAttributeHolder.get(BuiltInRegistries.GAME_EVENT).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.NUMBER_FORMAT_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.POSITION_SOURCE_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.DATA_COMPONENT_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.MAP_DECORATION_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.CONSUME_EFFECT_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_DISPLAY).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.SLOT_DISPLAY).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.RECIPE_BOOK_CATEGORY).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.POINT_OF_INTEREST_TYPE).addAttribute(RegistryAttribute.SYNCED);
        RegistryAttributeHolder.get(BuiltInRegistries.DEBUG_SUBSCRIPTION).addAttribute(RegistryAttribute.SYNCED);
    }
}

