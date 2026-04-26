/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.microsoft.aad.msal4j.BindingPolicy;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import com.microsoft.aad.msal4j.IHttpResponse;
import com.microsoft.aad.msal4j.MexParser;
import com.microsoft.aad.msal4j.MsalServiceException;
import com.microsoft.aad.msal4j.MsalServiceExceptionFactory;
import com.microsoft.aad.msal4j.RequestContext;
import com.microsoft.aad.msal4j.ServiceBundle;
import com.microsoft.aad.msal4j.StringHelper;
import com.microsoft.aad.msal4j.WSTrustResponse;
import com.microsoft.aad.msal4j.WSTrustVersion;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

class WSTrustRequest {
    private static final int MAX_EXPECTED_MESSAGE_SIZE = 1024;
    static final String DEFAULT_APPLIES_TO = "urn:federation:MicrosoftOnline";

    WSTrustRequest() {
    }

    static WSTrustResponse execute(String username, String password, String cloudAudienceUrn, BindingPolicy policy, RequestContext requestContext, ServiceBundle serviceBundle) throws Exception {
        HashMap<String, String> headers = new HashMap<String, String>();
        headers.put("Content-Type", "application/soap+xml; charset=utf-8");
        headers.put("return-client-request-id", "true");
        String soapAction = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue";
        if (policy.getVersion() == WSTrustVersion.WSTRUST2005) {
            soapAction = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue";
        }
        headers.put("SOAPAction", soapAction);
        String body = WSTrustRequest.buildMessage(policy.getUrl(), username, password, policy.getVersion(), cloudAudienceUrn).toString();
        HttpRequest httpRequest = new HttpRequest(HttpMethod.POST, policy.getUrl(), headers, body);
        IHttpResponse response = serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, requestContext, serviceBundle);
        return WSTrustResponse.parse(response.body(), policy.getVersion());
    }

    static WSTrustResponse execute(String url, String username, String password, String cloudAudienceUrn, RequestContext requestContext, ServiceBundle serviceBundle, boolean logPii) throws Exception {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, url);
        IHttpResponse mexResponse = serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, requestContext, serviceBundle);
        if (mexResponse.statusCode() != 200 || StringHelper.isBlank(mexResponse.body())) {
            throw MsalServiceExceptionFactory.fromHttpResponse(mexResponse);
        }
        BindingPolicy policy = MexParser.getWsTrustEndpointFromMexResponse(mexResponse.body(), logPii);
        if (policy == null) {
            throw new MsalServiceException("WsTrust endpoint not found in metadata document", "wstrust_endpoint_not_found");
        }
        return WSTrustRequest.execute(username, password, cloudAudienceUrn, policy, requestContext, serviceBundle);
    }

    static WSTrustResponse execute(String mexURL, String cloudAudienceUrn, RequestContext requestContext, ServiceBundle serviceBundle, boolean logPii) throws Exception {
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, mexURL);
        IHttpResponse mexResponse = serviceBundle.getHttpHelper().executeHttpRequest(httpRequest, requestContext, serviceBundle);
        BindingPolicy policy = MexParser.getPolicyFromMexResponseForIntegrated(mexResponse.body(), logPii);
        if (policy == null) {
            throw new MsalServiceException("WsTrust endpoint not found in metadata document", "wstrust_endpoint_not_found");
        }
        return WSTrustRequest.execute(null, null, cloudAudienceUrn, policy, requestContext, serviceBundle);
    }

    static StringBuilder buildMessage(String address, String username, String password, WSTrustVersion addressVersion, String cloudAudienceUrn) {
        boolean integrated = username == null & password == null;
        StringBuilder securityHeaderBuilder = new StringBuilder(1024);
        if (!integrated) {
            WSTrustRequest.buildSecurityHeader(securityHeaderBuilder, username, password, addressVersion);
        }
        String guid = UUID.randomUUID().toString();
        StringBuilder messageBuilder = new StringBuilder(1024);
        String schemaLocation = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
        String soapAction = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/RST/Issue";
        String rstTrustNamespace = "http://docs.oasis-open.org/ws-sx/ws-trust/200512";
        String keyType = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Bearer";
        String requestType = "http://docs.oasis-open.org/ws-sx/ws-trust/200512/Issue";
        if (addressVersion == WSTrustVersion.WSTRUST2005) {
            soapAction = "http://schemas.xmlsoap.org/ws/2005/02/trust/RST/Issue";
            rstTrustNamespace = "http://schemas.xmlsoap.org/ws/2005/02/trust";
            keyType = "http://schemas.xmlsoap.org/ws/2005/05/identity/NoProofKey";
            requestType = "http://schemas.xmlsoap.org/ws/2005/02/trust/Issue";
        }
        messageBuilder.append(String.format("<s:Envelope xmlns:s='http://www.w3.org/2003/05/soap-envelope' xmlns:a='http://www.w3.org/2005/08/addressing' xmlns:u='%s'><s:Header><a:Action s:mustUnderstand='1'>%s</a:Action><a:messageID>urn:uuid:%s</a:messageID><a:ReplyTo><a:Address>http://www.w3.org/2005/08/addressing/anonymous</a:Address></a:ReplyTo><a:To s:mustUnderstand='1'>%s</a:To>%s</s:Header><s:Body><trust:RequestSecurityToken xmlns:trust='%s'><wsp:AppliesTo xmlns:wsp='http://schemas.xmlsoap.org/ws/2004/09/policy'><a:EndpointReference><a:Address>%s</a:Address></a:EndpointReference></wsp:AppliesTo><trust:KeyType>%s</trust:KeyType><trust:RequestType>%s</trust:RequestType></trust:RequestSecurityToken></s:Body></s:Envelope>", schemaLocation, soapAction, guid, address, integrated ? "" : securityHeaderBuilder.toString(), rstTrustNamespace, StringHelper.isBlank(cloudAudienceUrn) ? DEFAULT_APPLIES_TO : cloudAudienceUrn, keyType, requestType));
        return messageBuilder;
    }

    static String escapeXMLElementData(String data) {
        StringBuilder sb = new StringBuilder();
        block7: for (char ch : data.toCharArray()) {
            switch (ch) {
                case '<': {
                    sb.append("&lt;");
                    continue block7;
                }
                case '>': {
                    sb.append("&gt;");
                    continue block7;
                }
                case '\"': {
                    sb.append("&quot;");
                    continue block7;
                }
                case '\'': {
                    sb.append("&apos;");
                    continue block7;
                }
                case '&': {
                    sb.append("&amp;");
                    continue block7;
                }
                default: {
                    sb.append(ch);
                }
            }
        }
        return sb.toString();
    }

    private static StringBuilder buildSecurityHeader(StringBuilder securityHeaderBuilder, String username, String password, WSTrustVersion version) {
        StringBuilder messageCredentialsBuilder = new StringBuilder(1024);
        String guid = UUID.randomUUID().toString();
        username = WSTrustRequest.escapeXMLElementData(username);
        password = WSTrustRequest.escapeXMLElementData(password);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        String currentTimeString = dateFormat.format(date);
        int toAdd = 600000;
        date = new Date(date.getTime() + (long)toAdd);
        String expiryTimeString = dateFormat.format(date);
        messageCredentialsBuilder.append(String.format("<o:UsernameToken u:Id='uuid-%s'><o:Username>%s</o:Username><o:Password>%s</o:Password></o:UsernameToken>", guid, username, password));
        securityHeaderBuilder.append("<o:Security s:mustUnderstand='1' xmlns:o='http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd'>");
        securityHeaderBuilder.append(String.format("<u:Timestamp u:Id='_0'><u:Created>%s</u:Created><u:Expires>%s</u:Expires></u:Timestamp>", currentTimeString, expiryTimeString));
        securityHeaderBuilder.append(messageCredentialsBuilder.toString());
        securityHeaderBuilder.append("</o:Security>");
        return securityHeaderBuilder;
    }
}

