/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Objects;
import org.apache.logging.log4j.core.config.ConfigurationSource;
import org.apache.logging.log4j.core.net.ssl.MemoryPasswordProvider;
import org.apache.logging.log4j.core.net.ssl.PasswordProvider;
import org.apache.logging.log4j.core.net.ssl.SslConfigurationDefaults;
import org.apache.logging.log4j.core.net.ssl.StoreConfiguration;
import org.apache.logging.log4j.core.net.ssl.StoreConfigurationException;
import org.apache.logging.log4j.core.util.NetUtils;

public class AbstractKeyStoreConfiguration
extends StoreConfiguration<KeyStore> {
    private final String keyStoreType;
    private final transient KeyStore keyStore;

    public AbstractKeyStoreConfiguration(String location, PasswordProvider passwordProvider, String keyStoreType) throws StoreConfigurationException {
        super(location, passwordProvider);
        this.keyStoreType = keyStoreType == null ? SslConfigurationDefaults.KEYSTORE_TYPE : keyStoreType;
        this.keyStore = this.load();
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String location, char[] password, String keyStoreType) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password), keyStoreType);
    }

    @Deprecated
    public AbstractKeyStoreConfiguration(String location, String password, String keyStoreType) throws StoreConfigurationException {
        this(location, new MemoryPasswordProvider(password == null ? null : password.toCharArray()), keyStoreType);
    }

    @Override
    protected KeyStore load() throws StoreConfigurationException {
        String loadLocation = this.getLocation();
        char[] password = this.getPasswordAsCharArray();
        LOGGER.debug("Loading keystore from location {}", (Object)loadLocation);
        try {
            KeyStore keyStore;
            block19: {
                KeyStore ks = KeyStore.getInstance(this.keyStoreType);
                if (loadLocation == null) {
                    if (this.keyStoreType.equalsIgnoreCase("JKS") || this.keyStoreType.equalsIgnoreCase("PKCS12")) {
                        throw new IOException("The location is null");
                    }
                    ks.load(null, password);
                    LOGGER.debug("KeyStore successfully loaded");
                    KeyStore keyStore2 = ks;
                    return keyStore2;
                }
                InputStream fin = AbstractKeyStoreConfiguration.openInputStream(loadLocation);
                try {
                    ks.load(fin, password);
                    LOGGER.debug("KeyStore successfully loaded from location {}", (Object)loadLocation);
                    keyStore = ks;
                    if (fin == null) break block19;
                }
                catch (Throwable throwable) {
                    try {
                        if (fin != null) {
                            try {
                                fin.close();
                            }
                            catch (Throwable throwable2) {
                                throwable.addSuppressed(throwable2);
                            }
                        }
                        throw throwable;
                    }
                    catch (CertificateException e) {
                        LOGGER.error("No Provider supports a KeyStoreSpi implementation for the specified type {} for location {}", (Object)this.keyStoreType, (Object)loadLocation, (Object)e);
                        throw new StoreConfigurationException(loadLocation, e);
                    }
                    catch (NoSuchAlgorithmException e) {
                        LOGGER.error("The algorithm used to check the integrity of the keystore cannot be found for location {}", (Object)loadLocation, (Object)e);
                        throw new StoreConfigurationException(loadLocation, e);
                    }
                    catch (KeyStoreException e) {
                        LOGGER.error("KeyStoreException for location {}", (Object)loadLocation, (Object)e);
                        throw new StoreConfigurationException(loadLocation, e);
                    }
                    catch (FileNotFoundException e) {
                        LOGGER.error("The keystore file {} is not found", (Object)loadLocation, (Object)e);
                        throw new StoreConfigurationException(loadLocation, e);
                    }
                    catch (IOException e) {
                        LOGGER.error("Something is wrong with the format of the keystore or the given password for location {}", (Object)loadLocation, (Object)e);
                        throw new StoreConfigurationException(loadLocation, e);
                    }
                }
                fin.close();
            }
            return keyStore;
        }
        finally {
            if (password != null) {
                Arrays.fill(password, '\u0000');
            }
        }
    }

    private static InputStream openInputStream(String filePathOrUri) {
        return ConfigurationSource.fromUri(NetUtils.toURI(filePathOrUri)).getInputStream();
    }

    public KeyStore getKeyStore() {
        return this.keyStore;
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = super.hashCode();
        result = 31 * result + (this.keyStoreType == null ? 0 : this.keyStoreType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        AbstractKeyStoreConfiguration other = (AbstractKeyStoreConfiguration)obj;
        return Objects.equals(this.keyStoreType, other.keyStoreType);
    }

    public String getKeyStoreType() {
        return this.keyStoreType;
    }
}

