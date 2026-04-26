/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import java.util.function.BiConsumer;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={ModelPart.class})
public interface ModelPartAccessor {
    @Invoker(value="addAllChildren")
    public void fabric$callAddAllChildren(BiConsumer<String, ModelPart> var1);
}

