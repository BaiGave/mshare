/*
 * Decompiled with CFR 0.152.
 */
package com.microsoft.aad.msal4j;

import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

public class ManagedIdentityErrorResponse
implements JsonSerializable<ManagedIdentityErrorResponse> {
    private String message;
    private String correlationId;
    private String error;
    private String errorDescription;

    public static ManagedIdentityErrorResponse fromJson(JsonReader jsonReader) throws IOException {
        ManagedIdentityErrorResponse response = new ManagedIdentityErrorResponse();
        return jsonReader.readObject(reader -> {
            block12: while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                switch (fieldName) {
                    case "message": {
                        response.message = reader.getString();
                        continue block12;
                    }
                    case "correlationId": {
                        response.correlationId = reader.getString();
                        continue block12;
                    }
                    case "error": {
                        if (reader.currentToken() == JsonToken.START_OBJECT) {
                            ErrorField errorField = ErrorField.fromJson(reader);
                            response.error = errorField.getCode();
                            response.message = errorField.getMessage();
                            continue block12;
                        }
                        response.error = reader.getString();
                        continue block12;
                    }
                    case "error_description": {
                        response.errorDescription = reader.getString();
                        continue block12;
                    }
                }
                reader.skipChildren();
            }
            return response;
        });
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("message", this.message);
        jsonWriter.writeStringField("correlationId", this.correlationId);
        jsonWriter.writeStringField("error", this.error);
        jsonWriter.writeStringField("error_description", this.errorDescription);
        jsonWriter.writeEndObject();
        return jsonWriter;
    }

    public String getMessage() {
        return this.message;
    }

    public String getCorrelationId() {
        return this.correlationId;
    }

    public String getError() {
        return this.error;
    }

    public String getErrorDescription() {
        return this.errorDescription;
    }

    private static class ErrorField {
        private String code;
        private String message;

        private ErrorField() {
        }

        static ErrorField fromJson(JsonReader jsonReader) throws IOException {
            ErrorField errorField = new ErrorField();
            return jsonReader.readObject(reader -> {
                block8: while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();
                    switch (fieldName) {
                        case "code": {
                            errorField.code = reader.getString();
                            continue block8;
                        }
                        case "message": {
                            errorField.message = reader.getString();
                            continue block8;
                        }
                    }
                    reader.skipChildren();
                }
                return errorField;
            });
        }

        String getCode() {
            return this.code;
        }

        String getMessage() {
            return this.message;
        }
    }
}

