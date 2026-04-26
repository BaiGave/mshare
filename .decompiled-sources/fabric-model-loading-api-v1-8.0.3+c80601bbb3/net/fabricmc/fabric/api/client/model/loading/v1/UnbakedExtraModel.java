/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.model.loading.v1;

import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;

public interface UnbakedExtraModel<T>
extends ResolvableModel {
    public T bake(ModelBaker var1);
}

