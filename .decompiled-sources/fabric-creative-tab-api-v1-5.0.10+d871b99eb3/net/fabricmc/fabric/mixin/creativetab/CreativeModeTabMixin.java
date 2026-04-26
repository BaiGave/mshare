/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.creativetab;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTabOutput;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.impl.creativetab.CreativeModeTabEventsImpl;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabImpl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CreativeModeTab.class})
abstract class CreativeModeTabMixin
implements FabricCreativeModeTabImpl {
    @Shadow
    private Collection<ItemStack> displayItems;
    @Shadow
    private Set<ItemStack> displayItemsSearchTab;
    @Unique
    private int page = -1;

    CreativeModeTabMixin() {
    }

    @Inject(method={"buildContents"}, at={@At(value="TAIL")})
    public void getStacks(CreativeModeTab.ItemDisplayParameters context, CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab)((Object)this);
        ResourceKey<CreativeModeTab> resourceKey = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(self).orElseThrow(() -> new IllegalStateException("Unregistered creative mode tab : " + String.valueOf(self)));
        if (self.isAlignedRight() && resourceKey != CreativeModeTabs.OP_BLOCKS) {
            return;
        }
        Objects.requireNonNull(this.displayItems, "displayStacks");
        Objects.requireNonNull(this.displayItemsSearchTab, "searchTabStacks");
        LinkedList<ItemStack> mutableDisplayStacks = new LinkedList<ItemStack>(this.displayItems);
        LinkedList<ItemStack> mutableSearchTabStacks = new LinkedList<ItemStack>(this.displayItemsSearchTab);
        FabricCreativeModeTabOutput entries = new FabricCreativeModeTabOutput(context, mutableDisplayStacks, mutableSearchTabStacks);
        if (resourceKey != CreativeModeTabs.OP_BLOCKS || context.hasPermissions()) {
            Event<CreativeModeTabEvents.ModifyOutput> modifyEntriesEvent = CreativeModeTabEventsImpl.getModifyOutputEvent(resourceKey);
            if (modifyEntriesEvent != null) {
                modifyEntriesEvent.invoker().modifyOutput(entries);
            }
            CreativeModeTabEvents.MODIFY_OUTPUT_ALL.invoker().modifyOutput(self, entries);
        }
        this.displayItems.clear();
        this.displayItems.addAll(mutableDisplayStacks);
        this.displayItemsSearchTab.clear();
        this.displayItemsSearchTab.addAll(mutableSearchTabStacks);
    }

    @Override
    public int fabric_getPage() {
        if (this.page < 0) {
            throw new IllegalStateException("Creative mode tab has no page");
        }
        return this.page;
    }

    @Override
    public void fabric_setPage(int page) {
        this.page = page;
    }
}

