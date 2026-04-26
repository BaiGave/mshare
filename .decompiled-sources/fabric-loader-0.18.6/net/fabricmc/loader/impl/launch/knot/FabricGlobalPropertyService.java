/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import net.fabricmc.loader.impl.launch.FabricLauncherBase;
import net.fabricmc.loader.impl.launch.knot.MixinStringPropertyKey;
import org.spongepowered.asm.service.IGlobalPropertyService;
import org.spongepowered.asm.service.IPropertyKey;

public class FabricGlobalPropertyService
implements IGlobalPropertyService {
    @Override
    public IPropertyKey resolveKey(String name) {
        return new MixinStringPropertyKey(name);
    }

    private String keyString(IPropertyKey key) {
        return ((MixinStringPropertyKey)key).key;
    }

    @Override
    public <T> T getProperty(IPropertyKey key) {
        return (T)FabricLauncherBase.getProperties().get(this.keyString(key));
    }

    @Override
    public void setProperty(IPropertyKey key, Object value) {
        FabricLauncherBase.getProperties().put(this.keyString(key), value);
    }

    @Override
    public <T> T getProperty(IPropertyKey key, T defaultValue) {
        return (T)FabricLauncherBase.getProperties().getOrDefault(this.keyString(key), defaultValue);
    }

    @Override
    public String getPropertyString(IPropertyKey key, String defaultValue) {
        Object o = FabricLauncherBase.getProperties().get(this.keyString(key));
        return o != null ? o.toString() : defaultValue;
    }
}

