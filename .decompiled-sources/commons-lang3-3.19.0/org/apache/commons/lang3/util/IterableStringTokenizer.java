/*
 * Decompiled with CFR 0.152.
 */
package org.apache.commons.lang3.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import org.apache.commons.lang3.ArrayUtils;

public class IterableStringTokenizer
extends StringTokenizer
implements Iterable<String> {
    public IterableStringTokenizer(String str) {
        super(str);
    }

    public IterableStringTokenizer(String str, String delim) {
        super(str, delim);
    }

    public IterableStringTokenizer(String str, String delim, boolean returnDelims) {
        super(str, delim, returnDelims);
    }

    @Override
    public Iterator<String> iterator() {
        return new Iterator<String>(){

            @Override
            public boolean hasNext() {
                return IterableStringTokenizer.this.hasMoreElements();
            }

            @Override
            public String next() {
                return Objects.toString(IterableStringTokenizer.this.nextElement(), null);
            }
        };
    }

    public String[] toArray() {
        return this.toList().toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public List<String> toList() {
        ArrayList<String> list = new ArrayList<String>();
        this.forEach(list::add);
        return list;
    }

    public Stream<String> toStream() {
        return StreamSupport.stream(this.spliterator(), false);
    }
}

