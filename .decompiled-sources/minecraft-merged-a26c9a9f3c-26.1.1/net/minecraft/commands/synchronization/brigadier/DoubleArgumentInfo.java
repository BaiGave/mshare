/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.commands.synchronization.brigadier;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import java.util.Objects;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentUtils;
import net.minecraft.network.FriendlyByteBuf;

public class DoubleArgumentInfo
implements ArgumentTypeInfo<DoubleArgumentType, Template> {
    @Override
    public void serializeToNetwork(Template template, FriendlyByteBuf out) {
        boolean hasMin = template.min != -1.7976931348623157E308;
        boolean hasMax = template.max != Double.MAX_VALUE;
        out.writeByte(ArgumentUtils.createNumberFlags(hasMin, hasMax));
        if (hasMin) {
            out.writeDouble(template.min);
        }
        if (hasMax) {
            out.writeDouble(template.max);
        }
    }

    @Override
    public Template deserializeFromNetwork(FriendlyByteBuf in) {
        byte flags = in.readByte();
        double min = ArgumentUtils.numberHasMin(flags) ? in.readDouble() : -1.7976931348623157E308;
        double max = ArgumentUtils.numberHasMax(flags) ? in.readDouble() : Double.MAX_VALUE;
        return new Template(this, min, max);
    }

    @Override
    public void serializeToJson(Template template, JsonObject out) {
        if (template.min != -1.7976931348623157E308) {
            out.addProperty("min", template.min);
        }
        if (template.max != Double.MAX_VALUE) {
            out.addProperty("max", template.max);
        }
    }

    @Override
    public Template unpack(DoubleArgumentType argument) {
        return new Template(this, argument.getMinimum(), argument.getMaximum());
    }

    public final class Template
    implements ArgumentTypeInfo.Template<DoubleArgumentType> {
        private final double min;
        private final double max;
        final /* synthetic */ DoubleArgumentInfo this$0;

        private Template(DoubleArgumentInfo this$0, double min, double max) {
            DoubleArgumentInfo doubleArgumentInfo = this$0;
            Objects.requireNonNull(doubleArgumentInfo);
            this.this$0 = doubleArgumentInfo;
            this.min = min;
            this.max = max;
        }

        @Override
        public DoubleArgumentType instantiate(CommandBuildContext context) {
            return DoubleArgumentType.doubleArg(this.min, this.max);
        }

        @Override
        public ArgumentTypeInfo<DoubleArgumentType, ?> type() {
            return this.this$0;
        }
    }
}

