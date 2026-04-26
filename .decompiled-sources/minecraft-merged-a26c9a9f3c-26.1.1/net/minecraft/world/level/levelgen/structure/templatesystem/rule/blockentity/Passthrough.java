/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity;

import com.mojang.serialization.MapCodec;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.rule.blockentity.RuleBlockEntityModifierType;
import org.jspecify.annotations.Nullable;

public class Passthrough
implements RuleBlockEntityModifier {
    public static final Passthrough INSTANCE = new Passthrough();
    public static final MapCodec<Passthrough> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public @Nullable CompoundTag apply(RandomSource random, @Nullable CompoundTag existingTag) {
        return existingTag;
    }

    @Override
    public RuleBlockEntityModifierType<?> getType() {
        return RuleBlockEntityModifierType.PASSTHROUGH;
    }
}

