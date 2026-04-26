/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.creativetab.v1;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.ApiStatus;

public class FabricCreativeModeTabOutput
implements CreativeModeTab.Output {
    private final CreativeModeTab.ItemDisplayParameters context;
    private final List<ItemStack> displayStacks;
    private final List<ItemStack> searchTabStacks;

    @ApiStatus.Internal
    public FabricCreativeModeTabOutput(CreativeModeTab.ItemDisplayParameters context, List<ItemStack> displayStacks, List<ItemStack> searchTabStacks) {
        this.context = context;
        this.displayStacks = displayStacks;
        this.searchTabStacks = searchTabStacks;
    }

    public CreativeModeTab.ItemDisplayParameters getContext() {
        return this.context;
    }

    public FeatureFlagSet getEnabledFeatures() {
        return this.context.enabledFeatures();
    }

    public boolean shouldShowOpRestrictedItems() {
        return this.context.hasPermissions();
    }

    public List<ItemStack> getDisplayStacks() {
        return this.displayStacks;
    }

    public List<ItemStack> getSearchTabStacks() {
        return this.searchTabStacks;
    }

    @Override
    public void accept(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        if (this.isEnabled(stack)) {
            FabricCreativeModeTabOutput.checkStack(stack);
            switch (visibility) {
                case PARENT_AND_SEARCH_TABS: {
                    this.displayStacks.add(stack);
                    this.searchTabStacks.add(stack);
                    break;
                }
                case PARENT_TAB_ONLY: {
                    this.displayStacks.add(stack);
                    break;
                }
                case SEARCH_TAB_ONLY: {
                    this.searchTabStacks.add(stack);
                }
            }
        }
    }

    public void prepend(ItemStack stack) {
        this.prepend(stack, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void prepend(ItemStack stack, CreativeModeTab.TabVisibility visibility) {
        if (this.isEnabled(stack)) {
            FabricCreativeModeTabOutput.checkStack(stack);
            switch (visibility) {
                case PARENT_AND_SEARCH_TABS: {
                    this.displayStacks.add(0, stack);
                    this.searchTabStacks.add(0, stack);
                    break;
                }
                case PARENT_TAB_ONLY: {
                    this.displayStacks.add(0, stack);
                    break;
                }
                case SEARCH_TAB_ONLY: {
                    this.searchTabStacks.add(0, stack);
                }
            }
        }
    }

    public void prepend(ItemLike item) {
        this.prepend(item, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void prepend(ItemLike item, CreativeModeTab.TabVisibility visibility) {
        this.prepend(new ItemStack(item), visibility);
    }

    public void insertAfter(ItemLike afterLast, ItemStack ... newStack) {
        this.insertAfter(afterLast, Arrays.asList(newStack));
    }

    public void insertAfter(ItemStack afterLast, ItemStack ... newStack) {
        this.insertAfter(afterLast, Arrays.asList(newStack));
    }

    public void insertAfter(ItemLike afterLast, ItemLike ... newItem) {
        this.insertAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
    }

    public void insertAfter(ItemStack afterLast, ItemLike ... newItem) {
        this.insertAfter(afterLast, Arrays.stream(newItem).map(ItemStack::new).toList());
    }

    public void insertAfter(ItemLike afterLast, Collection<ItemStack> newStacks) {
        this.insertAfter(afterLast, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void insertAfter(ItemStack afterLast, Collection<ItemStack> newStacks) {
        this.insertAfter(afterLast, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void insertAfter(ItemLike afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
            }
        }
    }

    public void insertAfter(ItemStack afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
            }
        }
    }

    public void insertAfter(Predicate<ItemStack> afterLast, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertAfter(afterLast, newStacks, this.searchTabStacks);
            }
        }
    }

    public void insertBefore(ItemLike beforeFirst, ItemStack ... newStack) {
        this.insertBefore(beforeFirst, Arrays.asList(newStack));
    }

    public void insertBefore(ItemStack beforeFirst, ItemStack ... newStack) {
        this.insertBefore(beforeFirst, Arrays.asList(newStack));
    }

    public void insertBefore(ItemLike beforeFirst, ItemLike ... newItem) {
        this.insertBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
    }

    public void insertBefore(ItemStack beforeFirst, ItemLike ... newItem) {
        this.insertBefore(beforeFirst, Arrays.stream(newItem).map(ItemStack::new).toList());
    }

    public void insertBefore(ItemLike beforeFirst, Collection<ItemStack> newStacks) {
        this.insertBefore(beforeFirst, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void insertBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks) {
        this.insertBefore(beforeFirst, newStacks, CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS);
    }

    public void insertBefore(ItemLike beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
            }
        }
    }

    public void insertBefore(ItemStack beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
            }
        }
    }

    public void insertBefore(Predicate<ItemStack> beforeFirst, Collection<ItemStack> newStacks, CreativeModeTab.TabVisibility visibility) {
        if ((newStacks = this.getEnabledStacks(newStacks)).isEmpty()) {
            return;
        }
        switch (visibility) {
            case PARENT_AND_SEARCH_TABS: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
                break;
            }
            case PARENT_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.displayStacks);
                break;
            }
            case SEARCH_TAB_ONLY: {
                FabricCreativeModeTabOutput.insertBefore(beforeFirst, newStacks, this.searchTabStacks);
            }
        }
    }

    private boolean isEnabled(ItemStack stack) {
        return stack.getItem().isEnabled(this.getEnabledFeatures());
    }

    private Collection<ItemStack> getEnabledStacks(Collection<ItemStack> newStacks) {
        if (newStacks.stream().allMatch(this::isEnabled)) {
            return newStacks;
        }
        return newStacks.stream().filter(this::isEnabled).toList();
    }

    private static void insertBefore(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        for (int i = 0; i < addTo.size(); ++i) {
            if (!predicate.test(addTo.get(i))) continue;
            addTo.subList(i, i).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void insertAfter(Predicate<ItemStack> predicate, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        for (int i = addTo.size() - 1; i >= 0; --i) {
            if (!predicate.test(addTo.get(i))) continue;
            addTo.subList(i + 1, i + 1).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void insertBefore(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        for (int i = 0; i < addTo.size(); ++i) {
            if (!ItemStack.isSameItemSameComponents(anchor, addTo.get(i))) continue;
            addTo.subList(i, i).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void insertAfter(ItemStack anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        for (int i = addTo.size() - 1; i >= 0; --i) {
            if (!ItemStack.isSameItemSameComponents(anchor, addTo.get(i))) continue;
            addTo.subList(i + 1, i + 1).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void insertBefore(ItemLike anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        Item anchorItem = anchor.asItem();
        for (int i = 0; i < addTo.size(); ++i) {
            if (!addTo.get(i).is(anchorItem)) continue;
            addTo.subList(i, i).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void insertAfter(ItemLike anchor, Collection<ItemStack> newStacks, List<ItemStack> addTo) {
        FabricCreativeModeTabOutput.checkStacks(newStacks);
        Item anchorItem = anchor.asItem();
        for (int i = addTo.size() - 1; i >= 0; --i) {
            if (!addTo.get(i).is(anchorItem)) continue;
            addTo.subList(i + 1, i + 1).addAll(newStacks);
            return;
        }
        addTo.addAll(newStacks);
    }

    private static void checkStacks(Collection<ItemStack> stacks) {
        for (ItemStack stack : stacks) {
            FabricCreativeModeTabOutput.checkStack(stack);
        }
    }

    private static void checkStack(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("Cannot add empty stack");
        }
        if (stack.getCount() != 1) {
            throw new IllegalArgumentException("Stack size must be exactly 1 for stack: " + String.valueOf(stack));
        }
    }
}

