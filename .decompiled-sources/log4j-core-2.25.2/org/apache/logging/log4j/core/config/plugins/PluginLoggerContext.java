/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.config.plugins;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apache.logging.log4j.core.config.plugins.PluginVisitorStrategy;
import org.apache.logging.log4j.core.config.plugins.visitors.PluginLoggerContextVisitor;

@Documented
@Retention(value=RetentionPolicy.RUNTIME)
@Target(value={ElementType.PARAMETER, ElementType.FIELD})
@PluginVisitorStrategy(value=PluginLoggerContextVisitor.class)
public @interface PluginLoggerContext {
}

