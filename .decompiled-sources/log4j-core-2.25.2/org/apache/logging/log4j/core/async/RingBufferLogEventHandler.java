/*
 * Decompiled with CFR 0.152.
 */
package org.apache.logging.log4j.core.async;

import com.lmax.disruptor.LifecycleAware;
import com.lmax.disruptor.SequenceReportingEventHandler;
import org.apache.logging.log4j.core.async.RingBufferLogEvent;
import org.apache.logging.log4j.core.async.RingBufferLogEventHandler4;

@Deprecated
public class RingBufferLogEventHandler
extends RingBufferLogEventHandler4
implements SequenceReportingEventHandler<RingBufferLogEvent>,
LifecycleAware {
}

