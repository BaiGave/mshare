/*
 * Decompiled with CFR 0.152.
 */
package oshi.hardware.platform.mac;

import com.sun.jna.Native;
import com.sun.jna.platform.mac.SystemB;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.annotation.concurrent.ThreadSafe;
import oshi.hardware.PhysicalMemory;
import oshi.hardware.VirtualMemory;
import oshi.hardware.common.AbstractGlobalMemory;
import oshi.hardware.platform.mac.MacVirtualMemory;
import oshi.jna.ByRef;
import oshi.jna.Struct;
import oshi.util.ExecutingCommand;
import oshi.util.Memoizer;
import oshi.util.ParseUtil;
import oshi.util.platform.mac.SysctlUtil;

@ThreadSafe
final class MacGlobalMemory
extends AbstractGlobalMemory {
    private static final Logger LOG = LoggerFactory.getLogger(MacGlobalMemory.class);
    private final Supplier<Long> available = Memoizer.memoize(this::queryVmStats, Memoizer.defaultExpiration());
    private final Supplier<Long> total = Memoizer.memoize(MacGlobalMemory::queryPhysMem);
    private final Supplier<Long> pageSize = Memoizer.memoize(MacGlobalMemory::queryPageSize);
    private final Supplier<VirtualMemory> vm = Memoizer.memoize(this::createVirtualMemory);

    MacGlobalMemory() {
    }

    @Override
    public long getAvailable() {
        return this.available.get();
    }

    @Override
    public long getTotal() {
        return this.total.get();
    }

    @Override
    public long getPageSize() {
        return this.pageSize.get();
    }

    @Override
    public VirtualMemory getVirtualMemory() {
        return this.vm.get();
    }

    @Override
    public List<PhysicalMemory> getPhysicalMemory() {
        ArrayList<PhysicalMemory> pmList = new ArrayList<PhysicalMemory>();
        List<String> sp = ExecutingCommand.runNative("system_profiler SPMemoryDataType");
        int bank = 0;
        String bankLabel = "unknown";
        long capacity = 0L;
        long speed = 0L;
        String manufacturer = "unknown";
        String memoryType = "unknown";
        String partNumber = "unknown";
        String serialNumber = "unknown";
        for (String line : sp) {
            String[] split;
            if (line.trim().startsWith("BANK")) {
                int colon;
                if (bank++ > 0) {
                    pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, "unknown", serialNumber));
                }
                if ((colon = (bankLabel = line.trim()).lastIndexOf(58)) <= 0) continue;
                bankLabel = bankLabel.substring(0, colon - 1);
                continue;
            }
            if (bank <= 0 || (split = line.trim().split(":")).length != 2) continue;
            switch (split[0]) {
                case "Size": {
                    capacity = ParseUtil.parseDecimalMemorySizeToBinary(split[1].trim());
                    break;
                }
                case "Type": {
                    memoryType = split[1].trim();
                    break;
                }
                case "Speed": {
                    speed = ParseUtil.parseHertz(split[1]);
                    break;
                }
                case "Manufacturer": {
                    manufacturer = split[1].trim();
                    break;
                }
                case "Part Number": {
                    partNumber = split[1].trim();
                    break;
                }
                case "Serial Number": {
                    serialNumber = split[1].trim();
                    break;
                }
            }
        }
        pmList.add(new PhysicalMemory(bankLabel, capacity, speed, manufacturer, memoryType, partNumber, serialNumber));
        return pmList;
    }

    private long queryVmStats() {
        try (Struct.CloseableVMStatistics vmStats = new Struct.CloseableVMStatistics();){
            ByRef.CloseableIntByReference size;
            block12: {
                size = new ByRef.CloseableIntByReference(vmStats.size() / SystemB.INT_SIZE);
                try {
                    if (0 == SystemB.INSTANCE.host_statistics(SystemB.INSTANCE.mach_host_self(), 2, vmStats, size)) break block12;
                    LOG.error("Failed to get host VM info. Error code: {}", (Object)Native.getLastError());
                    long l = 0L;
                    size.close();
                    return l;
                }
                catch (Throwable throwable) {
                    try {
                        size.close();
                    }
                    catch (Throwable throwable2) {
                        throwable.addSuppressed(throwable2);
                    }
                    throw throwable;
                }
            }
            long l = (long)(vmStats.free_count + vmStats.inactive_count) * this.getPageSize();
            size.close();
            return l;
        }
    }

    private static long queryPhysMem() {
        return SysctlUtil.sysctl("hw.memsize", 0L);
    }

    private static long queryPageSize() {
        try (ByRef.CloseableLongByReference pPageSize = new ByRef.CloseableLongByReference();){
            if (0 == SystemB.INSTANCE.host_page_size(SystemB.INSTANCE.mach_host_self(), pPageSize)) {
                long l = pPageSize.getValue();
                return l;
            }
        }
        LOG.error("Failed to get host page size. Error code: {}", (Object)Native.getLastError());
        return 4098L;
    }

    private VirtualMemory createVirtualMemory() {
        return new MacVirtualMemory(this);
    }
}

