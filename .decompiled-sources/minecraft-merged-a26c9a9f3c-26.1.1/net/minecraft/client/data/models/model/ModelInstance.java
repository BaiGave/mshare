/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.data.models.model;

import com.google.gson.JsonElement;
import java.util.function.Supplier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public interface ModelInstance
extends Supplier<JsonElement> {
}

