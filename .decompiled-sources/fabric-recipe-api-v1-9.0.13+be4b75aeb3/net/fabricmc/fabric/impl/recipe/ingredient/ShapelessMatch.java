/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.recipe.ingredient;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

public class ShapelessMatch {
    private final int[] match;
    private final BitSet bitSet;

    private ShapelessMatch(int size) {
        this.match = new int[size];
        this.bitSet = new BitSet(size * (size + 1));
    }

    private boolean augment(int l) {
        if (this.bitSet.get(l)) {
            return false;
        }
        this.bitSet.set(l);
        for (int r = 0; r < this.match.length; ++r) {
            if (!this.bitSet.get(this.match.length + l * this.match.length + r) || this.match[r] != -1 && !this.augment(this.match[r])) continue;
            this.match[r] = l;
            return true;
        }
        return false;
    }

    public static boolean isMatch(List<ItemStack> stacks, List<Ingredient> ingredients) {
        int i;
        if (stacks.size() != ingredients.size()) {
            return false;
        }
        ShapelessMatch m = new ShapelessMatch(ingredients.size());
        for (i = 0; i < stacks.size(); ++i) {
            ItemStack stack = stacks.get(i);
            for (int j = 0; j < ingredients.size(); ++j) {
                if (!ingredients.get(j).test(stack)) continue;
                m.bitSet.set((i + 1) * m.match.length + j);
            }
        }
        Arrays.fill(m.match, -1);
        for (i = 0; i < ingredients.size(); ++i) {
            if (!m.augment(i)) {
                return false;
            }
            m.bitSet.set(0, m.match.length, false);
        }
        return true;
    }
}

