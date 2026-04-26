package com.mshare.screen;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

/**
 * Standalone camera data reader.
 * Uses Windows API via JNA to read camera shared memory.
 * Continuously reads camera data and outputs to stdout.
 */
public class CameraDataReader {
    private static final int FILE_MAP_ALL_ACCESS = 0xF001F;
    private static final String MAPPING_NAME = "Global\\MCCameraData";
    private static final int CAMERA_DATA_SIZE = 128;
    private static final int STATUS_READY = 2;

    private static final Kernel32 kernel32 = Native.load("kernel32", Kernel32.class);

    public interface Kernel32 extends Library {
        Pointer OpenFileMappingW(int dwDesiredAccess, boolean bInheritHandle, String lpName);
        Pointer MapViewOfFile(Pointer hFileMappingObject, int dwDesiredAccess,
                             int dwFileOffsetHigh, int dwFileOffsetLow, long dwNumberOfBytesToMap);
        boolean UnmapViewOfFile(Pointer lpBaseAddress);
        boolean CloseHandle(Pointer hObject);
    }

    public static void main(String[] args) {
        System.out.println("[CameraReader] Starting camera data reader...");

        int errorCount = 0;
        int dataCount = 0;
        long lastTimestamp = 0;

        while (errorCount < 10) {
            Pointer fileMapping = null;
            Pointer mappedView = null;

            try {
                // Open shared memory
                fileMapping = kernel32.OpenFileMappingW(FILE_MAP_ALL_ACCESS, false, MAPPING_NAME);
                if (fileMapping == null || fileMapping == Pointer.NULL) {
                    Thread.sleep(100);
                    continue;
                }

                // Map the view
                mappedView = kernel32.MapViewOfFile(fileMapping, FILE_MAP_ALL_ACCESS, 0, 0, 0);
                if (mappedView == null || mappedView == Pointer.NULL) {
                    if (fileMapping != null && fileMapping != Pointer.NULL) {
                        kernel32.CloseHandle(fileMapping);
                    }
                    Thread.sleep(100);
                    continue;
                }

                System.out.println("[CameraReader] Connected to camera shared memory");

                while (true) {
                    try {
                        // Read header
                        int magic = mappedView.getInt(0);
                        int status = mappedView.getInt(68);
                        long timestamp = mappedView.getLong(8);

                        // Check magic
                        if (magic != 0x4D434344) { // "MCCD"
                            Thread.sleep(33); // ~30 FPS
                            continue;
                        }

                        // Check if data is ready and new
                        if (status == STATUS_READY && timestamp != lastTimestamp) {
                            lastTimestamp = timestamp;

                            // Read camera data
                            double posX = Double.longBitsToDouble(mappedView.getLong(16));
                            double posY = Double.longBitsToDouble(mappedView.getLong(24));
                            double posZ = Double.longBitsToDouble(mappedView.getLong(32));
                            float xRot = Float.intBitsToFloat(mappedView.getInt(40));
                            float yRot = Float.intBitsToFloat(mappedView.getInt(44));
                            float quatX = Float.intBitsToFloat(mappedView.getInt(48));
                            float quatY = Float.intBitsToFloat(mappedView.getInt(52));
                            float quatZ = Float.intBitsToFloat(mappedView.getInt(56));
                            float quatW = Float.intBitsToFloat(mappedView.getInt(60));
                            float fov = Float.intBitsToFloat(mappedView.getInt(64));
                            int cameraType = mappedView.getInt(72);
                            int detached = mappedView.getInt(76);

                            // Output as JSON
                            System.out.println("[CameraReader] CAMERA_START");
                            System.out.println("[CameraReader] DATA:{\"type\":\"camera\"," +
                                    "\"x\":" + posX + "," +
                                    "\"y\":" + posY + "," +
                                    "\"z\":" + posZ + "," +
                                    "\"pitch\":" + xRot + "," +
                                    "\"yaw\":" + yRot + "," +
                                    "\"quatX\":" + quatX + "," +
                                    "\"quatY\":" + quatY + "," +
                                    "\"quatZ\":" + quatZ + "," +
                                    "\"quatW\":" + quatW + "," +
                                    "\"fov\":" + fov + "," +
                                    "\"cameraType\":" + cameraType + "," +
                                    "\"detached\":" + detached + "}");
                            System.out.println("[CameraReader] CAMERA_END");
                            System.out.flush();

                            dataCount++;
                            errorCount = 0; // Reset error count on successful read
                        }

                        // Small delay to avoid busy loop
                        Thread.sleep(33); // ~30 FPS polling

                    } catch (Exception e) {
                        System.out.println("[CameraReader] Read error: " + e.getMessage());
                        break;
                    }
                }

            } catch (Exception e) {
                errorCount++;
                System.out.println("[CameraReader] Error: " + e.getMessage());
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

        System.out.println("[CameraReader] Exiting after " + errorCount + " errors, read " + dataCount + " camera updates");
    }
}
