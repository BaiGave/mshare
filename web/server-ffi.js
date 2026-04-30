/**
 * server-ffi.js - WebSocket server using direct FFI shared memory reading
 * Zero-copy approach: Node.js reads shared memory directly via kernel32.dll
 * Run with: node server-ffi.js
 */

const http = require('http');
const fs = require('fs');
const path = require('path');
const { WebSocketServer } = require('ws');
const SharedMemoryReader = require('./shared-memory-reader');

const HTTP_PORT = 3000;
const WS_PORT = 3001;
const CAMERA_WS_PORT = 3002;

// MIME types
const mimeTypes = {
    '.html': 'text/html',
    '.js': 'application/javascript',
    '.css': 'text/css',
    '.png': 'image/png',
    '.jpg': 'image/jpeg',
    '.gif': 'image/gif',
};

// =====================
// HTTP Server (Static Files)
// =====================
const httpServer = http.createServer((req, res) => {
    console.log(`[HTTP] ${req.method} ${req.url}`);

    let filePath = req.url === '/' ? '/index-ffi.html' : req.url;
    filePath = path.join(__dirname, filePath);

    const ext = path.extname(filePath);
    const contentType = mimeTypes[ext] || 'application/octet-stream';

    fs.readFile(filePath, (err, content) => {
        if (err) {
            if (err.code === 'ENOENT') {
                // Fallback to regular index.html
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
// Screen WebSocket Server
// =====================
const wss = new WebSocketServer({ port: WS_PORT });
const browserClients = new Set();

// =====================
// Camera WebSocket Server
// =====================
const cameraWss = new WebSocketServer({ port: CAMERA_WS_PORT });
const cameraClients = new Set();

// =====================
// Shared Memory Readers (FFI - direct memory access)
// =====================
let screenReader = null;
let cameraReader = null;
let totalFrames = 0;

function initReaders() {
    // Screen capture reader
    screenReader = new SharedMemoryReader('MinecraftScreenCapture');

    if (screenReader.open()) {
        console.log('[SM] Screen capture reader initialized');
        screenReader.start((frame) => {
            totalFrames++;

            // Send binary frame data directly
            // Format: 4 bytes width + 4 bytes height + 8 bytes timestamp + pixels
            const headerSize = 16;
            const frameData = Buffer.alloc(headerSize + frame.pixels.length);

            frameData.writeInt32LE(frame.width, 0);
            frameData.writeInt32LE(frame.height, 4);
            frameData.writeBigInt64LE(BigInt(frame.timestamp), 8);
            frame.pixels.copy(frameData, headerSize);

            for (const client of browserClients) {
                if (client.readyState === 1) {
                    client.send(frameData);
                }
            }
        });
    } else {
        console.log('[SM] Failed to open screen capture shared memory');
    }

    // Camera data reader (still uses Java subprocess for simplicity)
    // TODO: Implement FFI camera reader
}

// =====================
// WebSocket Connection Handlers
// =====================
wss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    console.log(`[WS] Browser client connected: ${ip}`);
    browserClients.add(ws);

    ws.on('message', (message) => {
        console.log('[WS] Received:', message.toString());
    });

    ws.on('close', () => {
        console.log(`[WS] Browser client disconnected: ${ip}`);
        browserClients.delete(ws);
    });
});

cameraWss.on('connection', (ws, req) => {
    const ip = req.socket.remoteAddress;
    console.log(`[CameraWS] Browser client connected: ${ip}`);
    cameraClients.add(ws);

    ws.on('message', (message) => {
        console.log('[CameraWS] Received:', message.toString());
    });

    ws.on('close', () => {
        console.log(`[CameraWS] Browser client disconnected: ${ip}`);
        cameraClients.delete(ws);
    });
});

// =====================
// Status Broadcast
// =====================
setInterval(() => {
    const status = {
        type: 'status',
        connected: screenReader !== null && screenReader.running,
        totalFrames: totalFrames,
        source: 'ffi-direct'
    };

    for (const client of browserClients) {
        if (client.readyState === 1) {
            client.send(JSON.stringify(status));
        }
    }
}, 1000);

// =====================
// Graceful Shutdown
// =====================
process.on('SIGINT', () => {
    console.log('\n[Server] Shutting down...');

    if (screenReader) {
        screenReader.close();
    }

    wss.close();
    cameraWss.close();
    httpServer.close();

    process.exit(0);
});

// =====================
// Start Server
// =====================
httpServer.listen(HTTP_PORT, () => {
    console.log(`[HTTP] Server running at http://localhost:${HTTP_PORT}/`);
});

initReaders();
console.log('[Server] FFI-based server initialized');
console.log('[Server] Screen WS: ws://localhost:' + WS_PORT);
console.log('[Server] Camera WS: ws://localhost:' + CAMERA_WS_PORT);
