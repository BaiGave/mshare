/*
 * Decompiled with CFR 0.152.
 */
package oshi.jna.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.CoreFoundation;

public interface CoreFoundation
extends com.sun.jna.platform.mac.CoreFoundation {
    public static final CoreFoundation INSTANCE = Native.load("CoreFoundation", CoreFoundation.class);

    public CFLocale CFLocaleCopyCurrent();

    public CFDateFormatter CFDateFormatterCreate(CoreFoundation.CFAllocatorRef var1, CFLocale var2, CoreFoundation.CFIndex var3, CoreFoundation.CFIndex var4);

    public CoreFoundation.CFStringRef CFDateFormatterGetFormat(CFDateFormatter var1);

    public static enum CFDateFormatterStyle {
        kCFDateFormatterNoStyle,
        kCFDateFormatterShortStyle,
        kCFDateFormatterMediumStyle,
        kCFDateFormatterLongStyle,
        kCFDateFormatterFullStyle;


        public CoreFoundation.CFIndex index() {
            return new CoreFoundation.CFIndex((long)this.ordinal());
        }
    }

    public static class CFDateFormatter
    extends CoreFoundation.CFTypeRef {
    }

    public static class CFLocale
    extends CoreFoundation.CFTypeRef {
    }
}

