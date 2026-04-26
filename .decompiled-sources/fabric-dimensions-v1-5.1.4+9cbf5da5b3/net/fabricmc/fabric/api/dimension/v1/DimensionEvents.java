/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.dimension.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.attribute.EnvironmentAttributeMap;
import net.minecraft.world.level.dimension.DimensionType;

public class DimensionEvents {
    public static final Event<ModifyAttributes> MODIFY_ATTRIBUTES = EventFactory.createArrayBacked(ModifyAttributes.class, listeners -> (dimension, attributes, registries) -> {
        for (ModifyAttributes listener : listeners) {
            listener.modifyDimensionAttributes(dimension, attributes, registries);
        }
    });

    @FunctionalInterface
    public static interface ModifyAttributes {
        public void modifyDimensionAttributes(Holder<DimensionType> var1, EnvironmentAttributeMap.Builder var2, HolderLookup.Provider var3);
    }
}

