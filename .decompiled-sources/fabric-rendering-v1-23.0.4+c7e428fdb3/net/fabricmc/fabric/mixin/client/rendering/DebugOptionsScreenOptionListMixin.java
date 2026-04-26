/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.client.rendering;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import java.util.Map;
import net.fabricmc.fabric.impl.client.rendering.DebugOptionsComparator;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets={"net.minecraft.client.gui.screens.debug.DebugOptionsScreen$OptionList"})
public class DebugOptionsScreenOptionListMixin {
    @Redirect(method={"lambda$static$0"}, at=@At(value="INVOKE", target="Lnet/minecraft/resources/Identifier;compareTo(Lnet/minecraft/resources/Identifier;)I"))
    private static int sort(Identifier o1, Identifier o2) {
        return DebugOptionsComparator.INSTANCE.compare(o1, o2);
    }

    @WrapOperation(method={"updateSearch"}, at={@At(value="INVOKE", target="Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z")})
    private boolean searchPath(String instance, CharSequence searchStrings, Operation<Boolean> original, @Local(name={"entry"}) Map.Entry<Identifier, DebugScreenEntry> entry) {
        String namespace = entry.getKey().getNamespace();
        return original.call(instance, searchStrings) != false || !"minecraft".equals(namespace) && namespace.contains(searchStrings);
    }
}

