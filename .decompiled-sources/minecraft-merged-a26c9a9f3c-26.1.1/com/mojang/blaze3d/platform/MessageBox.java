/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.platform;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@Environment(value=EnvType.CLIENT)
public class MessageBox {
    private static final String DEFAULT_TITLE = "Minecraft";
    public static final String TYPE_OK = "ok";
    public static final String TYPE_OK_CANCEL = "okcancel";
    public static final String TYPE_YES_NO = "yesno";
    public static final String TYPE_YES_NO_CANCEL = "yesnocancel";
    public static final String ICON_INFO = "info";
    public static final String ICON_WARNING = "warning";
    public static final String ICON_ERROR = "error";
    public static final String ICON_QUESTION = "question";
    public static final int BUTTON_CANCEL_OR_NO = 0;
    public static final int BUTTON_OK_OR_YES = 1;
    public static final int BUTTON_NO = 2;

    public static void error(String message) {
        TinyFileDialogs.tinyfd_messageBox(DEFAULT_TITLE, message, TYPE_OK, ICON_ERROR, 1);
    }

    public static boolean errorWithContinue(String message) {
        return TinyFileDialogs.tinyfd_messageBox(DEFAULT_TITLE, message, TYPE_YES_NO, ICON_ERROR, 1) == 1;
    }
}

