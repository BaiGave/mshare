/*
 * Decompiled with CFR 0.152.
 */
package io.netty.buffer.svm;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.RecomputeFieldValue;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(className="io.netty.buffer.AdaptivePoolingAllocator$Chunk")
final class AdaptivePoolingAllocatorSubstitution {
    @Alias
    @RecomputeFieldValue(kind=RecomputeFieldValue.Kind.FieldOffset, declClassName="io.netty.buffer.AdaptivePoolingAllocator$Chunk", name="refCnt")
    public static long REFCNT_FIELD_OFFSET;

    private AdaptivePoolingAllocatorSubstitution() {
    }
}

