/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.creativetab.v1;

import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.fabricmc.fabric.impl.creativetab.CreativeModeTabEventsImpl;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

public final class CreativeModeTabEvents {
    public static final Event<ModifyOutputAll> MODIFY_OUTPUT_ALL = EventFactory.createArrayBacked(ModifyOutputAll.class, callbacks -> (tab, output) -> {
        for (ModifyOutputAll callback : callbacks) {
            callback.modifyOutput(tab, output);
        }
    });

    private CreativeModeTabEvents() {
    }

    public static Event<ModifyOutput> modifyOutputEvent(ResourceKey<CreativeModeTab> resourceKey) {
        return CreativeModeTabEventsImpl.getOrCreateModifyOutputEvent(resourceKey);
    }

    @FunctionalInterface
    public static interface ModifyOutputAll {
        public void modifyOutput(CreativeModeTab var1, FabricCreativeModeTabOutput var2);
    }

    @FunctionalInterface
    public static interface ModifyOutput {
        public void modifyOutput(FabricCreativeModeTabOutput var1);
    }
}

