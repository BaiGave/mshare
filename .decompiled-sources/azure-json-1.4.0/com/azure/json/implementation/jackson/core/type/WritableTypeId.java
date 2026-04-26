/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.type;

import com.azure.json.implementation.jackson.core.JsonToken;

public class WritableTypeId {
    public Object forValue;
    public Object id;
    public Inclusion include;
    public JsonToken valueShape;
    public Object extra;

    public WritableTypeId(Object value, JsonToken valueShape, Object id) {
        this.forValue = value;
        this.id = id;
        this.valueShape = valueShape;
    }

    public static enum Inclusion {
        WRAPPER_ARRAY,
        WRAPPER_OBJECT,
        METADATA_PROPERTY,
        PARENT_PROPERTY;

    }
}

