/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.gui.screens.options;

import com.mojang.datafixers.util.Unit;
import com.mojang.serialization.Codec;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Optionull;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Difficulty;
import org.jspecify.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class OnlineOptionsScreen
extends OptionsSubScreen {
    private static final Component TITLE = Component.translatable("options.online.title");
    private @Nullable OptionInstance<Unit> difficultyDisplay;

    public OnlineOptionsScreen(Screen lastScreen, Options options) {
        super(lastScreen, options, TITLE);
    }

    @Override
    protected void init() {
        AbstractWidget difficultyButton;
        super.init();
        if (this.difficultyDisplay != null && (difficultyButton = this.list.findOption(this.difficultyDisplay)) != null) {
            difficultyButton.active = false;
        }
    }

    private OptionInstance<?>[] options(Options options, Minecraft minecraft) {
        ArrayList<OptionInstance> optionList = new ArrayList<OptionInstance>();
        optionList.add(options.realmsNotifications());
        optionList.add(options.allowServerListing());
        OptionInstance difficultyDisplay = Optionull.map(minecraft.level, level -> {
            Difficulty difficulty = level.getDifficulty();
            return new OptionInstance<Unit>("options.difficulty.online", OptionInstance.noTooltip(), (caption, value) -> difficulty.getDisplayName(), new OptionInstance.Enum<Unit>(List.of(Unit.INSTANCE), Codec.EMPTY.codec()), Unit.INSTANCE, value -> {});
        });
        if (difficultyDisplay != null) {
            this.difficultyDisplay = difficultyDisplay;
            optionList.add(difficultyDisplay);
        }
        return optionList.toArray(new OptionInstance[0]);
    }

    @Override
    protected void addOptions() {
        this.list.addSmall(this.options(this.options, this.minecraft));
    }
}

