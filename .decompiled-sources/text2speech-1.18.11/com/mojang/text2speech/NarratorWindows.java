/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.text2speech;

import com.mojang.text2speech.Narrator;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.COM.COMUtils;
import com.sun.jna.platform.win32.COM.Unknown;
import com.sun.jna.platform.win32.Guid;
import com.sun.jna.platform.win32.Ole32;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

public class NarratorWindows
extends Unknown
implements Narrator {
    private static final String COM_CLASS_SP_VOICE = "96749377-3391-11D2-9EE3-00C04F797396";
    private static final String INTERFACE_SP_VOICE = "6C44DF74-72B9-4992-A1EC-EF996E0422D4";
    private static final int VTABLE_INDEX_SPEAK = 20;
    private static final int VTABLE_INDEX_SKIP = 23;
    private static final int VTABLE_INDEX_SET_VOLUME = 30;
    private static final int SPF_ASYNC = 1;
    private static final int SPF_PURGEBEFORESPEAK = 2;
    private static final int SPF_IS_NOT_XML = 16;
    private static final WString SKIP_TYPE = new WString("Sentence");
    private static final int MAX_NUM_ITEMS = Integer.MAX_VALUE;

    private static Pointer initSAPI() throws Narrator.InitializeException {
        Ole32.INSTANCE.CoInitialize(null);
        PointerByReference spVoicePointer = new PointerByReference();
        WinNT.HRESULT result = Ole32.INSTANCE.CoCreateInstance(new Guid.CLSID(COM_CLASS_SP_VOICE), null, 7, new Guid.IID(INTERFACE_SP_VOICE), spVoicePointer);
        if (COMUtils.FAILED(result)) {
            throw new Narrator.InitializeException("SP_VOICE returned code " + result);
        }
        return spVoicePointer.getValue();
    }

    public NarratorWindows() throws Narrator.InitializeException {
        super(NarratorWindows.initSAPI());
    }

    @Override
    public void say(String msg, boolean interrupt, float volume) {
        int flags = 17;
        if (interrupt) {
            flags |= 2;
        }
        this.setVolume(volume);
        this._invokeNativeInt(20, new Object[]{this.getPointer(), new WString(msg), flags, null});
    }

    private void setVolume(float volume) {
        short volumeLevel = (short)(volume * 100.0f);
        this._invokeNativeInt(30, new Object[]{this.getPointer(), volumeLevel});
    }

    @Override
    public void clear() {
        IntByReference pulNumSkipped = new IntByReference();
        this._invokeNativeInt(23, new Object[]{this.getPointer(), SKIP_TYPE, Integer.MAX_VALUE, pulNumSkipped});
    }

    @Override
    public void destroy() {
        this.Release();
        Ole32.INSTANCE.CoUninitialize();
    }
}

