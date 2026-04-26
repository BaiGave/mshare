/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.creativetab;

import java.util.HashMap;
import java.util.Map;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import org.jspecify.annotations.Nullable;

public class CreativeModeTabEventsImpl {
    private static final Map<ResourceKey<CreativeModeTab>, Event<CreativeModeTabEvents.ModifyOutput>> CREATIVE_MODE_TAB_EVENT_MAP = new HashMap<ResourceKey<CreativeModeTab>, Event<CreativeModeTabEvents.ModifyOutput>>();

    public static Event<CreativeModeTabEvents.ModifyOutput> getOrCreateModifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
        return CREATIVE_MODE_TAB_EVENT_MAP.computeIfAbsent(resourceKey, g -> CreativeModeTabEventsImpl.createModifyEvent());
    }

    public static @Nullable Event<CreativeModeTabEvents.ModifyOutput> getModifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
        return CREATIVE_MODE_TAB_EVENT_MAP.get(resourceKey);
    }

    private static Event<CreativeModeTabEvents.ModifyOutput> createModifyEvent() {
        return EventFactory.createArrayBacked(CreativeModeTabEvents.ModifyOutput.class, callbacks -> entries -> {
            for (CreativeModeTabEvents.ModifyOutput callback : callbacks) {
                callback.modifyOutput(entries);
            }
        });
    }
}

