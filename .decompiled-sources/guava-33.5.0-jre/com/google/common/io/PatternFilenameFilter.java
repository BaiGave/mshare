/*
 * Decompiled with CFR 0.152.
 */
package com.google.common.io;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.annotations.J2ktIncompatible;
import com.google.common.base.Preconditions;
import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

@J2ktIncompatible
@GwtIncompatible
public final class PatternFilenameFilter
implements FilenameFilter {
    private final Pattern pattern;

    public PatternFilenameFilter(String patternStr) {
        this(Pattern.compile(patternStr));
    }

    public PatternFilenameFilter(Pattern pattern) {
        this.pattern = Preconditions.checkNotNull(pattern);
    }

    @Override
    public boolean accept(File dir, String fileName) {
        return this.pattern.matcher(fileName).matches();
    }
}

