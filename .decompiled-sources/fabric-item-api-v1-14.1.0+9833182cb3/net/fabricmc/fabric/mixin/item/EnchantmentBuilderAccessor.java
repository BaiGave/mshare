/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import java.util.List;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(value={Enchantment.Builder.class})
public interface EnchantmentBuilderAccessor {
    @Accessor(value="definition")
    public Enchantment.EnchantmentDefinition getDefinition();

    @Accessor(value="exclusiveSet")
    public HolderSet<Enchantment> getExclusiveSet();

    @Accessor(value="effectMapBuilder")
    public DataComponentMap.Builder getEffectMap();

    @Invoker(value="getEffectsList")
    public <E> List<E> invokeGetEffectsList(DataComponentType<List<E>> var1);
}

