// WebSocket + HTTP server for Minecraft Screen Capture
// Uses Java subprocess to read shared memory
// Run with: node server.js

const http = require('http');
const fs = require('fs');
const path = require('path');
const os = require('os');
const { WebSocketServer } = require('ws');
const { spawn } = require('child_process');

const HTTP_PORT = 3000;
const WS_PORT = 3001;
const CAMERA_WS_PORT = 3002;

// Base directory (mshare project root, relative to server.js)
const PROJECT_ROOT = path.join(__dirname, '..');

// Classes directory - extracted from the built JAR
const CLASSES_DIR = path.join(PROJECT_ROOT, 'build', 'classes', 'java', 'main');

// JNA jar paths - match the version used in build.gradle (5.15.0)
const JNA_JARS = [
    path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1',
        'net.java.dev.jna', 'jna', '5.15.0',
        '1ee1d80ff44f08280188f7c0e740d57207841ac', 'jna-5.15.0.jar'),
    path.join(os.homedir(), '.gradle', 'caches', 'modules-2', 'files-2.1',
        'net.java.dev.jna', 'jna-platform', '5.15.0',
        '86b502cad57d45da172b5e3231c537b042e296ef', 'jna-platform-5.15.0.jar')
];

/**
 * Find Java executable. Prefer Java 25 installed under user home,
 * fall back to PATH lookup.
 */
/**
 * Find Java 25+ executable. Searches common Windows install locations
 * in order, falls back to PATH lookup.
 */
function findJava() {
    const candidates = [
        // IntelliJ / Gradle user install (most common for dev machines)
        path.join(os.homedir(), '.jdks', 'jdk-25.0.2', 'bin', 'java.exe'),
        // Gradle JVM selection
        path.join(os.homedir(), '.gradle', 'jdks', 'jdk-25.0.2', 'windows-x86_64', 'jdk-25.0.2', 'bin', 'java.exe'),
        // Oracle / standard Program Files
        'C:\\Program Files\\Java\\jdk-25.0.2\\bin\\java.exe',
        'C:\\Program Files\\Eclipse Adoptium\\jdk-25.0.2-hotspot\\bin\\java.exe',
        'C:\\Program Files\\Amazon Corretto\\jdk25.0.2\\bin\\java.exe',
        'C:\\Program Files\\Microsoft\\jdk-25.0.2\\bin\\java.exe',
        'C:\\Program Files\\ Azul JDK\\jdk-25.0.2\\bin\\java.exe',
        // 32-bit variants
        'C:\\Program Files (x86)\\Java\\jdk-25.0.2\\bin\\java.exe',
    ];
    for (const javaHome of candidates) {
        if (fs.existsSync(javaHome)) {
            return javaHome;
        }
    }
    // Fallback: use java from PATH
    return 'java';
}

const mimeTypes = {
    '.html': 'text/html',
    '.js': 'application/javascript',
    '.css': 'text/css'
};

// Line buffer for parsing incomplete lines
const RING_BUFFER_SIZE = 256 * 1024;

class LineBuffer {
    constructor() {
        this.buffer = '';
    }
    
    append(data) {
        this.buffer += data;
        if (this.buffer.length > RING_BUFFER_SIZE) {
            this.buffer = this.buffer.slice(-RING_BUFFER_SIZE);
        }
    }
    
    getLines() {
        const result = [];
        let newlineIndex;
        while ((newlineIndex = this.buffer.indexOf('\n')) !== -1) {
            result.push(this.buffer.substring(0, newlineIndex));
            this.buffer = this.buffer.substring(newlineIndex + 1);
        }
        return result;
    }
    
    hasContent() {
        return this.buffer.length > 0;
    }
}

