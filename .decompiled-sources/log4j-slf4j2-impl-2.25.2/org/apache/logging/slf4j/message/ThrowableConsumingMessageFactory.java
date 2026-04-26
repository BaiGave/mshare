/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.slf4j.message;

import java.util.Arrays;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory2;
import org.apache.logging.log4j.message.ObjectMessage;
import org.apache.logging.log4j.message.ParameterizedMessage;
import org.apache.logging.log4j.message.SimpleMessage;

public final class ThrowableConsumingMessageFactory
implements MessageFactory2 {
    private Message newParameterizedMessage(Object throwable, String pattern, Object ... args) {
        return new ParameterizedMessage(pattern, args, (Throwable)throwable);
    }

    @Override
    public Message newMessage(Object message) {
        return new ObjectMessage(message);
    }

    @Override
    public Message newMessage(String message) {
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(String message, Object ... params) {
        if (params != null && params.length > 0) {
            Object lastArg = params[params.length - 1];
            return lastArg instanceof Throwable ? this.newParameterizedMessage(lastArg, message, Arrays.copyOf(params, params.length - 1)) : this.newParameterizedMessage(null, message, params);
        }
        return new SimpleMessage(message);
    }

    @Override
    public Message newMessage(CharSequence charSequence) {
        return new SimpleMessage(charSequence);
    }

    @Override
    public Message newMessage(String message, Object p0) {
        return p0 instanceof Throwable ? this.newParameterizedMessage(p0, message, new Object[0]) : this.newParameterizedMessage(null, message, p0);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1) {
        return p1 instanceof Throwable ? this.newParameterizedMessage(p1, message, p0) : this.newParameterizedMessage(null, message, p0, p1);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2) {
        return p2 instanceof Throwable ? this.newParameterizedMessage(p2, message, p0, p1) : this.newParameterizedMessage(null, message, p0, p1, p2);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3) {
        return p3 instanceof Throwable ? this.newParameterizedMessage(p3, message, p0, p1, p2) : this.newParameterizedMessage(null, message, p0, p1, p2, p3);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return p4 instanceof Throwable ? this.newParameterizedMessage(p4, message, p0, p1, p2, p3) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return p5 instanceof Throwable ? this.newParameterizedMessage(p5, message, p0, p1, p2, p3, p4) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4, p5);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return p6 instanceof Throwable ? this.newParameterizedMessage(p6, message, p0, p1, p2, p3, p4, p5) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return p7 instanceof Throwable ? this.newParameterizedMessage(p7, message, p0, p1, p2, p3, p4, p5, p6) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return p8 instanceof Throwable ? this.newParameterizedMessage(p8, message, p0, p1, p2, p3, p4, p5, p6, p7) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override
    public Message newMessage(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return p9 instanceof Throwable ? this.newParameterizedMessage(p9, message, p0, p1, p2, p3, p4, p5, p6, p7, p8) : this.newParameterizedMessage(null, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }
}

