/*
 * Decompiled with CFR 0.152.
 */
package oshi.software.os.mac;

import com.sun.jna.platform.mac.CoreFoundation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import oshi.jna.platform.mac.CoreFoundation;
import oshi.software.os.ApplicationInfo;
import oshi.util.ExecutingCommand;
import oshi.util.ParseUtil;

public final class MacInstalledApps {
    private static final String COLON = ":";
    private static final CoreFoundation CF = CoreFoundation.INSTANCE;

    private MacInstalledApps() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        List<String> output = ExecutingCommand.runNative("system_profiler SPApplicationsDataType");
        return MacInstalledApps.parseMacAppInfo(output);
    }

    private static List<ApplicationInfo> parseMacAppInfo(List<String> lines) {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        String appName = null;
        HashMap<String, String> appDetails = null;
        boolean collectingAppDetails = false;
        String dateFormat = MacInstalledApps.getLocaleDateTimeFormat(CoreFoundation.CFDateFormatterStyle.kCFDateFormatterShortStyle);
        for (String line : lines) {
            if ((line = line.trim()).endsWith(COLON)) {
                if (appName != null && !appDetails.isEmpty()) {
                    appInfoSet.add(MacInstalledApps.createAppInfo(appName, (Map<String, String>)appDetails, dateFormat));
                }
                appName = line.substring(0, line.length() - 1);
                appDetails = new HashMap<String, String>();
                collectingAppDetails = true;
                continue;
            }
            if (!collectingAppDetails || !line.contains(COLON)) continue;
            int colonIndex = line.indexOf(COLON);
            String key = line.substring(0, colonIndex).trim();
            String value = line.substring(colonIndex + 1).trim();
            appDetails.put(key, value);
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }

    private static ApplicationInfo createAppInfo(String name, Map<String, String> details, String dateFormat) {
        String obtainedFrom = ParseUtil.getValueOrUnknown(details, "Obtained from");
        String signedBy = ParseUtil.getValueOrUnknown(details, "Signed by");
        String vendor = obtainedFrom.equals("Identified Developer") ? signedBy : obtainedFrom;
        String lastModified = details.getOrDefault("Last Modified", "unknown");
        long lastModifiedEpoch = ParseUtil.parseDateToEpoch(lastModified, dateFormat);
        LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
        additionalInfo.put("Kind", ParseUtil.getValueOrUnknown(details, "Kind"));
        additionalInfo.put("Location", ParseUtil.getValueOrUnknown(details, "Location"));
        additionalInfo.put("Get Info String", ParseUtil.getValueOrUnknown(details, "Get Info String"));
        return new ApplicationInfo(name, ParseUtil.getValueOrUnknown(details, "Version"), vendor, lastModifiedEpoch, additionalInfo);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static String getLocaleDateTimeFormat(CoreFoundation.CFDateFormatterStyle style) {
        CoreFoundation.CFIndex styleIndex = style.index();
        CoreFoundation.CFLocale locale = CF.CFLocaleCopyCurrent();
        try {
            String string;
            CoreFoundation.CFDateFormatter formatter = CF.CFDateFormatterCreate(null, locale, styleIndex, styleIndex);
            if (formatter == null) {
                String string2 = "";
                return string2;
            }
            try {
                CoreFoundation.CFStringRef format = CF.CFDateFormatterGetFormat(formatter);
                string = format == null ? "" : format.stringValue();
            }
            catch (Throwable throwable) {
                CF.CFRelease(formatter);
                throw throwable;
            }
            CF.CFRelease(formatter);
            return string;
        }
        finally {
            CF.CFRelease(locale);
        }
    }
}

