/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.message.StructuredDataMessage;

@Plugin(name="sd", category="Lookup")
public class StructuredDataLookup
extends AbstractLookup {
    public static final String ID_KEY = "id";
    public static final String TYPE_KEY = "type";

    @Override
    public String lookup(LogEvent event, String key) {
        if (event == null || !(event.getMessage() instanceof StructuredDataMessage)) {
            return null;
        }
        StructuredDataMessage msg = (StructuredDataMessage)event.getMessage();
        if (ID_KEY.equalsIgnoreCase(key)) {
            return msg.getId().getName();
        }
        if (TYPE_KEY.equalsIgnoreCase(key)) {
            return msg.getType();
        }
        return msg.get(key);
    }
}

