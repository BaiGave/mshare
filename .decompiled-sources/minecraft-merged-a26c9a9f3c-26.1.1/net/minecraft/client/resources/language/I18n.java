/*
 * Decompiled with CFR 0.152.
 */
package net.minecraft.client.resources.language;

import java.util.IllegalFormatException;
import java.util.Locale;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.locale.Language;

@Environment(value=EnvType.CLIENT)
public class I18n {
    private static volatile Language language = Language.getInstance();

    private I18n() {
    }

    static void setLanguage(Language locale) {
        language = locale;
    }

    public static String get(String id, Object ... args) {
        String value = language.getOrDefault(id);
        try {
            return String.format(Locale.ROOT, value, args);
        }
        catch (IllegalFormatException ignored) {
            return "Format error: " + value;
        }
    }

    public static boolean exists(String id) {
        return language.has(id);
    }
}

