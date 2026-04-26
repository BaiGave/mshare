/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.ClientAssertion;
import com.microsoft.aad.msal4j.ClientCertificate;
import com.microsoft.aad.msal4j.JsonHelper;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.ParameterValidationUtils;
import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.UUID;

final class JwtHelper {
    JwtHelper() {
    }

    static ClientAssertion buildJwt(String clientId, ClientCertificate credential, String jwtAudience, boolean sendX5c, boolean useSha1) throws MsalClientException {
        ParameterValidationUtils.validateNotBlank("clientId", clientId);
        ParameterValidationUtils.validateNotNull("credential", clientId);
        try {
            long time = System.currentTimeMillis();
            HashMap<String, Object> header = new HashMap<String, Object>();
            header.put("alg", "RS256");
            header.put("typ", "JWT");
            if (sendX5c) {
                ArrayList<String> certs = new ArrayList<String>(credential.getEncodedPublicKeyCertificateChain());
                header.put("x5c", certs);
            }
            String hash256 = credential.publicCertificateHash256();
            if (useSha1 || hash256 == null) {
                header.put("x5t", credential.publicCertificateHash());
            } else {
                header.put("x5t#S256", hash256);
            }
            HashMap<String, Object> payload = new HashMap<String, Object>();
            payload.put("aud", jwtAudience);
            payload.put("iss", clientId);
            payload.put("jti", UUID.randomUUID().toString());
            payload.put("nbf", time / 1000L);
            payload.put("exp", time / 1000L + 600L);
            payload.put("sub", clientId);
            String jsonHeader = JsonHelper.writeJsonMap(header);
            String jsonPayload = JsonHelper.writeJsonMap(payload);
            String encodedHeader = JwtHelper.base64UrlEncode(jsonHeader.getBytes(StandardCharsets.UTF_8));
            String encodedPayload = JwtHelper.base64UrlEncode(jsonPayload.getBytes(StandardCharsets.UTF_8));
            String dataToSign = encodedHeader + "." + encodedPayload;
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initSign(credential.privateKey());
            sig.update(dataToSign.getBytes(StandardCharsets.UTF_8));
            byte[] signatureBytes = sig.sign();
            String encodedSignature = JwtHelper.base64UrlEncode(signatureBytes);
            String jwt = dataToSign + "." + encodedSignature;
            return new ClientAssertion(jwt);
        }
        catch (Exception e) {
            throw new MsalClientException(e);
        }
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }
}

