/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.CustomUnbakedBlockStateModel;
import net.fabricmc.fabric.impl.client.model.loading.CompositeBlockStateModelImpl;
import net.minecraft.resources.Identifier;

public class CustomUnbakedBlockStateModelInit
implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        CustomUnbakedBlockStateModel.register(Identifier.fromNamespaceAndPath("fabric", "composite"), CompositeBlockStateModelImpl.Unbaked.CODEC);
    }
}

