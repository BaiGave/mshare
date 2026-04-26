/*
 * Decompiled with CFR 0.152.
 */
package net.fabricmc.loader.impl.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public final class Localization {
    public static final ResourceBundle BUNDLE = Localization.createBundle("net.fabricmc.loader.Messages", Locale.getDefault());
    public static final ResourceBundle ROOT_LOCALE_BUNDLE = Localization.createBundle("net.fabricmc.loader.Messages", Locale.ROOT);

    public static String format(String key, Object ... args) {
        String pattern = BUNDLE.getString(key);
        if (args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    public static String formatRoot(String key, Object ... args) {
        String pattern = ROOT_LOCALE_BUNDLE.getString(key);
        if (args.length == 0) {
            return pattern;
        }
        return MessageFormat.format(pattern, args);
    }

    private static ResourceBundle createBundle(String name, Locale locale) {
        if (System.getProperty("java.version", "").startsWith("1.")) {
            return ResourceBundle.getBundle(name, locale, new ResourceBundle.Control(){

                @Override
                public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload) throws IllegalAccessException, InstantiationException, IOException {
                    InputStream is;
                    if (format.equals("java.properties") && (is = loader.getResourceAsStream(this.toResourceName(this.toBundleName(baseName, locale), "properties"))) != null) {
                        try (InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8);){
                            PropertyResourceBundle propertyResourceBundle = new PropertyResourceBundle(reader);
                            return propertyResourceBundle;
                        }
                    }
                    return super.newBundle(baseName, locale, format, loader, reload);
                }
            });
        }
        return ResourceBundle.getBundle(name, locale);
    }
}

