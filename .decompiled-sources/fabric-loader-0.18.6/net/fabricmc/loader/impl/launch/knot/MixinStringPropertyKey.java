/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.launch.knot;

import java.util.Objects;
import org.spongepowered.asm.service.IPropertyKey;

public class MixinStringPropertyKey
implements IPropertyKey {
    public final String key;

    public MixinStringPropertyKey(String key) {
        this.key = key;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof MixinStringPropertyKey)) {
            return false;
        }
        return Objects.equals(this.key, ((MixinStringPropertyKey)obj).key);
    }

    public int hashCode() {
        return this.key.hashCode();
    }

    public String toString() {
        return this.key;
    }
}

