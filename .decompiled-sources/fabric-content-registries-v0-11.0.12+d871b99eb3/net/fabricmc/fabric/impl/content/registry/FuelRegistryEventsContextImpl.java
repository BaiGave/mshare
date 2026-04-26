/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.content.registry;

import net.fabricmc.fabric.api.registry.FuelValueEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;

public record FuelRegistryEventsContextImpl(HolderLookup.Provider registries, FeatureFlagSet enabledFeatures, int baseSmeltTime) implements FuelValueEvents.Context
{
}

