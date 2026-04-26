/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.StringHelper;

public class UserIdentifier {
    private static final String OID_HEADER_FORMAT = "%s@%s";
    private String upn;
    private String oid;

    private UserIdentifier() {
    }

    public static UserIdentifier fromUpn(String upn) {
        UserIdentifier userIdentifier = new UserIdentifier();
        userIdentifier.upn = upn;
        return userIdentifier;
    }

    public static UserIdentifier fromHomeAccountId(String homeAccountId) {
        UserIdentifier userIdentifier = new UserIdentifier();
        String[] homeAccountIdParts = homeAccountId.split("\\.");
        if (homeAccountIdParts.length < 2 || StringHelper.isBlank(homeAccountIdParts[0]) || StringHelper.isBlank(homeAccountIdParts[1])) {
            userIdentifier.oid = null;
            return userIdentifier;
        }
        userIdentifier.oid = String.format(OID_HEADER_FORMAT, homeAccountIdParts[0], homeAccountIdParts[1]);
        return userIdentifier;
    }

    String upn() {
        return this.upn;
    }

    String oid() {
        return this.oid;
    }
}

