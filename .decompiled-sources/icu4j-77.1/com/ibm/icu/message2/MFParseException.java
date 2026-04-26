/*
 * Decompiled with CFR 0.152.
 */
package com.ibm.icu.message2;

import java.text.ParseException;

@Deprecated
public class MFParseException
extends ParseException {
    private static final long serialVersionUID = -7634219305388292407L;

    public MFParseException(String message, int errorOffset) {
        super(message, errorOffset);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}

