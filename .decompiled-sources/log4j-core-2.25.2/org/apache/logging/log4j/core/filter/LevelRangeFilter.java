/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.filter;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.PerformanceSensitive;

@Plugin(name="LevelRangeFilter", category="Core", elementType="filter", printObject=true)
@PerformanceSensitive(value={"allocation"})
public final class LevelRangeFilter
extends AbstractFilter {
    public static final Level DEFAULT_MIN_LEVEL = Level.OFF;
    public static final Level DEFAULT_MAX_LEVEL = Level.ALL;
    public static final Filter.Result DEFAULT_ON_MATCH = Filter.Result.NEUTRAL;
    public static final Filter.Result DEFAULT_ON_MISMATCH = Filter.Result.DENY;
    private final Level maxLevel;
    private final Level minLevel;

    @PluginFactory
    public static LevelRangeFilter createFilter(@PluginAttribute(value="minLevel") Level minLevel, @PluginAttribute(value="maxLevel") Level maxLevel, @PluginAttribute(value="onMatch") Filter.Result onMatch, @PluginAttribute(value="onMismatch") Filter.Result onMismatch) {
        Level effectiveMinLevel = minLevel == null ? DEFAULT_MIN_LEVEL : minLevel;
        Level effectiveMaxLevel = maxLevel == null ? DEFAULT_MAX_LEVEL : maxLevel;
        Filter.Result effectiveOnMatch = onMatch == null ? DEFAULT_ON_MATCH : onMatch;
        Filter.Result effectiveOnMismatch = onMismatch == null ? DEFAULT_ON_MISMATCH : onMismatch;
        return new LevelRangeFilter(effectiveMinLevel, effectiveMaxLevel, effectiveOnMatch, effectiveOnMismatch);
    }

    private LevelRangeFilter(Level minLevel, Level maxLevel, Filter.Result onMatch, Filter.Result onMismatch) {
        super(onMatch, onMismatch);
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
    }

    private Filter.Result filter(Level level) {
        return level.isInRange(this.minLevel, this.maxLevel) ? this.onMatch : this.onMismatch;
    }

    @Override
    public Filter.Result filter(LogEvent event) {
        return this.filter(event.getLevel());
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object ... params) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return this.filter(level);
    }

    @Override
    public Filter.Result filter(Logger logger, Level level, Marker marker, String msg, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return this.filter(level);
    }

    public Level getMinLevel() {
        return this.minLevel;
    }

    public Level getMaxLevel() {
        return this.maxLevel;
    }

    @Override
    public String toString() {
        return String.format("[%s,%s]", this.minLevel, this.maxLevel);
    }
}

