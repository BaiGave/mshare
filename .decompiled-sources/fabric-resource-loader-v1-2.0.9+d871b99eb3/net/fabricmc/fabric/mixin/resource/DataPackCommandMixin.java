/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.fabricmc.fabric.impl.resource.pack.FabricPack;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value={DataPackCommand.class})
public class DataPackCommandMixin {
    @Unique
    private static final DynamicCommandExceptionType INTERNAL_PACK_EXCEPTION = new DynamicCommandExceptionType(packName -> Component.translatableEscape("commands.datapack.fabric.internal", packName));

    @Redirect(method={"lambda$static$10"}, at=@At(value="INVOKE", target="Lnet/minecraft/server/packs/repository/PackRepository;getSelectedIds()Ljava/util/Collection;"))
    private static Collection<String> filterEnabledPackSuggestions(PackRepository dataPackManager) {
        return dataPackManager.getSelectedPacks().stream().filter(profile -> !((FabricPack)((Object)profile)).fabric$isHidden()).map(Pack::getId).toList();
    }

    @WrapOperation(method={"lambda$static$11"}, at={@At(value="INVOKE", target="Ljava/util/stream/Stream;filter(Ljava/util/function/Predicate;)Ljava/util/stream/Stream;", ordinal=0)})
    private static Stream<Pack> filterDisabledPackSuggestions(Stream<Pack> instance, Predicate<? super Pack> predicate, Operation<Stream<Pack>> original) {
        return original.call(instance, predicate).filter(profile -> !((FabricPack)((Object)profile)).fabric$isHidden());
    }

    @Inject(method={"getPack"}, at={@At(value="INVOKE", target="Ljava/util/Collection;contains(Ljava/lang/Object;)Z")})
    private static void errorOnInternalPack(CommandContext<CommandSourceStack> context, String name, boolean enable, CallbackInfoReturnable<Pack> cir, @Local(name={"pack"}) Pack pack) throws CommandSyntaxException {
        if (((FabricPack)((Object)pack)).fabric$isHidden()) {
            throw INTERNAL_PACK_EXCEPTION.create(pack.getId());
        }
    }
}

