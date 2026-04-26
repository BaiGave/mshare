/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.MsalClientException;
import com.microsoft.aad.msal4j.RequestedClaim;
import com.microsoft.aad.msal4j.RequestedClaimAdditionalInfo;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClaimsRequest
implements JsonSerializable<ClaimsRequest> {
    List<RequestedClaim> idTokenRequestedClaims = new ArrayList<RequestedClaim>();
    List<RequestedClaim> userInfoRequestedClaims = new ArrayList<RequestedClaim>();
    List<RequestedClaim> accessTokenRequestedClaims = new ArrayList<RequestedClaim>();

    public void requestClaimInIdToken(String claim, RequestedClaimAdditionalInfo requestedClaimAdditionalInfo) {
        this.idTokenRequestedClaims.add(new RequestedClaim(claim, requestedClaimAdditionalInfo));
    }

    protected void requestClaimInUserInfo(String claim, RequestedClaimAdditionalInfo requestedClaimAdditionalInfo) {
        this.userInfoRequestedClaims.add(new RequestedClaim(claim, requestedClaimAdditionalInfo));
    }

    protected void requestClaimInAccessToken(String claim, RequestedClaimAdditionalInfo requestedClaimAdditionalInfo) {
        this.accessTokenRequestedClaims.add(new RequestedClaim(claim, requestedClaimAdditionalInfo));
    }

    /*
     * Exception decompiling
     */
    public String formatAsJSONString() {
        /*
         * This method has failed to decompile.  When submitting a bug report, please provide this stack trace, and (if you hold appropriate legal rights) the relevant class file.
         * 
         * org.benf.cfr.reader.util.ConfusedCFRException: Started 3 blocks at once
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.getStartingBlocks(Op04StructuredStatement.java:412)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op04StructuredStatement.buildNestedBlocks(Op04StructuredStatement.java:487)
         *     at org.benf.cfr.reader.bytecode.analysis.opgraph.Op03SimpleStatement.createInitialStructuredBlock(Op03SimpleStatement.java:736)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisInner(CodeAnalyser.java:850)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysisOrWrapFail(CodeAnalyser.java:278)
         *     at org.benf.cfr.reader.bytecode.CodeAnalyser.getAnalysis(CodeAnalyser.java:201)
         *     at org.benf.cfr.reader.entities.attributes.AttributeCode.analyse(AttributeCode.java:94)
         *     at org.benf.cfr.reader.entities.Method.analyse(Method.java:531)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseMid(ClassFile.java:1055)
         *     at org.benf.cfr.reader.entities.ClassFile.analyseTop(ClassFile.java:942)
         *     at org.benf.cfr.reader.Driver.doJarVersionTypes(Driver.java:257)
         *     at org.benf.cfr.reader.Driver.doJar(Driver.java:139)
         *     at org.benf.cfr.reader.CfrDriverImpl.analyse(CfrDriverImpl.java:76)
         *     at org.benf.cfr.reader.Main.main(Main.java:54)
         */
        throw new IllegalStateException("Decompilation failed");
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        this.writeClaimsToJsonWriter(jsonWriter, "id_token", this.idTokenRequestedClaims);
        this.writeClaimsToJsonWriter(jsonWriter, "userinfo", this.userInfoRequestedClaims);
        this.writeClaimsToJsonWriter(jsonWriter, "access_token", this.accessTokenRequestedClaims);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    private void writeClaimsToJsonWriter(JsonWriter jsonWriter, String sectionName, List<RequestedClaim> claims) throws IOException {
        if (claims.isEmpty()) {
            return;
        }
        jsonWriter.writeStartObject(sectionName);
        for (RequestedClaim claim : claims) {
            if (claim.name == null) continue;
            if (claim.getRequestedClaimAdditionalInfo() != null) {
                jsonWriter.writeJsonField(claim.name, claim.getRequestedClaimAdditionalInfo());
                continue;
            }
            jsonWriter.writeNullField(claim.name);
        }
        jsonWriter.writeEndObject();
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    public static ClaimsRequest formatAsClaimsRequest(String claims) {
        try (JsonReader jsonReader = JsonProviders.createReader(claims);){
            ClaimsRequest claimsRequest = new ClaimsRequest();
            ClaimsRequest claimsRequest2 = jsonReader.readObject(reader -> {
                if (reader.currentToken() != JsonToken.START_OBJECT) {
                    throw new IllegalStateException("Expected start of object but was " + (Object)((Object)reader.currentToken()));
                }
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    ClaimsRequest.parseClaims(reader, claimsRequest, reader.getFieldName());
                }
                return claimsRequest;
            });
            return claimsRequest2;
        }
        catch (IOException e) {
            throw new MsalClientException("Could not convert string to ClaimsRequest: " + e.getMessage(), "invalid_json");
        }
    }

    private static void parseClaims(JsonReader jsonReader, ClaimsRequest claimsRequest, String section) throws IOException {
        if (jsonReader.currentToken() != JsonToken.FIELD_NAME) {
            jsonReader.nextToken();
        }
        jsonReader.nextToken();
        if (jsonReader.currentToken() == JsonToken.NULL) {
            return;
        }
        if (jsonReader.currentToken() != JsonToken.START_OBJECT) {
            throw new IllegalStateException("Expected start of object but was " + (Object)((Object)jsonReader.currentToken()));
        }
        while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
            String claimName = jsonReader.getFieldName();
            jsonReader.nextToken();
            RequestedClaimAdditionalInfo claimInfo = null;
            if (jsonReader.currentToken() == JsonToken.START_OBJECT) {
                boolean essential = false;
                String value = null;
                ArrayList<String> values = null;
                block21: while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = jsonReader.getFieldName();
                    jsonReader.nextToken();
                    switch (fieldName) {
                        case "essential": {
                            essential = jsonReader.getBoolean();
                            continue block21;
                        }
                        case "value": {
                            value = jsonReader.getString();
                            continue block21;
                        }
                        case "values": {
                            values = new ArrayList<String>();
                            if (jsonReader.currentToken() != JsonToken.START_ARRAY) continue block21;
                            while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                                values.add(jsonReader.getString());
                            }
                            continue block21;
                        }
                    }
                    jsonReader.skipChildren();
                }
                if (essential || value != null || values != null) {
                    claimInfo = new RequestedClaimAdditionalInfo(essential, value, values);
                }
            }
            switch (section) {
                case "access_token": {
                    claimsRequest.requestClaimInAccessToken(claimName, claimInfo);
                    break;
                }
                case "id_token": {
                    claimsRequest.requestClaimInIdToken(claimName, claimInfo);
                    break;
                }
                case "userinfo": {
                    claimsRequest.requestClaimInUserInfo(claimName, claimInfo);
                }
            }
        }
    }

    public List<RequestedClaim> getIdTokenRequestedClaims() {
        return this.idTokenRequestedClaims;
    }

    public void setIdTokenRequestedClaims(List<RequestedClaim> idTokenRequestedClaims) {
        this.idTokenRequestedClaims = idTokenRequestedClaims;
    }
}

