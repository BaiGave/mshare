/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.model.loading;

import com.google.gson.Gson;
import net.minecraft.client.resources.model.cuboid.CuboidModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={CuboidModel.class})
public interface CuboidModelAccessor {
    @Accessor(value="GSON")
    public static Gson fabric_getGson() {
        throw new AssertionError();
    }
}

