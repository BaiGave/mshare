/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.commons.lang3.JavaVersion;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.SystemProperties;

public class SystemUtils {
    private static final String OS_NAME_WINDOWS_PREFIX = "Windows";
    public static final String FILE_ENCODING = SystemProperties.getFileEncoding();
    @Deprecated
    public static final String FILE_SEPARATOR = SystemProperties.getFileSeparator();
    @Deprecated
    public static final String JAVA_AWT_FONTS = SystemProperties.getJavaAwtFonts();
    @Deprecated
    public static final String JAVA_AWT_GRAPHICSENV = SystemProperties.getJavaAwtGraphicsenv();
    @Deprecated
    public static final String JAVA_AWT_HEADLESS = SystemProperties.getJavaAwtHeadless();
    @Deprecated
    public static final String JAVA_AWT_PRINTERJOB = SystemProperties.getJavaAwtPrinterjob();
    public static final String JAVA_CLASS_PATH = SystemProperties.getJavaClassPath();
    public static final String JAVA_CLASS_VERSION = SystemProperties.getJavaClassVersion();
    @Deprecated
    public static final String JAVA_COMPILER = SystemProperties.getJavaCompiler();
    @Deprecated
    public static final String JAVA_ENDORSED_DIRS = SystemProperties.getJavaEndorsedDirs();
    @Deprecated
    public static final String JAVA_EXT_DIRS = SystemProperties.getJavaExtDirs();
    public static final String JAVA_HOME = SystemProperties.getJavaHome();
    public static final String JAVA_IO_TMPDIR = SystemProperties.getJavaIoTmpdir();
    public static final String JAVA_LIBRARY_PATH = SystemProperties.getJavaLibraryPath();
    public static final String JAVA_RUNTIME_NAME = SystemProperties.getJavaRuntimeName();
    public static final String JAVA_RUNTIME_VERSION = SystemProperties.getJavaRuntimeVersion();
    public static final String JAVA_SPECIFICATION_NAME = SystemProperties.getJavaSpecificationName();
    public static final String JAVA_SPECIFICATION_VENDOR = SystemProperties.getJavaSpecificationVendor();
    public static final String JAVA_SPECIFICATION_VERSION = SystemProperties.getJavaSpecificationVersion();
    private static final JavaVersion JAVA_SPECIFICATION_VERSION_AS_ENUM = JavaVersion.get(JAVA_SPECIFICATION_VERSION);
    public static final String JAVA_UTIL_PREFS_PREFERENCES_FACTORY = SystemProperties.getJavaUtilPrefsPreferencesFactory();
    public static final String JAVA_VENDOR = SystemProperties.getJavaVendor();
    public static final String JAVA_VENDOR_URL = SystemProperties.getJavaVendorUrl();
    public static final String JAVA_VERSION = SystemProperties.getJavaVersion();
    public static final String JAVA_VM_INFO = SystemProperties.getJavaVmInfo();
    public static final String JAVA_VM_NAME = SystemProperties.getJavaVmName();
    public static final String JAVA_VM_SPECIFICATION_NAME = SystemProperties.getJavaVmSpecificationName();
    public static final String JAVA_VM_SPECIFICATION_VENDOR = SystemProperties.getJavaVmSpecificationVendor();
    public static final String JAVA_VM_SPECIFICATION_VERSION = SystemProperties.getJavaVmSpecificationVersion();
    public static final String JAVA_VM_VENDOR = SystemProperties.getJavaVmVendor();
    public static final String JAVA_VM_VERSION = SystemProperties.getJavaVmVersion();
    @Deprecated
    public static final String LINE_SEPARATOR = SystemProperties.getLineSeparator();
    public static final String OS_ARCH = SystemProperties.getOsArch();
    public static final String OS_NAME = SystemProperties.getOsName();
    public static final String OS_VERSION = SystemProperties.getOsVersion();
    @Deprecated
    public static final String PATH_SEPARATOR = SystemProperties.getPathSeparator();
    public static final String USER_COUNTRY = SystemProperties.getProperty("user.country", () -> SystemProperties.getProperty("user.region"));
    public static final String USER_DIR = SystemProperties.getUserDir();
    public static final String USER_HOME = SystemProperties.getUserHome();
    public static final String USER_LANGUAGE = SystemProperties.getUserLanguage();
    public static final String USER_NAME = SystemProperties.getUserName();
    public static final String USER_TIMEZONE = SystemProperties.getUserTimezone();
    public static final boolean IS_JAVA_1_1 = SystemUtils.getJavaVersionMatches("1.1");
    public static final boolean IS_JAVA_1_2 = SystemUtils.getJavaVersionMatches("1.2");
    public static final boolean IS_JAVA_1_3 = SystemUtils.getJavaVersionMatches("1.3");
    public static final boolean IS_JAVA_1_4 = SystemUtils.getJavaVersionMatches("1.4");
    public static final boolean IS_JAVA_1_5 = SystemUtils.getJavaVersionMatches("1.5");
    public static final boolean IS_JAVA_1_6 = SystemUtils.getJavaVersionMatches("1.6");
    public static final boolean IS_JAVA_1_7 = SystemUtils.getJavaVersionMatches("1.7");
    public static final boolean IS_JAVA_1_8 = SystemUtils.getJavaVersionMatches("1.8");
    @Deprecated
    public static final boolean IS_JAVA_1_9 = SystemUtils.getJavaVersionMatches("9");
    public static final boolean IS_JAVA_9 = SystemUtils.getJavaVersionMatches("9");
    public static final boolean IS_JAVA_10 = SystemUtils.getJavaVersionMatches("10");
    public static final boolean IS_JAVA_11 = SystemUtils.getJavaVersionMatches("11");
    public static final boolean IS_JAVA_12 = SystemUtils.getJavaVersionMatches("12");
    public static final boolean IS_JAVA_13 = SystemUtils.getJavaVersionMatches("13");
    public static final boolean IS_JAVA_14 = SystemUtils.getJavaVersionMatches("14");
    public static final boolean IS_JAVA_15 = SystemUtils.getJavaVersionMatches("15");
    public static final boolean IS_JAVA_16 = SystemUtils.getJavaVersionMatches("16");
    public static final boolean IS_JAVA_17 = SystemUtils.getJavaVersionMatches("17");
    public static final boolean IS_JAVA_18 = SystemUtils.getJavaVersionMatches("18");
    public static final boolean IS_JAVA_19 = SystemUtils.getJavaVersionMatches("19");
    public static final boolean IS_JAVA_20 = SystemUtils.getJavaVersionMatches("20");
    public static final boolean IS_JAVA_21 = SystemUtils.getJavaVersionMatches("21");
    public static final boolean IS_JAVA_22 = SystemUtils.getJavaVersionMatches("22");
    public static final boolean IS_JAVA_23 = SystemUtils.getJavaVersionMatches("23");
    public static final boolean IS_JAVA_24 = SystemUtils.getJavaVersionMatches("24");
    public static final boolean IS_OS_AIX = SystemUtils.getOsNameMatches("AIX");
    public static final boolean IS_OS_ANDROID = Strings.CS.contains(SystemProperties.getJavaVendor(), "Android");
    public static final boolean IS_OS_HP_UX = SystemUtils.getOsNameMatches("HP-UX");
    public static final boolean IS_OS_400 = SystemUtils.getOsNameMatches("OS/400");
    public static final boolean IS_OS_IRIX = SystemUtils.getOsNameMatches("Irix");
    public static final boolean IS_OS_LINUX = SystemUtils.getOsNameMatches("Linux");
    public static final boolean IS_OS_MAC = SystemUtils.getOsNameMatches("Mac");
    public static final boolean IS_OS_MAC_OSX = SystemUtils.getOsNameMatches("Mac OS X");
    public static final boolean IS_OS_MAC_OSX_CHEETAH = SystemUtils.getOsMatches("Mac OS X", "10.0");
    public static final boolean IS_OS_MAC_OSX_PUMA = SystemUtils.getOsMatches("Mac OS X", "10.1");
    public static final boolean IS_OS_MAC_OSX_JAGUAR = SystemUtils.getOsMatches("Mac OS X", "10.2");
    public static final boolean IS_OS_MAC_OSX_PANTHER = SystemUtils.getOsMatches("Mac OS X", "10.3");
    public static final boolean IS_OS_MAC_OSX_TIGER = SystemUtils.getOsMatches("Mac OS X", "10.4");
    public static final boolean IS_OS_MAC_OSX_LEOPARD = SystemUtils.getOsMatches("Mac OS X", "10.5");
    public static final boolean IS_OS_MAC_OSX_SNOW_LEOPARD = SystemUtils.getOsMatches("Mac OS X", "10.6");
    public static final boolean IS_OS_MAC_OSX_LION = SystemUtils.getOsMatches("Mac OS X", "10.7");
    public static final boolean IS_OS_MAC_OSX_MOUNTAIN_LION = SystemUtils.getOsMatches("Mac OS X", "10.8");
    public static final boolean IS_OS_MAC_OSX_MAVERICKS = SystemUtils.getOsMatches("Mac OS X", "10.9");
    public static final boolean IS_OS_MAC_OSX_YOSEMITE = SystemUtils.getOsMatches("Mac OS X", "10.10");
    public static final boolean IS_OS_MAC_OSX_EL_CAPITAN = SystemUtils.getOsMatches("Mac OS X", "10.11");
    public static final boolean IS_OS_MAC_OSX_SIERRA = SystemUtils.getOsMatches("Mac OS X", "10.12");
    public static final boolean IS_OS_MAC_OSX_HIGH_SIERRA = SystemUtils.getOsMatches("Mac OS X", "10.13");
    public static final boolean IS_OS_MAC_OSX_MOJAVE = SystemUtils.getOsMatches("Mac OS X", "10.14");
    public static final boolean IS_OS_MAC_OSX_CATALINA = SystemUtils.getOsMatches("Mac OS X", "10.15");
    public static final boolean IS_OS_MAC_OSX_BIG_SUR = SystemUtils.getOsMatches("Mac OS X", "11");
    public static final boolean IS_OS_MAC_OSX_MONTEREY = SystemUtils.getOsMatches("Mac OS X", "12");
    public static final boolean IS_OS_MAC_OSX_VENTURA = SystemUtils.getOsMatches("Mac OS X", "13");
    public static final boolean IS_OS_MAC_OSX_SONOMA = SystemUtils.getOsMatches("Mac OS X", "14");
    public static final boolean IS_OS_MAC_OSX_SEQUOIA = SystemUtils.getOsMatches("Mac OS X", "15");
    public static final boolean IS_OS_FREE_BSD = SystemUtils.getOsNameMatches("FreeBSD");
    public static final boolean IS_OS_OPEN_BSD = SystemUtils.getOsNameMatches("OpenBSD");
    public static final boolean IS_OS_NET_BSD = SystemUtils.getOsNameMatches("NetBSD");
    public static final boolean IS_OS_NETWARE = SystemUtils.getOsNameMatches("Netware");
    public static final boolean IS_OS_OS2 = SystemUtils.getOsNameMatches("OS/2");
    public static final boolean IS_OS_SOLARIS = SystemUtils.getOsNameMatches("Solaris");
    public static final boolean IS_OS_SUN_OS = SystemUtils.getOsNameMatches("SunOS");
    public static final boolean IS_OS_UNIX = IS_OS_AIX || IS_OS_HP_UX || IS_OS_IRIX || IS_OS_LINUX || IS_OS_MAC_OSX || IS_OS_SOLARIS || IS_OS_SUN_OS || IS_OS_FREE_BSD || IS_OS_OPEN_BSD || IS_OS_NET_BSD;
    public static final boolean IS_OS_WINDOWS = SystemUtils.getOsNameMatches("Windows");
    public static final boolean IS_OS_WINDOWS_2000 = SystemUtils.getOsNameMatches("Windows 2000");
    public static final boolean IS_OS_WINDOWS_2003 = SystemUtils.getOsNameMatches("Windows 2003");
    public static final boolean IS_OS_WINDOWS_2008 = SystemUtils.getOsNameMatches("Windows Server 2008");
    public static final boolean IS_OS_WINDOWS_2012 = SystemUtils.getOsNameMatches("Windows Server 2012");
    public static final boolean IS_OS_WINDOWS_95 = SystemUtils.getOsNameMatches("Windows 95");
    public static final boolean IS_OS_WINDOWS_98 = SystemUtils.getOsNameMatches("Windows 98");
    public static final boolean IS_OS_WINDOWS_ME = SystemUtils.getOsNameMatches("Windows Me");
    public static final boolean IS_OS_WINDOWS_NT = SystemUtils.getOsNameMatches("Windows NT");
    public static final boolean IS_OS_WINDOWS_XP = SystemUtils.getOsNameMatches("Windows XP");
    public static final boolean IS_OS_WINDOWS_VISTA = SystemUtils.getOsNameMatches("Windows Vista");
    public static final boolean IS_OS_WINDOWS_7 = SystemUtils.getOsNameMatches("Windows 7");
    public static final boolean IS_OS_WINDOWS_8 = SystemUtils.getOsNameMatches("Windows 8");
    public static final boolean IS_OS_WINDOWS_10 = SystemUtils.getOsNameMatches("Windows 10");
    public static final boolean IS_OS_WINDOWS_11 = SystemUtils.getOsNameMatches("Windows 11");
    public static final boolean IS_OS_ZOS = SystemUtils.getOsNameMatches("z/OS");
    public static final String USER_HOME_KEY = "user.home";
    @Deprecated
    public static final String USER_NAME_KEY = "user.name";
    @Deprecated
    public static final String USER_DIR_KEY = "user.dir";
    @Deprecated
    public static final String JAVA_IO_TMPDIR_KEY = "java.io.tmpdir";
    @Deprecated
    public static final String JAVA_HOME_KEY = "java.home";
    @Deprecated
    public static final String AWT_TOOLKIT = SystemProperties.getAwtToolkit();

