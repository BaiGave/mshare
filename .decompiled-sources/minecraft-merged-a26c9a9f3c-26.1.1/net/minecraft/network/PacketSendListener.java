/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network;

import com.mojang.logging.LogUtils;
import io.netty.channel.ChannelFutureListener;
import java.util.function.Supplier;
import net.minecraft.network.protocol.Packet;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

public class PacketSendListener {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static ChannelFutureListener thenRun(Runnable runnable) {
        return future -> {
            runnable.run();
            if (!future.isSuccess()) {
                future.channel().pipeline().fireExceptionCaught(future.cause());
            }
        };
    }

    public static ChannelFutureListener exceptionallySend(Supplier<@Nullable Packet<?>> handler) {
        return future -> {
            if (!future.isSuccess()) {
                Packet newPacket = (Packet)handler.get();
                if (newPacket != null) {
                    LOGGER.warn("Failed to deliver packet, sending fallback {}", (Object)newPacket.type(), (Object)future.cause());
                    future.channel().writeAndFlush(newPacket, future.channel().voidPromise());
                } else {
                    future.channel().pipeline().fireExceptionCaught(future.cause());
                }
            }
        };
    }
}

