package com.mshare.screen;

import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Mesh capture manager that collects mesh data from RenderType.draw() calls
 * and writes it to shared memory for the web viewer.
 * 
 * This class runs on the render thread and collects mesh data
 * each frame, then transfers it to shared memory for the Java reader process.
 */
public class MeshCaptureManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(MeshCaptureManager.class);
    
    // Shared memory configuration
    private static final String MAPPING_NAME = NamedSharedMemory.MESH_MAPPING_NAME;
    private static final long SHARED_MEMORY_SIZE = 100 * 1024 * 1024; // 100 MB
    private static final int HEADER_SIZE = MeshDataHeader.HEADER_SIZE;
    
    // Frame configuration
    private static final int MAX_MESHES_PER_FRAME = 1000;
    private static final int MAX_VERTICES_PER_FRAME = 500000;
    
    // Shared memory handles (JNA)
    private com.sun.jna.Pointer fileMapping;
    private com.sun.jna.Pointer mappedView;
    private boolean isInitialized = false;
    
    // Current frame data collection
    private final List<CapturedMesh> currentFrameMeshes = new ArrayList<>();
    private ByteBuffer vertexBuffer;
    private int vertexCount = 0;
    private int allocatedVertexCapacity = 0;
    
    // Frame tracking
    private final AtomicLong frameNumber = new AtomicLong(0);
    
    // Enable/disable flag
    private volatile boolean enabled = false;
    
    // Frame state
    private volatile boolean frameStarted = false;

    // Camera position for world coordinates
    private double cameraX = 0, cameraY = 0, cameraZ = 0;

    // Singleton instance
    private static final MeshCaptureManager INSTANCE = new MeshCaptureManager();
    public static MeshCaptureManager getInstance() { return INSTANCE; }
    
    private MeshCaptureManager() {}
    
    /**
     * Initialize the shared memory connection.
     * Should be called once at mod initialization.
     */
    public void initialize() {
        if (isInitialized) return;
        
        try {
            createSharedMemory();
            isInitialized = true;
            LOGGER.info("MeshCaptureManager initialized with {}MB shared memory", 
                SHARED_MEMORY_SIZE / (1024 * 1024));
        } catch (Exception e) {
            LOGGER.error("Failed to initialize MeshCaptureManager: {}", e.getMessage());
        }
    }
    
    private void createSharedMemory() {
        try {
            NamedSharedMemory.Kernel32 kernel32 = com.sun.jna.Native.load(
                "kernel32", NamedSharedMemory.Kernel32.class);
            
            fileMapping = kernel32.CreateFileMappingW(
                new com.sun.jna.Pointer(-1), // INVALID_HANDLE_VALUE
                null, // NULL security
                0x04, // PAGE_READWRITE
                (int) (SHARED_MEMORY_SIZE >>> 32),
                (int) SHARED_MEMORY_SIZE,
                MAPPING_NAME
            );
            
            if (fileMapping == null || fileMapping == com.sun.jna.Pointer.NULL) {
                throw new RuntimeException("CreateFileMappingW failed: " + 
                    com.sun.jna.Native.getLastError());
            }
            
            mappedView = kernel32.MapViewOfFile(
                fileMapping,
                0xF001F, // FILE_MAP_ALL_ACCESS
                0, 0, SHARED_MEMORY_SIZE
            );
            
            if (mappedView == null || mappedView == com.sun.jna.Pointer.NULL) {
                throw new RuntimeException("MapViewOfFile failed: " + 
                    com.sun.jna.Native.getLastError());
            }
            
        } catch (Exception e) {
            LOGGER.error("Failed to create shared memory: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * Called at the start of each frame to reset the mesh collector.
     */
    public void beginFrame() {
        currentFrameMeshes.clear();
        vertexCount = 0;

        // Clear buffer (don't reallocate every frame - just clear position)
        if (vertexBuffer != null) {
            vertexBuffer.clear();
        }
        allocatedVertexCapacity = 0;

        // Update camera position for world coordinates
        updateCameraPosition();
    }

    /**
     * Ensure vertex buffer has capacity for additional vertices.
     * Allocates on demand to avoid OutOfMemoryError.
     */
    private void ensureBufferCapacity(int additionalVertices) {
        int needed = vertexCount + additionalVertices;
        if (allocatedVertexCapacity >= needed) {
            return;
        }

        // Allocate enough for this batch, minimum 8192, grow as needed
        int newCapacity = Math.max(needed, 8192);
        // Round up to nearest 4096
        newCapacity = ((newCapacity + 4095) / 4096) * 4096;
        // Cap at max
        newCapacity = Math.min(newCapacity, MAX_VERTICES_PER_FRAME);

        vertexBuffer = ByteBuffer.allocateDirect(newCapacity * VERTEX_STRIDE);
        vertexBuffer.order(ByteOrder.LITTLE_ENDIAN);
        allocatedVertexCapacity = newCapacity;

        LOGGER.debug("Allocated vertex buffer for {} vertices ({} bytes)",
            newCapacity, newCapacity * VERTEX_STRIDE);
    }

    /**
     * Update camera position from Minecraft's camera.
     */
    private void updateCameraPosition() {
        try {
            Minecraft mc = Minecraft.getInstance();
            if (mc != null && mc.gameRenderer != null) {
                Camera camera = mc.gameRenderer.getMainCamera();
                if (camera != null) {
                    var position = camera.position();
                    cameraX = position.x;
                    cameraY = position.y;
                    cameraZ = position.z;
                }
            }
        } catch (Exception e) {
            // Ignore - use default (0,0,0)
        }
    }

    /**
     * Get camera position for external use.
     */
    public double[] getCameraPosition() {
        return new double[] { cameraX, cameraY, cameraZ };
    }
    
    /**
     * Start frame if not already started (thread-safe).
     */
    public void beginFrameIfNeeded() {
        if (!frameStarted) {
            synchronized (this) {
                if (!frameStarted) {
                    beginFrame();
                    frameStarted = true;
                }
            }
        }
    }
    
    /**
     * Capture a mesh from RenderType.draw() or BufferBuilder.build().
     * Reads vertex data from the MeshData and stores it in our format.
     */
    public void captureMesh(String renderTypeName, MeshData mesh) {
        if (!isInitialized || !enabled) return;
        if (currentFrameMeshes.size() >= MAX_MESHES_PER_FRAME) return;
        if (mesh == null) return;
        
        try {
            ByteBuffer vertexData = mesh.vertexBuffer();
            if (vertexData == null || !vertexData.hasRemaining()) return;
            
            MeshData.DrawState drawState = mesh.drawState();
            VertexFormat format = drawState.format();
            int meshVertexCount = drawState.vertexCount();
            int vertexSize = format.getVertexSize();
            
            if (vertexCount + meshVertexCount > MAX_VERTICES_PER_FRAME) return;

            // Ensure buffer has capacity before writing
            ensureBufferCapacity(meshVertexCount);

            int vertexOffset = vertexCount;
            
            // Read vertex data and convert to our format
            int[] offsets = format.getOffsetsByElement();
            
            for (int i = 0; i < meshVertexCount; i++) {
                int baseOffset = i * vertexSize;

                // Get position (3 floats) and add camera position for world coordinates
                float x = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION)) + (float) cameraX;
                float y = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION) + 4) + (float) cameraY;
                float z = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION) + 8) + (float) cameraZ;

                // Get color (4 bytes, normalized UBYTE -> int)
                int color = getColor(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.COLOR));

                // Get UV (2 floats)
                float u = getFloatOrDefault(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.UV0), 0f);
                float v = getFloatOrDefault(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.UV0) + 4, 0f);

                // Get normal (3 bytes, normalized BYTE -> float)
                float nx = 0f, ny = 0f, nz = 1f;
                int normalOffset = getOffset(offsets, VertexFormatElement.NORMAL);
                if (normalOffset >= 0) {
                    nx = getNormal(vertexData, baseOffset + normalOffset);
                    ny = getNormal(vertexData, baseOffset + normalOffset + 1);
                    nz = getNormal(vertexData, baseOffset + normalOffset + 2);
                }

                // Write in our format: x,y,z,color,u,v,nx,ny,nz (36 bytes)
                vertexBuffer.putFloat(x);
                vertexBuffer.putFloat(y);
                vertexBuffer.putFloat(z);
                vertexBuffer.putInt(color);
                vertexBuffer.putFloat(u);
                vertexBuffer.putFloat(v);
                vertexBuffer.putFloat(nx);
                vertexBuffer.putFloat(ny);
                vertexBuffer.putFloat(nz);
                
                vertexCount++;
            }
            
            // Record this mesh
            CapturedMesh captured = new CapturedMesh();
            captured.renderType = renderTypeName;
            captured.vertexOffset = vertexOffset;
            captured.vertexCount = meshVertexCount;
            captured.triangleCount = meshVertexCount / 3; // Each 3 vertices form a triangle
            currentFrameMeshes.add(captured);
            
            LOGGER.debug("Captured mesh: {} with {} vertices", renderTypeName, meshVertexCount);
            
        } catch (Exception e) {
            LOGGER.error("Error capturing mesh {}: {}", renderTypeName, e.getMessage());
        }
    }

    /**
     * Capture a chunk terrain mesh with world bounds information.
     * This provides section coordinates for proper mesh positioning in the viewer.
     *
     * @param sourceName Descriptive name including section coordinates and layer
     * @param mesh The mesh data to capture
     * @param minX World bounds min X
     * @param minY World bounds min Y
     * @param minZ World bounds min Z
     * @param maxX World bounds max X
     * @param maxY World bounds max Y
     * @param maxZ World bounds max Z
     */
    public void captureChunkMesh(String sourceName, MeshData mesh,
                                 float minX, float minY, float minZ,
                                 float maxX, float maxY, float maxZ) {
        if (!isInitialized || !enabled) return;
        if (currentFrameMeshes.size() >= MAX_MESHES_PER_FRAME) return;
        if (mesh == null) return;

        try {
            ByteBuffer vertexData = mesh.vertexBuffer();
            if (vertexData == null || !vertexData.hasRemaining()) return;

            MeshData.DrawState drawState = mesh.drawState();
            VertexFormat format = drawState.format();
            int meshVertexCount = drawState.vertexCount();
            int vertexSize = format.getVertexSize();

            if (vertexCount + meshVertexCount > MAX_VERTICES_PER_FRAME) return;

            // Ensure buffer has capacity before writing
            ensureBufferCapacity(meshVertexCount);

            int vertexOffset = vertexCount;

            // Read vertex data and convert to our format
            int[] offsets = format.getOffsetsByElement();

            for (int i = 0; i < meshVertexCount; i++) {
                int baseOffset = i * vertexSize;

                // Get position (3 floats) - chunk vertices are already in world coordinates
                float x = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION));
                float y = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION) + 4);
                float z = getFloat(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.POSITION) + 8);

                // Get color (4 bytes, normalized UBYTE -> int)
                int color = getColor(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.COLOR));

                // Get UV (2 floats)
                float u = getFloatOrDefault(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.UV0), 0f);
                float v = getFloatOrDefault(vertexData, baseOffset + getOffset(offsets, VertexFormatElement.UV0) + 4, 0f);

                // Get normal (3 bytes, normalized BYTE -> float)
                float nx = 0f, ny = 0f, nz = 1f;
                int normalOffset = getOffset(offsets, VertexFormatElement.NORMAL);
                if (normalOffset >= 0) {
                    nx = getNormal(vertexData, baseOffset + normalOffset);
                    ny = getNormal(vertexData, baseOffset + normalOffset + 1);
                    nz = getNormal(vertexData, baseOffset + normalOffset + 2);
                }

                // Write in our format: x,y,z,color,u,v,nx,ny,nz (36 bytes)
                vertexBuffer.putFloat(x);
                vertexBuffer.putFloat(y);
                vertexBuffer.putFloat(z);
                vertexBuffer.putInt(color);
                vertexBuffer.putFloat(u);
                vertexBuffer.putFloat(v);
                vertexBuffer.putFloat(nx);
                vertexBuffer.putFloat(ny);
                vertexBuffer.putFloat(nz);

                vertexCount++;
            }

            // Record this mesh
            CapturedMesh captured = new CapturedMesh();
            captured.renderType = sourceName;
            captured.vertexOffset = vertexOffset;
            captured.vertexCount = meshVertexCount;
            captured.triangleCount = meshVertexCount / 3;
            currentFrameMeshes.add(captured);

            LOGGER.debug("Captured chunk mesh: {} with {} vertices", sourceName, meshVertexCount);

        } catch (Exception e) {
            LOGGER.error("Error capturing chunk mesh {}: {}", sourceName, e.getMessage());
        }
    }
    
    private int getOffset(int[] offsets, VertexFormatElement element) {
        if (element == null) return -1;
        return offsets[element.id()];
    }
    
    private float getFloat(ByteBuffer buffer, int offset) {
        if (offset < 0 || offset + 4 > buffer.capacity()) return 0f;
        return buffer.getFloat(offset);
    }
    
    private float getFloatOrDefault(ByteBuffer buffer, int offset, float defaultVal) {
        if (offset < 0 || offset + 4 > buffer.capacity()) return defaultVal;
        return buffer.getFloat(offset);
    }
    
    private int getColor(ByteBuffer buffer, int offset) {
        if (offset < 0 || offset + 4 > buffer.capacity()) return 0xFFFFFFFF;
        int b0 = buffer.get(offset) & 0xFF;
        int b1 = buffer.get(offset + 1) & 0xFF;
        int b2 = buffer.get(offset + 2) & 0xFF;
        int b3 = buffer.get(offset + 3) & 0xFF;
        return (b3 << 24) | (b2 << 16) | (b1 << 8) | b0;
    }
    
    private float getNormal(ByteBuffer buffer, int offset) {
        if (offset < 0 || offset >= buffer.capacity()) return 0f;
        return buffer.get(offset) / 127.0f;
    }
    
    /**
     * Called at the end of each frame to finalize and write to shared memory.
     */
    public void endFrame() {
        // Reset frame state for next frame
        frameStarted = false;
        
        if (!isInitialized || vertexCount == 0) return;
        
        try {
            writeToSharedMemory();
        } catch (Exception e) {
            LOGGER.error("Failed to write mesh data to shared memory: {}", e.getMessage());
        }
    }
    
    private void writeToSharedMemory() {
        if (mappedView == null || mappedView == com.sun.jna.Pointer.NULL) return;

        vertexBuffer.flip();

        long frameNum = frameNumber.incrementAndGet();

        // Calculate data offsets
        int meshListSize = currentFrameMeshes.size() * MeshInfo.SIZE;
        int vertexDataOffset = HEADER_SIZE + meshListSize;

        // Write header
        mappedView.setInt(MeshDataHeader.OFFSET_MAGIC, MeshDataHeader.MAGIC);
        mappedView.setInt(MeshDataHeader.OFFSET_VERSION, MeshDataHeader.VERSION);
        mappedView.setInt(MeshDataHeader.OFFSET_FLAGS, 
            MeshDataHeader.MESH_FLAG_POSITIONS | 
            MeshDataHeader.MESH_FLAG_COLORS | 
            MeshDataHeader.MESH_FLAG_UVS | 
            MeshDataHeader.MESH_FLAG_NORMALS);
        mappedView.setInt(MeshDataHeader.OFFSET_MESH_COUNT, currentFrameMeshes.size());
        mappedView.setInt(MeshDataHeader.OFFSET_VERTEX_COUNT, vertexCount);
        mappedView.setInt(MeshDataHeader.OFFSET_TRIANGLE_COUNT, estimateTriangleCount());
        mappedView.setInt(MeshDataHeader.OFFSET_VERTEX_DATA_OFFSET, vertexDataOffset);
        mappedView.setInt(MeshDataHeader.OFFSET_INDEX_DATA_OFFSET, 0);
        mappedView.setLong(MeshDataHeader.OFFSET_TIMESTAMP, System.nanoTime());
        mappedView.setLong(MeshDataHeader.OFFSET_FRAME_NUMBER, frameNum);
        mappedView.setInt(MeshDataHeader.OFFSET_STATUS, MeshDataHeader.STATUS_WRITING);

        // Write camera position
        mappedView.setDouble(MeshDataHeader.OFFSET_CAMERA_X, cameraX);
        mappedView.setDouble(MeshDataHeader.OFFSET_CAMERA_Y, cameraY);
        mappedView.setDouble(MeshDataHeader.OFFSET_CAMERA_Z, cameraZ);

        // Write vertex stride
        mappedView.setInt(MeshDataHeader.OFFSET_VERTEX_STRIDE, VERTEX_STRIDE);

        // Write mesh info list
        int offset = HEADER_SIZE;
        for (CapturedMesh mesh : currentFrameMeshes) {
            writeMeshInfo(mappedView, offset, mesh);
            offset += MeshInfo.SIZE;
        }

        // Write vertex data
        byte[] vertexBytes = new byte[vertexBuffer.remaining()];
        vertexBuffer.get(vertexBytes);
        mappedView.write(vertexDataOffset, vertexBytes, 0, vertexBytes.length);

        // Status: Ready
        mappedView.setInt(MeshDataHeader.OFFSET_STATUS, MeshDataHeader.STATUS_READY);

        LOGGER.info("Frame {}: {} meshes, {} vertices", 
            frameNum, currentFrameMeshes.size(), vertexCount);
    }
    
    private int estimateTriangleCount() {
        int count = 0;
        for (CapturedMesh mesh : currentFrameMeshes) {
            if (mesh.vertexCount >= 3) {
                count += mesh.vertexCount / 3;
            }
        }
        return count;
    }
    
    private void writeMeshInfo(com.sun.jna.Pointer view, int offset, CapturedMesh mesh) {
        view.setInt(offset + 0, mesh.vertexOffset);
        view.setInt(offset + 4, mesh.vertexCount);
        view.setInt(offset + 8, mesh.triangleCount);
        view.setInt(offset + 12, 0);
        
        // Write render type name (up to 60 chars)
        String name = mesh.renderType;
        if (name == null) name = "unknown";
        if (name.length() > 59) name = name.substring(0, 59);
        byte[] nameBytes = name.getBytes();
        byte[] nameBuf = new byte[60];
        System.arraycopy(nameBytes, 0, nameBuf, 0, Math.min(nameBytes.length, 60));
        view.write(offset + 16, nameBuf, 0, 60);
    }
    
    /**
     * Cleanup resources.
     */
    public void shutdown() {
        enabled = false;
        if (mappedView != null && mappedView != com.sun.jna.Pointer.NULL) {
            try {
                NamedSharedMemory.Kernel32 kernel32 = com.sun.jna.Native.load(
                    "kernel32", NamedSharedMemory.Kernel32.class);
                kernel32.UnmapViewOfFile(mappedView);
            } catch (Exception ignored) {}
            mappedView = com.sun.jna.Pointer.NULL;
        }
        if (fileMapping != null && fileMapping != com.sun.jna.Pointer.NULL) {
            try {
                NamedSharedMemory.Kernel32 kernel32 = com.sun.jna.Native.load(
                    "kernel32", NamedSharedMemory.Kernel32.class);
                kernel32.CloseHandle(fileMapping);
            } catch (Exception ignored) {}
            fileMapping = com.sun.jna.Pointer.NULL;
        }
        isInitialized = false;
        LOGGER.info("MeshCaptureManager shutdown");
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.info("MeshCaptureManager {}", enabled ? "enabled" : "disabled");
    }
    
    // Vertex stride: x,y,z (12) + color (4) + u,v (8) + nx,ny,nz (12) = 36 bytes
    public static final int VERTEX_STRIDE = 36;
    
    /**
     * Internal class to hold captured mesh info.
     */
    private static class CapturedMesh {
        String renderType = "unknown";
        int vertexOffset;
        int vertexCount;
        int triangleCount;
    }
    
    /**
     * Mesh info structure (64 bytes).
     */
    public static class MeshInfo {
        public static final int SIZE = 76;
        public int vertexOffset;
        public int vertexCount;
        public int triangleCount;
        public int dataOffset;
        public String renderType;
    }
}
