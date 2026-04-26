/*
 * Decompiled with CFR 0.152.
 */
package oshi.driver.windows.registry;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.W32APITypeMapper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.software.os.ApplicationInfo;
import oshi.util.ParseUtil;

public final class InstalledAppsData {
    private static final Logger LOG = LoggerFactory.getLogger(InstalledAppsData.class);
    private static final Advapi32 ADV = Advapi32.INSTANCE;
    private static final long THIRTY_YEARS_IN_SECS = 946080000L;
    private static final Map<WinReg.HKEY, List<String>> REGISTRY_PATHS = new HashMap<WinReg.HKEY, List<String>>();
    private static final int[] ACCESS_FLAGS = new int[]{256, 512};

    private InstalledAppsData() {
    }

    public static List<ApplicationInfo> queryInstalledApps() {
        LinkedHashSet<ApplicationInfo> appInfoSet = new LinkedHashSet<ApplicationInfo>();
        for (Map.Entry<WinReg.HKEY, List<String>> entry : REGISTRY_PATHS.entrySet()) {
            WinReg.HKEY rootKey = entry.getKey();
            List<String> uninstallPaths = entry.getValue();
            for (String registryPath : uninstallPaths) {
                for (int accessFlag : ACCESS_FLAGS) {
                    try {
                        String[] keys;
                        for (String key : keys = Advapi32Util.registryGetKeys(rootKey, registryPath, accessFlag)) {
                            String fullPath = registryPath + "\\" + key;
                            try {
                                String name = InstalledAppsData.registryValueToString(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "DisplayName", accessFlag));
                                if (name == null) continue;
                                String version = InstalledAppsData.registryValueToString(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "DisplayVersion", accessFlag));
                                String publisher = InstalledAppsData.registryValueToString(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "Publisher", accessFlag));
                                long installDate = InstalledAppsData.registryValueToLong(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "InstallDate", accessFlag));
                                String installLocation = InstalledAppsData.registryValueToString(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "InstallLocation", accessFlag));
                                String installSource = InstalledAppsData.registryValueToString(InstalledAppsData.getRegistryValueOrNull(rootKey, fullPath, "InstallSource", accessFlag));
                                LinkedHashMap<String, String> additionalInfo = new LinkedHashMap<String, String>();
                                additionalInfo.put("installLocation", installLocation);
                                additionalInfo.put("installSource", installSource);
                                ApplicationInfo app = new ApplicationInfo(name, version, publisher, installDate, additionalInfo);
                                appInfoSet.add(app);
                            }
                            catch (Win32Exception win32Exception) {
                                // empty catch block
                            }
                        }
                    }
                    catch (Win32Exception win32Exception) {
                        // empty catch block
                    }
                }
            }
        }
        return new ArrayList<ApplicationInfo>(appInfoSet);
    }

    private static String registryValueToString(Object registryValueOrNull) {
        if (registryValueOrNull instanceof Integer) {
            return Integer.toString((Integer)registryValueOrNull);
        }
        return (String)registryValueOrNull;
    }

    private static long registryValueToLong(Object registryValueOrNull) {
        if (registryValueOrNull == null) {
            return 0L;
        }
        long currentTimeSecs = System.currentTimeMillis() / 1000L;
        long minSaneTimestamp = currentTimeSecs - 946080000L;
        if (registryValueOrNull instanceof Integer) {
            int value = (Integer)registryValueOrNull;
            if ((long)value > minSaneTimestamp && (long)value < currentTimeSecs) {
                return (long)value * 1000L;
            }
            return value;
        }
        if (registryValueOrNull instanceof String) {
            String dateStr = ((String)registryValueOrNull).trim();
            long epoch = ParseUtil.parseDateToEpoch(dateStr, "yyyyMMdd");
            if (epoch == 0L) {
                epoch = ParseUtil.parseDateToEpoch(dateStr, "MM/dd/yyyy");
            }
            return epoch;
        }
        return 0L;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private static Object getRegistryValueOrNull(WinReg.HKEY rootKey, String path, String key, int accessFlag) {
        block6: {
            WinReg.HKEY hKey = null;
            try {
                hKey = InstalledAppsData.getRegistryHKey(rootKey, path, accessFlag);
                Object value = InstalledAppsData.registryGetValue(hKey, key);
                if (value instanceof Integer || value instanceof String && !((String)value).trim().isEmpty()) {
                    Object object = value;
                    return object;
                }
            }
            catch (Win32Exception e) {
                LOG.trace("Unable to access " + path + " with flag " + accessFlag + ": " + e.getMessage());
            }
            finally {
                int rc;
                if (hKey == null || (rc = ADV.RegCloseKey(hKey)) == 0) break block6;
                throw new Win32Exception(rc);
            }
        }
        return null;
    }

    private static WinReg.HKEY getRegistryHKey(WinReg.HKEY rootKey, String path, int accessFlag) {
        WinReg.HKEYByReference phkKey = new WinReg.HKEYByReference();
        int rc = ADV.RegOpenKeyEx(rootKey, path, 0, 0x20019 | accessFlag, phkKey);
        if (rc != 0) {
            throw new Win32Exception(rc);
        }
        return phkKey.getValue();
    }

    private static Object registryGetValue(WinReg.HKEY hKey, String value) {
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, (Pointer)null, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        int type = lpType.getValue();
        switch (type) {
            case 1: 
            case 2: {
                return InstalledAppsData.registryGetString(hKey, value, lpType, lpcbData);
            }
            case 4: {
                return InstalledAppsData.registryGetDword(hKey, value, lpType, lpcbData);
            }
        }
        LOG.warn("Unsupported registry data type {} for {}", (Object)type, (Object)value);
        return null;
    }

    private static String registryGetString(WinReg.HKEY hKey, String value, IntByReference lpType, IntByReference lpcbData) {
        if (lpcbData.getValue() == 0) {
            return "";
        }
        Memory mem = new Memory(lpcbData.getValue() + Native.WCHAR_SIZE);
        mem.clear();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, mem, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        if (W32APITypeMapper.DEFAULT == W32APITypeMapper.UNICODE) {
            return mem.getWideString(0L);
        }
        return mem.getString(0L);
    }

    private static int registryGetDword(WinReg.HKEY hKey, String value, IntByReference lpType, IntByReference lpcbData) {
        IntByReference pData = new IntByReference();
        int rc = ADV.RegQueryValueEx(hKey, value, 0, lpType, pData, lpcbData);
        if (rc != 0 && rc != 122) {
            throw new Win32Exception(rc);
        }
        return pData.getValue();
    }

    static {
        REGISTRY_PATHS.put(WinReg.HKEY_LOCAL_MACHINE, Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall", "SOFTWARE\\WOW6432Node\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
        REGISTRY_PATHS.put(WinReg.HKEY_CURRENT_USER, Arrays.asList("SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\Uninstall"));
    }
}

