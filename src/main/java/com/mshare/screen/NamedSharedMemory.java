package com.mshare.screen;

import com.sun.jna.Pointer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Windows Named Shared Memory for screen capture.
 * Uses Global\ prefix for cross-session access.
 */
public final class NamedSharedMemory extends BaseSharedMemory {
    private static final Logger LOGGER = LoggerFactory.getLogger(NamedSharedMemory.class);

    private static final String MAPPING_NAME = "Global\\MinecraftScreenCapture";

    private static final int DEFAULT_SIZE = 3840 * 2160 * 4 + 64; // Max resolution + header

    private NamedSharedMemory() {
        super();
    }

    /**
     * Create shared memory server for screen capture.
     */
    public static NamedSharedMemory createServer(long size) {
        NamedSharedMemory nsm = new NamedSharedMemory();
        nsm.mappedSize = size;
        if (!nsm.createServer()) {
            throw new RuntimeException("Failed to create screen shared memory");
        }
        return nsm;
    }

    /**
     * Connect as client to read screen data.
     */
    public static NamedSharedMemory connectClient() {
        NamedSharedMemory nsm = new NamedSharedMemory();
        if (!nsm.doConnect()) {
            return null;
        }
        return nsm;
    }

    @Override
    protected String getMappingName() {
        return MAPPING_NAME;
    }

    @Override
    protected long getSize() {
        return mappedSize > 0 ? mappedSize : DEFAULT_SIZE;
    }

    public ByteBufferWrapper getByteBuffer(long offset, long size) {
        return new ByteBufferWrapper(getPointer(), offset, size);
    }

    public static class ByteBufferWrapper {
        private final Pointer pointer;
        private final long offset;
        private final long size;

        public ByteBufferWrapper(Pointer pointer, long offset, long size) {
            this.pointer = pointer;
            this.offset = offset;
            this.size = size;
        }

        public java.nio.ByteBuffer getByteBuffer() {
            return pointer.getByteBuffer(offset, size);
        }
    }
}
