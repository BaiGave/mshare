/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.StringHelper;

public class ManagedIdentityId {
    private String userAssignedId;
    private ManagedIdentityIdType idType;

    private ManagedIdentityId(ManagedIdentityIdType idType) {
        this.idType = idType;
    }

    private ManagedIdentityId(ManagedIdentityIdType idType, String id) {
        this.idType = idType;
        this.userAssignedId = id;
    }

    public static ManagedIdentityId systemAssigned() {
        return new ManagedIdentityId(ManagedIdentityIdType.SYSTEM_ASSIGNED);
    }

    public static ManagedIdentityId userAssignedClientId(String clientId) {
        if (StringHelper.isNullOrBlank(clientId)) {
            throw new NullPointerException(clientId);
        }
        return new ManagedIdentityId(ManagedIdentityIdType.CLIENT_ID, clientId);
    }

    public static ManagedIdentityId userAssignedResourceId(String resourceId) {
        if (StringHelper.isNullOrBlank(resourceId)) {
            throw new NullPointerException(resourceId);
        }
        return new ManagedIdentityId(ManagedIdentityIdType.RESOURCE_ID, resourceId);
    }

    public static ManagedIdentityId userAssignedObjectId(String objectId) {
        if (StringHelper.isNullOrBlank(objectId)) {
            throw new NullPointerException(objectId);
        }
        return new ManagedIdentityId(ManagedIdentityIdType.OBJECT_ID, objectId);
    }

    public String getUserAssignedId() {
        return this.userAssignedId;
    }

    public ManagedIdentityIdType getIdType() {
        return this.idType;
    }
}

