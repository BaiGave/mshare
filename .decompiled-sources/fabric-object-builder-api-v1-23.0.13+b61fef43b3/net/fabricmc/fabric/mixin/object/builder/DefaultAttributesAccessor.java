/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.object.builder;

import java.util.Map;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={DefaultAttributes.class})
public interface DefaultAttributesAccessor {
    @Accessor(value="SUPPLIERS")
    public static Map<EntityType<? extends LivingEntity>, AttributeSupplier> getRegistry() {
        throw new AssertionError((Object)"mixin dummy");
    }
}

