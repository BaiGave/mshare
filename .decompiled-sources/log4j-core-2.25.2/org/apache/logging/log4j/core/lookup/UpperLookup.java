/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.apache.logging.log4j.util.Strings;

@Plugin(name="upper", category="Lookup")
public class UpperLookup
implements StrLookup {
    @Override
    public String lookup(String key) {
        return key != null ? Strings.toRootUpperCase(key) : null;
    }

    @Override
    public String lookup(LogEvent event, String key) {
        return this.lookup(key);
    }
}

