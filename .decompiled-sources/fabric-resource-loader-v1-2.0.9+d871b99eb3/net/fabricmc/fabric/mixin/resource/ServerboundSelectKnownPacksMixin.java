/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.resource;

import net.fabricmc.fabric.impl.resource.pack.ModResourcePackCreator;
import net.minecraft.network.protocol.configuration.ServerboundSelectKnownPacks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value={ServerboundSelectKnownPacks.class})
public class ServerboundSelectKnownPacksMixin {
    @ModifyArg(method={"<clinit>"}, at=@At(value="INVOKE", target="Lnet/minecraft/network/codec/ByteBufCodecs;list(I)Lnet/minecraft/network/codec/StreamCodec$CodecOperation;"))
    private static int setMaxKnownPacks(int constant) {
        return ModResourcePackCreator.MAX_KNOWN_PACKS;
    }
}

