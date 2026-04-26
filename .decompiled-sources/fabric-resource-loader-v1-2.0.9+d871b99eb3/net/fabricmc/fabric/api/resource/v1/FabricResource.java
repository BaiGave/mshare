/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.resource.v1;

import net.minecraft.server.packs.repository.PackSource;
import org.slf4j.LoggerFactory;

public interface FabricResource {
    default public PackSource getFabricPackSource() {
        LoggerFactory.getLogger(FabricResource.class).error("Unknown Resource implementation {}, returning DEFAULT as the source", (Object)this.getClass().getName());
        return PackSource.DEFAULT;
    }
}

