/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.mixin.networking;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value={BundlePacket.class})
public class BundlePacketMixin {
    @ModifyVariable(method={"<init>"}, at=@At(value="HEAD"), argsOnly=true, name={"packets"})
    private static Iterable<? extends Packet<?>> flattenBundlePackets(Iterable<? extends Packet<?>> value) {
        ArrayList packets = new ArrayList();
        BundlePacketMixin.iterateBundle(value, packets);
        return packets;
    }

    @Unique
    private static void iterateBundle(Iterable<? extends Packet<?>> value, List<Packet<?>> result) {
        for (Packet<?> packet : value) {
            if (packet instanceof BundlePacket) {
                BundlePacket bundlePacket = (BundlePacket)packet;
                BundlePacketMixin.iterateBundle(bundlePacket.subPackets(), result);
                continue;
            }
            result.add(packet);
        }
    }
}

