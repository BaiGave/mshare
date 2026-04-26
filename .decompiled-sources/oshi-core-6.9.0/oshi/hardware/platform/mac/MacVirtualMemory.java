/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.common.AbstractVirtualMemory;
import oshi.hardware.platform.mac.MacGlobalMemory;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtil;
import oshi.util.tuples.Pair;

@ThreadSafe
final class MacVirtualMemory
extends AbstractVirtualMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacVirtualMemory.class);
    private final MacGlobalMemory global;
    private final Supplier<Pair<Long, Long>> usedTotal = Memoizer.memoize(MacVirtualMemory::querySwapUsage, Memoizer.defaultExpiration());
    private final Supplier<Pair<Long, Long>> inOut = Memoizer.memoize(MacVirtualMemory::queryVmStat, Memoizer.defaultExpiration());

    MacVirtualMemory(MacGlobalMemory macGlobalMemory) {
        this.global = macGlobalMemory;
    }

    @Override
    public long getSwapUsed() {
        return this.usedTotal.get().getA();
    }

    @Override
    public long getSwapTotal() {
        return this.usedTotal.get().getB();
    }

    @Override
    public long getVirtualMax() {
        return this.global.getTotal() + this.getSwapTotal();
    }

    @Override
    public long getVirtualInUse() {
        return this.global.getTotal() - this.global.getAvailable() + this.getSwapUsed();
    }

    @Override
    public long getSwapPagesIn() {
        return this.inOut.get().getA();
    }

    @Override
    public long getSwapPagesOut() {
        return this.inOut.get().getB();
    }

    private static Pair<Long, Long> querySwapUsage() {
        long swapUsed = 0L;
        long swapTotal = 0L;
        try (Struct.CloseableXswUsage xswUsage = new Struct.CloseableXswUsage();){
            if (SysctlUtil.sysctl("vm.swapusage", xswUsage)) {
                swapUsed = xswUsage.xsu_used;
                swapTotal = xswUsage.xsu_total;
            }
        }
        return new Pair<Long, Long>(swapUsed, swapTotal);
    }

    private static Pair<Long, Long> queryVmStat() {
        long swapPagesIn = 0L;
        long swapPagesOut = 0L;
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();
             ByRef.CloseableIntByReference size = new ByRef.CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE);){
            if (0 == SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), 2, vmStats, size)) {
                swapPagesIn = ParseUtil.unsignedIntToLong(vmStats.pageins);
                swapPagesOut = ParseUtil.unsignedIntToLong(vmStats.pageouts);
            } else {
                LOG.error("Failed to get host VM info. Error code: {}", (Object)Native.getLastError());
            }
        }
        return new Pair<Long, Long>(swapPagesIn, swapPagesOut);
    }
}

