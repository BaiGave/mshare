/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.tag;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.impl.tag.TagAliasLoader;
import net.minecraft.server.packs.PackType;

public final class TagInit
implements ModInitializer {
    @Override
    public void onInitialize() {
        ResourceLoader.get(PackType.SERVER_DATA).registerReloadListener(TagAliasLoader.ID, new TagAliasLoader());
    }
}

