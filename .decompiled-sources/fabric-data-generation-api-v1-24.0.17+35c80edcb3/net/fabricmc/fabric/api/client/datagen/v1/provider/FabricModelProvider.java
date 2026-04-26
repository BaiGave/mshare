/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.datagen.v1.provider;

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;

public abstract class FabricModelProvider
extends ModelProvider {
    public FabricModelProvider(FabricPackOutput output) {
        super(output);
    }

    public abstract void generateBlockStateModels(BlockModelGenerators var1);

    public abstract void generateItemModels(ItemModelGenerators var1);
}

