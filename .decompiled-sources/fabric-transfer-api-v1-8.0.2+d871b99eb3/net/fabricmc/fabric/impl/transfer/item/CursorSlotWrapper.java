/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.transfer.item;

import com.google.common.collect.MapMaker;
import java.util.Map;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CursorSlotWrapper
extends SingleStackStorage {
    private static final Map<AbstractContainerMenu, CursorSlotWrapper> WRAPPERS = new MapMaker().weakValues().makeMap();
    private final AbstractContainerMenu menu;

    public static CursorSlotWrapper get(AbstractContainerMenu menu) {
        return WRAPPERS.computeIfAbsent(menu, CursorSlotWrapper::new);
    }

    private CursorSlotWrapper(AbstractContainerMenu menu) {
        this.menu = menu;
    }

    @Override
    protected ItemStack getStack() {
        return this.menu.getCarried();
    }

    @Override
    protected void setStack(ItemStack stack) {
        this.menu.setCarried(stack);
    }

    @Override
    public String toString() {
        return "CursorSlotWrapper[" + String.valueOf(this.menu) + "/" + String.valueOf(BuiltInRegistries.MENU.getKey(this.menu.getType())) + "]";
    }
}

