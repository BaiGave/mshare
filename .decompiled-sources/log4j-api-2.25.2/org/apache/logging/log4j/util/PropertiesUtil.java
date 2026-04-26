/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util;

import aQute.bnd.annotation.spi.ServiceConsumer;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.status.StatusLogger;
import org.apache.logging.log4j.util.Lazy;
import org.apache.logging.log4j.util.PropertiesPropertySource;
import org.apache.logging.log4j.util.PropertyFilePropertySource;
import org.apache.logging.log4j.util.PropertySource;
import org.apache.logging.log4j.util.ServiceLoaderUtil;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.util.SystemPropertiesPropertySource;

@ServiceConsumer(value=PropertySource.class, resolution="optional", cardinality="multiple")
public final class PropertiesUtil {
    private static final Logger LOGGER = StatusLogger.getLogger();
    private static final String LOG4J_PROPERTIES_FILE_NAME = "log4j2.component.properties";
    private static final String LOG4J_SYSTEM_PROPERTIES_FILE_NAME = "log4j2.system.properties";
    private static final Lazy<PropertiesUtil> COMPONENT_PROPERTIES = Lazy.lazy(() -> new PropertiesUtil(LOG4J_PROPERTIES_FILE_NAME, false));
    private static final Pattern DURATION_PATTERN = Pattern.compile("([+-]?\\d+)\\s*(\\w+)?", 2);
    private final Environment environment;

    public PropertiesUtil(Properties props) {
        this(new PropertiesPropertySource(props));
    }

    public PropertiesUtil(String propertiesFileName) {
        this(propertiesFileName, true);
    }

    private PropertiesUtil(String propertiesFileName, boolean useTccl) {
        this(new PropertyFilePropertySource(propertiesFileName, useTccl));
    }

    PropertiesUtil(PropertySource source) {
        this.environment = new Environment(source);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    static Properties loadClose(InputStream in, Object source) {
        Properties props = new Properties();
        if (null != in) {
            try {
                props.load(in);
            }
            catch (IOException error) {
                LOGGER.error("Unable to read source `{}`", source, (Object)error);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException error) {
                    LOGGER.error("Unable to close source `{}`", source, (Object)error);
                }
            }
        }
        return props;
    }

    public static PropertiesUtil getProperties() {
        return COMPONENT_PROPERTIES.get();
    }

    public void addPropertySource(PropertySource propertySource) {
        this.environment.addPropertySource(Objects.requireNonNull(propertySource));
    }

    public void removePropertySource(PropertySource propertySource) {
        this.environment.removePropertySource(Objects.requireNonNull(propertySource));
    }

    public boolean hasProperty(String name) {
        return this.environment.containsKey(name);
    }

    public boolean getBooleanProperty(String name) {
        return this.getBooleanProperty(name, false);
    }

    public boolean getBooleanProperty(String name, boolean defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : "true".equalsIgnoreCase(prop);
    }

