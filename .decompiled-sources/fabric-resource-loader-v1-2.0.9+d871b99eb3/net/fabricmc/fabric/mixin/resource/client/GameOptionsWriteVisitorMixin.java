/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource.client;

import com.llamalad7.mixinextras.sugar.Local;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.minecraft.client.Minecraft;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(targets={"net.minecraft.client.Options$3"})
public class GameOptionsWriteVisitorMixin {
    @Unique
    private static List<String> toPackListString(List<String> packs) {
        ArrayList<String> copy = new ArrayList<String>(packs.size());
        PackRepository manager = Minecraft.getInstance().getResourcePackRepository();
        for (String pack : packs) {
            Pack profile = manager.getPack(pack);
            if (profile != null && ((FabricPack)((Object)profile)).fabric$isHidden()) continue;
            copy.add(pack);
        }
        return copy;
    }

    @ModifyArg(method={"process(Ljava/lang/String;Ljava/lang/Object;Ljava/util/function/Function;Ljava/util/function/Function;)Ljava/lang/Object;"}, at=@At(value="INVOKE", target="Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;"))
    private <T> T skipHiddenPacks(T value, @Local(argsOnly=true) String key) {
        if ("resourcePacks".equals(key) && value instanceof List) {
            return (T)GameOptionsWriteVisitorMixin.toPackListString((List)value);
        }
        return value;
    }
}

