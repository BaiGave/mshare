package com.mshare.screen;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.util.Base64;

/**
 * Mesh data reader - standalone Java subprocess.
 * Reads mesh data from shared memory and outputs to stdout as JSON.
 *
 * Protocol (version 3):
 * - Writes MESH_START when starting
 * - Writes DATA with JSON payload for each frame
 * - Writes VERTEX_DATA with base64-encoded vertex data
 * - Writes MESH_END when complete
 *
 * Header (192 bytes):
 * - OFFSET_MAGIC(0) .. OFFSET_VERSION(4) .. OFFSET_FLAGS(8)
 * - OFFSET_MESH_COUNT(12) .. OFFSET_VERTEX_COUNT(16) .. OFFSET_TRIANGLE_COUNT(20)
 * - OFFSET_VERTEX_DATA_OFFSET(24) .. OFFSET_INDEX_DATA_OFFSET(28)
 * - OFFSET_TIMESTAMP(32,8bytes) .. OFFSET_FRAME_NUMBER(40,8bytes)
 * - OFFSET_STATUS(48) .. OFFSET_CAMERA_X(52,8bytes) .. OFFSET_CAMERA_Y(60) .. OFFSET_CAMERA_Z(68)
 * - OFFSET_VERTEX_STRIDE(76) .. OFFSET_RESERVED(80)
 *
 * Per-mesh info (76 bytes):
 * - vertexOffset(4) .. vertexCount(4) .. triangleCount(4) .. dataOffset(4)
 * - sourceName(60 bytes, padded with zeros)
 */
public class MeshDataReader {
    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final String MAPPING_NAME = NamedSharedMemory.MAPPING_NAME;
    private static final int SHARED_MEMORY_SIZE = 100 * 1024 * 1024; // 100 MB (must match MeshCaptureManager)
    private static final int STATUS_READY = 2;
    private static final int MAGIC = 0x4D455348;
    private static final int MESH_INFO_SIZE = 76;
    private static final int HEADER_SIZE = 192;
    private static final int VERTEX_STRIDE = 36;

    public interface Kernel32 extends Library {
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }

