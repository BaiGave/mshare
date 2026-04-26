/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.status;

import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.BiConsumer;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.message.ParameterizedNoReferenceMessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.apache.logging.log4j.status.StatusConsoleListener;
import org.apache.logging.log4j.status.StatusData;
import org.apache.logging.log4j.status.StatusListener;

public class StatusLogger
extends AbstractLogger {
    private static final long serialVersionUID = 2L;
    private static final String DEBUG_PROPERTY_NAME = "log4j2.debug";
    public static final String MAX_STATUS_ENTRIES = "log4j2.status.entries";
    static final int DEFAULT_FALLBACK_LISTENER_BUFFER_CAPACITY = 0;
    public static final String DEFAULT_STATUS_LISTENER_LEVEL = "log4j2.StatusLogger.level";
    static final Level DEFAULT_FALLBACK_LISTENER_LEVEL = Level.ERROR;
    public static final String STATUS_DATE_FORMAT = "log4j2.StatusLogger.dateFormat";
    static final String STATUS_DATE_FORMAT_ZONE = "log4j2.StatusLogger.dateFormatZone";
    public static final String PROPERTIES_FILE_NAME = "log4j2.StatusLogger.properties";
    private final Config config;
    private final StatusConsoleListener fallbackListener;
    private final List<StatusListener> listeners;
    private final transient ReadWriteLock listenerLock = new ReentrantReadWriteLock();
    private final transient Lock listenerReadLock = this.listenerLock.readLock();
    private final transient Lock listenerWriteLock = this.listenerLock.writeLock();
    private final Queue<StatusData> buffer = new ConcurrentLinkedQueue<StatusData>();

    StatusLogger() {
        this(StatusLogger.class.getSimpleName(), ParameterizedNoReferenceMessageFactory.INSTANCE, Config.getInstance(), new StatusConsoleListener(Objects.requireNonNull(Config.getInstance().fallbackListenerLevel), System.err));
    }

    public StatusLogger(String name, MessageFactory messageFactory, Config config, StatusConsoleListener fallbackListener) {
        super(Objects.requireNonNull(name, "name"), Objects.requireNonNull(messageFactory, "messageFactory"));
        this.config = Objects.requireNonNull(config, "config");
        this.fallbackListener = Objects.requireNonNull(fallbackListener, "fallbackListener");
        this.listeners = new ArrayList<StatusListener>();
    }

    public static StatusLogger getLogger() {
        return InstanceHolder.INSTANCE;
    }

    public static void setLogger(StatusLogger logger) {
        InstanceHolder.INSTANCE = Objects.requireNonNull(logger, "logger");
    }

    public StatusConsoleListener getFallbackListener() {
        return this.fallbackListener;
    }

    @Deprecated
    public void setLevel(Level level) {
        Objects.requireNonNull(level, "level");
        this.fallbackListener.setLevel(level);
    }

    public void registerListener(StatusListener listener) {
        Objects.requireNonNull(listener, "listener");
        this.listenerWriteLock.lock();
        try {
            this.listeners.add(listener);
        }
        finally {
            this.listenerWriteLock.unlock();
        }
    }

    public void removeListener(StatusListener listener) {
        Objects.requireNonNull(listener, "listener");
        this.listenerWriteLock.lock();
        try {
            this.listeners.remove(listener);
            StatusLogger.closeListenerSafely(listener);
        }
        finally {
            this.listenerWriteLock.unlock();
        }
    }

    @Deprecated
    public void updateListenerLevel(Level level) {
        Objects.requireNonNull(level, "level");
        this.fallbackListener.setLevel(level);
    }

    public Iterable<StatusListener> getListeners() {
        this.listenerReadLock.lock();
        try {
            Collection<StatusListener> collection = Collections.unmodifiableCollection(this.listeners);
            return collection;
        }
        finally {
            this.listenerReadLock.unlock();
        }
    }

    public void reset() {
        this.listenerWriteLock.lock();
        try {
            Iterator<StatusListener> listenerIterator = this.listeners.iterator();
            while (listenerIterator.hasNext()) {
                StatusListener listener = listenerIterator.next();
                StatusLogger.closeListenerSafely(listener);
                listenerIterator.remove();
            }
        }
        finally {
            this.listenerWriteLock.unlock();
        }
        this.fallbackListener.close();
        this.buffer.clear();
    }

    private static void closeListenerSafely(StatusListener listener) {
        try {
            listener.close();
        }
        catch (IOException error) {
            String message = String.format("failed closing listener: %s", listener);
            RuntimeException extendedError = new RuntimeException(message, error);
            extendedError.printStackTrace(System.err);
        }
    }

    @Deprecated
    public List<StatusData> getStatusData() {
        return Collections.unmodifiableList(new ArrayList<StatusData>(this.buffer));
    }

    @Deprecated
    public void clear() {
        this.buffer.clear();
    }

    @Override
    public Level getLevel() {
        Level leastSpecificLevel = this.fallbackListener.getStatusLevel();
        for (int listenerIndex = 0; listenerIndex < this.listeners.size(); ++listenerIndex) {
            StatusListener listener = this.listeners.get(listenerIndex);
            Level listenerLevel = listener.getStatusLevel();
            if (!listenerLevel.isLessSpecificThan(leastSpecificLevel)) continue;
            leastSpecificLevel = listenerLevel;
        }
        return leastSpecificLevel;
    }

    @Override
    @SuppressFBWarnings(value={"INFORMATION_EXPOSURE_THROUGH_AN_ERROR_MESSAGE"})
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable throwable) {
        try {
            StatusData statusData = this.createStatusData(fqcn, level, message, throwable);
            this.buffer(statusData);
            this.notifyListeners(statusData);
        }
        catch (Exception error) {
            error.printStackTrace(System.err);
        }
    }

    private void buffer(StatusData statusData) {
        if (this.config.bufferCapacity == 0) {
            return;
        }
        this.buffer.add(statusData);
        while (this.buffer.size() >= this.config.bufferCapacity) {
            this.buffer.remove();
        }
    }

    private void notifyListeners(StatusData statusData) {
        boolean foundListeners;
        this.listenerReadLock.lock();
        try {
            foundListeners = !this.listeners.isEmpty();
            this.listeners.forEach(listener -> this.notifyListener((StatusListener)listener, statusData));
        }
        finally {
            this.listenerReadLock.unlock();
        }
        if (!foundListeners) {
            this.notifyListener(this.fallbackListener, statusData);
        }
    }

    private void notifyListener(StatusListener listener, StatusData statusData) {
        boolean levelEnabled = this.isLevelEnabled(listener.getStatusLevel(), statusData.getLevel());
        if (levelEnabled) {
            listener.log(statusData);
        }
    }

    private StatusData createStatusData(@Nullable String fqcn, Level level, Message message, @Nullable Throwable throwable) {
        StackTraceElement caller = StatusLogger.getStackTraceElement(fqcn);
        Instant instant = Instant.now();
        return new StatusData(caller, level, message, throwable, null, this.config.instantFormatter, instant);
    }

    @Nullable
    private static StackTraceElement getStackTraceElement(@Nullable String fqcn) {
        StackTraceElement[] stackTrace;
        if (fqcn == null) {
            return null;
        }
        boolean next = false;
        for (StackTraceElement element : stackTrace = Thread.currentThread().getStackTrace()) {
            String className = element.getClassName();
            if (next && !fqcn.equals(className)) {
                return element;
            }
            if (fqcn.equals(className)) {
                next = true;
                continue;
            }
            if ("?".equals(className)) break;
        }
        return null;
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object ... params) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable throwable) {
        return this.isEnabled(level, marker);
    }

    @Override
    public boolean isEnabled(Level messageLevel, Marker marker) {
        Objects.requireNonNull(messageLevel, "messageLevel");
        Level loggerLevel = this.getLevel();
        return this.isLevelEnabled(loggerLevel, messageLevel);
    }

    private boolean isLevelEnabled(Level filteringLevel, Level messageLevel) {
        return this.config.debugEnabled || filteringLevel.isLessSpecificThan(messageLevel);
    }

    public static final class Config {
        private static final Config INSTANCE = new Config();
        final boolean debugEnabled;
        final int bufferCapacity;
        @Nullable
        final Level fallbackListenerLevel;
        @Nullable
        final DateTimeFormatter instantFormatter;

        public Config(boolean debugEnabled, int bufferCapacity, @Nullable DateTimeFormatter instantFormatter) {
            this.debugEnabled = debugEnabled;
            if (bufferCapacity < 0) {
                throw new IllegalArgumentException("was expecting a positive `bufferCapacity`, found: " + bufferCapacity);
            }
            this.bufferCapacity = bufferCapacity;
            this.fallbackListenerLevel = null;
            this.instantFormatter = instantFormatter;
        }

        private Config() {
            this(PropertiesUtilsDouble.readAllAvailableProperties());
        }

        Config(Properties ... propertiesList) {
            this(PropertiesUtilsDouble.normalizeProperties(propertiesList));
        }

        private Config(Map<String, Object> normalizedProperties) {
            this.debugEnabled = Config.readDebugEnabled(normalizedProperties);
            this.bufferCapacity = Config.readBufferCapacity(normalizedProperties);
            this.fallbackListenerLevel = Config.readFallbackListenerLevel(normalizedProperties);
            this.instantFormatter = Config.readInstantFormatter(normalizedProperties);
        }

        public static Config getInstance() {
            return INSTANCE;
        }

        private static boolean readDebugEnabled(Map<String, Object> normalizedProperties) {
            String debug = PropertiesUtilsDouble.readProperty(normalizedProperties, StatusLogger.DEBUG_PROPERTY_NAME);
            return debug != null && !"false".equalsIgnoreCase(debug);
        }

        private static int readBufferCapacity(Map<String, Object> normalizedProperties) {
            String propertyName = StatusLogger.MAX_STATUS_ENTRIES;
            String capacityString = PropertiesUtilsDouble.readProperty(normalizedProperties, StatusLogger.MAX_STATUS_ENTRIES);
            boolean defaultCapacity = false;
            int effectiveCapacity = 0;
            if (capacityString != null) {
                try {
                    int capacity = Integer.parseInt(capacityString);
                    if (capacity < 0) {
                        String message = String.format("was expecting a positive buffer capacity, found: %d", capacity);
                        throw new IllegalArgumentException(message);
                    }
                    effectiveCapacity = capacity;
                }
                catch (Exception error) {
                    String message = String.format("Failed reading the buffer capacity from the `%s` property: `%s`. Falling back to the default: %d.", StatusLogger.MAX_STATUS_ENTRIES, capacityString, 0);
                    IllegalArgumentException extendedError = new IllegalArgumentException(message, error);
                    extendedError.printStackTrace(System.err);
                }
            }
            return effectiveCapacity;
        }

        private static Level readFallbackListenerLevel(Map<String, Object> normalizedProperties) {
            String propertyName = StatusLogger.DEFAULT_STATUS_LISTENER_LEVEL;
            String level = PropertiesUtilsDouble.readProperty(normalizedProperties, StatusLogger.DEFAULT_STATUS_LISTENER_LEVEL);
            Level defaultLevel = DEFAULT_FALLBACK_LISTENER_LEVEL;
            try {
                return level != null ? Level.valueOf(level) : defaultLevel;
            }
            catch (Exception error) {
                String message = String.format("Failed reading the level from the `%s` property: `%s`. Falling back to the default: `%s`.", StatusLogger.DEFAULT_STATUS_LISTENER_LEVEL, level, defaultLevel);
                IllegalArgumentException extendedError = new IllegalArgumentException(message, error);
                extendedError.printStackTrace(System.err);
                return defaultLevel;
            }
        }

        @Nullable
        private static DateTimeFormatter readInstantFormatter(Map<String, Object> normalizedProperties) {
            ZoneId defaultZoneId;
            DateTimeFormatter formatter;
            String formatPropertyName = StatusLogger.STATUS_DATE_FORMAT;
            String format = PropertiesUtilsDouble.readProperty(normalizedProperties, StatusLogger.STATUS_DATE_FORMAT);
            if (format == null) {
                return null;
            }
            try {
                formatter = DateTimeFormatter.ofPattern(format);
            }
            catch (Exception error) {
                String message = String.format("failed reading the instant format from the `%s` property: `%s`", StatusLogger.STATUS_DATE_FORMAT, format);
                IllegalArgumentException extendedError = new IllegalArgumentException(message, error);
                extendedError.printStackTrace(System.err);
                return null;
            }
            String zonePropertyName = StatusLogger.STATUS_DATE_FORMAT_ZONE;
            String zoneIdString = PropertiesUtilsDouble.readProperty(normalizedProperties, StatusLogger.STATUS_DATE_FORMAT_ZONE);
            ZoneId zoneId = defaultZoneId = ZoneId.systemDefault();
            if (zoneIdString != null) {
                try {
                    zoneId = ZoneId.of(zoneIdString);
                }
                catch (Exception error) {
                    String message = String.format("Failed reading the instant formatting zone ID from the `%s` property: `%s`. Falling back to the default: `%s`.", StatusLogger.STATUS_DATE_FORMAT_ZONE, zoneIdString, defaultZoneId);
                    IllegalArgumentException extendedError = new IllegalArgumentException(message, error);
                    extendedError.printStackTrace(System.err);
                }
            }
            return formatter.withZone(zoneId);
        }
    }

    private static final class InstanceHolder {
        private static volatile StatusLogger INSTANCE = new StatusLogger();

        private InstanceHolder() {
        }
    }

    static final class PropertiesUtilsDouble {
        PropertiesUtilsDouble() {
        }

        @Nullable
        static String readProperty(Map<String, Object> normalizedProperties, String propertyName) {
            String normalizedPropertyName = PropertiesUtilsDouble.normalizePropertyName(propertyName);
            Object value = normalizedProperties.get(normalizedPropertyName);
            return value instanceof String ? (String)value : null;
        }

        static Map<String, Object> readAllAvailableProperties() {
            Properties systemProperties = System.getProperties();
            Properties environmentProperties = PropertiesUtilsDouble.readEnvironmentProperties();
            Properties fileProvidedProperties = PropertiesUtilsDouble.readPropertiesFile(StatusLogger.PROPERTIES_FILE_NAME);
            return PropertiesUtilsDouble.normalizeProperties(systemProperties, environmentProperties, fileProvidedProperties);
        }

        private static Properties readEnvironmentProperties() {
            Properties properties = new Properties();
            properties.putAll(System.getenv());
            return properties;
        }

        static Properties readPropertiesFile(String propertiesFileName) {
            Properties properties = new Properties();
            String resourceName = '/' + propertiesFileName;
            URL url = StatusLogger.class.getResource(resourceName);
            if (url == null) {
                return properties;
            }
            try (InputStream stream = url.openStream();){
                properties.load(stream);
            }
            catch (IOException error) {
                String message = String.format("failed reading properties from `%s`", propertiesFileName);
                RuntimeException extendedError = new RuntimeException(message, error);
                extendedError.printStackTrace(System.err);
            }
            return properties;
        }

        private static Map<String, Object> normalizeProperties(Properties ... propertiesList) {
            HashMap<String, Object> map = new HashMap<String, Object>();
            for (Properties properties : propertiesList) {
                properties.forEach((BiConsumer<? super Object, ? super Object>)((BiConsumer<Object, Object>)(name, value) -> {
                    boolean relevant = PropertiesUtilsDouble.isRelevantPropertyName(name);
                    if (relevant) {
                        String normalizedName = PropertiesUtilsDouble.normalizePropertyName((String)name);
                        map.put(normalizedName, value);
                    }
                }));
            }
            return map;
        }

        private static boolean isRelevantPropertyName(@Nullable Object propertyName) {
            return propertyName instanceof String && ((String)propertyName).matches("^(?i)log4j.*");
        }

        private static String normalizePropertyName(String propertyName) {
            return propertyName.replaceAll("[._-]", "").replaceAll("\\P{InBasic_Latin}", ".").toLowerCase(Locale.US).replaceAll("^log4j2", "log4j");
        }
    }
}

