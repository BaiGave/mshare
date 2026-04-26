/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core;

import com.azure.json.implementation.jackson.core.JsonEncoding;
import com.azure.json.implementation.jackson.core.JsonFactoryBuilder;
import com.azure.json.implementation.jackson.core.JsonGenerator;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.ObjectCodec;
import com.azure.json.implementation.jackson.core.SerializableString;
import com.azure.json.implementation.jackson.core.StreamReadFeature;
import com.azure.json.implementation.jackson.core.StreamWriteFeature;
import com.azure.json.implementation.jackson.core.TSFBuilder;
import com.azure.json.implementation.jackson.core.TokenStreamFactory;
import com.azure.json.implementation.jackson.core.Version;
import com.azure.json.implementation.jackson.core.Versioned;
import com.azure.json.implementation.jackson.core.io.CharacterEscapes;
import com.azure.json.implementation.jackson.core.io.ContentReference;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.InputDecorator;
import com.azure.json.implementation.jackson.core.io.OutputDecorator;
import com.azure.json.implementation.jackson.core.io.SerializedString;
import com.azure.json.implementation.jackson.core.io.UTF8Writer;
import com.azure.json.implementation.jackson.core.json.ByteSourceJsonBootstrapper;
import com.azure.json.implementation.jackson.core.json.PackageVersion;
import com.azure.json.implementation.jackson.core.json.ReaderBasedJsonParser;
import com.azure.json.implementation.jackson.core.json.UTF8JsonGenerator;
import com.azure.json.implementation.jackson.core.json.WriterBasedJsonGenerator;
import com.azure.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.azure.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import com.azure.json.implementation.jackson.core.util.BufferRecycler;
import com.azure.json.implementation.jackson.core.util.BufferRecyclers;
import com.azure.json.implementation.jackson.core.util.JacksonFeature;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.Writer;
import java.net.URL;

