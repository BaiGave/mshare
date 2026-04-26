/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class ItemBannerColorFix
extends DataFix {
    public ItemBannerColorFix(Schema outputSchema, boolean changesType) {
        super(outputSchema, changesType);
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<Pair<String, String>> idF = DSL.fieldFinder("id", DSL.named(References.ITEM_NAME.typeName(), NamespacedSchema.namespacedString()));
        OpticFinder<?> tagF = itemStackType.findField("tag");
        OpticFinder<?> blockEntityF = tagF.type().findField("BlockEntityTag");
        return this.fixTypeEverywhereTyped("ItemBannerColorFix", itemStackType, input -> {
            Optional id = input.getOptional(idF);
            if (id.isPresent() && Objects.equals(((Pair)id.get()).getSecond(), "minecraft:banner")) {
                Typed<Dynamic<?>> tag;
                Optional blockEntityOpt;
                Dynamic rest = input.get(DSL.remainderFinder());
                Optional tagOpt = input.getOptionalTyped(tagF);
                if (tagOpt.isPresent() && (blockEntityOpt = (tag = tagOpt.get()).getOptionalTyped(blockEntityF)).isPresent()) {
                    Typed<Dynamic<?>> blockEntity = blockEntityOpt.get();
                    Dynamic<?> tagRest = tag.get(DSL.remainderFinder());
                    Dynamic<?> blockEntityRest = blockEntity.getOrCreate(DSL.remainderFinder());
                    if (blockEntityRest.get("Base").asNumber().result().isPresent()) {
                        Dynamic pickMarker;
                        Dynamic display;
                        rest = rest.set("Damage", rest.createShort((short)(blockEntityRest.get("Base").asInt(0) & 0xF)));
                        Optional<Dynamic<?>> displayOptional = tagRest.get("display").result();
                        if (displayOptional.isPresent() && Objects.equals(display = displayOptional.get(), pickMarker = display.createMap(ImmutableMap.of(display.createString("Lore"), display.createList(Stream.of(display.createString("(+NBT"))))))) {
                            return input.set(DSL.remainderFinder(), rest);
                        }
                        blockEntityRest.remove("Base");
                        return input.set(DSL.remainderFinder(), rest).set(tagF, tag.set(blockEntityF, blockEntity.set(DSL.remainderFinder(), blockEntityRest)));
                    }
                }
                return input.set(DSL.remainderFinder(), rest);
            }
            return input;
        });
    }
}

