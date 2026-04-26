/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.dimension;

import com.mojang.datafixers.types.Type;
import com.mojang.datafixers.types.templates.TaggedChoice;
import com.mojang.datafixers.util.Pair;
import net.fabricmc.fabric.impl.dimension.TaggedChoiceExtension;
import net.fabricmc.fabric.impl.dimension.TaggedChoiceTypeExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={TaggedChoice.class})
public class TaggedChoiceMixin
implements TaggedChoiceExtension {
    @Unique
    boolean failSoft = false;

    @Override
    public void fabric$setFailSoft(boolean cond) {
        this.failSoft = cond;
    }

    @Inject(method={"lambda$apply$0"}, at={@At(value="RETURN")})
    private void onApply(Pair key, CallbackInfoReturnable<Type> cir) {
        Type returnValue;
        if (this.failSoft && (returnValue = cir.getReturnValue()) instanceof TaggedChoice.TaggedChoiceType) {
            TaggedChoice.TaggedChoiceType taggedChoiceType = (TaggedChoice.TaggedChoiceType)returnValue;
            ((TaggedChoiceTypeExtension)((Object)taggedChoiceType)).fabric$setFailSoft(true);
        }
    }
}

