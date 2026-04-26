/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.model.loading;

import java.util.Map;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import org.jspecify.annotations.Nullable;

public interface BakedModelsHooks {
    public @Nullable Map<ExtraModelKey<?>, ?> fabric_getExtraModels();

    public void fabric_setExtraModels(@Nullable Map<ExtraModelKey<?>, ?> var1);
}

