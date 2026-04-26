/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config;

import java.nio.charset.Charset;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.StringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.status.StatusData;

final class DefaultLayout
implements StringLayout {
    static final StringLayout INSTANCE = new DefaultLayout();

    private DefaultLayout() {
    }

    @Override
    public String toSerializable(LogEvent event) {
        return new StatusData(event.getSource(), event.getLevel(), event.getMessage(), event.getThrown(), event.getThreadName()).getFormattedStatus() + System.lineSeparator();
    }

    @Override
    public byte[] toByteArray(LogEvent event) {
        return this.toSerializable(event).getBytes(Charset.defaultCharset());
    }

    @Override
    public void encode(LogEvent event, ByteBufferDestination destination) {
        byte[] data = this.toByteArray(event);
        destination.writeBytes(data, 0, data.length);
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public Charset getCharset() {
        return Charset.defaultCharset();
    }

    @Override
    public byte[] getFooter() {
        return null;
    }

    @Override
    public byte[] getHeader() {
        return null;
    }

    @Override
    public Map<String, String> getContentFormat() {
        return Collections.emptyMap();
    }
}

