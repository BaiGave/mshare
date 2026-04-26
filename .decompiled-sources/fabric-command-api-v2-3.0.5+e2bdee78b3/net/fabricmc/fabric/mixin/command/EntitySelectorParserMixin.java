/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.command;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.command.v2.FabricEntitySelectorParser;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(value={EntitySelectorParser.class})
public class EntitySelectorParserMixin
implements FabricEntitySelectorParser {
    @Unique
    private final Set<Identifier> flags = new HashSet<Identifier>();

    @Override
    public void setCustomFlag(Identifier key, boolean value) {
        if (value) {
            this.flags.add(key);
        } else {
            this.flags.remove(key);
        }
    }

    @Override
    public boolean getCustomFlag(Identifier key) {
        return this.flags.contains(key);
    }
}