    private static final Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);

    public static void main(String[] args) {
        System.out.println("[MeshReader] Starting mesh data reader (v3)...");

        int errorCount = 0;
        int frameCount = 0;
        long lastFrameNumber = -1;

        while (errorCount < 10) {
            Pointer fileMapping = null;
            Pointer mappedView = null;

            try {
                fileMapping = kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);
                if (fileMapping == null || fileMapping == Pointer.NULL) {
                    Thread.sleep(100);
                    continue;
                }

                mappedView = kernel32.MapViewOfFile(fileMapping, FILE_MAP_ALL_ACCESS, 0, 0, SHARED_MEMORY_SIZE);
                if (mappedView == null || mappedView == Pointer.NULL) {
                    if (fileMapping != null && fileMapping != Pointer.NULL) {
                        kernel32.CloseHandle(fileMapping);
                    }
                    Thread.sleep(100);
                    continue;
                }

                System.out.println("[MeshReader] Connected to mesh shared memory: " + MAPPING_NAME);

                while (true) {
                    try {
                        int magic = mappedView.getInt(0);
                        int status = mappedView.getInt(48);
                        long frameNumber = mappedView.getLong(40);

                        if (magic != MAGIC) {
                            Thread.sleep(33);
                            continue;
                        }

                        if (status == STATUS_READY && frameNumber != lastFrameNumber) {
                            lastFrameNumber = frameNumber;

                            int meshCount = mappedView.getInt(12);
                            int vertexCount = mappedView.getInt(16);
                            int triangleCount = mappedView.getInt(20);
                            int vertexDataOffset = mappedView.getInt(24);
                            long timestamp = mappedView.getLong(32);
                            double cameraX = mappedView.getDouble(52);
                            double cameraY = mappedView.getDouble(60);
                            double cameraZ = mappedView.getDouble(68);
                            int vertexStride = mappedView.getInt(76);

                            // Build JSON
                            StringBuilder json = new StringBuilder();
                            json.append("{\"type\":\"mesh\",");
                            json.append("\"version\":3,");
                            json.append("\"frame\":").append(frameNumber).append(",");
                            json.append("\"meshCount\":").append(meshCount).append(",");
                            json.append("\"vertexCount\":").append(vertexCount).append(",");
                            json.append("\"triangleCount\":").append(triangleCount).append(",");
                            json.append("\"timestamp\":").append(timestamp).append(",");
                            json.append("\"vertexStride\":").append(vertexStride).append(",");
                            json.append("\"cameraX\":").append(cameraX).append(",");
                            json.append("\"cameraY\":").append(cameraY).append(",");
                            json.append("\"cameraZ\":").append(cameraZ).append(",");
                            json.append("\"meshes\":[");

                            for (int i = 0; i < meshCount; i++) {
                                if (i > 0) json.append(",");

                                int offset = HEADER_SIZE + i * MESH_INFO_SIZE;
                                int vertOffset = mappedView.getInt(offset + 0);
                                int vertCount = mappedView.getInt(offset + 4);
                                int triCount = mappedView.getInt(offset + 8);
                                int dataOff = mappedView.getInt(offset + 12);

                                // Read name: 60 bytes at offset + 16
                                byte[] nameBytes = new byte[60];
                                mappedView.read(offset + 16, nameBytes, 0, 60);
                                String name = new String(nameBytes).trim();

                                json.append("{\"name\":\"").append(escapeJson(name)).append("\",");
                                json.append("\"vertexOffset\":").append(vertOffset).append(",");
                                json.append("\"vertexCount\":").append(vertCount).append(",");
                                json.append("\"triangleCount\":").append(triCount).append(",");
                                json.append("\"dataOffset\":").append(dataOff).append("}");
                            }

                            json.append("]}");

                            frameCount++;
                            System.out.println("[MeshReader] MESH_START");
                            System.out.println("[MeshReader] DATA:" + json.toString());

                            // Read and output vertex data
                            int effectiveStride = (vertexStride > 0) ? vertexStride : VERTEX_STRIDE;
                            int vertexDataSize = vertexCount * effectiveStride;
                            if (vertexDataSize > 0 && vertexDataOffset + vertexDataSize <= SHARED_MEMORY_SIZE) {
                                byte[] vertexData = new byte[vertexDataSize];
                                mappedView.read(vertexDataOffset, vertexData, 0, vertexDataSize);
                                String vertexBase64 = Base64.getEncoder().encodeToString(vertexData);
                                System.out.println("[MeshReader] VERTEX_DATA:" + vertexBase64);
                            } else {
                                System.out.println("[MeshReader] VERTEX_DATA:");
                            }

                            System.out.println("[MeshReader] MESH_END");
                            System.out.flush();

                            errorCount = 0;
                        }

                        Thread.sleep(33);

                    } catch (Exception e) {
                        System.out.println("[MeshReader] Read error: " + e.getMessage());
                        break;
                    }
                }

            } catch (Exception e) {
                errorCount++;
                System.out.println("[MeshReader] Error: " + e.getMessage());
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ie) {
                    break;
                }
            } finally {
                if (mappedView != null && mappedView != Pointer.NULL) {
                    try {
                        kernel32.UnmapViewOfFile(mappedView);
                    } catch (Exception ignored) {}
                }
                if (fileMapping != null && fileMapping != Pointer.NULL) {
                    try {
                        kernel32.CloseHandle(fileMapping);
                    } catch (Exception ignored) {}
                }
            }
        }

        System.out.println("[MeshReader] Exiting after " + errorCount + " errors, read " + frameCount + " frames");
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        s = s.replaceAll("[^\\x20-\\x7E]", "");
        if (s.length() > 30) s = s.substring(0, 30);
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
