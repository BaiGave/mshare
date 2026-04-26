/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.authlib;

import com.mojang.authlib.Environment;
import com.mojang.authlib.yggdrasil.YggdrasilEnvironment;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentParser {
    @Nullable
    private static String environmentOverride;
    private static final String PROP_PREFIX = "minecraft.api.";
    private static final Logger LOGGER;
    public static final String PROP_ENV = "minecraft.api.env";
    public static final String PROP_SESSION_HOST = "minecraft.api.session.host";
    public static final String PROP_SERVICES_HOST = "minecraft.api.services.host";
    public static final String PROP_PROFILES_HOST = "minecraft.api.profiles.host";

    public static void setEnvironmentOverride(@Nullable String override) {
        environmentOverride = override;
    }

    public static Optional<Environment> getEnvironmentFromProperties() {
        String envName = environmentOverride != null ? environmentOverride : System.getProperty(PROP_ENV);
        Optional<Environment> env = YggdrasilEnvironment.fromString(envName);
        return env.isPresent() ? env : EnvironmentParser.fromHostNames();
    }

    private static Optional<Environment> fromHostNames() {
        String session = System.getProperty(PROP_SESSION_HOST);
        String services = System.getProperty(PROP_SERVICES_HOST);
        String profiles = System.getProperty(PROP_PROFILES_HOST);
        if (services != null && session != null && profiles != null) {
            return Optional.of(new Environment(session, services, profiles, "properties"));
        }
        if (services != null || session != null || profiles != null) {
            LOGGER.info("Ignoring hosts properties. All need to be set: {}", (Object)List.of(PROP_SERVICES_HOST, PROP_SESSION_HOST, PROP_PROFILES_HOST));
        }
        return Optional.empty();
    }

    static {
        LOGGER = LoggerFactory.getLogger(EnvironmentParser.class);
    }
}

