/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import java.util.Map;
import net.fabricmc.fabric.api.client.model.loading.v1.ExtraModelKey;
import net.fabricmc.fabric.impl.client.model.loading.BakedModelsHooks;
import net.minecraft.client.resources.model.ModelBakery;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={ModelBakery.BakingResult.class})
abstract class ModelBakeryBakingResultMixin
implements BakedModelsHooks {
    @Unique
    private @Nullable Map<ExtraModelKey<?>, ?> extraModels;

    ModelBakeryBakingResultMixin() {
    }

    @Override
    public @Nullable Map<ExtraModelKey<?>, ?> fabric_getExtraModels() {
        return this.extraModels;
    }

    @Override
    public void fabric_setExtraModels(@Nullable Map<ExtraModelKey<?>, ?> extraModels) {
        this.extraModels = extraModels;
    }
}

