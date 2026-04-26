/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.net.ssl.KeyStoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;
import org.apache.logging.log4j.status.StatusLogger;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;
import org.jspecify.annotations.Nullable;

@NullMarked
@Plugin(name="Ssl", category="Core", printObject=true)
public class SslConfiguration {
    private static final StatusLogger LOGGER = StatusLogger.getLogger();
    private final String protocol;
    private final boolean verifyHostName;
    private final @Nullable KeyStoreConfiguration keyStoreConfig;
    private final @Nullable TrustStoreConfiguration trustStoreConfig;
    private final transient SSLContext sslContext;

    private SslConfiguration(@Nullable String protocol, boolean verifyHostName, @Nullable KeyStoreConfiguration keyStoreConfig, @Nullable TrustStoreConfiguration trustStoreConfig) {
        String effectiveProtocol;
        this.keyStoreConfig = keyStoreConfig;
        this.trustStoreConfig = trustStoreConfig;
        this.protocol = effectiveProtocol = protocol == null ? "TLS" : protocol;
        this.verifyHostName = verifyHostName;
        this.sslContext = SslConfiguration.createSslContext(effectiveProtocol, keyStoreConfig, trustStoreConfig);
    }

    public void clearSecrets() {
        if (this.keyStoreConfig != null) {
            this.keyStoreConfig.clearSecrets();
        }
        if (this.trustStoreConfig != null) {
            this.trustStoreConfig.clearSecrets();
        }
    }

    @Deprecated
    public SSLSocketFactory getSslSocketFactory() {
        return this.sslContext.getSocketFactory();
    }

    @Deprecated
    public SSLServerSocketFactory getSslServerSocketFactory() {
        return this.sslContext.getServerSocketFactory();
    }

    private static SSLContext createDefaultSslContext(String protocol) {
        try {
            return SSLContext.getDefault();
        }
        catch (NoSuchAlgorithmException defaultContextError) {
            LOGGER.error("Failed to create an `SSLContext` using the default configuration, falling back to creating an empty one", (Throwable)defaultContextError);
            try {
                SSLContext emptyContext = SSLContext.getInstance(protocol);
                emptyContext.init(new KeyManager[0], new TrustManager[0], null);
                return emptyContext;
            }
            catch (Exception emptyContextError) {
                LOGGER.error("Failed to create an empty `SSLContext`", (Throwable)emptyContextError);
                return null;
            }
        }
    }

    private static SSLContext createSslContext(String protocol, @Nullable KeyStoreConfiguration keyStoreConfig, @Nullable TrustStoreConfiguration trustStoreConfig) {
        try {
            SSLContext sslContext = SSLContext.getInstance(protocol);
            KeyManager[] keyManagers = SslConfiguration.loadKeyManagers(keyStoreConfig);
            TrustManager[] trustManagers = SslConfiguration.loadTrustManagers(trustStoreConfig);
            sslContext.init(keyManagers, trustManagers, null);
            return sslContext;
        }
        catch (Exception error) {
            LOGGER.error("Failed to create an `SSLContext` using the provided configuration, falling back to a default instance", (Throwable)error);
            return SslConfiguration.createDefaultSslContext(protocol);
        }
    }

    private static KeyManager[] loadKeyManagers(@Nullable KeyStoreConfiguration config) throws Exception {
        if (config == null) {
            return new KeyManager[0];
        }
        KeyManagerFactory factory = KeyManagerFactory.getInstance(config.getKeyManagerFactoryAlgorithm());
        char[] password = config.getPasswordAsCharArray();
        try {
            factory.init(config.getKeyStore(), password);
        }
        finally {
            config.clearSecrets();
        }
        return factory.getKeyManagers();
    }

    private static TrustManager[] loadTrustManagers(@Nullable TrustStoreConfiguration config) throws Exception {
        if (config == null) {
            return new TrustManager[0];
        }
        TrustManagerFactory factory = TrustManagerFactory.getInstance(config.getTrustManagerFactoryAlgorithm());
        factory.init(config.getKeyStore());
        return factory.getTrustManagers();
    }

    @NullUnmarked
    @PluginFactory
    public static SslConfiguration createSSLConfiguration(@PluginAttribute(value="protocol") String protocol, @PluginElement(value="KeyStore") KeyStoreConfiguration keyStoreConfig, @PluginElement(value="TrustStore") TrustStoreConfiguration trustStoreConfig) {
        return new SslConfiguration(protocol, false, keyStoreConfig, trustStoreConfig);
    }

    @NullUnmarked
    public static SslConfiguration createSSLConfiguration(@PluginAttribute(value="protocol") String protocol, @PluginElement(value="KeyStore") KeyStoreConfiguration keyStoreConfig, @PluginElement(value="TrustStore") TrustStoreConfiguration trustStoreConfig, @PluginAttribute(value="verifyHostName") boolean verifyHostName) {
        return new SslConfiguration(protocol, verifyHostName, keyStoreConfig, trustStoreConfig);
    }

    public int hashCode() {
        return Objects.hash(this.keyStoreConfig, this.protocol, this.sslContext, this.trustStoreConfig);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        SslConfiguration other = (SslConfiguration)obj;
        if (!Objects.equals(this.protocol, other.protocol)) {
            return false;
        }
        if (!Objects.equals(this.verifyHostName, other.verifyHostName)) {
            return false;
        }
        if (!Objects.equals(this.keyStoreConfig, other.keyStoreConfig)) {
            return false;
        }
        return Objects.equals(this.trustStoreConfig, other.trustStoreConfig);
    }

    public String getProtocol() {
        return this.protocol;
    }

    public boolean isVerifyHostName() {
        return this.verifyHostName;
    }

    public KeyStoreConfiguration getKeyStoreConfig() {
        return this.keyStoreConfig;
    }

    public TrustStoreConfiguration getTrustStoreConfig() {
        return this.trustStoreConfig;
    }

    public SSLContext getSslContext() {
        return this.sslContext;
    }
}

