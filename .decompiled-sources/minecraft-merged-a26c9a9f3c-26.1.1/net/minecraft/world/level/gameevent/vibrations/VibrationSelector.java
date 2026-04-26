/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.world.level.gameevent.vibrations;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.level.gameevent.vibrations.VibrationInfo;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.apache.commons.lang3.tuple.Pair;

public class VibrationSelector {
    public static final Codec<VibrationSelector> CODEC = RecordCodecBuilder.create(i -> i.group(VibrationInfo.CODEC.lenientOptionalFieldOf("event").forGetter(o -> o.currentVibrationData.map(Pair::getLeft)), ((MapCodec)Codec.LONG.fieldOf("tick")).forGetter(o -> o.currentVibrationData.map(Pair::getRight).orElse(-1L))).apply((Applicative<VibrationSelector, ?>)i, VibrationSelector::new));
    private Optional<Pair<VibrationInfo, Long>> currentVibrationData;

    public VibrationSelector(Optional<VibrationInfo> currentVibration, long tick) {
        this.currentVibrationData = currentVibration.map(vibrationInfo -> Pair.of(vibrationInfo, tick));
    }

    public VibrationSelector() {
        this.currentVibrationData = Optional.empty();
    }

    public void addCandidate(VibrationInfo newVibration, long tickTime) {
        if (this.shouldReplaceVibration(newVibration, tickTime)) {
            this.currentVibrationData = Optional.of(Pair.of(newVibration, tickTime));
        }
    }

    private boolean shouldReplaceVibration(VibrationInfo newVibration, long tickTime) {
        if (this.currentVibrationData.isEmpty()) {
            return true;
        }
        Pair<VibrationInfo, Long> previousData = this.currentVibrationData.get();
        long previousTick = previousData.getRight();
        if (tickTime != previousTick) {
            return false;
        }
        VibrationInfo previousVibration = previousData.getLeft();
        if (newVibration.distance() < previousVibration.distance()) {
            return true;
        }
        if (newVibration.distance() > previousVibration.distance()) {
            return false;
        }
        return VibrationSystem.getGameEventFrequency(newVibration.gameEvent()) > VibrationSystem.getGameEventFrequency(previousVibration.gameEvent());
    }

    public Optional<VibrationInfo> chosenCandidate(long time) {
        if (this.currentVibrationData.isEmpty()) {
            return Optional.empty();
        }
        if (this.currentVibrationData.get().getRight() < time) {
            return Optional.of(this.currentVibrationData.get().getLeft());
        }
        return Optional.empty();
    }

    public void startOver() {
        this.currentVibrationData = Optional.empty();
    }
}

