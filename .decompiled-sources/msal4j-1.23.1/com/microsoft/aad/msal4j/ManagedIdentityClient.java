/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.AbstractManagedIdentitySource;
import com.microsoft.aad.msal4j.AppServiceManagedIdentitySource;
import com.microsoft.aad.msal4j.AzureArcManagedIdentitySource;
import com.microsoft.aad.msal4j.CloudShellManagedIdentitySource;
import com.microsoft.aad.msal4j.IEnvironmentVariables;
import com.microsoft.aad.msal4j.IMDSManagedIdentitySource;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentityIdType;
import com.microsoft.aad.msal4j.ManagedIdentityParameters;
import com.microsoft.aad.msal4j.ManagedIdentityResponse;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import com.microsoft.aad.msal4j.MsalRequest;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.ServiceFabricManagedIdentitySource;
import com.microsoft.aad.msal4j.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class ManagedIdentityClient {
    private static final Logger LOG = LoggerFactory.getLogger(ManagedIdentityClient.class);
    AbstractManagedIdentitySource managedIdentitySource;

    static ManagedIdentitySourceType getManagedIdentitySource() {
        IEnvironmentVariables environmentVariables = AbstractManagedIdentitySource.getEnvironmentVariables();
        if (!StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("IDENTITY_ENDPOINT")) && !StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("IDENTITY_HEADER"))) {
            if (!StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("IDENTITY_SERVER_THUMBPRINT"))) {
                return ManagedIdentitySourceType.SERVICE_FABRIC;
            }
            return ManagedIdentitySourceType.APP_SERVICE;
        }
        if (!StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("MSI_ENDPOINT"))) {
            return ManagedIdentitySourceType.CLOUD_SHELL;
        }
        if (!StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("IDENTITY_ENDPOINT")) && !StringHelper.isNullOrBlank(environmentVariables.getEnvironmentVariable("IMDS_ENDPOINT"))) {
            return ManagedIdentitySourceType.AZURE_ARC;
        }
        return ManagedIdentitySourceType.DEFAULT_TO_IMDS;
    }

    ManagedIdentityClient(MsalRequest msalRequest, ServiceBundle serviceBundle) {
        this.managedIdentitySource = ManagedIdentityClient.createManagedIdentitySource(msalRequest, serviceBundle);
        ManagedIdentityApplication managedIdentityApplication = (ManagedIdentityApplication)msalRequest.application();
        ManagedIdentityIdType identityIdType = managedIdentityApplication.getManagedIdentityId().getIdType();
        if (!identityIdType.equals((Object)ManagedIdentityIdType.SYSTEM_ASSIGNED)) {
            this.managedIdentitySource.setUserAssignedManagedIdentity(true);
        }
    }

    ManagedIdentityResponse getManagedIdentityResponse(ManagedIdentityParameters parameters) {
        return this.managedIdentitySource.getManagedIdentityResponse(parameters);
    }

    private static AbstractManagedIdentitySource createManagedIdentitySource(MsalRequest msalRequest, ServiceBundle serviceBundle) {
        switch (ManagedIdentityClient.getManagedIdentitySource()) {
            case SERVICE_FABRIC: {
                return ServiceFabricManagedIdentitySource.create(msalRequest, serviceBundle);
            }
            case APP_SERVICE: {
                return AppServiceManagedIdentitySource.create(msalRequest, serviceBundle);
            }
            case CLOUD_SHELL: {
                return CloudShellManagedIdentitySource.create(msalRequest, serviceBundle);
            }
            case AZURE_ARC: {
                return AzureArcManagedIdentitySource.create(msalRequest, serviceBundle);
            }
        }
        return new IMDSManagedIdentitySource(msalRequest, serviceBundle);
    }
}