public class JsonFactory
extends TokenStreamFactory
implements Versioned,
Serializable {
    private static final long serialVersionUID = 2L;
    public static final String FORMAT_NAME_JSON = "JSON";
    protected static final int DEFAULT_FACTORY_FEATURE_FLAGS = Feature.collectDefaults();
    protected static final int DEFAULT_PARSER_FEATURE_FLAGS = JsonParser.Feature.collectDefaults();
    protected static final int DEFAULT_GENERATOR_FEATURE_FLAGS = JsonGenerator.Feature.collectDefaults();
    public static final SerializableString DEFAULT_ROOT_VALUE_SEPARATOR = new SerializedString(" ");
    public static final char DEFAULT_QUOTE_CHAR = '\"';
    protected final transient CharsToNameCanonicalizer _rootCharSymbols = CharsToNameCanonicalizer.createRoot();
    protected final transient ByteQuadsCanonicalizer _byteSymbolCanonicalizer = ByteQuadsCanonicalizer.createRoot();
    protected int _factoryFeatures = DEFAULT_FACTORY_FEATURE_FLAGS;
    protected int _parserFeatures = DEFAULT_PARSER_FEATURE_FLAGS;
    protected int _generatorFeatures = DEFAULT_GENERATOR_FEATURE_FLAGS;
    protected ObjectCodec _objectCodec;
    protected CharacterEscapes _characterEscapes;
    protected InputDecorator _inputDecorator;
    protected OutputDecorator _outputDecorator;
    protected SerializableString _rootValueSeparator = DEFAULT_ROOT_VALUE_SEPARATOR;
    protected int _maximumNonEscapedChar;
    protected final char _quoteChar;

    public JsonFactory() {
        this((ObjectCodec)null);
    }

    public JsonFactory(ObjectCodec oc) {
        this._objectCodec = oc;
        this._quoteChar = (char)34;
    }

    protected JsonFactory(JsonFactory src, ObjectCodec codec) {
        this._objectCodec = codec;
        this._factoryFeatures = src._factoryFeatures;
        this._parserFeatures = src._parserFeatures;
        this._generatorFeatures = src._generatorFeatures;
        this._inputDecorator = src._inputDecorator;
        this._outputDecorator = src._outputDecorator;
        this._characterEscapes = src._characterEscapes;
        this._rootValueSeparator = src._rootValueSeparator;
        this._maximumNonEscapedChar = src._maximumNonEscapedChar;
        this._quoteChar = src._quoteChar;
    }

    public JsonFactory(JsonFactoryBuilder b) {
        this._objectCodec = null;
        this._factoryFeatures = b._factoryFeatures;
        this._parserFeatures = b._streamReadFeatures;
        this._generatorFeatures = b._streamWriteFeatures;
        this._inputDecorator = b._inputDecorator;
        this._outputDecorator = b._outputDecorator;
        this._characterEscapes = b._characterEscapes;
        this._rootValueSeparator = b._rootValueSeparator;
        this._maximumNonEscapedChar = b._maximumNonEscapedChar;
        this._quoteChar = b._quoteChar;
    }

    protected JsonFactory(TSFBuilder<?, ?> b, boolean bogus) {
        this._objectCodec = null;
        this._factoryFeatures = b._factoryFeatures;
        this._parserFeatures = b._streamReadFeatures;
        this._generatorFeatures = b._streamWriteFeatures;
        this._inputDecorator = b._inputDecorator;
        this._outputDecorator = b._outputDecorator;
        this._characterEscapes = null;
        this._rootValueSeparator = null;
        this._maximumNonEscapedChar = 0;
        this._quoteChar = (char)34;
    }

    public static TSFBuilder<?, ?> builder() {
        return new JsonFactoryBuilder();
    }

    public JsonFactory copy() {
        this._checkInvalidCopy(JsonFactory.class);
        return new JsonFactory(this, null);
    }

    protected void _checkInvalidCopy(Class<?> exp) {
        if (this.getClass() != exp) {
            throw new IllegalStateException("Failed copy(): " + this.getClass().getName() + " (version: " + this.version() + ") does not override copy(); it has to");
        }
    }

    protected Object readResolve() {
        return new JsonFactory(this, this._objectCodec);
    }

    @Override
    public boolean canHandleBinaryNatively() {
        return false;
    }

    public boolean canUseCharArrays() {
        return true;
    }

    @Override
    public String getFormatName() {
        if (this.getClass() == JsonFactory.class) {
            return FORMAT_NAME_JSON;
        }
        return null;
    }

    @Override
    public Version version() {
        return PackageVersion.VERSION;
    }

    @Deprecated
    public final JsonFactory configure(Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    @Deprecated
    public JsonFactory enable(Feature f) {
        this._factoryFeatures |= f.getMask();
        return this;
    }

    @Deprecated
    public JsonFactory disable(Feature f) {
        this._factoryFeatures &= ~f.getMask();
        return this;
    }

    public final boolean isEnabled(Feature f) {
        return (this._factoryFeatures & f.getMask()) != 0;
    }

    public final JsonFactory configure(JsonParser.Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public JsonFactory enable(JsonParser.Feature f) {
        this._parserFeatures |= f.getMask();
        return this;
    }

    public JsonFactory disable(JsonParser.Feature f) {
        this._parserFeatures &= ~f.getMask();
        return this;
    }

    @Override
    public final boolean isEnabled(JsonParser.Feature f) {
        return (this._parserFeatures & f.getMask()) != 0;
    }

    public final boolean isEnabled(StreamReadFeature f) {
        return (this._parserFeatures & f.mappedFeature().getMask()) != 0;
    }

    @Deprecated
    public JsonFactory setInputDecorator(InputDecorator d) {
        this._inputDecorator = d;
        return this;
    }

    public final JsonFactory configure(JsonGenerator.Feature f, boolean state) {
        return state ? this.enable(f) : this.disable(f);
    }

    public JsonFactory enable(JsonGenerator.Feature f) {
        this._generatorFeatures |= f.getMask();
        return this;
    }

    public JsonFactory disable(JsonGenerator.Feature f) {
        this._generatorFeatures &= ~f.getMask();
        return this;
    }

    @Override
    public final boolean isEnabled(JsonGenerator.Feature f) {
        return (this._generatorFeatures & f.getMask()) != 0;
    }

    public final boolean isEnabled(StreamWriteFeature f) {
        return (this._generatorFeatures & f.mappedFeature().getMask()) != 0;
    }

    @Deprecated
    public JsonFactory setOutputDecorator(OutputDecorator d) {
        this._outputDecorator = d;
        return this;
    }

    public JsonFactory setCodec(ObjectCodec oc) {
        this._objectCodec = oc;
        return this;
    }

    public ObjectCodec getCodec() {
        return this._objectCodec;
    }

    @Override
    public JsonParser createParser(File f) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(f), true);
        FileInputStream in = new FileInputStream(f);
        return this._createParser(this._decorate(in, ctxt), ctxt);
    }

    @Override
    public JsonParser createParser(URL url) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(url), true);
        InputStream in = this._optimizedStreamFromURL(url);
        return this._createParser(this._decorate(in, ctxt), ctxt);
    }

    @Override
    public JsonParser createParser(InputStream in) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(in), false);
        return this._createParser(this._decorate(in, ctxt), ctxt);
    }

    @Override
    public JsonParser createParser(Reader r) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(r), false);
        return this._createParser(this._decorate(r, ctxt), ctxt);
    }

    @Override
    public JsonParser createParser(byte[] data) throws IOException {
        InputStream in;
        IOContext ctxt = this._createContext(this._createContentReference(data), true);
        if (this._inputDecorator != null && (in = this._inputDecorator.decorate(ctxt, data, 0, data.length)) != null) {
            return this._createParser(in, ctxt);
        }
        return this._createParser(data, 0, data.length, ctxt);
    }

    @Override
    public JsonParser createParser(byte[] data, int offset, int len) throws IOException {
        InputStream in;
        IOContext ctxt = this._createContext(this._createContentReference(data, offset, len), true);
        if (this._inputDecorator != null && (in = this._inputDecorator.decorate(ctxt, data, offset, len)) != null) {
            return this._createParser(in, ctxt);
        }
        return this._createParser(data, offset, len, ctxt);
    }

    @Override
    public JsonParser createParser(String content) throws IOException {
        int strLen = content.length();
        if (this._inputDecorator != null || strLen > 32768 || !this.canUseCharArrays()) {
            return this.createParser(new StringReader(content));
        }
        IOContext ctxt = this._createContext(this._createContentReference(content), true);
        char[] buf = ctxt.allocTokenBuffer(strLen);
        content.getChars(0, strLen, buf, 0);
        return this._createParser(buf, 0, strLen, ctxt, true);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(out), false);
        ctxt.setEncoding(enc);
        if (enc == JsonEncoding.UTF8) {
            return this._createUTF8Generator(this._decorate(out, ctxt), ctxt);
        }
        Writer w = this._createWriter(out, enc, ctxt);
        return this._createGenerator(this._decorate(w, ctxt), ctxt);
    }

    @Override
    public JsonGenerator createGenerator(OutputStream out) throws IOException {
        return this.createGenerator(out, JsonEncoding.UTF8);
    }

    @Override
    public JsonGenerator createGenerator(Writer w) throws IOException {
        IOContext ctxt = this._createContext(this._createContentReference(w), false);
        return this._createGenerator(this._decorate(w, ctxt), ctxt);
    }

    @Deprecated
    public JsonParser createJsonParser(File f) throws IOException {
        return this.createParser(f);
    }

    @Deprecated
    public JsonParser createJsonParser(URL url) throws IOException {
        return this.createParser(url);
    }

    @Deprecated
    public JsonParser createJsonParser(InputStream in) throws IOException {
        return this.createParser(in);
    }

    @Deprecated
    public JsonParser createJsonParser(Reader r) throws IOException {
        return this.createParser(r);
    }

    @Deprecated
    public JsonParser createJsonParser(byte[] data) throws IOException {
        return this.createParser(data);
    }

    @Deprecated
    public JsonParser createJsonParser(byte[] data, int offset, int len) throws IOException {
        return this.createParser(data, offset, len);
    }

    @Deprecated
    public JsonParser createJsonParser(String content) throws IOException {
        return this.createParser(content);
    }

    @Deprecated
    public JsonGenerator createJsonGenerator(OutputStream out, JsonEncoding enc) throws IOException {
        return this.createGenerator(out, enc);
    }

    @Deprecated
    public JsonGenerator createJsonGenerator(Writer out) throws IOException {
        return this.createGenerator(out);
    }

    @Deprecated
    public JsonGenerator createJsonGenerator(OutputStream out) throws IOException {
        return this.createGenerator(out, JsonEncoding.UTF8);
    }

    protected JsonParser _createParser(InputStream in, IOContext ctxt) throws IOException {
        return new ByteSourceJsonBootstrapper(ctxt, in).constructParser(this._parserFeatures, this._objectCodec, this._byteSymbolCanonicalizer, this._rootCharSymbols, this._factoryFeatures);
    }

    protected JsonParser _createParser(Reader r, IOContext ctxt) {
        return new ReaderBasedJsonParser(ctxt, this._parserFeatures, r, this._objectCodec, this._rootCharSymbols.makeChild(this._factoryFeatures));
    }

    protected JsonParser _createParser(char[] data, int offset, int len, IOContext ctxt, boolean recyclable) {
        return new ReaderBasedJsonParser(ctxt, this._parserFeatures, null, this._objectCodec, this._rootCharSymbols.makeChild(this._factoryFeatures), data, offset, offset + len, recyclable);
    }

    protected JsonParser _createParser(byte[] data, int offset, int len, IOContext ctxt) throws IOException {
        return new ByteSourceJsonBootstrapper(ctxt, data, offset, len).constructParser(this._parserFeatures, this._objectCodec, this._byteSymbolCanonicalizer, this._rootCharSymbols, this._factoryFeatures);
    }

    protected JsonGenerator _createGenerator(Writer out, IOContext ctxt) {
        SerializableString rootSep;
        WriterBasedJsonGenerator gen = new WriterBasedJsonGenerator(ctxt, this._generatorFeatures, this._objectCodec, out, this._quoteChar);
        if (this._maximumNonEscapedChar > 0) {
            gen.setHighestNonEscapedChar(this._maximumNonEscapedChar);
        }
        if (this._characterEscapes != null) {
            gen.setCharacterEscapes(this._characterEscapes);
        }
        if ((rootSep = this._rootValueSeparator) != DEFAULT_ROOT_VALUE_SEPARATOR) {
            gen.setRootValueSeparator(rootSep);
        }
        return gen;
    }

    protected JsonGenerator _createUTF8Generator(OutputStream out, IOContext ctxt) {
        SerializableString rootSep;
        UTF8JsonGenerator gen = new UTF8JsonGenerator(ctxt, this._generatorFeatures, this._objectCodec, out, this._quoteChar);
        if (this._maximumNonEscapedChar > 0) {
            gen.setHighestNonEscapedChar(this._maximumNonEscapedChar);
        }
        if (this._characterEscapes != null) {
            gen.setCharacterEscapes(this._characterEscapes);
        }
        if ((rootSep = this._rootValueSeparator) != DEFAULT_ROOT_VALUE_SEPARATOR) {
            gen.setRootValueSeparator(rootSep);
        }
        return gen;
    }

    protected Writer _createWriter(OutputStream out, JsonEncoding enc, IOContext ctxt) throws IOException {
        if (enc == JsonEncoding.UTF8) {
            return new UTF8Writer(ctxt, out);
        }
        return new OutputStreamWriter(out, enc.getJavaName());
    }

    protected final InputStream _decorate(InputStream in, IOContext ctxt) throws IOException {
        InputStream in2;
        if (this._inputDecorator != null && (in2 = this._inputDecorator.decorate(ctxt, in)) != null) {
            return in2;
        }
        return in;
    }

    protected final Reader _decorate(Reader in, IOContext ctxt) throws IOException {
        Reader in2;
        if (this._inputDecorator != null && (in2 = this._inputDecorator.decorate(ctxt, in)) != null) {
            return in2;
        }
        return in;
    }

    protected final OutputStream _decorate(OutputStream out, IOContext ctxt) throws IOException {
        OutputStream out2;
        if (this._outputDecorator != null && (out2 = this._outputDecorator.decorate(ctxt, out)) != null) {
            return out2;
        }
        return out;
    }

    protected final Writer _decorate(Writer out, IOContext ctxt) throws IOException {
        Writer out2;
        if (this._outputDecorator != null && (out2 = this._outputDecorator.decorate(ctxt, out)) != null) {
            return out2;
        }
        return out;
    }

    public BufferRecycler _getBufferRecycler() {
        if (Feature.USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING.enabledIn(this._factoryFeatures)) {
            return BufferRecyclers.getBufferRecycler();
        }
        return new BufferRecycler();
    }

    protected IOContext _createContext(ContentReference contentRef, boolean resourceManaged) {
        if (contentRef == null) {
            contentRef = ContentReference.unknown();
        }
        return new IOContext(this._getBufferRecycler(), contentRef, resourceManaged);
    }

    @Deprecated
    protected IOContext _createContext(Object rawContentRef, boolean resourceManaged) {
        return new IOContext(this._getBufferRecycler(), this._createContentReference(rawContentRef), resourceManaged);
    }

    protected ContentReference _createContentReference(Object contentAccessor) {
        return ContentReference.construct(!this.canHandleBinaryNatively(), contentAccessor);
    }

    protected ContentReference _createContentReference(Object contentAccessor, int offset, int length) {
        return ContentReference.construct(!this.canHandleBinaryNatively(), contentAccessor, offset, length);
    }

    public static enum Feature implements JacksonFeature
    {
        INTERN_FIELD_NAMES(true),
        CANONICALIZE_FIELD_NAMES(true),
        FAIL_ON_SYMBOL_HASH_OVERFLOW(true),
        USE_THREAD_LOCAL_FOR_BUFFER_RECYCLING(true);

        private final boolean _defaultState;

        public static int collectDefaults() {
            int flags = 0;
            for (Feature f : Feature.values()) {
                if (!f.enabledByDefault()) continue;
                flags |= f.getMask();
            }
            return flags;
        }

        private Feature(boolean defaultState) {
            this._defaultState = defaultState;
        }

        @Override
        public boolean enabledByDefault() {
            return this._defaultState;
        }

        @Override
        public boolean enabledIn(int flags) {
            return (flags & this.getMask()) != 0;
        }

        @Override
        public int getMask() {
            return 1 << this.ordinal();
        }
    }
}

