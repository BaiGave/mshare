package com.mshare.screen;

/**
 * Mesh data header structure for shared memory.
 * Used to communicate mesh metadata between Minecraft and the web viewer.
 *
 * Layout (192 bytes total):
 * - Magic: "MESH" (0x4D455348)
 * - Version: Protocol version (3)
 * - Flags: Mesh flags (see MESH_FLAG_* constants)
 * - Mesh count: Number of meshes in this frame
 * - Total vertices: Total vertex count
 * - Total triangles: Total triangle count
 * - Vertex data offset: Offset to vertex data
 * - Index data offset: Offset to index data
 * - Timestamp: Frame timestamp (nanoseconds)
 * - Frame number: Frame counter
 * - Status: 0=idle, 1=writing, 2=ready
 * - Camera X/Y/Z: World position of camera (for coordinate transformation)
 * - Vertex stride: Bytes per vertex (28 for BLOCK format)
 * - Reserved: Padding to 192 bytes
 */
public class MeshDataHeader {
    // Magic number "MESH"
    public static final int MAGIC = 0x4D455348;
    public static final int VERSION = 3;

    // Flags
    public static final int MESH_FLAG_POSITIONS = 1;
    public static final int MESH_FLAG_COLORS = 2;
    public static final int MESH_FLAG_UVS = 4;
    public static final int MESH_FLAG_LIGHTMAP = 8;
    public static final int MESH_FLAG_NORMALS = 16;

    // Status values
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_WRITING = 1;
    public static final int STATUS_READY = 2;

    // Offsets
    public static final int OFFSET_MAGIC = 0;
    public static final int OFFSET_VERSION = 4;
    public static final int OFFSET_FLAGS = 8;
    public static final int OFFSET_MESH_COUNT = 12;
    public static final int OFFSET_VERTEX_COUNT = 16;
    public static final int OFFSET_TRIANGLE_COUNT = 20;
    public static final int OFFSET_VERTEX_DATA_OFFSET = 24;
    public static final int OFFSET_INDEX_DATA_OFFSET = 28;
    public static final int OFFSET_TIMESTAMP = 32;
    public static final int OFFSET_FRAME_NUMBER = 40;
    public static final int OFFSET_STATUS = 48;
    public static final int OFFSET_CAMERA_X = 52;
    public static final int OFFSET_CAMERA_Y = 60;
    public static final int OFFSET_CAMERA_Z = 68;
    public static final int OFFSET_VERTEX_STRIDE = 76;
    public static final int OFFSET_RESERVED = 80;

    public static final int HEADER_SIZE = 192;
}
