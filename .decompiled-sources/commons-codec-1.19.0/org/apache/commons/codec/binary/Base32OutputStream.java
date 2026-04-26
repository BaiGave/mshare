/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.codec.binary;

import java.io.OutputStream;
import org.apache.commons.codec.CodecPolicy;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.codec.binary.BaseNCodecOutputStream;

public class Base32OutputStream
extends BaseNCodecOutputStream {
    public Base32OutputStream(OutputStream outputStream) {
        this(outputStream, true);
    }

    public Base32OutputStream(OutputStream outputStream, boolean doEncode) {
        super(outputStream, new Base32(false), doEncode);
    }

    public Base32OutputStream(OutputStream outputStream, boolean doEncode, int lineLength, byte[] lineSeparator) {
        super(outputStream, new Base32(lineLength, lineSeparator), doEncode);
    }

    public Base32OutputStream(OutputStream outputStream, boolean doEncode, int lineLength, byte[] lineSeparator, CodecPolicy decodingPolicy) {
        super(outputStream, new Base32(lineLength, lineSeparator, false, 61, decodingPolicy), doEncode);
    }
}

