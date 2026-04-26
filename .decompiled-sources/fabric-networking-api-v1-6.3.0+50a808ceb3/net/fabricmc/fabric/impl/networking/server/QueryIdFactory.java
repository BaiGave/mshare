/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.fabric.impl.networking.server;

import java.util.concurrent.atomic.AtomicInteger;

interface QueryIdFactory {
    public static QueryIdFactory create() {
        return new QueryIdFactory(){
            private final AtomicInteger currentId = new AtomicInteger();

            @Override
            public int nextId() {
                return this.currentId.getAndIncrement();
            }
        };
    }

    public int nextId();
}

