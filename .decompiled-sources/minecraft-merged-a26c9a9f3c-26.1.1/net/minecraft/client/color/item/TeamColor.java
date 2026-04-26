/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.color.item;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public record TeamColor(int defaultColor) implements ItemTintSource
{
    public static final MapCodec<TeamColor> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default")).forGetter(TeamColor::defaultColor)).apply((Applicative<TeamColor, ?>)i, TeamColor::new));

    @Override
    public int calculate(ItemStack itemStack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        ChatFormatting color;
        PlayerTeam team;
        if (owner != null && (team = owner.getTeam()) != null && (color = ((Team)team).getColor()).getColor() != null) {
            return ARGB.opaque(color.getColor());
        }
        return ARGB.opaque(this.defaultColor);
    }

    public MapCodec<TeamColor> type() {
        return MAP_CODEC;
    }
}

