/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.StringHelper;

public enum InteractionRequiredExceptionReason {
    NONE("none"),
    MESSAGE_ONLY("message_only"),
    BASIC_ACTION("basic_action"),
    ADDITIONAL_ACTION("additional_action"),
    CONSENT_REQUIRED("consent_required"),
    USER_PASSWORD_EXPIRED("user_password_expired");

    private String error;

    private InteractionRequiredExceptionReason(String error) {
        this.error = error;
    }

    static InteractionRequiredExceptionReason fromSubErrorString(String subError) {
        if (StringHelper.isBlank(subError)) {
            return NONE;
        }
        for (InteractionRequiredExceptionReason reason : InteractionRequiredExceptionReason.values()) {
            if (!reason.error.equalsIgnoreCase(subError)) continue;
            return reason;
        }
        return NONE;
    }
}

