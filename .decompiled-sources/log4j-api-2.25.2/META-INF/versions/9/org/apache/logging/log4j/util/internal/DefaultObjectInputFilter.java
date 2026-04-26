/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.util.internal;

import java.io.ObjectInputFilter;
import org.apache.logging.log4j.util.internal.SerializationUtil;

public class DefaultObjectInputFilter
implements ObjectInputFilter {
    private final ObjectInputFilter delegate;

    public DefaultObjectInputFilter() {
        this.delegate = null;
    }

    public DefaultObjectInputFilter(ObjectInputFilter filter) {
        this.delegate = filter;
    }

    public static DefaultObjectInputFilter newInstance(ObjectInputFilter filter) {
        return new DefaultObjectInputFilter(filter);
    }

    @Override
    public ObjectInputFilter.Status checkInput(ObjectInputFilter.FilterInfo filterInfo) {
        ObjectInputFilter.Status status;
        if (this.delegate != null && (status = this.delegate.checkInput(filterInfo)) != ObjectInputFilter.Status.UNDECIDED) {
            return status;
        }
        ObjectInputFilter serialFilter = ObjectInputFilter.Config.getSerialFilter();
        if (serialFilter != null && (status = serialFilter.checkInput(filterInfo)) != ObjectInputFilter.Status.UNDECIDED) {
            return status;
        }
        Class<?> serialClass = filterInfo.serialClass();
        if (serialClass != null) {
            String name = SerializationUtil.stripArray(serialClass);
            if (DefaultObjectInputFilter.isAllowedByDefault(name)) {
                return ObjectInputFilter.Status.ALLOWED;
            }
        } else {
            return ObjectInputFilter.Status.ALLOWED;
        }
        return ObjectInputFilter.Status.REJECTED;
    }

    private static boolean isAllowedByDefault(String name) {
        return DefaultObjectInputFilter.isRequiredPackage(name) || SerializationUtil.REQUIRED_JAVA_CLASSES.contains(name);
    }

    private static boolean isRequiredPackage(String name) {
        for (String packageName : SerializationUtil.REQUIRED_JAVA_PACKAGES) {
            if (!name.startsWith(packageName)) continue;
            return true;
        }
        return false;
    }
}

