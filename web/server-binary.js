/**
 * server-binary.js - WebSocket server with binary frame transfer
 * Java outputs raw pixels, Node.js forwards them as binary WebSocket messages
 * Run with: node server-binary.js
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { WebSocketServer } = require('ws');
const { spawn } = require('child_process');

const HTTP_PORT = 3000;
const WS_PORT = 3001;
const CAMERA_WS_PORT = 3002;

// Base directory
const PROJECT_ROOT = path.join(__dirname, '..');
const CLASSES_DIR = path.join(PROJECT_ROOT, 'build', 'classes', 'java', 'main');

// JNA jar paths
const JNA_JARS = [
    path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1',
        'net.java.dev.jna', 'jna', '5.15.0',
        '1ee1d80ff44f08280188f7c0e740d57207841ac', 'jna-5.15.0.jar'),
    path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1',
        'net.java.dev.jna', 'jna-platform', '5.15.0',
        '86b502cad57d45da172b5e3231c537b042e296ef', 'jna-platform-5.15.0.jar')
];

function findJava() {
    const candidates = [
        // IntelliJ / Gradle user install
        path.join(os.homedir(), '.jdks', 'jdk-25.0.2', 'bin', 'java.exe'),
        // Gradle JVM selection
        path.join(os.homedir(), '.gradle', 'jdks', 'jdk-25.0.2', 'windows-x86_64', 'jdk-25.0.2', 'bin', 'java.exe'),
        // Java 21 variants
        path.join(os.homedir(), '.jdks', 'jdk-21', 'bin', 'java.exe'),
        path.join(os.homedir(), '.jdks', 'jdk-21.0.5', 'bin', 'java.exe'),
        path.join(os.homedir(), '.gradle', 'jdks', 'jdk-21', 'windows-x86_64', 'jdk-21', 'bin', 'java.exe'),
        // Oracle / standard Program Files
        'C:\\Program Files\\Java\\jdk-25.0.2\\bin\\java.exe',
        'C:\\Program Files\\Java\\jdk-21\\bin\\java.exe',
        'C:\\Program Files\\Eclipse Adoptium\\jdk-21.0.5-hotspot\\bin\\java.exe',
        'C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.2-hotspot\\bin\\java.exe',
        'C:\\Program Files\\Amazon Corretto\\jdk21\\bin\\java.exe',
        'C:\\Program Files\\Amazon Corretto\\jdk25.0.2\\bin\\java.exe',
        'C:\\Program Files\\Microsoft\\jdk-21\\bin\\java.exe',
        'C:\\Program Files\\Microsoft\\jdk-25.0.2\\bin\\java.exe',
    ];
    for (const javaHome of candidates) {
        if (fs.existsSync(javaHome)) return javaHome;
    }
    return 'java';
}

const mimeTypes = {
    '.html': 'text/html',
    '.js': 'application/javascript',
    '.css': 'text/css'
};

// =====================
// HTTP Server
// =====================
const httpServer = http.createServer((req, res) => {
    console.log(`[HTTP] ${req.method} ${req.url}`);

    let filePath = req.url === '/' ? '/index-binary.html' : req.url;
    filePath = path.join(__dirname, filePath);

    const ext = path.extname(filePath);
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                filePath = path.join(__dirname, req.url === '/' ? '/index.html' : req.url);
                fs.readFile(filePath, (err2, content2) => {
                    if (err2) {
                        res.writeHead(404);
                        res.end('404 Not Found');
                    } else {
                        res.writeHead(200, { 'Content-Type': contentType });
                        res.end(content2);
                    }
                });
            } else {
                res.writeHead(500);
                res.end('500 Internal Server Error');
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

// =====================
// WebSocket Servers
// =====================
const wss = new WebSocketServer({ port: WS_PORT });
const browserClients = new Set();

const cameraWss = new WebSocketServer({ port: CAMERA_WS_PORT });
const cameraClients = new Set();

// =====================
// Binary Frame Reader (Java subprocess)
// =====================
class BinaryFrameReader {
    constructor() {
        this.process = null;
        this.running = false;
        this.totalFrames = 0;
        this.onFrame = null;
        this.restartTimer = null;
        this.frameBuffer = Buffer.alloc(0);
    }

    start() {
        if (this.process) {
            this.process.kill();
        }

        const javaExe = findJava();
        const existingJars = JNA_JARS.filter(f => fs.existsSync(f));
        let classpath = CLASSES_DIR;
        if (existingJars.length > 0) {
            classpath += path.delimiter + existingJars.join(path.delimiter);
        }

        console.log('[BinaryReader] Starting Java binary reader...');
        console.log('[BinaryReader] Classpath:', classpath);

        this.process = spawn(javaExe, [
            '--add-opens=java.base/java.lang=ALL-UNNAMED',
            '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
            '--add-opens=java.base/java.io=ALL-UNNAMED',
            '--add-opens=java.base/java.nio=ALL-UNNAMED',
            '--add-opens=java.base/sun.nio.ch=ALL-UNNAMED',
            '--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED',
            '-Djna.enableNativeAccess=ALL-UNNAMED',
            '-Djna.nosys=true',
            '-cp', classpath,
            'com.mshare.screen.SharedMemReader'
        ], {
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        });

        this.running = true;

        // Binary data comes through stdout
        this.process.stdout.on('data', (data) => {
            this.frameBuffer = Buffer.concat([this.frameBuffer, data]);
            this.parseFrames();
        });

        this.process.stderr.on('data', (data) => {
            console.log('[Java stderr]', data.toString().trim());
        });

        this.process.on('close', (code) => {
            console.log('[BinaryReader] Process exited with code:', code);
            this.running = false;
            this._scheduleRestart();
        });
    }

    parseFrames() {
        // Binary frame format:
        // 4 bytes: magic (0x4D435348)
        // 4 bytes: width
        // 4 bytes: height
        // 8 bytes: timestamp
        // N bytes: pixels (BGRA)
        const HEADER_SIZE = 20;
        const MAGIC = 0x4D435348;

        while (this.frameBuffer.length >= HEADER_SIZE) {
            const magic = this.frameBuffer.readInt32LE(0);
            if (magic !== MAGIC) {
                // Find next magic or discard
                const nextMagic = this.frameBuffer.indexOf('MCSH');
                if (nextMagic === -1 || nextMagic > 1000) {
                    // Too far, discard buffer
                    this.frameBuffer = Buffer.alloc(0);
                    return;
                }
                this.frameBuffer = this.frameBuffer.subarray(nextMagic);
                continue;
            }

            const width = this.frameBuffer.readInt32LE(4);
            const height = this.frameBuffer.readInt32LE(8);
            // timestamp at offset 12 (8 bytes)
            const pixelCount = width * height;

            // Validate dimensions
            if (width <= 0 || width > 3840 || height <= 0 || height > 2160) {
                console.log('[BinaryReader] Invalid dimensions:', width, 'x', height);
                this.frameBuffer = this.frameBuffer.subarray(HEADER_SIZE);
                continue;
            }

            const frameSize = HEADER_SIZE + pixelCount * 4;
            if (this.frameBuffer.length < frameSize) {
                // Wait for more data
                return;
            }

            // Extract frame
            const frameData = this.frameBuffer.subarray(0, frameSize);
            this.frameBuffer = this.frameBuffer.subarray(frameSize);

            // Parse frame
            const pixels = Buffer.alloc(pixelCount * 4);
            frameData.copy(pixels, 0, HEADER_SIZE, frameSize);

            this.totalFrames++;

            if (this.onFrame) {
                this.onFrame({
                    width,
                    height,
                    pixels,
                    frameCount: this.totalFrames
                });
            }
        }
    }

    stop() {
        if (this.restartTimer) {
            clearTimeout(this.restartTimer);
            this.restartTimer = null;
        }
        if (this.process) {
            this.process.kill();
            this.process = null;
        }
        this.running = false;
    }

    isRunning() {
        return this.running;
    }

    _scheduleRestart() {
        if (this.restartTimer) return;
        this.restartTimer = setTimeout(() => {
            this.restartTimer = null;
            this.start();
        }, 2000);
    }
}

// =====================
// Camera Data Reader (keep existing text-based)
// =====================
class CameraDataReader {
    constructor() {
        this.process = null;
        this.running = false;
        this.onCameraData = null;
        this.ringBuffer = { buffer: '' };

        this.ringBuffer.append = (data) => {
            this.ringBuffer.buffer += data;
            if (this.ringBuffer.buffer.length > 256 * 1024) {
                this.ringBuffer.buffer = this.ringBuffer.buffer.slice(-256 * 1024);
            }
        };

        this.ringBuffer.getLines = () => {
            const result = [];
            let idx;
            while ((idx = this.ringBuffer.buffer.indexOf('\n')) !== -1) {
                result.push(this.ringBuffer.buffer.substring(0, idx));
                this.ringBuffer.buffer = this.ringBuffer.buffer.substring(idx + 1);
            }
            return result;
        };
    }

    start() {
        if (this.process) this.process.kill();

        const javaExe = findJava();
        const existingJars = JNA_JARS.filter(f => fs.existsSync(f));
        let classpath = CLASSES_DIR;
        if (existingJars.length > 0) {
            classpath += path.delimiter + existingJars.join(path.delimiter);
        }

        console.log('[CameraReader] Starting Java camera reader...');

        this.process = spawn(javaExe, [
            '--add-opens=java.base/java.lang=ALL-UNNAMED',
            '--add-opens=java.base/java.lang.reflect=ALL-UNNAMED',
            '--add-opens=java.base/java.io=ALL-UNNAMED',
            '--add-opens=java.base/java.nio=ALL-UNNAMED',
            '--add-opens=java.base/sun.nio.ch=ALL-UNNAMED',
            '--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED',
            '-Djna.enableNativeAccess=ALL-UNNAMED',
            '-Djna.nosys=true',
            '-cp', classpath,
            'com.mshare.screen.CameraDataReader'
        ], {
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        });

        this.running = true;

        let inCamera = false;
        let cameraData = '';

        this.process.stdout.on('data', (data) => {
            this.ringBuffer.append(data.toString('utf8'));
            const lines = this.ringBuffer.getLines();

            for (const line of lines) {
                const trimmed = line.trim();
                if (trimmed === '[CameraReader] CAMERA_START') {
                    inCamera = true;
                    cameraData = '';
                } else if (trimmed === '[CameraReader] CAMERA_END') {
                    if (inCamera && cameraData && this.onCameraData) {
                        try {
                            this.onCameraData(JSON.parse(cameraData));
                        } catch (e) {}
                    }
                    inCamera = false;
                    cameraData = '';
                } else if (inCamera && trimmed.startsWith('[CameraReader] DATA:')) {
                    cameraData = trimmed.substring('[CameraReader] DATA:'.length);
                }
            }
        });

        this.process.stderr.on('data', (data) => {
            console.log('[CameraJS]', data.toString().trim());
        });

        this.process.on('close', () => {
            this.running = false;
        });
    }

    stop() {
        if (this.process) {
            this.process.kill();
            this.process = null;
        }
        this.running = false;
    }
}

// =====================
// Initialize Readers
// =====================
const frameReader = new BinaryFrameReader();
const cameraReader = new CameraDataReader();

frameReader.start();
cameraReader.start();

// =====================
// WebSocket Connection Handlers
// =====================
wss.on('connection', (ws, req) => {
    console.log(`[WS] Browser client connected: ${req.socket.remoteAddress}`);
    browserClients.add(ws);

    ws.on('close', () => {
        console.log('[WS] Browser client disconnected');
        browserClients.delete(ws);
    });
});

cameraWss.on('connection', (ws, req) => {
    console.log(`[CameraWS] Browser client connected: ${req.socket.remoteAddress}`);
    cameraClients.add(ws);

    ws.on('close', () => {
        console.log('[CameraWS] Browser client disconnected');
        cameraClients.delete(ws);
    });
});

// =====================
// Frame Forwarding
// =====================
frameReader.onFrame = (frame) => {
    // Send binary frame directly
    // Browser will use createImageData to render
    const message = Buffer.alloc(8 + frame.pixels.length);
    message.writeInt32LE(frame.width, 0);
    message.writeInt32LE(frame.height, 4);
    frame.pixels.copy(message, 8);

    for (const client of browserClients) {
        if (client.readyState === 1) {
            client.send(message);
        }
    }
};

cameraReader.onCameraData = (cameraData) => {
    const message = JSON.stringify({
        type: 'camera',
        ...cameraData
    });

    for (const client of cameraClients) {
        if (client.readyState === 1) {
            client.send(message);
        }
    }
};

// =====================
// Status Broadcast
// =====================
setInterval(() => {
    const status = JSON.stringify({
        type: 'status',
        connected: frameReader.isRunning(),
        totalFrames: frameReader.totalFrames,
        source: 'binary-transfer'
    });

    for (const client of browserClients) {
        if (client.readyState === 1) {
            client.send(status);
        }
    }
}, 1000);

// =====================
// Shutdown
// =====================
process.on('SIGINT', () => {
    console.log('\n[Server] Shutting down...');
    frameReader.stop();
    cameraReader.stop();
    httpServer.close();
    process.exit(0);
});

// =====================
// Start
// =====================
httpServer.listen(HTTP_PORT, () => {
    console.log(`[HTTP] Server running at http://localhost:${HTTP_PORT}/`);
});
console.log('[Server] Binary transfer server initialized');
console.log('[Server] Screen WS: ws://localhost:' + WS_PORT);
console.log('[Server] Camera WS: ws://localhost:' + CAMERA_WS_PORT);
