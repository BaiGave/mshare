/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.model;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.Identifier;

@Environment(value=EnvType.CLIENT)
public interface ResolvableModel {
    public void resolveDependencies(Resolver var1);

    @Environment(value=EnvType.CLIENT)
    public static interface Resolver {
        public void markDependency(Identifier var1);
    }
}

