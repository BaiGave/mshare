/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.datagen.v1;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.JsonKeySortOrderCallback;
import net.minecraft.core.RegistrySetBuilder;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
public interface DataGeneratorEntrypoint {
    public void onInitializeDataGenerator(FabricDataGenerator var1);

    default public @Nullable String getEffectiveModId() {
        return null;
    }

    default public void buildRegistry(RegistrySetBuilder registryBuilder) {
    }

    default public void addJsonKeySortOrders(JsonKeySortOrderCallback callback) {
    }
}

