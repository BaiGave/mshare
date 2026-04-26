/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.ObjectCodec;
import com.azure.json.implementation.jackson.core.SerializableString;
import com.azure.json.implementation.jackson.core.StreamWriteCapability;
import com.azure.json.implementation.jackson.core.Version;
import com.azure.json.implementation.jackson.core.base.GeneratorBase;
import com.azure.json.implementation.jackson.core.io.CharTypes;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.SerializedString;
import com.azure.json.implementation.jackson.core.util.JacksonFeatureSet;
import com.azure.json.implementation.jackson.core.util.VersionUtil;
import java.io.IOException;

public abstract class JsonGeneratorImpl
extends GeneratorBase {
    protected static final int[] sOutputEscapes = CharTypes.get7BitOutputEscapes();
    protected static final JacksonFeatureSet<StreamWriteCapability> JSON_WRITE_CAPABILITIES = DEFAULT_TEXTUAL_WRITE_CAPABILITIES;
    protected final IOContext _ioContext;
    protected int[] _outputEscapes = sOutputEscapes;
    protected int _maximumNonEscapedChar;
    protected CharacterEscapes _characterEscapes;
    protected SerializableString _rootValueSeparator = new SerializedString(" ");
    protected boolean _cfgUnqNames;

    public JsonGeneratorImpl(IOContext ctxt, int features, ObjectCodec codec) {
        super(features, codec);
        this._ioContext = ctxt;
        if (JsonGenerator.Feature.ESCAPE_NON_ASCII.enabledIn(features)) {
            this._maximumNonEscapedChar = 127;
        }
        this._cfgUnqNames = !JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledIn(features);
    }

    @Override
    public Version version() {
        return VersionUtil.versionFor(this.getClass());
    }

    @Override
    public JsonGenerator enable(JsonGenerator.Feature f) {
        super.enable(f);
        if (f == JsonGenerator.Feature.QUOTE_FIELD_NAMES) {
            this._cfgUnqNames = false;
        }
        return this;
    }

    @Override
    public JsonGenerator disable(JsonGenerator.Feature f) {
        super.disable(f);
        if (f == JsonGenerator.Feature.QUOTE_FIELD_NAMES) {
            this._cfgUnqNames = true;
        }
        return this;
    }

    @Override
    protected void _checkStdFeatureChanges(int newFeatureFlags, int changedFeatures) {
        super._checkStdFeatureChanges(newFeatureFlags, changedFeatures);
        this._cfgUnqNames = !JsonGenerator.Feature.QUOTE_FIELD_NAMES.enabledIn(newFeatureFlags);
    }

    @Override
    public JsonGenerator setHighestNonEscapedChar(int charCode) {
        this._maximumNonEscapedChar = Math.max(charCode, 0);
        return this;
    }

    @Override
    public int getHighestEscapedChar() {
        return this._maximumNonEscapedChar;
    }

    @Override
    public JsonGenerator setCharacterEscapes(CharacterEscapes esc) {
        this._characterEscapes = esc;
        this._outputEscapes = esc == null ? sOutputEscapes : esc.getEscapeCodesForAscii();
        return this;
    }

    @Override
    public CharacterEscapes getCharacterEscapes() {
        return this._characterEscapes;
    }

    @Override
    public JsonGenerator setRootValueSeparator(SerializableString sep) {
        this._rootValueSeparator = sep;
        return this;
    }

    @Override
    public JacksonFeatureSet<StreamWriteCapability> getWriteCapabilities() {
        return JSON_WRITE_CAPABILITIES;
    }

    protected void _reportCantWriteValueExpectName(String typeMsg) throws IOException {
        this._reportError(String.format("Can not %s, expecting field name (context: %s)", typeMsg, this._writeContext.typeDesc()));
    }
}

