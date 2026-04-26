/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.creativetab;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import net.fabricmc.fabric.impl.creativetab.FabricCreativeModeTabImpl;
import net.fabricmc.fabric.mixin.creativetab.CreativeModeTabAccessor;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value={CreativeModeTabs.class})
public class CreativeModeTabsMixin {
    @Unique
    private static final int TABS_PER_PAGE = 10;

    @Inject(method={"validate"}, at={@At(value="HEAD")}, cancellable=true)
    private static void deferDuplicateCheck(CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method={"buildAllTabContents"}, at={@At(value="TAIL")})
    private static void paginateTabs(CallbackInfo ci) {
        List<ResourceKey> vanillaTabs = List.of(CreativeModeTabs.BUILDING_BLOCKS, CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.NATURAL_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS, CreativeModeTabs.REDSTONE_BLOCKS, CreativeModeTabs.HOTBAR, CreativeModeTabs.SEARCH, CreativeModeTabs.TOOLS_AND_UTILITIES, CreativeModeTabs.COMBAT, CreativeModeTabs.FOOD_AND_DRINKS, CreativeModeTabs.INGREDIENTS, CreativeModeTabs.SPAWN_EGGS, CreativeModeTabs.OP_BLOCKS, CreativeModeTabs.INVENTORY);
        int count = 0;
        Comparator entryComparator = (e1, e2) -> {
            int displayCompare = Boolean.compare(((CreativeModeTab)e1.value()).shouldDisplay(), ((CreativeModeTab)e2.value()).shouldDisplay());
            if (displayCompare != 0) {
                return -displayCompare;
            }
            return CreativeModeTabsMixin.compareNamespaceFirst(e1.key().identifier(), e2.key().identifier());
        };
        List sortedCreativeModeTabs = BuiltInRegistries.CREATIVE_MODE_TAB.listElements().sorted(entryComparator).toList();
        for (Holder.Reference reference : sortedCreativeModeTabs) {
            CreativeModeTab creativeModeTab = (CreativeModeTab)reference.value();
            FabricCreativeModeTabImpl vanillaCreativeModeTab = (FabricCreativeModeTabImpl)((Object)creativeModeTab);
            if (vanillaTabs.contains(reference.key())) {
                vanillaCreativeModeTab.fabric_setPage(0);
                continue;
            }
            CreativeModeTabAccessor creativeModeTabAccessor = (CreativeModeTabAccessor)((Object)creativeModeTab);
            vanillaCreativeModeTab.fabric_setPage(count / 10 + 1);
            int pageIndex = count % 10;
            CreativeModeTab.Row row = pageIndex < 5 ? CreativeModeTab.Row.TOP : CreativeModeTab.Row.BOTTOM;
            creativeModeTabAccessor.setRow(row);
            creativeModeTabAccessor.setColumn(row == CreativeModeTab.Row.TOP ? pageIndex % 10 : (pageIndex - 5) % 10);
            ++count;
        }
        record CreativeModeTabPosition(CreativeModeTab.Row row, int column, int page) {
        }
        HashMap<CreativeModeTabPosition, String> map = new HashMap<CreativeModeTabPosition, String>();
        for (ResourceKey<CreativeModeTab> resourceKey : BuiltInRegistries.CREATIVE_MODE_TAB.registryKeySet()) {
            CreativeModeTab creativeModeTab = BuiltInRegistries.CREATIVE_MODE_TAB.getValueOrThrow(resourceKey);
            FabricCreativeModeTabImpl vanillaCreativeModeTab = (FabricCreativeModeTabImpl)((Object)creativeModeTab);
            String displayName = creativeModeTab.getDisplayName().getString();
            CreativeModeTabPosition position = new CreativeModeTabPosition(creativeModeTab.row(), creativeModeTab.column(), vanillaCreativeModeTab.fabric_getPage());
            String existingName = map.put(position, displayName);
            if (existingName == null) continue;
            throw new IllegalArgumentException("Duplicate position: (%s) for creative mode tabs %s vs %s".formatted(position, displayName, existingName));
        }
    }

    @Unique
    private static int compareNamespaceFirst(Identifier a, Identifier b) {
        int c = a.getNamespace().compareTo(b.getNamespace());
        if (c != 0) {
            return c;
        }
        return a.getPath().compareTo(b.getPath());
    }
}

