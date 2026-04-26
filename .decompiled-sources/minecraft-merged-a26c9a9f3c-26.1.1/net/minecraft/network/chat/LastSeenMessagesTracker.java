/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.network.chat;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.BitSet;
import java.util.Objects;
import net.minecraft.network.chat.LastSeenMessages;
import net.minecraft.network.chat.LastSeenTrackedEntry;
import net.minecraft.network.chat.MessageSignature;
import org.jspecify.annotations.Nullable;

public class LastSeenMessagesTracker {
    private final @Nullable LastSeenTrackedEntry[] trackedMessages;
    private int tail;
    private int offset;
    private @Nullable MessageSignature lastTrackedMessage;

    public LastSeenMessagesTracker(int lastSeenCount) {
        this.trackedMessages = new LastSeenTrackedEntry[lastSeenCount];
    }

    public boolean addPending(MessageSignature message, boolean wasShown) {
        if (Objects.equals(message, this.lastTrackedMessage)) {
            return false;
        }
        this.lastTrackedMessage = message;
        this.addEntry(wasShown ? new LastSeenTrackedEntry(message, true) : null);
        return true;
    }

    private void addEntry(@Nullable LastSeenTrackedEntry entry) {
        int index = this.tail;
        this.tail = (index + 1) % this.trackedMessages.length;
        ++this.offset;
        this.trackedMessages[index] = entry;
    }

    public void ignorePending(MessageSignature pendingMessage) {
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            LastSeenTrackedEntry entry = this.trackedMessages[i];
            if (entry == null || !entry.pending() || !pendingMessage.equals(entry.signature())) continue;
            this.trackedMessages[i] = null;
            break;
        }
    }

    public int getAndClearOffset() {
        int originalOffset = this.offset;
        this.offset = 0;
        return originalOffset;
    }

    public Update generateAndApplyUpdate() {
        int offset = this.getAndClearOffset();
        BitSet acknowledged = new BitSet(this.trackedMessages.length);
        ObjectArrayList<MessageSignature> lastSeenEntries = new ObjectArrayList<MessageSignature>(this.trackedMessages.length);
        for (int i = 0; i < this.trackedMessages.length; ++i) {
            int index = (this.tail + i) % this.trackedMessages.length;
            LastSeenTrackedEntry message = this.trackedMessages[index];
            if (message == null) continue;
            acknowledged.set(i, true);
            lastSeenEntries.add(message.signature());
            this.trackedMessages[index] = message.acknowledge();
        }
        LastSeenMessages lastSeen = new LastSeenMessages(lastSeenEntries);
        LastSeenMessages.Update update = new LastSeenMessages.Update(offset, acknowledged, lastSeen.computeChecksum());
        return new Update(lastSeen, update);
    }

    public int offset() {
        return this.offset;
    }

    public record Update(LastSeenMessages lastSeen, LastSeenMessages.Update update) {
    }
}

