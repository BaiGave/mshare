/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.server.level;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Util;

public class Ticket {
    public static final MapCodec<Ticket> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(((MapCodec)BuiltInRegistries.TICKET_TYPE.byNameCodec().fieldOf("type")).forGetter(Ticket::getType), ((MapCodec)ExtraCodecs.NON_NEGATIVE_INT.fieldOf("level")).forGetter(Ticket::getTicketLevel), Codec.LONG.optionalFieldOf("ticks_left", 0L).forGetter(t -> t.ticksLeft)).apply((Applicative<Ticket, ?>)i, Ticket::new));
    private final TicketType type;
    private final int ticketLevel;
    private long ticksLeft;

    public Ticket(TicketType type, int ticketLevel) {
        this(type, ticketLevel, type.timeout());
    }

    private Ticket(TicketType type, int ticketLevel, long ticksLeft) {
        this.type = type;
        this.ticketLevel = ticketLevel;
        this.ticksLeft = ticksLeft;
    }

    public String toString() {
        if (this.type.hasTimeout()) {
            return "Ticket[" + Util.getRegisteredName(BuiltInRegistries.TICKET_TYPE, this.type) + " " + this.ticketLevel + "] with " + this.ticksLeft + " ticks left ( out of" + this.type.timeout() + ")";
        }
        return "Ticket[" + Util.getRegisteredName(BuiltInRegistries.TICKET_TYPE, this.type) + " " + this.ticketLevel + "] with no timeout";
    }

    public TicketType getType() {
        return this.type;
    }

    public int getTicketLevel() {
        return this.ticketLevel;
    }

    public void resetTicksLeft() {
        this.ticksLeft = this.type.timeout();
    }

    public void decreaseTicksLeft() {
        if (this.type.hasTimeout()) {
            --this.ticksLeft;
        }
    }

    public boolean isTimedOut() {
        return this.type.hasTimeout() && this.ticksLeft < 0L;
    }
}

