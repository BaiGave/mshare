/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.impl;

import java.io.Serializable;
import java.util.Objects;
import org.apache.logging.log4j.core.impl.ExtendedClassInfo;
import org.apache.logging.log4j.core.pattern.PlainTextRenderer;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.util.Strings;

/*
 * Multiple versions of this class in jar - see https://www.benf.org/other/cfr/multi-version-jar.html
 */
public final class ExtendedStackTraceElement
implements Serializable {
    static final ExtendedStackTraceElement[] EMPTY_ARRAY = new ExtendedStackTraceElement[0];
    private static final long serialVersionUID = -2171069569241280505L;
    private final ExtendedClassInfo extraClassInfo;
    private final StackTraceElement stackTraceElement;

    public ExtendedStackTraceElement(StackTraceElement stackTraceElement, ExtendedClassInfo extraClassInfo) {
        this.stackTraceElement = stackTraceElement;
        this.extraClassInfo = extraClassInfo;
    }

    public ExtendedStackTraceElement(String classLoaderName, String moduleName, String moduleVersion, String declaringClass, String methodName, String fileName, int lineNumber, boolean exact, String location, String version) {
        this(new StackTraceElement(classLoaderName, moduleName, moduleVersion, declaringClass, methodName, fileName, lineNumber), new ExtendedClassInfo(exact, location, version));
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof ExtendedStackTraceElement)) {
            return false;
        }
        ExtendedStackTraceElement other = (ExtendedStackTraceElement)obj;
        if (!Objects.equals(this.extraClassInfo, other.extraClassInfo)) {
            return false;
        }
        return Objects.equals(this.stackTraceElement, other.stackTraceElement);
    }

    public String getClassLoaderName() {
        return this.stackTraceElement.getClassLoaderName();
    }

    public String getModuleName() {
        return this.stackTraceElement.getModuleName();
    }

    public String getModuleVersion() {
        return this.stackTraceElement.getModuleVersion();
    }

    public String getClassName() {
        return this.stackTraceElement.getClassName();
    }

    public boolean getExact() {
        return this.extraClassInfo.getExact();
    }

    public ExtendedClassInfo getExtraClassInfo() {
        return this.extraClassInfo;
    }

    public String getFileName() {
        return this.stackTraceElement.getFileName();
    }

    public int getLineNumber() {
        return this.stackTraceElement.getLineNumber();
    }

    public String getLocation() {
        return this.extraClassInfo.getLocation();
    }

    public String getMethodName() {
        return this.stackTraceElement.getMethodName();
    }

    public StackTraceElement getStackTraceElement() {
        return this.stackTraceElement;
    }

    public String getVersion() {
        return this.extraClassInfo.getVersion();
    }

    public int hashCode() {
        return Objects.hash(this.extraClassInfo, this.stackTraceElement);
    }

    public boolean isNativeMethod() {
        return this.stackTraceElement.isNativeMethod();
    }

    void renderOn(StringBuilder output, TextRenderer textRenderer) {
        this.render(this.stackTraceElement, output, textRenderer);
        textRenderer.render(" ", output, "Text");
        this.extraClassInfo.renderOn(output, textRenderer);
    }

    private void render(StackTraceElement stElement, StringBuilder output, TextRenderer textRenderer) {
        String classLoaderName = this.getClassLoaderName();
        if (Strings.isNotEmpty(classLoaderName)) {
            switch (classLoaderName) {
                case "app": 
                case "boot": 
                case "platform": {
                    break;
                }
                default: {
                    textRenderer.render(classLoaderName, output, "StackTraceElement.ClassLoaderName");
                    textRenderer.render("/", output, "StackTraceElement.ClassLoaderSeparator");
                }
            }
        }
        String fileName = stElement.getFileName();
        int lineNumber = stElement.getLineNumber();
        String moduleName = this.getModuleName();
        String moduleVersion = this.getModuleVersion();
        if (Strings.isNotEmpty(moduleName)) {
            textRenderer.render(moduleName, output, "StackTraceElement.ModuleName");
            if (Strings.isNotEmpty(moduleVersion) && !moduleName.startsWith("java")) {
                textRenderer.render("@", output, "StackTraceElement.ModuleVersionSeparator");
                textRenderer.render(moduleVersion, output, "StackTraceElement.ModuleVersion");
            }
            textRenderer.render("/", output, "StackTraceElement.ModuleNameSeparator");
        }
        textRenderer.render(this.getClassName(), output, "StackTraceElement.ClassName");
        textRenderer.render(".", output, "StackTraceElement.ClassMethodSeparator");
        textRenderer.render(stElement.getMethodName(), output, "StackTraceElement.MethodName");
        if (stElement.isNativeMethod()) {
            textRenderer.render("(Native Method)", output, "StackTraceElement.NativeMethod");
        } else if (fileName != null && lineNumber >= 0) {
            textRenderer.render("(", output, "StackTraceElement.Container");
            textRenderer.render(fileName, output, "StackTraceElement.FileName");
            textRenderer.render(":", output, "StackTraceElement.ContainerSeparator");
            textRenderer.render(Integer.toString(lineNumber), output, "StackTraceElement.LineNumber");
            textRenderer.render(")", output, "StackTraceElement.Container");
        } else if (fileName != null) {
            textRenderer.render("(", output, "StackTraceElement.Container");
            textRenderer.render(fileName, output, "StackTraceElement.FileName");
            textRenderer.render(")", output, "StackTraceElement.Container");
        } else {
            textRenderer.render("(", output, "StackTraceElement.Container");
            textRenderer.render("Unknown Source", output, "StackTraceElement.UnknownSource");
            textRenderer.render(")", output, "StackTraceElement.Container");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        this.renderOn(sb, PlainTextRenderer.getInstance());
        return sb.toString();
    }
}

