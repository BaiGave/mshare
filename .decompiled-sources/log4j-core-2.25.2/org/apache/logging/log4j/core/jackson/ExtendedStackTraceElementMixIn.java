/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.jackson;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import java.io.Serializable;
import org.apache.logging.log4j.core.impl.ExtendedClassInfo;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
@JsonPropertyOrder(value={"class", "method", "file", "line", "exact", "location", "version"})
@Deprecated
abstract class ExtendedStackTraceElementMixIn
implements Serializable {
    protected static final String ATTR_CLASS_LOADER_NAME = "classLoaderName";
    protected static final String ATTR_MODULE = "module";
    protected static final String ATTR_MODULE_VERSION = "moduleVersion";
    protected static final String ATTR_CLASS = "class";
    protected static final String ATTR_METHOD = "method";
    protected static final String ATTR_FILE = "file";
    protected static final String ATTR_LINE = "line";
    protected static final String ATTR_EXACT = "exact";
    protected static final String ATTR_LOCATION = "location";
    protected static final String ATTR_VERSION = "version";
    private static final long serialVersionUID = 1L;

    @JsonCreator
    public ExtendedStackTraceElementMixIn(@JsonProperty(value="class") String declaringClass, @JsonProperty(value="method") String methodName, @JsonProperty(value="file") String fileName, @JsonProperty(value="line") int lineNumber, @JsonProperty(value="exact") boolean exact, @JsonProperty(value="location") String location, @JsonProperty(value="version") String version) {
    }

    @JsonProperty(value="class")
    @JacksonXmlProperty(localName="class", isAttribute=true)
    public abstract String getClassName();

    @JsonProperty(value="exact")
    @JacksonXmlProperty(localName="exact", isAttribute=true)
    public abstract boolean getExact();

    @JsonIgnore
    public abstract ExtendedClassInfo getExtraClassInfo();

    @JsonProperty(value="file")
    @JacksonXmlProperty(localName="file", isAttribute=true)
    public abstract String getFileName();

    @JsonProperty(value="line")
    @JacksonXmlProperty(localName="line", isAttribute=true)
    public abstract int getLineNumber();

    @JsonProperty(value="location")
    @JacksonXmlProperty(localName="location", isAttribute=true)
    public abstract String getLocation();

    @JsonProperty(value="method")
    @JacksonXmlProperty(localName="method", isAttribute=true)
    public abstract String getMethodName();

    @JsonIgnore
    abstract StackTraceElement getStackTraceElement();

    @JsonProperty(value="version")
    @JacksonXmlProperty(localName="version", isAttribute=true)
    public abstract String getVersion();

    @JsonIgnore
    public abstract boolean isNativeMethod();
}

