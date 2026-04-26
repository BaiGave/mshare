/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.event.lifecycle.v1;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.core.RegistryAccess;

public final class CommonLifecycleEvents {
    public static final Event<TagsLoaded> TAGS_LOADED = EventFactory.createArrayBacked(TagsLoaded.class, callbacks -> (registries, client) -> {
        for (TagsLoaded callback : callbacks) {
            callback.onTagsLoaded(registries, client);
        }
    });

    private CommonLifecycleEvents() {
    }

    public static interface TagsLoaded {
        public void onTagsLoaded(RegistryAccess var1, boolean var2);
    }
}

