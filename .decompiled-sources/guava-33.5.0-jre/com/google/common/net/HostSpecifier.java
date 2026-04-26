/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.net;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import com.google.common.net.HostAndPort;
import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.net.InetAddress;
import java.text.ParseException;
import org.jspecify.annotations.Nullable;

@J2ktIncompatible
@GwtIncompatible
public final class HostSpecifier {
    private final String canonicalForm;

    private HostSpecifier(String canonicalForm) {
        this.canonicalForm = canonicalForm;
    }

    public static HostSpecifier fromValid(String specifier) {
        HostAndPort parsedHost = HostAndPort.fromString(specifier);
        Preconditions.checkArgument(!parsedHost.hasPort());
        String host = parsedHost.getHost();
        InetAddress addr = null;
        try {
            addr = InetAddresses.forString(host);
        }
        catch (IllegalArgumentException illegalArgumentException) {
            // empty catch block
        }
        if (addr != null) {
            return new HostSpecifier(InetAddresses.toUriString(addr));
        }
        InternetDomainName domain = InternetDomainName.from(host);
        if (domain.hasPublicSuffix()) {
            return new HostSpecifier(domain.toString());
        }
        throw new IllegalArgumentException("Domain name does not have a recognized public suffix: " + host);
    }

    @CanIgnoreReturnValue
    public static HostSpecifier from(String specifier) throws ParseException {
        try {
            return HostSpecifier.fromValid(specifier);
        }
        catch (IllegalArgumentException e) {
            ParseException parseException = new ParseException("Invalid host specifier: " + specifier, 0);
            parseException.initCause(e);
            throw parseException;
        }
    }

    public static boolean isValid(String specifier) {
        try {
            HostSpecifier unused = HostSpecifier.fromValid(specifier);
            return true;
        }
        catch (IllegalArgumentException e) {
            return false;
        }
    }

    public boolean equals(@Nullable Object other) {
        if (this == other) {
            return true;
        }
        if (other instanceof HostSpecifier) {
            HostSpecifier that = (HostSpecifier)other;
            return this.canonicalForm.equals(that.canonicalForm);
        }
        return false;
    }

    public int hashCode() {
        return this.canonicalForm.hashCode();
    }

    public String toString() {
        return this.canonicalForm;
    }
}

