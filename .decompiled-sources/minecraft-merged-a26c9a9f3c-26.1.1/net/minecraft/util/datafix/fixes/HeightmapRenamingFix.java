/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.minecraft.util.datafix.fixes.References;

public class HeightmapRenamingFix
extends DataFix {
    public HeightmapRenamingFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> inputType = this.getInputSchema().getType(References.CHUNK);
        OpticFinder<?> levelF = inputType.findField("Level");
        return this.fixTypeEverywhereTyped("HeightmapRenamingFix", inputType, input -> input.updateTyped(levelF, level -> level.update(DSL.remainderFinder(), this::fix)));
    }

    private Dynamic<?> fix(Dynamic<?> tag) {
        Optional<Dynamic<?>> rain;
        Optional<Dynamic<?>> light;
        Optional<Dynamic<?>> solid;
        Optional<Dynamic<?>> heightmaps = tag.get("Heightmaps").result();
        if (heightmaps.isEmpty()) {
            return tag;
        }
        Dynamic<?> heightmapsTag = heightmaps.get();
        Optional<Dynamic<?>> liquid = heightmapsTag.get("LIQUID").result();
        if (liquid.isPresent()) {
            heightmapsTag = heightmapsTag.remove("LIQUID");
            heightmapsTag = heightmapsTag.set("WORLD_SURFACE_WG", liquid.get());
        }
        if ((solid = heightmapsTag.get("SOLID").result()).isPresent()) {
            heightmapsTag = heightmapsTag.remove("SOLID");
            heightmapsTag = heightmapsTag.set("OCEAN_FLOOR_WG", solid.get());
            heightmapsTag = heightmapsTag.set("OCEAN_FLOOR", solid.get());
        }
        if ((light = heightmapsTag.get("LIGHT").result()).isPresent()) {
            heightmapsTag = heightmapsTag.remove("LIGHT");
            heightmapsTag = heightmapsTag.set("LIGHT_BLOCKING", light.get());
        }
        if ((rain = heightmapsTag.get("RAIN").result()).isPresent()) {
            heightmapsTag = heightmapsTag.remove("RAIN");
            heightmapsTag = heightmapsTag.set("MOTION_BLOCKING", rain.get());
            heightmapsTag = heightmapsTag.set("MOTION_BLOCKING_NO_LEAVES", rain.get());
        }
        return tag.set("Heightmaps", heightmapsTag);
    }
}

