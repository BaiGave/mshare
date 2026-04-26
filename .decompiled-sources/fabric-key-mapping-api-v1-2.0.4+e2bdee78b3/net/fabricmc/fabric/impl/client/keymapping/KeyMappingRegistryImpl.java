/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.keymapping;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class KeyMappingRegistryImpl {
    private static final List<KeyMapping> MODDED_KEY_BINDINGS = new ReferenceArrayList<KeyMapping>();

    private KeyMappingRegistryImpl() {
    }

    public static KeyMapping registerKeyMapping(KeyMapping binding) {
        if (Minecraft.getInstance().options != null) {
            throw new IllegalStateException("GameOptions has already been initialised");
        }
        for (KeyMapping existingKeyMappings : MODDED_KEY_BINDINGS) {
            if (existingKeyMappings == binding) {
                throw new IllegalArgumentException("Attempted to register a key mapping twice: " + binding.getName());
            }
            if (!existingKeyMappings.getName().equals(binding.getName())) continue;
            throw new IllegalArgumentException("Attempted to register two key mappings with equal ID: " + binding.getName() + "!");
        }
        MODDED_KEY_BINDINGS.add(binding);
        return binding;
    }

    public static KeyMapping[] process(KeyMapping[] keysAll) {
        ArrayList<KeyMapping> newKeysAll = Lists.newArrayList(keysAll);
        newKeysAll.removeAll(MODDED_KEY_BINDINGS);
        newKeysAll.addAll(MODDED_KEY_BINDINGS);
        return newKeysAll.toArray(new KeyMapping[0]);
    }
}

