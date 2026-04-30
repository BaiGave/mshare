// SharedMemoryReader.js - Reads Windows shared memory via a helper executable
// Node.js spawns a small C helper that uses ReadFile to read the memory mapped file

const { spawn, execSync } = require('child_process');
const path = require('path');
const fs = require('fs');
const os = require('os');

// Find the helper executable
function findHelper() {
    const candidates = [
        path.join(__dirname, 'smreader.exe'),
        path.join(__dirname, 'smreader'),
    ];
    for (const c of candidates) {
        if (fs.existsSync(c)) return c;
    }
    return null;
}

// Status values from shared memory header
const STATUS_IDLE = 0;
const STATUS_WRITING = 1;
const STATUS_READY = 2;

// Header offsets
const OFF_MAGIC = 0;
const OFF_VERSION = 4;
const OFF_SCREEN_WIDTH = 8;
const OFF_SCREEN_HEIGHT = 12;
const OFF_WIDTH = 16;
const OFF_HEIGHT = 20;
const OFF_FORMAT = 24;
const OFF_STRIDE = 28;
const OFF_TIMESTAMP = 36;
const OFF_FRAME_COUNT = 44;
const OFF_STATUS = 52;
const HEADER_SIZE = 64;

class SharedMemoryReader {
    constructor(name) {
        this.name = name;
        this.running = false;
        this.frameCallback = null;
        this.lastFrameCount = -1;
        this.pollInterval = null;
        this.process = null;
        this.dataBuffer = Buffer.alloc(3840 * 2160 * 4 + HEADER_SIZE);
        this.headerBuffer = Buffer.alloc(HEADER_SIZE);
        this.connected = false;
    }

    open() {
        const helper = findHelper();
        if (!helper) {
            console.error('[SMReader] Helper executable not found. Run build-smreader.bat first.');
            return false;
        }

        console.log('[SMReader] Using helper:', helper);
        this.connected = true;
        return true;
    }

    close() {
        this.running = false;
        if (this.pollInterval) {
            clearInterval(this.pollInterval);
            this.pollInterval = null;
        }
        if (this.process) {
            this.process.kill();
            this.process = null;
        }
        this.connected = false;
        console.log('[SMReader] Closed');
    }

    /**
     * Read shared memory directly using PowerShell (no external helper needed)
     */
    readViaPowerShell() {
        return new Promise((resolve) => {
            const ps = `
                Add-Type -TypeDefinition @"
                using System;
                using System.Runtime.InteropServices;
                public class SmReader {
                    [DllImport("kernel32.dll", SetLastError=true)]
                    public static extern IntPtr OpenFileMapping(uint dwDesiredAccess, bool bInheritHandle, string lpName);
                    [DllImport("kernel32.dll", SetLastError=true)]
                    public static extern IntPtr MapViewOfFile(IntPtr hFileMappingObject, uint dwDesiredAccess, uint dwFileOffsetHigh, uint dwFileOffsetLow, UIntPtr dwNumberOfBytesToMap);
                    [DllImport("kernel32.dll", SetLastError=true)]
                    public static extern bool UnmapViewOfFile(IntPtr lpBaseAddress);
                    [DllImport("kernel32.dll", SetLastError=true)]
                    public static extern bool CloseHandle(IntPtr hObject);
                    [DllImport("msvcrt.dll", CallingConvention=CallingConvention.Cdecl)]
                    public static extern void memcpy(IntPtr dest, IntPtr src, int count);
                }
"@
                $hFile = [SmReader]::OpenFileMapping(0x0004, $false, "Global\\MinecraftScreenCapture")
                if ($hFile -eq [IntPtr]::Zero) { Write-Output "ERROR:OpenFailed"; exit }
                $pData = [SmReader]::MapViewOfFile($hFile, 0x0004, 0, 0, [UIntPtr]::new(33554432))
                if ($pData -eq [IntPtr]::Zero) { [SmReader]::CloseHandle($hFile); Write-Output "ERROR:MapFailed"; exit }
                $buf = $pData.ToInt64()
                # Output magic (4 bytes)
                [System.BitConverter]::ToString((0..3 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }))
                [System.BitConverter]::ToString((4..7 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }))
                # Width (offset 16)
                $w = [System.BitConverter]::ToInt32((16..19 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }), 0)
                # Height (offset 20)
                $h = [System.BitConverter]::ToInt32((20..23 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }), 0)
                # Status (offset 52)
                $s = [System.BitConverter]::ToInt32((52..55 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }), 0)
                # FrameCount (offset 44)
                $fc = [System.BitConverter]::ToInt64((44..51 | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }), 0)
                Write-Output "HEADER:$w`:$h`:$s`:$fc"
                # Pixel data (offset 64, max 854*480*4 = 1639680 bytes)
                $pixels = (64..($w * $h * 4 + 63)) | % { [System.Runtime.InteropServices.Marshal]::ReadByte($pData, $_) }
                [System.Convert]::ToBase64String([byte[]]$pixels)
                [SmReader]::UnmapViewOfFile($pData)
                [SmReader]::CloseHandle($hFile)
            `;

            const psExec = spawn('powershell', ['-NoProfile', '-Command', ps], {
                windowsHide: true
            });

            let stdout = '';
            let stderr = '';

            psExec.stdout.on('data', (data) => {
                stdout += data.toString();
            });

            psExec.stderr.on('data', (data) => {
                stderr += data.toString();
            });

            psExec.on('close', (code) => {
                if (code !== 0 || stderr) {
                    resolve(null);
                    return;
                }
                resolve(stdout);
            });
        });
    }

    /**
     * Start polling for new frames
     */
    start(onFrame) {
        if (!this.connected) {
            console.error('[SMReader] Not connected');
            return;
        }

        this.frameCallback = onFrame;
        this.lastFrameCount = -1;
        this.running = true;

        // Poll at ~30 FPS
        this.pollInterval = setInterval(async () => {
            await this.poll();
        }, 33);
    }

    async poll() {
        if (!this.running) return;

        try {
            const output = await this.readViaPowerShell();
            if (!output) return;

            const lines = output.trim().split('\n');
            const headerLine = lines.find(l => l.startsWith('HEADER:'));
            if (!headerLine) return;

            const parts = headerLine.substring(7).split(':');
            if (parts.length < 4) return;

            const width = parseInt(parts[0]);
            const height = parseInt(parts[1]);
            const status = parseInt(parts[2]);
            const frameCount = parseInt(parts[3]);

            if (width <= 0 || height <= 0) return;
            if (status !== STATUS_READY) return;
            if (frameCount <= this.lastFrameCount) return;

            this.lastFrameCount = frameCount;

            // Find base64 line
            const base64Line = lines.find(l => !l.includes(':') && l.length > 100);
            if (!base64Line) return;

            const pixels = Buffer.from(base64Line.trim(), 'base64');

            if (this.frameCallback) {
                this.frameCallback({
                    width,
                    height,
                    timestamp: Date.now() * 1000000,
                    frameCount,
                    pixels
                });
            }
        } catch (e) {
            // Silently ignore errors
        }
    }
}

module.exports = SharedMemoryReader;
