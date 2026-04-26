/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.util.datafix.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.OpticFinder;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.Typed;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.util.datafix.schemas.NamespacedSchema;

public class RemoveBlockEntityTagFix
extends DataFix {
    private final Set<String> blockEntityIdsToDrop;

    public RemoveBlockEntityTagFix(Schema outputSchema, Set<String> blockEntityIdsToDrop) {
        super(outputSchema, true);
        this.blockEntityIdsToDrop = blockEntityIdsToDrop;
    }

    @Override
    public TypeRewriteRule makeRule() {
        Type<?> itemStackType = this.getInputSchema().getType(References.ITEM_STACK);
        OpticFinder<?> itemTagF = itemStackType.findField("tag");
        OpticFinder<?> itemBlockEntityF = itemTagF.type().findField("BlockEntityTag");
        Type<?> entityType = this.getInputSchema().getType(References.ENTITY);
        OpticFinder<?> fallingBlockF = DSL.namedChoice("minecraft:falling_block", this.getInputSchema().getChoiceType(References.ENTITY, "minecraft:falling_block"));
        OpticFinder<?> fallingBlockEntityTagF = fallingBlockF.type().findField("TileEntityData");
        Type<?> structureType = this.getInputSchema().getType(References.STRUCTURE);
        OpticFinder<?> blocksF = structureType.findField("blocks");
        OpticFinder blockTypeF = DSL.typeFinder(((List.ListType)blocksF.type()).getElement());
        OpticFinder<?> blockNbtF = blockTypeF.type().findField("nbt");
        OpticFinder<String> blockEntityIdF = DSL.fieldFinder("id", NamespacedSchema.namespacedString());
        return TypeRewriteRule.seq(this.fixTypeEverywhereTyped("ItemRemoveBlockEntityTagFix", itemStackType, input -> input.updateTyped(itemTagF, tag -> this.removeBlockEntity((Typed<?>)tag, itemBlockEntityF, blockEntityIdF, "BlockEntityTag"))), this.fixTypeEverywhereTyped("FallingBlockEntityRemoveBlockEntityTagFix", entityType, input -> input.updateTyped(fallingBlockF, tag -> this.removeBlockEntity((Typed<?>)tag, fallingBlockEntityTagF, blockEntityIdF, "TileEntityData"))), this.fixTypeEverywhereTyped("StructureRemoveBlockEntityTagFix", structureType, input -> input.updateTyped(blocksF, tag -> tag.updateTyped(blockTypeF, blockTag -> this.removeBlockEntity((Typed<?>)blockTag, blockNbtF, blockEntityIdF, "nbt")))), this.convertUnchecked("ItemRemoveBlockEntityTagFix - update block entity type", this.getInputSchema().getType(References.BLOCK_ENTITY), this.getOutputSchema().getType(References.BLOCK_ENTITY)));
    }

    private Typed<?> removeBlockEntity(Typed<?> tag, OpticFinder<?> blockEntityF, OpticFinder<String> blockEntityIdF, String blockEntityFieldName) {
        Optional<Typed<?>> maybeBlockEntity = tag.getOptionalTyped(blockEntityF);
        if (maybeBlockEntity.isEmpty()) {
            return tag;
        }
        String blockEntityId = maybeBlockEntity.get().getOptional(blockEntityIdF).orElse("");
        if (!this.blockEntityIdsToDrop.contains(blockEntityId)) {
            return tag;
        }
        return Util.writeAndReadTypedOrThrow(tag, tag.getType(), tagData -> tagData.remove(blockEntityFieldName));
    }
}

