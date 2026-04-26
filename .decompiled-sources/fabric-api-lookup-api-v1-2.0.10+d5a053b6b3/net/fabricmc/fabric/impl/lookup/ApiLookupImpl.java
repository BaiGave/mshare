/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.lookup;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.impl.lookup.entity.EntityApiLookupImpl;

public class ApiLookupImpl
implements ModInitializer {
    @Override
    public void onInitialize() {
        ServerLifecycleEvents.SERVER_STARTED.register(EntityApiLookupImpl::checkSelfImplementingTypes);
    }
}

