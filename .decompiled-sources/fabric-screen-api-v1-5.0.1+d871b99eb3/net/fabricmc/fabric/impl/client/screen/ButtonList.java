/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.client.screen;

import java.util.AbstractList;
import java.util.List;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;

public final class ButtonList
extends AbstractList<AbstractWidget> {
    private final List<Renderable> renderables;
    private final List<NarratableEntry> narratables;
    private final List<GuiEventListener> children;

    public ButtonList(List<Renderable> renderables, List<NarratableEntry> narratables, List<GuiEventListener> children) {
        this.renderables = renderables;
        this.narratables = narratables;
        this.children = children;
    }

    @Override
    public AbstractWidget get(int index) {
        int renderableIndex = this.translateIndex(this.renderables, index, false);
        return (AbstractWidget)this.renderables.get(renderableIndex);
    }

    @Override
    public AbstractWidget set(int index, AbstractWidget element) {
        int renderableIndex = this.translateIndex(this.renderables, index, false);
        this.renderables.set(renderableIndex, element);
        int narratableIndex = this.translateIndex(this.narratables, index, false);
        this.narratables.set(narratableIndex, element);
        int childIndex = this.translateIndex(this.children, index, false);
        return (AbstractWidget)this.children.set(childIndex, element);
    }

    @Override
    public void add(int index, AbstractWidget element) {
        int duplicateIndex = this.renderables.indexOf(element);
        if (duplicateIndex >= 0) {
            this.renderables.remove(element);
            this.narratables.remove(element);
            this.children.remove(element);
            if (duplicateIndex <= this.translateIndex(this.renderables, index, true)) {
                --index;
            }
        }
        int renderableIndx = this.translateIndex(this.renderables, index, true);
        this.renderables.add(renderableIndx, element);
        int narratableIndex = this.translateIndex(this.narratables, index, true);
        this.narratables.add(narratableIndex, element);
        int childIndex = this.translateIndex(this.children, index, true);
        this.children.add(childIndex, element);
    }

    @Override
    public AbstractWidget remove(int index) {
        index = this.translateIndex(this.renderables, index, false);
        AbstractWidget removedButton = (AbstractWidget)this.renderables.remove(index);
        this.narratables.remove(removedButton);
        this.children.remove(removedButton);
        return removedButton;
    }

    @Override
    public int size() {
        int ret = 0;
        for (Renderable renderable : this.renderables) {
            if (!(renderable instanceof AbstractWidget)) continue;
            ++ret;
        }
        return ret;
    }

    private int translateIndex(List<?> list, int index, boolean allowAfter) {
        int remaining = index;
        int max = list.size();
        for (int i = 0; i < max; ++i) {
            if (!(list.get(i) instanceof AbstractWidget)) continue;
            if (remaining == 0) {
                return i;
            }
            --remaining;
        }
        if (allowAfter && remaining == 0) {
            return list.size();
        }
        throw new IndexOutOfBoundsException(String.format("Index: %d, Size: %d", index, index - remaining));
    }
}

