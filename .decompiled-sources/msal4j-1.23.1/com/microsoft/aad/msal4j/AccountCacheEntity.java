/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.microsoft.aad.msal4j.Account;
import com.microsoft.aad.msal4j.Authority;
import com.microsoft.aad.msal4j.ClientInfo;
import com.microsoft.aad.msal4j.IAccount;
import com.microsoft.aad.msal4j.IdToken;
import com.microsoft.aad.msal4j.StringHelper;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

class AccountCacheEntity
implements JsonSerializable<AccountCacheEntity>,
Serializable {
    static final String MSSTS_ACCOUNT_TYPE = "MSSTS";
    static final String ADFS_ACCOUNT_TYPE = "ADFS";
    protected String homeAccountId;
    protected String environment;
    protected String realm;
    protected String localAccountId;
    protected String username;
    protected String name;
    protected String clientInfoStr;
    protected String userAssertionHash;
    protected String authorityType;

    AccountCacheEntity() {
    }

    static AccountCacheEntity fromJson(JsonReader jsonReader) throws IOException {
        AccountCacheEntity entity = new AccountCacheEntity();
        return jsonReader.readObject(reader -> {
            block22: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "home_account_id": {
                        entity.homeAccountId = reader.getString();
                        continue block22;
                    }
                    case "environment": {
                        entity.environment = reader.getString();
                        continue block22;
                    }
                    case "realm": {
                        entity.realm = reader.getString();
                        continue block22;
                    }
                    case "local_account_id": {
                        entity.localAccountId = reader.getString();
                        continue block22;
                    }
                    case "username": {
                        entity.username = reader.getString();
                        continue block22;
                    }
                    case "name": {
                        entity.name = reader.getString();
                        continue block22;
                    }
                    case "client_info": {
                        entity.clientInfoStr = reader.getString();
                        continue block22;
                    }
                    case "user_assertion_hash": {
                        entity.userAssertionHash = reader.getString();
                        continue block22;
                    }
                    case "authority_type": {
                        entity.authorityType = reader.getString();
                        continue block22;
                    }
                }
                reader.skipChildren();
            }
            return entity;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("home_account_id", this.homeAccountId);
        jsonWriter.writeStringField("environment", this.environment);
        jsonWriter.writeStringField("realm", this.realm);
        jsonWriter.writeStringField("local_account_id", this.localAccountId);
        jsonWriter.writeStringField("username", this.username);
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeStringField("client_info", this.clientInfoStr);
        jsonWriter.writeStringField("user_assertion_hash", this.userAssertionHash);
        jsonWriter.writeStringField("authority_type", this.authorityType);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    ClientInfo clientInfo() {
        return ClientInfo.createFromJson(this.clientInfoStr);
    }

    String getKey() {
        ArrayList<String> keyParts = new ArrayList<String>();
        keyParts.add(this.homeAccountId);
        keyParts.add(this.environment);
        keyParts.add(StringHelper.isBlank(this.realm) ? "" : this.realm);
        return String.join((CharSequence)"-", keyParts).toLowerCase();
    }

    static AccountCacheEntity create(String clientInfoStr, Authority requestAuthority, IdToken idToken, String policy) {
        AccountCacheEntity account = new AccountCacheEntity();
        account.authorityType(MSSTS_ACCOUNT_TYPE);
        account.clientInfoStr = clientInfoStr;
        account.homeAccountId(policy != null ? account.clientInfo().toAccountIdentifier() + "-" + policy : account.clientInfo().toAccountIdentifier());
        account.environment(requestAuthority.host());
        account.realm(requestAuthority.tenant());
        if (idToken != null) {
            String localAccountId = !StringHelper.isBlank(idToken.objectIdentifier) ? idToken.objectIdentifier : idToken.subject;
            account.localAccountId(localAccountId);
            account.username(idToken.preferredUsername);
            account.name(idToken.name);
        }
        return account;
    }

    static AccountCacheEntity createADFSAccount(Authority requestAuthority, IdToken idToken) {
        AccountCacheEntity account = new AccountCacheEntity();
        account.authorityType(ADFS_ACCOUNT_TYPE);
        account.homeAccountId(idToken.subject);
        account.environment(requestAuthority.host());
        account.username(idToken.upn);
        account.name(idToken.uniqueName);
        return account;
    }

    static AccountCacheEntity create(String clientInfoStr, Authority requestAuthority, IdToken idToken) {
        return AccountCacheEntity.create(clientInfoStr, requestAuthority, idToken, null);
    }

    IAccount toAccount() {
        return new Account(this.homeAccountId, this.environment, this.username, null);
    }

    String homeAccountId() {
        return this.homeAccountId;
    }

    String environment() {
        return this.environment;
    }

    String realm() {
        return this.realm;
    }

    String localAccountId() {
        return this.localAccountId;
    }

    String username() {
        return this.username;
    }

    String name() {
        return this.name;
    }

    String clientInfoStr() {
        return this.clientInfoStr;
    }

    String userAssertionHash() {
        return this.userAssertionHash;
    }

    String authorityType() {
        return this.authorityType;
    }

    void homeAccountId(String homeAccountId) {
        this.homeAccountId = homeAccountId;
    }

    void environment(String environment) {
        this.environment = environment;
    }

    void realm(String realm) {
        this.realm = realm;
    }

    void localAccountId(String localAccountId) {
        this.localAccountId = localAccountId;
    }

    void username(String username) {
        this.username = username;
    }

    void name(String name) {
        this.name = name;
    }

    void clientInfoStr(String clientInfoStr) {
        this.clientInfoStr = clientInfoStr;
    }

    void userAssertionHash(String userAssertionHash) {
        this.userAssertionHash = userAssertionHash;
    }

    void authorityType(String authorityType) {
        this.authorityType = authorityType;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AccountCacheEntity)) {
            return false;
        }
        AccountCacheEntity other = (AccountCacheEntity)o;
        if (!Objects.equals(this.homeAccountId(), other.homeAccountId())) {
            return false;
        }
        if (!Objects.equals(this.environment(), other.environment())) {
            return false;
        }
        if (!Objects.equals(this.realm(), other.realm())) {
            return false;
        }
        if (!Objects.equals(this.localAccountId(), other.localAccountId())) {
            return false;
        }
        if (!Objects.equals(this.username(), other.username())) {
            return false;
        }
        if (!Objects.equals(this.name(), other.name())) {
            return false;
        }
        if (!Objects.equals(this.clientInfoStr(), other.clientInfoStr())) {
            return false;
        }
        if (!Objects.equals(this.userAssertionHash(), other.userAssertionHash())) {
            return false;
        }
        return Objects.equals(this.authorityType(), other.authorityType());
    }

    public int hashCode() {
        int result = 1;
        result = result * 59 + (this.homeAccountId == null ? 43 : this.homeAccountId.hashCode());
        result = result * 59 + (this.environment == null ? 43 : this.environment.hashCode());
        result = result * 59 + (this.realm == null ? 43 : this.realm.hashCode());
        result = result * 59 + (this.localAccountId == null ? 43 : this.localAccountId.hashCode());
        result = result * 59 + (this.username == null ? 43 : this.username.hashCode());
        result = result * 59 + (this.name() == null ? 43 : this.name().hashCode());
        result = result * 59 + (this.clientInfoStr == null ? 43 : this.clientInfoStr.hashCode());
        result = result * 59 + (this.userAssertionHash == null ? 43 : this.userAssertionHash.hashCode());
        result = result * 59 + (this.authorityType == null ? 43 : this.authorityType.hashCode());
        return result;
    }
}

