/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.lookup;

import java.util.Objects;
import javax.naming.NamingException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.net.JndiManager;
import org.apache.logging.log4j.status.StatusLogger;

@Plugin(name="jndi", category="Lookup")
public class JndiLookup
extends AbstractLookup {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final Marker LOOKUP = MarkerManager.getMarker("LOOKUP");
    static final String CONTAINER_JNDI_RESOURCE_PATH_PREFIX = "java:comp/env/";

    public JndiLookup() {
        if (!JndiManager.isJndiLookupEnabled()) {
            throw new IllegalStateException("JNDI must be enabled by setting log4j2.enableJndiLookup=true");
        }
    }

    @Override
    public String lookup(LogEvent ignored, String key) {
        String string;
        block9: {
            if (key == null) {
                return null;
            }
            String jndiName = this.convertJndiName(key);
            JndiManager jndiManager = JndiManager.getDefaultManager();
            try {
                string = Objects.toString(jndiManager.lookup(jndiName), null);
                if (jndiManager == null) break block9;
            }
            catch (Throwable throwable) {
                try {
                    if (jndiManager != null) {
                        try {
                            jndiManager.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                catch (NamingException e) {
                    LOGGER.warn(LOOKUP, "Error looking up JNDI resource [{}].", (Object)jndiName, (Object)e);
                    return null;
                }
            }
            jndiManager.close();
        }
        return string;
    }

    private String convertJndiName(String jndiName) {
        if (!jndiName.startsWith(CONTAINER_JNDI_RESOURCE_PATH_PREFIX) && jndiName.indexOf(58) == -1) {
            return CONTAINER_JNDI_RESOURCE_PATH_PREFIX + jndiName;
        }
        return jndiName;
    }
}

