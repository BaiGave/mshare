/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.item;

import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.minecraft.resources.DependantName;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value={Item.Properties.class})
public class ItemPropertiesMixin
implements FabricItem.Properties {
    @Final
    @Shadow
    @Mutable
    private DependantName<Item, Identifier> model;
    @Shadow
    private @Nullable ResourceKey<Item> id;

    @Override
    public Item.Properties modelId(Identifier modelId) {
        this.model = DependantName.fixed(modelId);
        return FabricItem.Properties.super.modelId(modelId);
    }

    @Override
    public @Nullable ResourceKey<Item> itemId() {
        return this.id;
    }
}

