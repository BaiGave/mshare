/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.biome.v1;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.fabricmc.fabric.api.biome.v1.BiomeModificationContext;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectionContext;
import net.fabricmc.fabric.api.biome.v1.ModificationPhase;
import net.fabricmc.fabric.impl.biome.modification.BiomeModificationImpl;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.ApiStatus;

public class BiomeModification {
    private final Identifier id;

    @ApiStatus.Internal
    BiomeModification(Identifier id) {
        this.id = id;
    }

    public BiomeModification add(ModificationPhase phase, Predicate<BiomeSelectionContext> selector, Consumer<BiomeModificationContext> modifier) {
        BiomeModificationImpl.INSTANCE.addModifier(this.id, phase, selector, modifier);
        return this;
    }

    public BiomeModification add(ModificationPhase phase, Predicate<BiomeSelectionContext> selector, BiConsumer<BiomeSelectionContext, BiomeModificationContext> modifier) {
        BiomeModificationImpl.INSTANCE.addModifier(this.id, phase, selector, modifier);
        return this;
    }
}