    public static String getEnvironmentVariable(String name, String defaultValue) {
        try {
            String value = System.getenv(name);
            return value == null ? defaultValue : value;
        }
        catch (SecurityException ex) {
            return defaultValue;
        }
    }

    public static String getHostName() {
        return IS_OS_WINDOWS ? System.getenv("COMPUTERNAME") : System.getenv("HOSTNAME");
    }

    public static File getJavaHome() {
        return new File(SystemProperties.getJavaHome());
    }

    public static Path getJavaHomePath() {
        return Paths.get(SystemProperties.getJavaHome(), new String[0]);
    }

    public static File getJavaIoTmpDir() {
        return new File(SystemProperties.getJavaIoTmpdir());
    }

    public static Path getJavaIoTmpDirPath() {
        return Paths.get(SystemProperties.getJavaIoTmpdir(), new String[0]);
    }

    private static boolean getJavaVersionMatches(String versionPrefix) {
        return SystemUtils.isJavaVersionMatch(JAVA_SPECIFICATION_VERSION, versionPrefix);
    }

    private static boolean getOsMatches(String osNamePrefix, String osVersionPrefix) {
        return SystemUtils.isOsMatch(OS_NAME, OS_VERSION, osNamePrefix, osVersionPrefix);
    }

    private static boolean getOsNameMatches(String osNamePrefix) {
        return SystemUtils.isOsNameMatch(OS_NAME, osNamePrefix);
    }

