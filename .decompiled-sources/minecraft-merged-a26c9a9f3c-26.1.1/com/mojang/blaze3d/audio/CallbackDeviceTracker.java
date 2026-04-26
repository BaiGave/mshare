/*
 * Decompiled with CFR 0.152.
 */
package com.mojang.blaze3d.audio;

import com.mojang.blaze3d.audio.AbstractDeviceTracker;
import com.mojang.blaze3d.audio.DeviceList;
import com.mojang.logging.LogUtils;
import java.util.HexFormat;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.lwjgl.openal.ALC10;
import org.lwjgl.openal.SOFTSystemEventProcI;
import org.lwjgl.openal.SOFTSystemEvents;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

@Environment(value=EnvType.CLIENT)
public class CallbackDeviceTracker
extends AbstractDeviceTracker {
    private static final Logger LOGGER = LogUtils.getLogger();
    private volatile boolean updateRequested;
    private static final int[] SUBSCRIBED_EVENT_TYPES = new int[]{6614, 6615, 6616};
    public static final HexFormat HEX_FORMAT = HexFormat.of();

    public CallbackDeviceTracker(DeviceList deviceList) {
        super(deviceList);
    }

    @Override
    protected boolean isUpdateRequested() {
        return this.updateRequested;
    }

    @Override
    protected void discardUpdateRequest() {
        this.updateRequested = false;
    }

    public static boolean isSupported() {
        for (int eventType : SUBSCRIBED_EVENT_TYPES) {
            if (CallbackDeviceTracker.isSupportedForPlaybackDevice(eventType)) continue;
            return false;
        }
        return true;
    }

    public static CallbackDeviceTracker createAndInstall(DeviceList deviceList) {
        CallbackDeviceTracker result = new CallbackDeviceTracker(deviceList);
        SOFTSystemEvents.alcEventControlSOFT(SUBSCRIBED_EVENT_TYPES, true);
        SOFTSystemEvents.alcEventCallbackSOFT(result.createCallback(), 0L);
        return result;
    }

    private SOFTSystemEventProcI createCallback() {
        return (eventType, deviceType, device, messageLength, messagePtr, userParam) -> {
            String deviceTypeString = CallbackDeviceTracker.deviceTypeToString(deviceType);
            String message = MemoryUtil.memASCII(messagePtr, messageLength);
            switch (eventType) {
                case 6614: {
                    LOGGER.debug("Default {} device changed: {}", (Object)deviceTypeString, (Object)message);
                    break;
                }
                case 6615: {
                    LOGGER.debug("Added new {} device: {}", (Object)deviceTypeString, (Object)message);
                    break;
                }
                case 6616: {
                    LOGGER.debug("Removed {} device: {}", (Object)deviceTypeString, (Object)message);
                }
            }
            if (deviceType == 6612) {
                this.updateRequested = true;
            }
        };
    }

    private static boolean isSupportedForPlaybackDevice(int eventType) {
        int result = SOFTSystemEvents.alcEventIsSupportedSOFT(eventType, 6612);
        if (result == 0) {
            int error = ALC10.alcGetError(0L);
            LOGGER.warn("Failed to check event {}, error: {}", (Object)HEX_FORMAT.toHexDigits(eventType), (Object)HEX_FORMAT.toHexDigits(error));
            return false;
        }
        return result == 6617;
    }

    private static String deviceTypeToString(int deviceType) {
        return switch (deviceType) {
            case 6613 -> "capture";
            case 6612 -> "playback";
            default -> "unknown (0x" + HEX_FORMAT.toHexDigits(deviceType) + ")";
        };
    }
}

