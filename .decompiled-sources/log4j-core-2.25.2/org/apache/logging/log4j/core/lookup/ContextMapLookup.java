/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Objects;
import org.apache.logging.log4j.core.ContextDataInjector;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.impl.ContextDataInjectorFactory;
import org.apache.logging.log4j.core.lookup.StrLookup;

@Plugin(name="ctx", category="Lookup")
public class ContextMapLookup
implements StrLookup {
    private final ContextDataInjector injector = ContextDataInjectorFactory.createInjector();

    @Override
    public String lookup(String key) {
        return Objects.toString(this.injector.getValue(key), null);
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return event == null ? null : (String)event.getContextData().getValue(key);
    }
}

