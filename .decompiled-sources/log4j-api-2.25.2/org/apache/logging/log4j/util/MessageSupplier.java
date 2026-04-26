/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

@FunctionalInterface
public interface MessageSupplier
extends Supplier<Message> {
    @Override
    public Message get();
}

