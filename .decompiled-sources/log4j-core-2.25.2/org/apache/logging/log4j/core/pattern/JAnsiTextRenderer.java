/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.pattern;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.pattern.AnsiEscape;
import org.apache.logging.log4j.core.pattern.TextRenderer;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Strings;

public final class JAnsiTextRenderer
implements TextRenderer {
    private static final Logger LOGGER = StatusLogger.getLogger();
    public static final Map<String, String> DefaultExceptionStyleMap;
    static final Map<String, String> DEFAULT_MESSAGE_STYLE_MAP;
    private static final Map<String, Map<String, String>> PREFEDINED_STYLE_MAPS;
    private static final String BEGIN_TOKEN = "@|";
    private static final String END_TOKEN = "|@";
    private static final int CSI_LENGTH = 2;
    private final String beginToken;
    private final int beginTokenLen;
    private final String endToken;
    private final int endTokenLen;
    private final Map<String, String> styleMap;

    private static Map.Entry<String, String> entry(String name, AnsiEscape ... codes) {
        StringBuilder sb = new StringBuilder(AnsiEscape.CSI.getCode());
        for (AnsiEscape code : codes) {
            sb.append(code.getCode());
        }
        return new AbstractMap.SimpleImmutableEntry<String, String>(name, sb.toString());
    }

    @SafeVarargs
    private static <V> Map<String, V> ofEntries(Map.Entry<String, V> ... entries) {
        HashMap<String, V> map = new HashMap<String, V>(entries.length);
        for (Map.Entry<String, V> entry : entries) {
            map.put(entry.getKey(), entry.getValue());
        }
        return Collections.unmodifiableMap(map);
    }

    public JAnsiTextRenderer(String[] formats, Map<String, String> defaultStyleMap) {
        if (formats.length > 1) {
            String stylesStr = formats[1];
            Map<String, String> map = AnsiEscape.createMap(stylesStr.split("\\s", -1), new String[]{"BeginToken", "EndToken", "Style"}, ",");
            this.beginToken = Objects.toString(map.remove("BeginToken"), BEGIN_TOKEN);
            this.endToken = Objects.toString(map.remove("EndToken"), END_TOKEN);
            String predefinedStyle = map.remove("Style");
            HashMap<String, String> styleMap = new HashMap<String, String>(map.size() + defaultStyleMap.size());
            defaultStyleMap.forEach((k, v) -> styleMap.put(Strings.toRootUpperCase(k), (String)v));
            if (predefinedStyle != null) {
                Map<String, String> predefinedMap = PREFEDINED_STYLE_MAPS.get(predefinedStyle);
                if (predefinedMap != null) {
                    map.putAll(predefinedMap);
                } else {
                    LOGGER.warn("Unknown predefined map name {}, pick one of {}", (Object)predefinedStyle, (Object)PREFEDINED_STYLE_MAPS.keySet());
                }
            }
            styleMap.putAll(map);
            this.styleMap = Collections.unmodifiableMap(styleMap);
        } else {
            this.beginToken = BEGIN_TOKEN;
            this.endToken = END_TOKEN;
            this.styleMap = Collections.unmodifiableMap(defaultStyleMap);
        }
        this.beginTokenLen = this.beginToken.length();
        this.endTokenLen = this.endToken.length();
    }

    private void render(String input, StringBuilder output, String ... styleNames) {
        boolean first = true;
        for (String styleName : styleNames) {
            String escape = this.styleMap.get(Strings.toRootUpperCase(styleName));
            if (escape != null) {
                JAnsiTextRenderer.merge(escape, output, first);
            } else {
                JAnsiTextRenderer.merge(AnsiEscape.createSequence(styleName), output, first);
            }
            first = false;
        }
        output.append(input).append(AnsiEscape.getDefaultStyle());
    }

    private static void merge(String escapeSequence, StringBuilder output, boolean first) {
        if (first) {
            output.append(escapeSequence);
        } else {
            output.setLength(output.length() - 1);
            output.append(AnsiEscape.SEPARATOR.getCode());
            output.append(escapeSequence.substring(2));
        }
    }

    @Override
    public void render(String input, StringBuilder output, String styleName) throws IllegalArgumentException {
        this.render(input, output, styleName.split(",", -1));
    }

    @Override
    public void render(StringBuilder input, StringBuilder output) throws IllegalArgumentException {
        int pos = 0;
        while (true) {
            int beginTokenPos;
            if ((beginTokenPos = input.indexOf(this.beginToken, pos)) == -1) {
                output.append(pos == 0 ? input : input.substring(pos, input.length()));
                return;
            }
            output.append(input.substring(pos, beginTokenPos));
            int endTokenPos = input.indexOf(this.endToken, beginTokenPos);
            if (endTokenPos == -1) {
                LOGGER.warn("Missing matching end token {} for token at position {}: '{}'", (Object)this.endToken, (Object)beginTokenPos, (Object)input);
                output.append(beginTokenPos == 0 ? input : input.substring(beginTokenPos, input.length()));
                return;
            }
            String spec = input.substring(beginTokenPos += this.beginTokenLen, endTokenPos);
            String[] items = spec.split("\\s", 2);
            if (items.length == 1) {
                LOGGER.warn("Missing argument in ANSI escape specification '{}'", (Object)spec);
                output.append(this.beginToken).append(spec).append(this.endToken);
            } else {
                this.render(items[1], output, items[0].split(",", -1));
            }
            pos = endTokenPos + this.endTokenLen;
        }
    }

    public Map<String, String> getStyleMap() {
        return this.styleMap;
    }

    public String toString() {
        return "AnsiMessageRenderer [beginToken=" + this.beginToken + ", beginTokenLen=" + this.beginTokenLen + ", endToken=" + this.endToken + ", endTokenLen=" + this.endTokenLen + ", styleMap=" + this.styleMap + "]";
    }

    static {
        Map spock = JAnsiTextRenderer.ofEntries(JAnsiTextRenderer.entry("Prefix", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Name", AnsiEscape.BG_RED, AnsiEscape.WHITE), JAnsiTextRenderer.entry("NameMessageSeparator", AnsiEscape.BG_RED, AnsiEscape.WHITE), JAnsiTextRenderer.entry("Message", AnsiEscape.BG_RED, AnsiEscape.WHITE, AnsiEscape.BOLD), JAnsiTextRenderer.entry("At", AnsiEscape.WHITE), JAnsiTextRenderer.entry("CauseLabel", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Text", AnsiEscape.WHITE), JAnsiTextRenderer.entry("More", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Suppressed", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassLoaderName", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassLoaderSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleName", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleVersionSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleVersion", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleNameSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassName", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.ClassMethodSeparator", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.MethodName", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.NativeMethod", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.FileName", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.LineNumber", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.Container", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.ContainerSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.UnknownSource", AnsiEscape.RED), JAnsiTextRenderer.entry("ExtraClassInfo.Inexact", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.Container", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.ContainerSeparator", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.Location", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.Version", AnsiEscape.YELLOW));
        Map kirk = JAnsiTextRenderer.ofEntries(JAnsiTextRenderer.entry("Prefix", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Name", AnsiEscape.BG_RED, AnsiEscape.YELLOW, AnsiEscape.BOLD), JAnsiTextRenderer.entry("NameMessageSeparator", AnsiEscape.BG_RED, AnsiEscape.YELLOW), JAnsiTextRenderer.entry("Message", AnsiEscape.BG_RED, AnsiEscape.WHITE, AnsiEscape.BOLD), JAnsiTextRenderer.entry("At", AnsiEscape.WHITE), JAnsiTextRenderer.entry("CauseLabel", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Text", AnsiEscape.WHITE), JAnsiTextRenderer.entry("More", AnsiEscape.WHITE), JAnsiTextRenderer.entry("Suppressed", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassLoaderName", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassLoaderSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleName", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleVersionSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleVersion", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ModuleNameSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassName", AnsiEscape.BG_RED, AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.ClassMethodSeparator", AnsiEscape.BG_RED, AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.MethodName", AnsiEscape.BG_RED, AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.NativeMethod", AnsiEscape.BG_RED, AnsiEscape.YELLOW), JAnsiTextRenderer.entry("StackTraceElement.FileName", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.LineNumber", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.Container", AnsiEscape.RED), JAnsiTextRenderer.entry("StackTraceElement.ContainerSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("StackTraceElement.UnknownSource", AnsiEscape.RED), JAnsiTextRenderer.entry("ExtraClassInfo.Inexact", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.Container", AnsiEscape.WHITE), JAnsiTextRenderer.entry("ExtraClassInfo.ContainerSeparator", AnsiEscape.WHITE), JAnsiTextRenderer.entry("ExtraClassInfo.Location", AnsiEscape.YELLOW), JAnsiTextRenderer.entry("ExtraClassInfo.Version", AnsiEscape.YELLOW));
        DefaultExceptionStyleMap = spock;
        DEFAULT_MESSAGE_STYLE_MAP = Collections.emptyMap();
        HashMap predefinedStyleMaps = new HashMap();
        predefinedStyleMaps.put("Spock", spock);
        predefinedStyleMaps.put("Kirk", kirk);
        PREFEDINED_STYLE_MAPS = Collections.unmodifiableMap(predefinedStyleMaps);
    }
}

