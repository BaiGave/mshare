/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.systems;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
public class BackendCreationException
extends Exception {
    public BackendCreationException(String message) {
        super(message);
    }
}

