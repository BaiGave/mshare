/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.item;

import java.util.List;
import net.fabricmc.fabric.api.item.v1.EnchantmentEvents;
import net.fabricmc.fabric.api.item.v1.EnchantmentSource;
import net.fabricmc.fabric.impl.resource.pack.BuiltinModPackSource;
import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.fabricmc.fabric.mixin.item.EnchantmentBuilderAccessor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.item.enchantment.Enchantment;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnchantmentUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(EnchantmentUtil.class);

    public static @Nullable Enchantment modify(ResourceKey<Enchantment> key, Enchantment originalEnchantment, EnchantmentSource source) {
        Enchantment.Builder builder = Enchantment.enchantment(originalEnchantment.definition());
        EnchantmentBuilderAccessor accessor = (EnchantmentBuilderAccessor)((Object)builder);
        BuilderExtensions builderExtensions = (BuilderExtensions)((Object)builder);
        builder.exclusiveWith(originalEnchantment.exclusiveSet());
        accessor.getEffectMap().addAll(originalEnchantment.effects());
        originalEnchantment.effects().stream().forEach(component -> {
            Object patt0$temp = component.value();
            if (patt0$temp instanceof List) {
                List valueList = (List)patt0$temp;
                accessor.invokeGetEffectsList(component.type()).addAll(valueList);
            }
        });
        builderExtensions.fabric$resetModified();
        EnchantmentEvents.MODIFY.invoker().modify(key, builder, source);
        if (builderExtensions.fabric$didModify()) {
            LOGGER.debug("Enchantment {} was modified", (Object)key.identifier());
            return new Enchantment(originalEnchantment.description(), accessor.getDefinition(), accessor.getExclusiveSet(), accessor.getEffectMap().build());
        }
        return null;
    }

    public static EnchantmentSource determineSource(Resource resource) {
        if (resource != null) {
            PackSource packSource = resource.getFabricPackSource();
            if (packSource == PackSource.BUILT_IN) {
                return EnchantmentSource.VANILLA;
            }
            if (packSource == ModResourcePackCreator.RESOURCE_PACK_SOURCE || packSource instanceof BuiltinModPackSource) {
                return EnchantmentSource.MOD;
            }
        }
        return EnchantmentSource.DATA_PACK;
    }

    private EnchantmentUtil() {
    }

    public static interface BuilderExtensions {
        public void fabric$resetModified();

        public boolean fabric$didModify();
    }
}

