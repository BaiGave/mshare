/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VibrationFrequencyRegistry {
    private static final Logger LOGGER = LoggerFactory.getLogger(VibrationFrequencyRegistry.class);

    private VibrationFrequencyRegistry() {
    }

    public static void register(ResourceKey<GameEvent> event, int frequency) {
        if (frequency <= 0 || frequency >= 16) {
            throw new IllegalArgumentException("Attempted to register vibration frequency for event " + String.valueOf(event.identifier()) + " with frequency " + frequency + ". Sculk Sensor frequencies must be between 1 and 15 inclusive.");
        }
        Reference2IntOpenHashMap map = (Reference2IntOpenHashMap)VibrationSystem.VIBRATION_FREQUENCY_FOR_EVENT;
        int replaced = map.put(event, frequency);
        if (replaced != 0) {
            LOGGER.debug("Replaced old frequency mapping for {} - was {}, now {}", event.identifier(), replaced, frequency);
        }
    }
}

