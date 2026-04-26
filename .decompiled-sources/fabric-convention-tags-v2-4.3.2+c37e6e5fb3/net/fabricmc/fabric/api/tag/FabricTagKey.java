/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.tag;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;

public interface FabricTagKey {
    default public String getTranslationKey() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("tag.");
        TagKey tagKey = (TagKey)this;
        Identifier registryIdentifier = tagKey.registry().identifier();
        Identifier tagIdentifier = tagKey.location();
        if (!registryIdentifier.getNamespace().equals("minecraft")) {
            stringBuilder.append(registryIdentifier.getNamespace()).append(".");
        }
        stringBuilder.append(registryIdentifier.getPath().replace("/", ".")).append(".").append(tagIdentifier.getNamespace()).append(".").append(tagIdentifier.getPath().replace("/", ".").replace(":", "."));
        return stringBuilder.toString();
    }

    default public Component getName() {
        return Component.translatableWithFallback(this.getTranslationKey(), "#" + ((TagKey)this).location().toString());
    }
}

