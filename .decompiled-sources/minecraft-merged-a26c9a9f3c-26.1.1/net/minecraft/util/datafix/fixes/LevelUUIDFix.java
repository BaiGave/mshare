/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.datafixers.util.Unit;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import net.minecraft.util.datafix.fixes.AbstractUUIDFix;
import net.minecraft.util.datafix.fixes.References;
import org.slf4j.Logger;

public class LevelUUIDFix
extends AbstractUUIDFix {
    private static final Logger LOGGER = LogUtils.getLogger();

    public LevelUUIDFix(Schema outputSchema) {
        super(outputSchema, References.LEVEL);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        Type<?> type = this.getInputSchema().getType(this.typeReference);
        OpticFinder<?> customBossEventsF = type.findField("CustomBossEvents");
        OpticFinder<Pair<Either<?, Unit>, Dynamic<?>>> customBossEventF = DSL.typeFinder(DSL.and(DSL.optional(DSL.field("Name", this.getInputSchema().getTypeRaw(References.TEXT_COMPONENT))), DSL.remainderType()));
        return this.fixTypeEverywhereTyped("LevelUUIDFix", type, input -> input.update(DSL.remainderFinder(), tag -> {
            tag = this.updateDragonFight((Dynamic<?>)tag);
            tag = this.updateWanderingTrader((Dynamic<?>)tag);
            return tag;
        }).updateTyped(customBossEventsF, customBossEvents -> customBossEvents.updateTyped(customBossEventF, event -> event.update(DSL.remainderFinder(), this::updateCustomBossEvent))));
    }

    private Dynamic<?> updateWanderingTrader(Dynamic<?> tag) {
        return LevelUUIDFix.replaceUUIDString(tag, "WanderingTraderId", "WanderingTraderId").orElse(tag);
    }

    private Dynamic<?> updateDragonFight(Dynamic<?> tag) {
        return tag.update("DimensionData", dimensionDataMap -> dimensionDataMap.updateMapValues(dimensionDataPair -> dimensionDataPair.mapSecond(dimensionData -> dimensionData.update("DragonFight", dragonfight -> LevelUUIDFix.replaceUUIDLeastMost(dragonfight, "DragonUUID", "Dragon").orElse((Dynamic<?>)dragonfight)))));
    }

    private Dynamic<?> updateCustomBossEvent(Dynamic<?> tag) {
        return tag.update("Players", players -> tag.createList(players.asStream().map(player -> LevelUUIDFix.createUUIDFromML(player).orElseGet(() -> {
            LOGGER.warn("CustomBossEvents contains invalid UUIDs.");
            return player;
        }))));
    }
}

