/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Map;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.MapLookup;

@Plugin(name="main", category="Lookup")
public class MainMapLookup
extends MapLookup {
    static final MapLookup MAIN_SINGLETON = new MapLookup(MapLookup.newMap(0));

    public MainMapLookup() {
    }

    public MainMapLookup(Map<String, String> map) {
        super(map);
    }

    @SuppressFBWarnings(value={"HSM_HIDING_METHOD"}, justification="The MapLookup.setMainArguments() method hidden by this one is deprecated.")
    public static void setMainArguments(String ... args) {
        if (args == null) {
            return;
        }
        MainMapLookup.initMap(args, MAIN_SINGLETON.getMap());
    }

    @Override
    public String lookup(LogEvent ignored, String key) {
        return MAIN_SINGLETON.getMap().get(key);
    }

    @Override
    public String lookup(String key) {
        return MAIN_SINGLETON.getMap().get(key);
    }
}