    public boolean getBooleanProperty(String name, boolean defaultValueIfAbsent, boolean defaultValueIfPresent) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValueIfAbsent : (prop.isEmpty() ? defaultValueIfPresent : "true".equalsIgnoreCase(prop));
    }

    public Boolean getBooleanProperty(String[] prefixes, String key, Supplier<Boolean> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getBooleanProperty(prefix + key);
        }
        return supplier != null ? supplier.get() : null;
    }

    public Charset getCharsetProperty(String name) {
        return this.getCharsetProperty(name, Charset.defaultCharset());
    }

    public Charset getCharsetProperty(String name, Charset defaultValue) {
        String mapped;
        String charsetName = this.getStringProperty(name);
        if (charsetName == null) {
            return defaultValue;
        }
        if (Charset.isSupported(charsetName)) {
            return Charset.forName(charsetName);
        }
        ResourceBundle bundle = PropertiesUtil.getCharsetsResourceBundle();
        if (bundle.containsKey(name) && Charset.isSupported(mapped = bundle.getString(name))) {
            return Charset.forName(mapped);
        }
        LOGGER.warn("Unable to read charset `{}` from property `{}`. Falling back to the default: `{}`", (Object)charsetName, (Object)name, (Object)defaultValue);
        return defaultValue;
    }

    public double getDoubleProperty(String name, double defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Double.parseDouble(prop);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Unable to read double `{}` from property `{}`. Falling back to the default: `{}`", (Object)prop, (Object)name, (Object)defaultValue, (Object)e);
            }
        }
        return defaultValue;
    }

    public int getIntegerProperty(String name, int defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Integer.parseInt(prop.trim());
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Unable to read int `{}` from property `{}`. Falling back to the default: `{}`", (Object)prop, (Object)name, (Object)defaultValue, (Object)e);
            }
        }
        return defaultValue;
    }

    public Integer getIntegerProperty(String[] prefixes, String key, Supplier<Integer> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getIntegerProperty(prefix + key, 0);
        }
        return supplier != null ? supplier.get() : null;
    }

    public long getLongProperty(String name, long defaultValue) {
        String prop = this.getStringProperty(name);
        if (prop != null) {
            try {
                return Long.parseLong(prop);
            }
            catch (NumberFormatException e) {
                LOGGER.warn("Unable to read long `{}` from property `{}`. Falling back to the default: `{}`", (Object)prop, (Object)name, (Object)defaultValue, (Object)e);
            }
        }
        return defaultValue;
    }

    public Long getLongProperty(String[] prefixes, String key, Supplier<Long> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getLongProperty(prefix + key, 0L);
        }
        return supplier != null ? supplier.get() : null;
    }

    public Duration getDurationProperty(String name, Duration defaultValue) {
        String prop = this.getStringProperty(name);
        try {
            return PropertiesUtil.parseDuration(prop);
        }
        catch (IllegalArgumentException e) {
            LOGGER.warn("Unable to read duration `{}` from property `{}`.\nExpected format 'n unit', where 'n' is an integer and 'unit' is one of: {}.", (Object)prop, (Object)name, (Object)TimeUnit.getValidUnits().collect(Collectors.joining(", ")), (Object)e);
            return defaultValue;
        }
    }

    public Duration getDurationProperty(String[] prefixes, String key, Supplier<Duration> supplier) {
        for (String prefix : prefixes) {
            if (!this.hasProperty(prefix + key)) continue;
            return this.getDurationProperty(prefix + key, null);
        }
        return supplier != null ? supplier.get() : null;
    }

    public String getStringProperty(String[] prefixes, String key, Supplier<String> supplier) {
        for (String prefix : prefixes) {
            String result = this.getStringProperty(prefix + key);
            if (result == null) continue;
            return result;
        }
        return supplier != null ? supplier.get() : null;
    }

    public String getStringProperty(String name) {
        return this.environment.get(name);
    }

    public String getStringProperty(String name, String defaultValue) {
        String prop = this.getStringProperty(name);
        return prop == null ? defaultValue : prop;
    }

    public static Properties getSystemProperties() {
        try {
            return new Properties(System.getProperties());
        }
        catch (SecurityException error) {
            LOGGER.error("Unable to access system properties.", (Throwable)error);
            return new Properties();
        }
    }

    @Deprecated
    public void reload() {
    }

    public static Properties extractSubset(Properties properties, String prefix) {
        Properties subset = new Properties();
        if (prefix == null || prefix.isEmpty()) {
            return subset;
        }
        String prefixToMatch = prefix.charAt(prefix.length() - 1) != '.' ? prefix + '.' : prefix;
        ArrayList<String> keys = new ArrayList<String>();
        for (String key : properties.stringPropertyNames()) {
            if (!key.startsWith(prefixToMatch)) continue;
            subset.setProperty(key.substring(prefixToMatch.length()), properties.getProperty(key));
            keys.add(key);
        }
        for (String key : keys) {
            properties.remove(key);
        }
        return subset;
    }

    static ResourceBundle getCharsetsResourceBundle() {
        return ResourceBundle.getBundle("Log4j-charsets");
    }

    public static Map<String, Properties> partitionOnCommonPrefixes(Properties properties) {
        return PropertiesUtil.partitionOnCommonPrefixes(properties, false);
    }

    public static Map<String, Properties> partitionOnCommonPrefixes(Properties properties, boolean includeBaseKey) {
        ConcurrentHashMap<String, Properties> parts = new ConcurrentHashMap<String, Properties>();
        for (String key : properties.stringPropertyNames()) {
            int idx = key.indexOf(46);
            if (idx < 0) {
                if (!includeBaseKey) continue;
                if (!parts.containsKey(key)) {
                    parts.put(key, new Properties());
                }
                ((Properties)parts.get(key)).setProperty("", properties.getProperty(key));
                continue;
            }
            String prefix = key.substring(0, idx);
            if (!parts.containsKey(prefix)) {
                parts.put(prefix, new Properties());
            }
            ((Properties)parts.get(prefix)).setProperty(key.substring(idx + 1), properties.getProperty(key));
        }
        return parts;
    }

    public boolean isOsWindows() {
        return SystemPropertiesPropertySource.getSystemProperty("os.name", "").startsWith("Windows");
    }

    static Duration parseDuration(CharSequence value) {
        Matcher matcher = DURATION_PATTERN.matcher(value);
        if (matcher.matches()) {
            return Duration.of(PropertiesUtil.parseDurationAmount(matcher.group(1)), TimeUnit.parseUnit(matcher.group(2)));
        }
        throw new IllegalArgumentException("Invalid duration value '" + value + "'.");
    }

    private static long parseDurationAmount(String amount) {
        try {
            return Long.parseLong(amount);
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid duration amount '" + amount + "'", e);
        }
    }

    private static final class Environment {
        private final Set<PropertySource> sources = ConcurrentHashMap.newKeySet();
        private final ThreadLocal<PropertySource> CURRENT_PROPERTY_SOURCE = new ThreadLocal();

        private Environment(PropertySource propertySource) {
            PropertyFilePropertySource sysProps = new PropertyFilePropertySource(PropertiesUtil.LOG4J_SYSTEM_PROPERTIES_FILE_NAME, false);
            try {
                sysProps.forEach((key, value) -> {
                    if (System.getProperty(key) == null) {
                        System.setProperty(key, value);
                    }
                });
            }
            catch (SecurityException e) {
                LOGGER.warn("Unable to set Java system properties from {} file, due to security restrictions.", (Object)PropertiesUtil.LOG4J_SYSTEM_PROPERTIES_FILE_NAME, (Object)e);
            }
            this.sources.add(propertySource);
            ServiceLoaderUtil.safeStream(PropertySource.class, ServiceLoader.load(PropertySource.class, PropertiesUtil.class.getClassLoader()), LOGGER).forEach(this.sources::add);
        }

        private void addPropertySource(PropertySource propertySource) {
            this.sources.add(propertySource);
        }

        private void removePropertySource(PropertySource propertySource) {
            this.sources.remove(propertySource);
        }

        private String get(String key) {
            List<CharSequence> tokens = PropertySource.Util.tokenize(key);
            return this.sources.stream().sorted(PropertySource.Comparator.INSTANCE).map(source -> {
                String normalKey;
                if (!tokens.isEmpty() && (normalKey = Objects.toString(source.getNormalForm(tokens), null)) != null && this.sourceContainsProperty((PropertySource)source, normalKey)) {
                    return this.sourceGetProperty((PropertySource)source, normalKey);
                }
                return this.sourceGetProperty((PropertySource)source, key);
            }).filter(Objects::nonNull).findFirst().orElse(null);
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private boolean sourceContainsProperty(PropertySource source, String key) {
            PropertySource recursiveSource = this.CURRENT_PROPERTY_SOURCE.get();
            if (recursiveSource == null) {
                this.CURRENT_PROPERTY_SOURCE.set(source);
                try {
                    boolean bl = source.containsProperty(key);
                    return bl;
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to retrieve Log4j property {} from property source {}.", (Object)key, (Object)source, (Object)e);
                }
                finally {
                    this.CURRENT_PROPERTY_SOURCE.remove();
                }
            }
            LOGGER.warn("Recursive call to `containsProperty()` from property source {}.", (Object)recursiveSource);
            return false;
        }

        /*
         * WARNING - Removed try catching itself - possible behaviour change.
         */
        private String sourceGetProperty(PropertySource source, String key) {
            PropertySource recursiveSource = this.CURRENT_PROPERTY_SOURCE.get();
            if (recursiveSource == null) {
                this.CURRENT_PROPERTY_SOURCE.set(source);
                try {
                    String string = source.getProperty(key);
                    return string;
                }
                catch (Exception e) {
                    LOGGER.warn("Failed to retrieve Log4j property {} from property source {}.", (Object)key, (Object)source, (Object)e);
                }
                finally {
                    this.CURRENT_PROPERTY_SOURCE.remove();
                }
            }
            LOGGER.warn("Recursive call to `getProperty()` from property source {}.", (Object)recursiveSource);
            return null;
        }

        private boolean containsKey(String key) {
            List<CharSequence> tokens = PropertySource.Util.tokenize(key);
            return this.sources.stream().anyMatch(s -> {
                CharSequence normalizedKey = tokens.isEmpty() ? null : s.getNormalForm(tokens);
                return this.sourceContainsProperty((PropertySource)s, key) || normalizedKey != null && this.sourceContainsProperty((PropertySource)s, normalizedKey.toString());
            });
        }
    }

    private static enum TimeUnit {
        NANOS(new String[]{"ns", "nano", "nanos", "nanosecond", "nanoseconds"}, ChronoUnit.NANOS),
        MICROS(new String[]{"us", "micro", "micros", "microsecond", "microseconds"}, ChronoUnit.MICROS),
        MILLIS(new String[]{"ms", "milli", "millis", "millisecond", "milliseconds"}, ChronoUnit.MILLIS),
        SECONDS(new String[]{"s", "second", "seconds"}, ChronoUnit.SECONDS),
        MINUTES(new String[]{"m", "minute", "minutes"}, ChronoUnit.MINUTES),
        HOURS(new String[]{"h", "hour", "hours"}, ChronoUnit.HOURS),
        DAYS(new String[]{"d", "day", "days"}, ChronoUnit.DAYS);

        private final String[] descriptions;
        private final TemporalUnit timeUnit;

        private TimeUnit(String[] descriptions, TemporalUnit timeUnit) {
            this.descriptions = descriptions;
            this.timeUnit = timeUnit;
        }

        private static Stream<String> getValidUnits() {
            return Arrays.stream(TimeUnit.values()).flatMap(unit -> Arrays.stream(unit.descriptions));
        }

        private static TemporalUnit parseUnit(String unit) {
            if (unit != null) {
                for (TimeUnit value : TimeUnit.values()) {
                    for (String description : value.descriptions) {
                        if (!unit.equals(description)) continue;
                        return value.timeUnit;
                    }
                }
                throw new IllegalArgumentException("Invalid duration unit '" + unit + "'");
            }
            return ChronoUnit.MILLIS;
        }
    }
}

