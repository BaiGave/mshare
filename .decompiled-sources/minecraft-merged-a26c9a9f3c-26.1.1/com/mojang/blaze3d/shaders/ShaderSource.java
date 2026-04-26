/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

import com.mojang.blaze3d.shaders.ShaderType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

@FunctionalInterface
@Environment(value=EnvType.CLIENT)
public interface ShaderSource {
    public @Nullable String get(Identifier var1, ShaderType var2);
}

