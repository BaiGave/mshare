/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.compress.compressors.gzip;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public final class ExtraField
implements Iterable<SubField> {
    private static final int MAX_SIZE = 65535;
    private static final byte[] ZERO_BYTES = new byte[0];
    private final List<SubField> subFields = new ArrayList<SubField>();
    private int totalSize;

    static ExtraField fromBytes(byte[] bytes) throws IOException {
        if (bytes == null) {
            return null;
        }
        ExtraField extra = new ExtraField();
        int pos = 0;
        while (pos <= bytes.length - 4) {
            int sublen;
            byte si1 = bytes[pos++];
            byte si2 = bytes[pos++];
            if ((sublen = bytes[pos++] & 0xFF | (bytes[pos++] & 0xFF) << 8) > bytes.length - pos) {
                throw new IOException("Extra subfield lenght exceeds remaining bytes in extra: " + sublen + " > " + (bytes.length - pos));
            }
            byte[] payload = new byte[sublen];
            System.arraycopy(bytes, pos, payload, 0, sublen);
            extra.subFields.add(new SubField(si1, si2, payload));
            extra.totalSize = pos += sublen;
        }
        if (pos < bytes.length) {
            throw new IOException("" + (bytes.length - pos) + " remaining bytes not used to parse an extra subfield.");
        }
        return extra;
    }

    public ExtraField addSubField(String id, byte[] payload) throws IOException {
        Objects.requireNonNull(id, "payload");
        Objects.requireNonNull(payload, "payload");
        if (id.length() != 2) {
            throw new IllegalArgumentException("Subfield id must be a 2 character ISO-8859-1 string.");
        }
        char si1 = id.charAt(0);
        char si2 = id.charAt(1);
        if ((si1 & 0xFF00) != 0 || (si2 & 0xFF00) != 0) {
            throw new IllegalArgumentException("Subfield id must be a 2 character ISO-8859-1 string.");
        }
        SubField f = new SubField((byte)(si1 & 0xFF), (byte)(si2 & 0xFF), payload);
        int len = 4 + payload.length;
        if (this.totalSize + len > 65535) {
            throw new IOException("Extra subfield '" + f.getId() + "' too big (extras total size is already at " + this.totalSize + ")");
        }
        this.subFields.add(f);
        this.totalSize += len;
        return this;
    }

    public void clear() {
        this.subFields.clear();
        this.totalSize = 0;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (this.getClass() != obj.getClass()) {
            return false;
        }
        ExtraField other = (ExtraField)obj;
        return Objects.equals(this.subFields, other.subFields) && this.totalSize == other.totalSize;
    }

    public SubField findFirstSubField(String id) {
        return this.subFields.stream().filter(f -> f.getId().equals(id)).findFirst().orElse(null);
    }

    public int getEncodedSize() {
        return this.totalSize;
    }

    public SubField getSubField(int index) {
        return this.subFields.get(index);
    }

    public int hashCode() {
        return Objects.hash(this.subFields, this.totalSize);
    }

    public boolean isEmpty() {
        return this.subFields.isEmpty();
    }

    @Override
    public Iterator<SubField> iterator() {
        return Collections.unmodifiableList(this.subFields).iterator();
    }

    public int size() {
        return this.subFields.size();
    }

    byte[] toByteArray() {
        if (this.subFields.isEmpty()) {
            return ZERO_BYTES;
        }
        byte[] ba = new byte[this.totalSize];
        int pos = 0;
        for (SubField f : this.subFields) {
            ba[pos++] = f.si1;
            ba[pos++] = f.si2;
            ba[pos++] = (byte)(f.payload.length & 0xFF);
            ba[pos++] = (byte)(f.payload.length >>> 8);
            System.arraycopy(f.payload, 0, ba, pos, f.payload.length);
            pos += f.payload.length;
        }
        return ba;
    }

    public static final class SubField {
        private final byte si1;
        private final byte si2;
        private final byte[] payload;

        SubField(byte si1, byte si2, byte[] payload) {
            this.si1 = si1;
            this.si2 = si2;
            this.payload = payload;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (this.getClass() != obj.getClass()) {
                return false;
            }
            SubField other = (SubField)obj;
            return Arrays.equals(this.payload, other.payload) && this.si1 == other.si1 && this.si2 == other.si2;
        }

        public String getId() {
            return String.valueOf(new char[]{(char)(this.si1 & 0xFF), (char)(this.si2 & 0xFF)});
        }

        public byte[] getPayload() {
            return this.payload;
        }

        public int hashCode() {
            int prime = 31;
            int result = 1;
            result = 31 * result + Arrays.hashCode(this.payload);
            result = 31 * result + Objects.hash(this.si1, this.si2);
            return result;
        }
    }
}