// =====================
// HTTP Server (Static Files)
// =====================
const httpServer = http.createServer((req, res) => {
    console.log(`[HTTP] ${req.method} ${req.url}`);

    let filePath = req.url === '/' ? '/index.html' : req.url;
    filePath = path.join(__dirname, filePath);

    const ext = path.extname(filePath);
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                res.writeHead(404, { 'Content-Type': 'text/plain' });
                res.end('404 Not Found');
            } else {
                res.writeHead(500, { 'Content-Type': 'text/plain' });
                res.end('500 Internal Server Error');
            }
        } else {
            res.writeHead(200, { 'Content-Type': contentType });
            res.end(content);
        }
    });
});

// =====================
// Shared Memory Reader (Java subprocess)
// =====================
class SharedMemoryReader {
    constructor() {
        this.process = null;
        this.running = false;
        this.totalFrames = 0;
        this.onFrame = null;
        this.restartTimer = null;

        this.parseState = {
            width: 0,
            height: 0,
            size: 0,
            data: '',
            inFrame: false,
            chunksSeen: 0
        };

        this.ringBuffer = new LineBuffer();
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

        console.log('[Java] Found', existingJars.length, 'JNA jars');
        console.log('[Java] Classes:', CLASSES_DIR);
        console.log('[Java] Starting Java subprocess...');

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
            'com.mshare.screen.SharedMemoryReader'
        ], {
            stdio: ['ignore', 'pipe', 'pipe'],
            windowsHide: true
        });

        this.running = true;

        this.process.stdout.on('data', (data) => {
            this.ringBuffer.append(data.toString('utf8'));
            const lines = this.ringBuffer.getLines();
            if (lines.length > 0) {
                this.parseLines(lines);
            }
        });

        this.process.stderr.on('data', (data) => {
            console.log('[Java stderr]', data.toString().trim());
        });

        this.process.on('close', (code) => {
            console.log('[Java] Process exited with code:', code);
            this.running = false;
            this._scheduleRestart();
        });

        this.process.on('error', (err) => {
            console.error('[Java] Process error:', err.message);
            this.running = false;
            this._scheduleRestart();
        });
    }

    _scheduleRestart() {
        // Clear any pending restart timer first
        if (this.restartTimer) {
            clearTimeout(this.restartTimer);
            this.restartTimer = null;
        }
        this.restartTimer = setTimeout(() => {
            if (!this.running) {
                console.log('[Java] Restarting reader...');
                this.start();
            }
        }, 2000);
    }

    parseLines(lines) {
        for (const line of lines) {
            const trimmed = line.trim();
            
            if (trimmed === '[Reader] FRAME_START') {
                this.parseState.inFrame = true;
                this.parseState.data = '';
                this.parseState.dataContinued = false;
                this.parseState.chunksSeen = 0;
            } else if (trimmed === '[Reader] FRAME_END') {
                if (this.parseState.inFrame && this.parseState.data) {
                    this.totalFrames++;
                    const dataLen = this.parseState.data.length;
                    console.log('[JS] Frame', this.totalFrames, '-', this.parseState.width, 'x', this.parseState.height, 
                        '- base64 length:', dataLen, '- chunks seen:', this.parseState.chunksSeen, '- header says:', this.parseState.size);

                    if (this.onFrame) {
                        this.onFrame({
                            imageData: this.parseState.data,
                            width: this.parseState.width,
                            height: this.parseState.height,
                            totalFrames: this.totalFrames
                        });
                    }
                }
                this.parseState.inFrame = false;
                this.parseState.data = '';
                this.parseState.dataContinued = false;
            } else if (this.parseState.inFrame) {
                if (trimmed.startsWith('[Reader] WIDTH:')) {
                    this.parseState.width = parseInt(trimmed.substring('[Reader] WIDTH:'.length));
                } else if (trimmed.startsWith('[Reader] HEIGHT:')) {
                    this.parseState.height = parseInt(trimmed.substring('[Reader] HEIGHT:'.length));
                } else if (trimmed.startsWith('[Reader] SIZE:')) {
                    this.parseState.size = parseInt(trimmed.substring('[Reader] SIZE:'.length));
                } else if (trimmed.startsWith('[Reader] BASE64_LENGTH:')) {
                    console.log('[JS] Java says base64 should be:', trimmed.substring('[Reader] BASE64_LENGTH:'.length), 'chars');
                } else if (trimmed.startsWith('[Reader] CHUNKS:')) {
                    console.log('[JS] Java says there are:', trimmed.substring('[Reader] CHUNKS:'.length), 'chunks');
                } else if (trimmed.startsWith('[Reader] DATA:')) {
                    if (!this.parseState.dataContinued) {
                        // First DATA line
                        this.parseState.data = trimmed.substring('[Reader] DATA:'.length);
                        this.parseState.dataContinued = true;
                        this.parseState.chunksSeen = 1;
                    } else {
                        // Additional DATA line
                        this.parseState.data += trimmed.substring('[Reader] DATA:'.length);
                        this.parseState.chunksSeen++;
                    }
                }
            } else if (trimmed.startsWith('[Reader]') && !trimmed.startsWith('[Reader] DATA:')) {
                const msg = trimmed.substring('[Reader]'.length).trim();
                if (msg && !msg.startsWith('WIDTH') && !msg.startsWith('HEIGHT') && !msg.startsWith('SIZE') && !msg.startsWith('DATA') && !msg.startsWith('FRAME') && !msg.startsWith('BASE64') && !msg.startsWith('CHUNKS')) {
                    console.log('[Java]', msg);
                }
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
}

// =====================
// Camera Data Reader (Java subprocess)
// =====================
class CameraDataReader {
    constructor() {
        this.process = null;
        this.running = false;
        this.totalUpdates = 0;
        this.onCameraData = null;
        this.restartTimer = null;

        this.parseState = {
            inCamera: false,
            data: ''
        };

        this.ringBuffer = new LineBuffer();
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

        console.log('[CameraJS] Camera classes:', CLASSES_DIR);

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

        this.process.stdout.on('data', (data) => {
            this.ringBuffer.append(data.toString('utf8'));
            const lines = this.ringBuffer.getLines();
            if (lines.length > 0) {
                this.parseLines(lines);
            }
        });

        this.process.stderr.on('data', (data) => {
            console.log('[CameraJS stderr]', data.toString().trim());
        });

        this.process.on('close', (code) => {
            console.log('[CameraJS] Process exited with code:', code);
            this.running = false;
            this._scheduleRestart();
        });

        this.process.on('error', (err) => {
            console.error('[CameraJS] Process error:', err.message);
            this.running = false;
            this._scheduleRestart();
        });
    }

    _scheduleRestart() {
        if (this.restartTimer) {
            clearTimeout(this.restartTimer);
            this.restartTimer = null;
        }
        this.restartTimer = setTimeout(() => {
            if (!this.running) {
                console.log('[CameraJS] Restarting reader...');
                this.start();
            }
        }, 2000);
    }

    parseLines(lines) {
        for (const line of lines) {
            const trimmed = line.trim();

            if (trimmed === '[CameraReader] CAMERA_START') {
                this.parseState.inCamera = true;
                this.parseState.data = '';
            } else if (trimmed === '[CameraReader] CAMERA_END') {
                if (this.parseState.inCamera && this.parseState.data) {
                    this.totalUpdates++;

                    if (this.onCameraData) {
                        try {
                            const cameraData = JSON.parse(this.parseState.data);
                            this.onCameraData(cameraData);
                        } catch (e) {
                            console.error('[CameraJS] JSON parse error:', e.message);
                        }
                    }
                }
                this.parseState.inCamera = false;
                this.parseState.data = '';
            } else if (this.parseState.inCamera) {
                if (trimmed.startsWith('[CameraReader] DATA:')) {
                    this.parseState.data = trimmed.substring('[CameraReader] DATA:'.length);
                }
            } else if (trimmed.startsWith('[CameraReader]') && !trimmed.startsWith('[CameraReader] DATA:')) {
                const msg = trimmed.substring('[CameraReader]'.length).trim();
                if (msg && !msg.startsWith('CAMERA') && !msg.startsWith('DATA')) {
                    console.log('[CameraJS]', msg);
                }
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
}

// =====================
// WebSocket Server
// =====================
const wss = new WebSocketServer({ port: WS_PORT });
const browserClients = new Set();
const reader = new SharedMemoryReader();

// =====================
// Camera WebSocket Server
// =====================
const cameraWss = new WebSocketServer({ port: CAMERA_WS_PORT });
const cameraClients = new Set();
const cameraReader = new CameraDataReader();

// Start both readers
reader.start();
cameraReader.start();

// Forward frames to WebSocket clients
reader.onFrame = (frame) => {
    const message = JSON.stringify({
        type: 'frame',
        imageData: frame.imageData,
        width: frame.width,
        height: frame.height,
        totalFrames: frame.totalFrames
    });

    for (const client of browserClients) {
        if (client.readyState === 1) {
            client.send(message);
        }
    }
};

// Forward camera data to WebSocket clients
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

// Broadcast status periodically
setInterval(() => {
    const statusMsg = JSON.stringify({
        type: 'status',
        modConnected: reader.isRunning(),
        totalFrames: reader.totalFrames,
        source: 'shared-memory-java'
    });

    for (const client of browserClients) {
        if (client.readyState === 1) {
            client.send(statusMsg);
        }
    }
}, 1000);

// =====================
// Handle browser WebSocket connections
// =====================
wss.on('connection', (ws, req) => {
    const clientIp = req.socket.remoteAddress;
    console.log(`[WS] Browser client connected: ${clientIp}`);
    browserClients.add(ws);

    ws.send(JSON.stringify({
        type: 'status',
        modConnected: reader.isRunning(),
        totalFrames: reader.totalFrames,
        source: 'shared-memory-java'
    }));

    ws.on('close', () => {
        console.log(`[WS] Browser client disconnected: ${clientIp}`);
        browserClients.delete(ws);
    });
});

// =====================
// Handle camera WebSocket connections
// =====================
cameraWss.on('connection', (ws, req) => {
    const clientIp = req.socket.remoteAddress;
    console.log(`[CameraWS] Browser client connected: ${clientIp}`);
    cameraClients.add(ws);

    ws.on('close', () => {
        console.log(`[CameraWS] Browser client disconnected: ${clientIp}`);
        cameraClients.delete(ws);
    });
});

// =====================
// Start HTTP Server
// =====================
httpServer.listen(HTTP_PORT, () => {
    console.log(`
╔════════════════════════════════════════════════════════════════╗
║           Minecraft Screen Capture + Camera Web Viewer        ║
║              (Using Shared Memory via Java)                  ║
╠════════════════════════════════════════════════════════════════╣
║  HTTP Server:  http://localhost:${HTTP_PORT}                       ║
║  WS Server:   ws://localhost:${WS_PORT}                            ║
║  Camera WS:   ws://localhost:${CAMERA_WS_PORT}                       ║
║                                                                ║
║  Method: Java subprocess reads shared memory                   ║
║                                                                ║
║  1. Make sure Minecraft with the mod is running              ║
║  2. Open http://localhost:${HTTP_PORT} in your browser            ║
║  3. The viewer will automatically display captured frames     ║
║  4. Camera data is broadcast on port ${CAMERA_WS_PORT}               ║
║                                                                ║
║  Press Ctrl+C to stop                                        ║
╚════════════════════════════════════════════════════════════════╝
    `);
});

process.on('SIGINT', () => {
    console.log('\nShutting down...');
    reader.stop();
    cameraReader.stop();
    wss.close();
    cameraWss.close();
    httpServer.close();
    process.exit(0);
});
