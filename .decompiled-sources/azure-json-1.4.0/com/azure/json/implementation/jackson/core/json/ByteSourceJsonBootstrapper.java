/*
 * Decompiled with CFR 0.152.
 */
package com.azure.json.implementation.jackson.core.json;

import com.azure.json.implementation.jackson.core.JsonEncoding;
import com.azure.json.implementation.jackson.core.JsonFactory;
import com.azure.json.implementation.jackson.core.JsonParser;
import com.azure.json.implementation.jackson.core.ObjectCodec;
import com.azure.json.implementation.jackson.core.io.IOContext;
import com.azure.json.implementation.jackson.core.io.MergedStream;
import com.azure.json.implementation.jackson.core.io.UTF32Reader;
import com.azure.json.implementation.jackson.core.json.ReaderBasedJsonParser;
import com.azure.json.implementation.jackson.core.json.UTF8StreamJsonParser;
import com.azure.json.implementation.jackson.core.sym.ByteQuadsCanonicalizer;
import com.azure.json.implementation.jackson.core.sym.CharsToNameCanonicalizer;
import java.io.ByteArrayInputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public final class ByteSourceJsonBootstrapper {
    private final IOContext _context;
    private final InputStream _in;
    private final byte[] _inputBuffer;
    private int _inputPtr;
    private int _inputEnd;
    private final boolean _bufferRecyclable;
    private boolean _bigEndian = true;
    private int _bytesPerChar;

    public ByteSourceJsonBootstrapper(IOContext ctxt, InputStream in) {
        this._context = ctxt;
        this._in = in;
        this._inputBuffer = ctxt.allocReadIOBuffer();
        this._inputPtr = 0;
        this._inputEnd = 0;
        this._bufferRecyclable = true;
    }

    public ByteSourceJsonBootstrapper(IOContext ctxt, byte[] inputBuffer, int inputStart, int inputLen) {
        this._context = ctxt;
        this._in = null;
        this._inputBuffer = inputBuffer;
        this._inputPtr = inputStart;
        this._inputEnd = inputStart + inputLen;
        this._bufferRecyclable = false;
    }

    public JsonEncoding detectEncoding() throws IOException {
        JsonEncoding enc;
        int i16;
        boolean foundEncoding = false;
        if (this.ensureLoaded(4)) {
            int quad = this._inputBuffer[this._inputPtr] << 24 | (this._inputBuffer[this._inputPtr + 1] & 0xFF) << 16 | (this._inputBuffer[this._inputPtr + 2] & 0xFF) << 8 | this._inputBuffer[this._inputPtr + 3] & 0xFF;
            if (this.handleBOM(quad)) {
                foundEncoding = true;
            } else if (this.checkUTF32(quad)) {
                foundEncoding = true;
            } else if (this.checkUTF16(quad >>> 16)) {
                foundEncoding = true;
            }
        } else if (this.ensureLoaded(2) && this.checkUTF16(i16 = (this._inputBuffer[this._inputPtr] & 0xFF) << 8 | this._inputBuffer[this._inputPtr + 1] & 0xFF)) {
            foundEncoding = true;
        }
        if (!foundEncoding) {
            enc = JsonEncoding.UTF8;
        } else {
            switch (this._bytesPerChar) {
                case 1: {
                    enc = JsonEncoding.UTF8;
                    break;
                }
                case 2: {
                    enc = this._bigEndian ? JsonEncoding.UTF16_BE : JsonEncoding.UTF16_LE;
                    break;
                }
                case 4: {
                    enc = this._bigEndian ? JsonEncoding.UTF32_BE : JsonEncoding.UTF32_LE;
                    break;
                }
                default: {
                    throw new RuntimeException("Internal error");
                }
            }
        }
        this._context.setEncoding(enc);
        return enc;
    }

    public Reader constructReader() throws IOException {
        JsonEncoding enc = this._context.getEncoding();
        switch (enc.bits()) {
            case 8: 
            case 16: {
                InputStream in = this._in;
                if (in == null) {
                    in = new ByteArrayInputStream(this._inputBuffer, this._inputPtr, this._inputEnd);
                } else if (this._inputPtr < this._inputEnd) {
                    in = new MergedStream(this._context, in, this._inputBuffer, this._inputPtr, this._inputEnd);
                }
                return new InputStreamReader(in, enc.getJavaName());
            }
            case 32: {
                return new UTF32Reader(this._context, this._in, this._inputBuffer, this._inputPtr, this._inputEnd, this._context.getEncoding().isBigEndian());
            }
        }
        throw new RuntimeException("Internal error");
    }

    public JsonParser constructParser(int parserFeatures, ObjectCodec codec, ByteQuadsCanonicalizer rootByteSymbols, CharsToNameCanonicalizer rootCharSymbols, int factoryFeatures) throws IOException {
        int prevInputPtr = this._inputPtr;
        JsonEncoding enc = this.detectEncoding();
        int bytesProcessed = this._inputPtr - prevInputPtr;
        if (enc == JsonEncoding.UTF8 && JsonFactory.Feature.CANONICALIZE_FIELD_NAMES.enabledIn(factoryFeatures)) {
            ByteQuadsCanonicalizer can = rootByteSymbols.makeChild(factoryFeatures);
            return new UTF8StreamJsonParser(this._context, parserFeatures, this._in, codec, can, this._inputBuffer, this._inputPtr, this._inputEnd, bytesProcessed, this._bufferRecyclable);
        }
        return new ReaderBasedJsonParser(this._context, parserFeatures, this.constructReader(), codec, rootCharSymbols.makeChild(factoryFeatures));
    }

    private boolean handleBOM(int quad) throws IOException {
        switch (quad) {
            case 65279: {
                this._bigEndian = true;
                this._inputPtr += 4;
                this._bytesPerChar = 4;
                return true;
            }
            case -131072: {
                this._inputPtr += 4;
                this._bytesPerChar = 4;
                this._bigEndian = false;
                return true;
            }
            case 65534: {
                this.reportWeirdUCS4("2143");
                break;
            }
            case -16842752: {
                this.reportWeirdUCS4("3412");
                break;
            }
        }
        int msw = quad >>> 16;
        if (msw == 65279) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = true;
            return true;
        }
        if (msw == 65534) {
            this._inputPtr += 2;
            this._bytesPerChar = 2;
            this._bigEndian = false;
            return true;
        }
        if (quad >>> 8 == 0xEFBBBF) {
            this._inputPtr += 3;
            this._bytesPerChar = 1;
            this._bigEndian = true;
            return true;
        }
        return false;
    }

    private boolean checkUTF32(int quad) throws IOException {
        if (quad >> 8 == 0) {
            this._bigEndian = true;
        } else if ((quad & 0xFFFFFF) == 0) {
            this._bigEndian = false;
        } else if ((quad & 0xFF00FFFF) == 0) {
            this.reportWeirdUCS4("3412");
        } else if ((quad & 0xFFFF00FF) == 0) {
            this.reportWeirdUCS4("2143");
        } else {
            return false;
        }
        this._bytesPerChar = 4;
        return true;
    }

    private boolean checkUTF16(int i16) {
        if ((i16 & 0xFF00) == 0) {
            this._bigEndian = true;
        } else if ((i16 & 0xFF) == 0) {
            this._bigEndian = false;
        } else {
            return false;
        }
        this._bytesPerChar = 2;
        return true;
    }

    private void reportWeirdUCS4(String type) throws IOException {
        throw new CharConversionException("Unsupported UCS-4 endianness (" + type + ") detected");
    }

    private boolean ensureLoaded(int minimum) throws IOException {
        int count;
        for (int gotten = this._inputEnd - this._inputPtr; gotten < minimum; gotten += count) {
            count = this._in == null ? -1 : this._in.read(this._inputBuffer, this._inputEnd, this._inputBuffer.length - this._inputEnd);
            if (count < 1) {
                return false;
            }
            this._inputEnd += count;
        }
        return true;
    }
}