    public static File getUserDir() {
        return new File(SystemProperties.getUserDir());
    }

    public static Path getUserDirPath() {
        return Paths.get(SystemProperties.getUserDir(), new String[0]);
    }

    public static File getUserHome() {
        return new File(SystemProperties.getUserHome());
    }

    public static Path getUserHomePath() {
        return Paths.get(SystemProperties.getUserHome(), new String[0]);
    }

    @Deprecated
    public static String getUserName() {
        return SystemProperties.getUserName();
    }

    @Deprecated
    public static String getUserName(String defaultValue) {
        return SystemProperties.getUserName(defaultValue);
    }

    @Deprecated
    public static boolean isJavaAwtHeadless() {
        return Boolean.TRUE.toString().equals(JAVA_AWT_HEADLESS);
    }

    public static boolean isJavaVersionAtLeast(JavaVersion requiredVersion) {
        return JAVA_SPECIFICATION_VERSION_AS_ENUM != null && JAVA_SPECIFICATION_VERSION_AS_ENUM.atLeast(requiredVersion);
    }

    public static boolean isJavaVersionAtMost(JavaVersion requiredVersion) {
        return JAVA_SPECIFICATION_VERSION_AS_ENUM != null && JAVA_SPECIFICATION_VERSION_AS_ENUM.atMost(requiredVersion);
    }

    static boolean isJavaVersionMatch(String version, String versionPrefix) {
        if (version == null) {
            return false;
        }
        return version.startsWith(versionPrefix);
    }

    static boolean isOsMatch(String osName, String osVersion, String osNamePrefix, String osVersionPrefix) {
        if (osName == null || osVersion == null) {
            return false;
        }
        return SystemUtils.isOsNameMatch(osName, osNamePrefix) && SystemUtils.isOsVersionMatch(osVersion, osVersionPrefix);
    }

    static boolean isOsNameMatch(String osName, String osNamePrefix) {
        if (osName == null) {
            return false;
        }
        return Strings.CI.startsWith(osName, osNamePrefix);
    }

    static boolean isOsVersionMatch(String osVersion, String osVersionPrefix) {
        if (StringUtils.isEmpty(osVersion)) {
            return false;
        }
        String[] versionPrefixParts = JavaVersion.split(osVersionPrefix);
        String[] versionParts = JavaVersion.split(osVersion);
        for (int i = 0; i < Math.min(versionPrefixParts.length, versionParts.length); ++i) {
            if (versionPrefixParts[i].equals(versionParts[i])) continue;
            return false;
        }
        return true;
    }
}

