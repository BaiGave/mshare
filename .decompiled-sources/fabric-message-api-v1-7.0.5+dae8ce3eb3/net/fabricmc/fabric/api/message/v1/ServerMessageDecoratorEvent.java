/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.api.message.v1;

import java.util.Objects;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.chat.ChatDecorator;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public final class ServerMessageDecoratorEvent {
    public static final Identifier CONTENT_PHASE = Identifier.fromNamespaceAndPath("fabric", "content");
    public static final Identifier STYLING_PHASE = Identifier.fromNamespaceAndPath("fabric", "styling");
    public static final Event<ChatDecorator> EVENT = EventFactory.createWithPhases(ChatDecorator.class, decorators -> (sender, message) -> {
        Component decorated = message;
        for (ChatDecorator decorator : decorators) {
            decorated = ServerMessageDecoratorEvent.handle(decorator.decorate(sender, decorated), decorator);
        }
        return decorated;
    }, CONTENT_PHASE, Event.DEFAULT_PHASE, STYLING_PHASE);

    private ServerMessageDecoratorEvent() {
    }

    private static <T extends Component> T handle(T decorated, ChatDecorator decorator) {
        String decoratorName = decorator.getClass().getName();
        return Objects.requireNonNull(decorated, "chat decorator %s returned null".formatted(decoratorName));
    }
}

