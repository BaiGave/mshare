/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonLocation;
import com.azure.json.implementation.jackson.core.JsonParseException;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.JsonProcessingException;
import com.azure.json.implementation.jackson.core.JsonStreamContext;
import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.json.DupDetector;

public final class JsonReadContext
extends JsonStreamContext {
    private final JsonReadContext _parent;
    private DupDetector _dups;
    private JsonReadContext _child;
    private String _currentName;
    private Object _currentValue;
    private int _lineNr;
    private int _columnNr;

    public JsonReadContext(JsonReadContext parent, DupDetector dups, int type, int lineNr, int colNr) {
        this._parent = parent;
        this._dups = dups;
        this._type = type;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._index = -1;
    }

    public void reset(int type, int lineNr, int colNr) {
        this._type = type;
        this._index = -1;
        this._lineNr = lineNr;
        this._columnNr = colNr;
        this._currentName = null;
        this._currentValue = null;
        if (this._dups != null) {
            this._dups.reset();
        }
    }

    public JsonReadContext withDupDetector(DupDetector dups) {
        this._dups = dups;
        return this;
    }

    @Override
    public Object getCurrentValue() {
        return this._currentValue;
    }

    @Override
    public void setCurrentValue(Object v) {
        this._currentValue = v;
    }

    public static JsonReadContext createRootContext(DupDetector dups) {
        return new JsonReadContext(null, dups, 0, 1, 0);
    }

    public JsonReadContext createChildArrayContext(int lineNr, int colNr) {
        JsonReadContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new JsonReadContext(this, this._dups == null ? null : this._dups.child(), 1, lineNr, colNr);
        } else {
            ctxt.reset(1, lineNr, colNr);
        }
        return ctxt;
    }

    public JsonReadContext createChildObjectContext(int lineNr, int colNr) {
        JsonReadContext ctxt = this._child;
        if (ctxt == null) {
            this._child = ctxt = new JsonReadContext(this, this._dups == null ? null : this._dups.child(), 2, lineNr, colNr);
            return ctxt;
        }
        ctxt.reset(2, lineNr, colNr);
        return ctxt;
    }

    @Override
    public String getCurrentName() {
        return this._currentName;
    }

    @Override
    public JsonReadContext getParent() {
        return this._parent;
    }

    @Override
    public JsonLocation startLocation(ContentReference srcRef) {
        long totalChars = -1L;
        return new JsonLocation(srcRef, totalChars, this._lineNr, this._columnNr);
    }

    @Override
    @Deprecated
    public JsonLocation getStartLocation(Object rawSrc) {
        return this.startLocation(ContentReference.rawReference(rawSrc));
    }

    public JsonReadContext clearAndGetParent() {
        this._currentValue = null;
        return this._parent;
    }

    public DupDetector getDupDetector() {
        return this._dups;
    }

    public boolean expectComma() {
        int ix = ++this._index;
        return this._type != 0 && ix > 0;
    }

    public void setCurrentName(String name) throws JsonProcessingException {
        this._currentName = name;
        if (this._dups != null) {
            this._checkDup(this._dups, name);
        }
    }

    private void _checkDup(DupDetector dd, String name) throws JsonProcessingException {
        if (dd.isDup(name)) {
            Object src = dd.getSource();
            throw new JsonParseException(src instanceof JsonParser ? (JsonParser)src : null, "Duplicate field '" + name + "'");
        }
    }
}

