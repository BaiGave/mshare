/*
 * Decompiled with CFR 0.152.
 */
package org.spongepowered.asm.launch.platform.container;

import java.util.Collection;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigSource;

public interface IContainerHandle
extends IMixinConfigSource {
    public String getAttribute(String var1);

    public Collection<IContainerHandle> getNestedContainers();
}

