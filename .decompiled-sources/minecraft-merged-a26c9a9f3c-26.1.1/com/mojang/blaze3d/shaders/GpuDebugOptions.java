/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.shaders;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record GpuDebugOptions(int logLevel, boolean synchronousLogs, boolean useLabels) {
}

