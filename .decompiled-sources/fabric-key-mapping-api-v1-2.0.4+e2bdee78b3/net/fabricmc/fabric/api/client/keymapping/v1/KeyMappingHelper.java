/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.client.keymapping.v1;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.Objects;
import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;
import net.fabricmc.fabric.mixin.client.keymapping.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;

public final class KeyMappingHelper {
    private KeyMappingHelper() {
    }

    public static KeyMapping registerKeyMapping(KeyMapping keyMapping) {
        Objects.requireNonNull(keyMapping, "key mapping cannot be null");
        return KeyMappingRegistryImpl.registerKeyMapping(keyMapping);
    }

    public static InputConstants.Key getBoundKeyOf(KeyMapping keyMapping) {
        return ((KeyMappingAccessor)((Object)keyMapping)).fabric_getBoundKey();
    }
}

