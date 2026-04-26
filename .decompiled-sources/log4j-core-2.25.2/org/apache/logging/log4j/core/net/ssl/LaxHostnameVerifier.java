/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.net.ssl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public final class LaxHostnameVerifier
implements HostnameVerifier {
    public static final HostnameVerifier INSTANCE = new LaxHostnameVerifier();

    private LaxHostnameVerifier() {
    }

    @Override
    @SuppressFBWarnings(value={"WEAK_HOSTNAME_VERIFIER"})
    public boolean verify(String s, SSLSession sslSession) {
        return true;
    }
}

