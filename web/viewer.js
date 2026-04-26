// WebSocket viewer for Minecraft Screen Capture
// Receives frames from the server via WebSocket

class ScreenViewer {
    constructor() {
        this.ws = null;
        this.connected = false;
        this.modConnected = false;
        this.frameCount = 0;
        this.lastFrameTime = 0;
        this.fps = 0;
        this.fpsUpdateTime = 0;
        this.lastDataTime = 0;

        // Initialize UI elements
        this.canvas = document.getElementById('screenCanvas');
        this.ctx = this.canvas.getContext('2d');
        this.statusEl = document.getElementById('connectionStatus');
        this.memorySizeEl = document.getElementById('memorySize');
        this.imageSizeEl = document.getElementById('imageSize');
        this.frameCountEl = document.getElementById('frameCount');
        this.fpsEl = document.getElementById('fps');
        this.overlayFpsEl = document.getElementById('overlayFps');
        this.lastUpdateEl = document.getElementById('lastUpdate');
        this.placeholderEl = document.getElementById('placeholder');
        this.connectBtn = document.getElementById('connectBtn');
        this.downloadBtn = document.getElementById('downloadBtn');

        // WebSocket URL - connect to our server at port 3001
        this.wsUrl = 'ws://localhost:3001';
    }

    toggleConnection() {
        if (this.connected) {
            this.disconnect();
        } else {
            this.connect();
        }
    }

    connect() {
        try {
            this.ws = new WebSocket(this.wsUrl);

            this.ws.onopen = () => {
                this.connected = true;
                this.updateConnectionStatus(true);
                console.log('[Viewer] Connected to screen capture server');
            };

            this.ws.onmessage = (event) => {
                if (typeof event.data === 'string') {
                    try {
                        const msg = JSON.parse(event.data);
                        console.log('[Viewer] Received message type:', msg.type, 'imageData length:', msg.imageData ? msg.imageData.length : 'none');
                        this.handleMessage(msg);
                    } catch (e) {
                        console.error('[Viewer] Failed to parse message:', e);
                    }
                }
            };

            this.ws.onclose = () => {
                this.connected = false;
                this.modConnected = false;
                this.updateConnectionStatus(false);
                console.log('Disconnected from server');
            };

            this.ws.onerror = (error) => {
                console.error('WebSocket error:', error);
            };
        } catch (e) {
            console.error('Failed to connect:', e);
        }
    }

    disconnect() {
        if (this.ws) {
            this.ws.close();
            this.ws = null;
        }
        this.connected = false;
        this.modConnected = false;
        this.updateConnectionStatus(false);
    }

    handleMessage(msg) {
        switch (msg.type) {
            case 'status':
                // Update mod connection status
                this.modConnected = msg.modConnected;
                this.memorySizeEl.textContent = msg.memoryName || '--';
                break;

            case 'frame':
                // Render the frame
                if (msg.imageData) {
                    this.imageSizeEl.textContent = `${msg.width} x ${msg.height}`;
                    this.renderImage(msg.imageData, msg.width, msg.height);
                }
                break;
        }
    }

    updateConnectionStatus(connected) {
        this.connected = connected;
        const statusText = this.modConnected ? 'Mod Connected' : (connected ? 'Server Only' : 'Disconnected');
        this.statusEl.textContent = statusText;
        this.statusEl.className = `status-value ${this.modConnected ? 'connected' : (connected ? 'connected' : 'disconnected')}`;
        this.connectBtn.textContent = connected ? 'Disconnect' : 'Connect';
        this.connectBtn.className = connected ? 'btn-disconnect' : 'btn-connect';
        this.downloadBtn.disabled = !connected;
    }

    renderImage(imageData, width, height) {
        console.log('[Viewer] renderImage called with data length:', imageData ? imageData.length : 'null', 'dims:', width, 'x', height);
        
        if (!imageData || imageData.length === 0) {
            console.error('[Viewer] No image data provided!');
            return;
        }
        
        // Create image from base64
        const img = new Image();
        img.onload = () => {
            console.log('[Viewer] Image loaded successfully, natural size:', img.naturalWidth, 'x', img.naturalHeight);
            
            // Resize canvas to match image dimensions
            const needResize = this.canvas.width !== width || this.canvas.height !== height;
            if (needResize) {
                this.canvas.width = width;
                this.canvas.height = height;
            }

            // ALWAYS clear the canvas before drawing to prevent stale pixel trails
            // from previous frames when the new frame is smaller or partially drawn
            this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

            // Draw image
            this.ctx.drawImage(img, 0, 0);

            // Hide placeholder
            this.placeholderEl.style.display = 'none';

            // Update stats
            this.frameCount++;
            this.frameCountEl.textContent = this.frameCount;

            // Update FPS
            const now = performance.now();
            if (this.fpsUpdateTime === 0) {
                this.fpsUpdateTime = now;
            }
            this.lastFrameTime = now;

            if (now - this.fpsUpdateTime >= 1000) {
                this.fps = Math.round(this.frameCount * 1000 / (now - this.fpsUpdateTime));
                this.fpsUpdateTime = now;
                this.frameCount = 0;
                this.fpsEl.textContent = this.fps;
                this.overlayFpsEl.textContent = this.fps;
            }

            // Update last update time
            const date = new Date();
            this.lastUpdateEl.textContent = date.toLocaleTimeString();
            this.lastDataTime = now;
        };
        
        img.onerror = (e) => {
            console.error('[Viewer] Image load error! First 100 chars of data:', imageData ? imageData.substring(0, 100) : 'null');
        };
        
        img.src = 'data:image/png;base64,' + imageData;
    }

    downloadFrame() {
        if (this.canvas.width > 0 && this.canvas.height > 0) {
            const link = document.createElement('a');
            link.download = `minecraft_screen_${Date.now()}.png`;
            link.href = this.canvas.toDataURL('image/png');
            link.click();
        }
    }
}

// Global instance
const viewer = new ScreenViewer();

// Auto-connect when page loads
window.addEventListener('load', () => {
    setTimeout(() => viewer.connect(), 500);
});

// Make toggleConnection available globally
function toggleConnection() {
    viewer.toggleConnection();
}

function downloadFrame() {
    viewer.downloadFrame();
}

// Also listen for frames via postMessage from parent
window.addEventListener('message', (event) => {
    if (event.data.type === 'screenFrame' && event.data.imageData) {
        viewer.renderImage(event.data.imageData, event.data.width, event.data.height);
    }
});

// Expose for external use
window.screenViewer = viewer;
