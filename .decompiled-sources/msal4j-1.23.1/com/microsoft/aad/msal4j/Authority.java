/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AADAuthority;
import com.microsoft.aad.msal4j.ADFSAuthority;
import com.microsoft.aad.msal4j.AuthorityType;
import com.microsoft.aad.msal4j.B2CAuthority;
import com.microsoft.aad.msal4j.CIAMAuthority;
import com.microsoft.aad.msal4j.StringHelper;
import java.net.MalformedURLException;
import java.net.URL;

abstract class Authority {
    private static final String ADFS_PATH_SEGMENT = "adfs";
    private static final String B2C_PATH_SEGMENT = "tfp";
    private static final String B2C_HOST_SEGMENT = "b2clogin.com";
    private static final String USER_REALM_ENDPOINT = "common/userrealm";
    private static final String userRealmEndpointFormat = "https://%s/common/userrealm/%s?api-version=1.0";
    String authority;
    final URL canonicalAuthorityUrl;
    protected final AuthorityType authorityType;
    String selfSignedJwtAudience;
    String host;
    String tenant;
    boolean isTenantless;
    String authorizationEndpoint;
    String tokenEndpoint;
    String deviceCodeEndpoint;

    URL tokenEndpointUrl() throws MalformedURLException {
        return new URL(this.tokenEndpoint);
    }

    Authority(URL canonicalAuthorityUrl, AuthorityType authorityType) {
        this.canonicalAuthorityUrl = canonicalAuthorityUrl;
        this.authorityType = authorityType;
        this.setCommonAuthorityProperties();
    }

    private void setCommonAuthorityProperties() {
        this.tenant = Authority.getTenant(this.canonicalAuthorityUrl, this.authorityType);
        this.host = this.canonicalAuthorityUrl.getAuthority().toLowerCase();
    }

    static Authority createAuthority(URL authorityUrl) throws MalformedURLException {
        Authority createdAuthority;
        AuthorityType authorityType = Authority.detectAuthorityType(authorityUrl);
        if (authorityType == AuthorityType.AAD) {
            createdAuthority = new AADAuthority(authorityUrl);
        } else if (authorityType == AuthorityType.B2C) {
            createdAuthority = new B2CAuthority(authorityUrl);
        } else if (authorityType == AuthorityType.ADFS) {
            createdAuthority = new ADFSAuthority(authorityUrl);
        } else if (authorityType == AuthorityType.CIAM) {
            createdAuthority = new CIAMAuthority(authorityUrl);
        } else {
            throw new IllegalArgumentException("Unsupported Authority Type");
        }
        Authority.validateAuthority(createdAuthority.canonicalAuthorityUrl());
        return createdAuthority;
    }

    static AuthorityType detectAuthorityType(URL authorityUrl) {
        String firstPath;
        if (authorityUrl == null) {
            throw new NullPointerException("canonicalAuthorityUrl");
        }
        String path = authorityUrl.getPath().substring(1);
        if (StringHelper.isBlank(path)) {
            if (Authority.isCiamAuthority(authorityUrl.getHost())) {
                return AuthorityType.CIAM;
            }
            throw new IllegalArgumentException("authority Uri should have at least one segment in the path (i.e. https://<host>/<path>/...)");
        }
        String host = authorityUrl.getHost();
        if (Authority.isB2CAuthority(host, firstPath = path.substring(0, path.indexOf("/")))) {
            return AuthorityType.B2C;
        }
        if (Authority.isAdfsAuthority(firstPath)) {
            return AuthorityType.ADFS;
        }
        if (Authority.isCiamAuthority(host)) {
            return AuthorityType.CIAM;
        }
        return AuthorityType.AAD;
    }

    static void validateAuthority(URL authorityUrl) {
        if (!authorityUrl.getProtocol().equalsIgnoreCase("https")) {
            throw new IllegalArgumentException("authority should use the 'https' scheme");
        }
        if (authorityUrl.toString().contains("#")) {
            throw new IllegalArgumentException("authority is invalid format (contains fragment)");
        }
        if (!StringHelper.isBlank(authorityUrl.getQuery())) {
            throw new IllegalArgumentException("authority cannot contain query parameters");
        }
        String path = authorityUrl.getPath();
        if (path.length() == 0) {
            throw new IllegalArgumentException("Authority Uri should have at least one segment in the path");
        }
        String[] segments = path.substring(1).split("/");
        if (segments.length == 0) {
            throw new IllegalArgumentException("Authority Uri must have at least one path segment. This is usually 'common' or the application's tenant id.");
        }
        for (String segment : segments) {
            if (!StringHelper.isBlank(segment)) continue;
            throw new IllegalArgumentException("Authority Uri should not have empty path segments");
        }
    }

    static Authority replaceTenant(Authority originalAuthority, String newTenant) throws MalformedURLException {
        String authorityString = originalAuthority.canonicalAuthorityUrl().toString();
        authorityString = authorityString.replace(originalAuthority.tenant, newTenant);
        return Authority.createAuthority(new URL(authorityString));
    }

    static String getTenant(URL authorityUrl, AuthorityType authorityType) {
        String[] segments = authorityUrl.getPath().substring(1).split("/");
        if (authorityType == AuthorityType.B2C) {
            if (segments.length < 3) {
                return segments[0];
            }
            return segments[1];
        }
        return segments[0];
    }

    String getUserRealmEndpoint(String username) {
        return String.format(userRealmEndpointFormat, this.host, username);
    }

    private static boolean isAdfsAuthority(String firstPath) {
        return firstPath.compareToIgnoreCase(ADFS_PATH_SEGMENT) == 0;
    }

    private static boolean isB2CAuthority(String host, String firstPath) {
        return host.contains(B2C_HOST_SEGMENT) || firstPath.compareToIgnoreCase(B2C_PATH_SEGMENT) == 0;
    }

    private static boolean isCiamAuthority(String host) {
        return host.endsWith(".ciamlogin.com");
    }

    String deviceCodeEndpoint() {
        return this.deviceCodeEndpoint;
    }

    protected static String enforceTrailingSlash(String authority) {
        if (!(authority = authority.toLowerCase()).endsWith("/")) {
            authority = authority + "/";
        }
        return authority;
    }

    String authority() {
        return this.authority;
    }

    URL canonicalAuthorityUrl() {
        return this.canonicalAuthorityUrl;
    }

    AuthorityType authorityType() {
        return this.authorityType;
    }

    String selfSignedJwtAudience() {
        return this.selfSignedJwtAudience;
    }

    String host() {
        return this.host;
    }

    String tenant() {
        return this.tenant;
    }

    boolean isTenantless() {
        return this.isTenantless;
    }

    String authorizationEndpoint() {
        return this.authorizationEndpoint;
    }

    String tokenEndpoint() {
        return this.tokenEndpoint;
    }
}

