/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ErrorResponse;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.MsalInteractionRequiredException;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.StringHelper;
import java.util.Arrays;
import java.util.HashSet;

class MsalServiceExceptionFactory {
    private MsalServiceExceptionFactory() {
    }

    static MsalServiceException fromHttpResponse(IHttpResponse response) {
        String responseBody = response.body();
        if (StringHelper.isBlank(responseBody)) {
            return new MsalServiceException(String.format("Unknown service exception. Http request returned status code %s with no response body", response.statusCode()), "unknown");
        }
        ErrorResponse errorResponse = JsonHelper.convertJsonStringToJsonSerializableObject(responseBody, ErrorResponse::fromJson);
        if (errorResponse.error() != null && errorResponse.error().equalsIgnoreCase("invalid_grant") && MsalServiceExceptionFactory.isInteractionRequired(errorResponse.subError)) {
            return new MsalInteractionRequiredException(errorResponse, response.headers());
        }
        if (!StringHelper.isBlank(errorResponse.error()) && !StringHelper.isBlank(errorResponse.errorDescription)) {
            errorResponse.statusCode(response.statusCode());
            return new MsalServiceException(errorResponse, response.headers());
        }
        return new MsalServiceException(String.format("Unknown service exception. Http request returned status code: %s with http body: %s", response.statusCode(), responseBody), "unknown");
    }

    private static boolean isInteractionRequired(String subError) {
        String[] nonUiSubErrors = new String[]{"client_mismatch", "protection_policy_required"};
        HashSet<String> set = new HashSet<String>(Arrays.asList(nonUiSubErrors));
        if (StringHelper.isBlank(subError)) {
            return true;
        }
        return !set.contains(subError);
    }
}

