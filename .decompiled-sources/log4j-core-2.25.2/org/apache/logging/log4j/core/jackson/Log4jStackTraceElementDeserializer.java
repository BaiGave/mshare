/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import java.io.IOException;
import org.apache.logging.log4j.core.util.Integers;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class Log4jStackTraceElementDeserializer
extends StdScalarDeserializer<StackTraceElement> {
    private static final long serialVersionUID = 1L;

    public Log4jStackTraceElementDeserializer() {
        super(StackTraceElement.class);
    }

    public StackTraceElement deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
        JsonToken t = jp.getCurrentToken();
        if (t == JsonToken.START_OBJECT) {
            String className = null;
            String methodName = null;
            String fileName = null;
            int lineNumber = -1;
            block15: while ((t = jp.nextValue()) != JsonToken.END_OBJECT) {
                String propName;
                switch (propName = jp.getCurrentName()) {
                    case "class": {
                        className = jp.getText();
                        continue block15;
                    }
                    case "file": {
                        fileName = jp.getText();
                        continue block15;
                    }
                    case "line": {
                        if (t.isNumeric()) {
                            lineNumber = jp.getIntValue();
                            continue block15;
                        }
                        try {
                            lineNumber = Integers.parseInt(jp.getText());
                            continue block15;
                        }
                        catch (NumberFormatException e) {
                            throw JsonMappingException.from((JsonParser)jp, (String)("Non-numeric token (" + t + ") for property 'line'"), (Throwable)e);
                        }
                    }
                    case "method": {
                        methodName = jp.getText();
                        continue block15;
                    }
                }
                this.handleUnknownProperty(jp, ctxt, this._valueClass, propName);
            }
            return new StackTraceElement(className, methodName, fileName, lineNumber);
        }
        throw JsonMappingException.from((JsonParser)jp, (String)String.format("Cannot deserialize instance of %s out of %s token", ClassUtil.nameOf((Class)this._valueClass), t));
    }
}

