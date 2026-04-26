/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.ArrayUtils;
import org.apache.logging.log4j.status.StatusLogger;

public final class NetUtils {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String UNKNOWN_LOCALHOST = "UNKNOWN_LOCALHOST";

    private NetUtils() {
    }

    public static String getLocalHostname() {
        return NetUtils.getHostname(InetAddress::getHostName);
    }

    public static String getCanonicalLocalHostname() {
        return NetUtils.getHostname(InetAddress::getCanonicalHostName);
    }

    private static String getHostname(Function<? super InetAddress, ? extends String> callback) {
        try {
            InetAddress address = InetAddress.getLocalHost();
            return address == null ? UNKNOWN_LOCALHOST : callback.apply(address);
        }
        catch (UnknownHostException uhe) {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                while (interfaces.hasMoreElements()) {
                    NetworkInterface nic = interfaces.nextElement();
                    Enumeration<InetAddress> addresses = nic.getInetAddresses();
                    while (addresses.hasMoreElements()) {
                        String hostname;
                        InetAddress address = addresses.nextElement();
                        if (address.isLoopbackAddress() || (hostname = callback.apply(address)) == null) continue;
                        return hostname;
                    }
                }
            }
            catch (SocketException socketException) {
                // empty catch block
            }
            LOGGER.error("Could not determine local host name", (Throwable)uhe);
            return UNKNOWN_LOCALHOST;
        }
    }

    public static byte[] getMacAddress() {
        byte[] mac = null;
        try {
            InetAddress localHost = InetAddress.getLocalHost();
            try {
                Enumeration<NetworkInterface> networkInterfaces;
                NetworkInterface localInterface = NetworkInterface.getByInetAddress(localHost);
                if (NetUtils.isUpAndNotLoopback(localInterface)) {
                    mac = localInterface.getHardwareAddress();
                }
                if (mac == null && (networkInterfaces = NetworkInterface.getNetworkInterfaces()) != null) {
                    while (networkInterfaces.hasMoreElements() && mac == null) {
                        NetworkInterface nic = networkInterfaces.nextElement();
                        if (!NetUtils.isUpAndNotLoopback(nic)) continue;
                        mac = nic.getHardwareAddress();
                    }
                }
            }
            catch (SocketException e) {
                LOGGER.catching(e);
            }
            if (ArrayUtils.isEmpty(mac) && localHost != null) {
                byte[] address = localHost.getAddress();
                mac = Arrays.copyOf(address, 6);
            }
        }
        catch (UnknownHostException unknownHostException) {
            // empty catch block
        }
        return mac;
    }

    public static String getMacAddressString() {
        byte[] macAddr = NetUtils.getMacAddress();
        if (!ArrayUtils.isEmpty(macAddr)) {
            StringBuilder sb = new StringBuilder(String.format("%02x", macAddr[0]));
            for (int i = 1; i < macAddr.length; ++i) {
                sb.append(":").append(String.format("%02x", macAddr[i]));
            }
            return sb.toString();
        }
        return null;
    }

    private static boolean isUpAndNotLoopback(NetworkInterface ni) throws SocketException {
        return ni != null && !ni.isLoopback() && ni.isUp();
    }

    @SuppressFBWarnings(value={"PATH_TRAVERSAL_IN"}, justification="Currently `path` comes from a configuration file.")
    public static URI toURI(String path) {
        try {
            return new URI(path);
        }
        catch (URISyntaxException e) {
            try {
                URL url = new URL(path);
                return new URI(url.getProtocol(), url.getHost(), url.getPath(), null);
            }
            catch (MalformedURLException | URISyntaxException nestedEx) {
                return new File(path).toURI();
            }
        }
    }

    public static List<URI> toURIs(String path) {
        String[] parts = path.split(",");
        String scheme = null;
        ArrayList<URI> uris = new ArrayList<URI>(parts.length);
        for (String part : parts) {
            URI uri = NetUtils.toURI(scheme != null ? scheme + ":" + part.trim() : part.trim());
            if (scheme == null && uri.getScheme() != null) {
                scheme = uri.getScheme();
            }
            uris.add(uri);
        }
        return uris;
    }
}

