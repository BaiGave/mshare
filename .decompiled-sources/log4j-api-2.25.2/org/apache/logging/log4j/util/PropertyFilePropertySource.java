/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.LoaderUtil;
import org.apache.logging.log4j.util.PropertiesPropertySource;

public class PropertyFilePropertySource
extends PropertiesPropertySource {
    private static final Logger LOGGER = StatusLogger.getLogger();

    public PropertyFilePropertySource(String fileName) {
        this(fileName, true);
    }

    public PropertyFilePropertySource(String fileName, boolean useTccl) {
        super(PropertyFilePropertySource.loadPropertiesFile(fileName, useTccl));
    }

    @SuppressFBWarnings(value={"URLCONNECTION_SSRF_FD"}, justification="This property source should only be used with hardcoded file names.")
    private static Properties loadPropertiesFile(String fileName, boolean useTccl) {
        Properties props = new Properties();
        for (URL url : LoaderUtil.findResources(fileName, useTccl)) {
            try {
                InputStream in = url.openStream();
                try {
                    props.load(in);
                }
                finally {
                    if (in == null) continue;
                    in.close();
                }
            }
            catch (IOException error) {
                LOGGER.error("Unable to read URL `{}`", (Object)url, (Object)error);
            }
        }
        return props;
    }
}

