/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.transfer;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.component.ItemContainerContents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value={ItemContainerContents.class})
public interface ItemContainerContentsAccessor {
    @Accessor(value="items")
    public List<Optional<ItemStackTemplate>> fabric_getItems();
}

