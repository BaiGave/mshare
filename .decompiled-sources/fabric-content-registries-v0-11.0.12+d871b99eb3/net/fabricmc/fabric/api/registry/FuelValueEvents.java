/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.block.entity.FuelValues;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.NonExtendable
public interface FuelValueEvents {
    public static final Event<BuildCallback> BUILD = EventFactory.createArrayBacked(BuildCallback.class, listeners -> (builder, context) -> {
        for (BuildCallback listener : listeners) {
            listener.build(builder, context);
        }
    });
    public static final Event<ExclusionsCallback> EXCLUSIONS = EventFactory.createArrayBacked(ExclusionsCallback.class, listeners -> (builder, context) -> {
        for (ExclusionsCallback listener : listeners) {
            listener.buildExclusions(builder, context);
        }
    });

    @FunctionalInterface
    public static interface ExclusionsCallback {
        public void buildExclusions(FuelValues.Builder var1, Context var2);
    }

    @ApiStatus.NonExtendable
    public static interface Context {
        public int baseSmeltTime();

        public HolderLookup.Provider registries();

        public FeatureFlagSet enabledFeatures();
    }

    @FunctionalInterface
    public static interface BuildCallback {
        public void build(FuelValues.Builder var1, Context var2);
    }
}

