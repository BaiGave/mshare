/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public record BackendOptions(boolean exclusiveFullScreen) {
}

