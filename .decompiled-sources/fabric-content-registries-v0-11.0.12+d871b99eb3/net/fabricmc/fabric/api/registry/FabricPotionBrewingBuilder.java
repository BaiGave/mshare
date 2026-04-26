/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.registry;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.crafting.Ingredient;

public interface FabricPotionBrewingBuilder {
    public static final Event<BuildCallback> BUILD = EventFactory.createArrayBacked(BuildCallback.class, listeners -> builder -> {
        for (BuildCallback listener : listeners) {
            listener.build(builder);
        }
    });

    default public void registerItemRecipe(Item input, Ingredient ingredient, Item output) {
        throw new AssertionError((Object)"Must be implemented via interface injection");
    }

    default public void registerPotionRecipe(Holder<Potion> input, Ingredient ingredient, Holder<Potion> output) {
        throw new AssertionError((Object)"Must be implemented via interface injection");
    }

    default public void registerRecipes(Ingredient ingredient, Holder<Potion> potion) {
        throw new AssertionError((Object)"Must be implemented via interface injection");
    }

    default public FeatureFlagSet getEnabledFeatures() {
        throw new AssertionError((Object)"Must be implemented via interface injection");
    }

    @FunctionalInterface
    public static interface BuildCallback {
        public void build(PotionBrewing.Builder var1);
    }
}

