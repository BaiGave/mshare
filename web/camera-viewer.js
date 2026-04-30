// Camera 3D Viewer using Three.js
// Blender-style camera visualization with correct MC coordinate system

class Camera3DViewer {
    constructor() {
        this.scene = null;
        this.camera = null;
        this.renderer = null;
        this.cameraModel = null;
        this.cameraMarker = null;
        this.pulseRing = null;
        this.frustumHelper = null;
        this.arrowHelper = null;
        this.trailLine = null;
        this.positionHistory = [];
        this.maxHistoryLength = 100;

        this.settings = {
            showGrid: true,
            showAxes: true,
            showTrail: true,
            showFrustum: true
        };

        this.ws = null;
        this.connected = false;
        this.cameraData = null;
        this.lastUpdateTime = 0;

        this.CAMERA_WS_URL = 'ws://localhost:3002';
        this.THREE = null;

        // Smooth interpolation
        this.targetPosition = { x: 0, y: 0, z: 0 };
        this.currentPosition = { x: 0, y: 0, z: 0 };
        this.smoothFactor = 0.2;

        // Free camera controls
        this.isDragging = false;
        this.previousMousePosition = { x: 0, y: 0 };
        this.spherical = { radius: 30, theta: Math.PI / 4, phi: Math.PI / 3 };
        this.targetSpherical = { radius: 30, theta: Math.PI / 4, phi: Math.PI / 3 };

        // FPS tracking
        this.frameCount = 0;
        this.lastFpsUpdate = 0;

        this.init();
    }

    async init() {
        if (typeof THREE === 'undefined') {
            console.error('Three.js not loaded.');
            return;
        }
        this.THREE = THREE;

        this.canvas = document.getElementById('camera3d-canvas');
        if (!this.canvas) {
            console.error('Canvas element not found');
            return;
        }

        this.scene = new THREE.Scene();
        this.createBackground();
        this.createCamera();
        this.createRenderer();
        this.createScene();
        this.setupControls();
        this.connect();
        this.animate();
        window.addEventListener('resize', () => this.onResize());
    }

    createBackground() {
        const THREE = this.THREE;
        const canvas = document.createElement('canvas');
        canvas.width = 512;
        canvas.height = 512;
        const ctx = canvas.getContext('2d');
        const gradient = ctx.createRadialGradient(256, 256, 0, 256, 256, 400);
        gradient.addColorStop(0, '#1a1a2e');
        gradient.addColorStop(0.5, '#16213e');
        gradient.addColorStop(1, '#0f0f1a');
        ctx.fillStyle = gradient;
        ctx.fillRect(0, 0, 512, 512);
        const texture = new THREE.CanvasTexture(canvas);
        this.scene.background = texture;
    }

    createCamera() {
        const container = this.canvas.parentElement;
        const width = container.clientWidth || 450;
        const height = container.clientHeight || 350;

        this.camera = new THREE.PerspectiveCamera(50, width / height, 0.1, 10000);
        this.updateFreeCamera();
    }

    createRenderer() {
        const container = this.canvas.parentElement;
        const width = container.clientWidth || 450;
        const height = container.clientHeight || 350;

        this.renderer = new THREE.WebGLRenderer({
            canvas: this.canvas,
            antialias: true,
            alpha: true
        });
        this.renderer.setSize(width, height);
        this.renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
    }

    createScene() {
        const THREE = this.THREE;

        // Lights
        const ambientLight = new THREE.AmbientLight(0x606080, 0.6);
        this.scene.add(ambientLight);

        const mainLight = new THREE.DirectionalLight(0xffffff, 0.8);
        mainLight.position.set(50, 100, 50);
        this.scene.add(mainLight);

        // Ground plane
        const groundGeometry = new THREE.PlaneGeometry(500, 500);
        const groundMaterial = new THREE.MeshStandardMaterial({
            color: 0x1a1a2e,
            roughness: 0.9
        });
        const ground = new THREE.Mesh(groundGeometry, groundMaterial);
        ground.rotation.x = -Math.PI / 2;
        this.scene.add(ground);

        // Grid
        const gridHelper = new THREE.GridHelper(500, 50, 0x4ade80, 0x2a2a4e);
        gridHelper.position.y = 0.01;
        this.scene.add(gridHelper);

        // Axes
        this.createAxesHelper();

        // Player position marker
        this.createPlayerMarker();

        // Blender-style camera frustum
        this.createBlenderCamera();

        // Trail
        this.createTrail();
    }

    createAxesHelper() {
        const THREE = this.THREE;
        const axisLength = 15;

        // X axis (red) - MC X axis (East)
        const xMat = new THREE.MeshBasicMaterial({ color: 0xf87171 });
        const xPole = new THREE.Mesh(
            new THREE.CylinderGeometry(0.1, 0.1, axisLength, 8),
            xMat
        );
        xPole.rotation.z = Math.PI / 2;
        xPole.position.set(axisLength / 2, 0.05, 0);
        this.scene.add(xPole);

        // Z axis (blue) - MC Z axis (South)
        const zMat = new THREE.MeshBasicMaterial({ color: 0x60a5fa });
        const zPole = new THREE.Mesh(
            new THREE.CylinderGeometry(0.1, 0.1, axisLength, 8),
            zMat
        );
        zPole.position.set(0, 0.05, -axisLength / 2);
        this.scene.add(zPole);

        // Y axis (green) - MC Y axis (Up)
        const yMat = new THREE.MeshBasicMaterial({ color: 0x4ade80 });
        const yPole = new THREE.Mesh(
            new THREE.CylinderGeometry(0.1, 0.1, axisLength, 8),
            yMat
        );
        yPole.position.set(0, axisLength / 2, 0);
        this.scene.add(yPole);

        // Origin sphere
        const originSphere = new THREE.Mesh(
            new THREE.SphereGeometry(0.3, 16, 16),
            new THREE.MeshBasicMaterial({ color: 0xffffff })
        );
        originSphere.position.y = 0.3;
        this.scene.add(originSphere);

        // Axis labels
        this.createAxisLabel('X', new THREE.Vector3(axisLength + 2, 0.5, 0), 0xf87171);
        this.createAxisLabel('Z', new THREE.Vector3(0, 0.5, -axisLength - 2), 0x60a5fa);
        this.createAxisLabel('Y', new THREE.Vector3(0, axisLength + 2, 0), 0x4ade80);
    }

    createAxisLabel(text, position, color) {
        const THREE = this.THREE;
        const canvas = document.createElement('canvas');
        canvas.width = 64;
        canvas.height = 64;
        const ctx = canvas.getContext('2d');
        ctx.fillStyle = '#' + color.toString(16).padStart(6, '0');
        ctx.font = 'bold 40px Arial';
        ctx.textAlign = 'center';
        ctx.textBaseline = 'middle';
        ctx.fillText(text, 32, 32);
        const texture = new THREE.CanvasTexture(canvas);
        const material = new THREE.SpriteMaterial({ map: texture });
        const sprite = new THREE.Sprite(material);
        sprite.position.copy(position);
        sprite.scale.set(3, 3, 1);
        this.scene.add(sprite);
    }

    createPlayerMarker() {
        const THREE = this.THREE;

        // Ground ring marker
        const ringGeometry = new THREE.RingGeometry(0.8, 1.2, 32);
        const ringMaterial = new THREE.MeshBasicMaterial({
            color: 0x4ade80,
            side: THREE.DoubleSide,
            transparent: true,
            opacity: 0.8
        });
        this.cameraMarker = new THREE.Mesh(ringGeometry, ringMaterial);
        this.cameraMarker.rotation.x = -Math.PI / 2;
        this.cameraMarker.position.y = 0.02;
        this.scene.add(this.cameraMarker);

        // Pulsing outer ring
        const pulseGeometry = new THREE.RingGeometry(1.2, 1.4, 32);
        const pulseMaterial = new THREE.MeshBasicMaterial({
            color: 0x4ade80,
            side: THREE.DoubleSide,
            transparent: true,
            opacity: 0.4
        });
        this.pulseRing = new THREE.Mesh(pulseGeometry, pulseMaterial);
        this.pulseRing.rotation.x = -Math.PI / 2;
        this.pulseRing.position.y = 0.02;
        this.scene.add(this.pulseRing);

        // Vertical line
        const lineGeometry = new THREE.BufferGeometry().setFromPoints([
            new THREE.Vector3(0, 0, 0),
            new THREE.Vector3(0, 3, 0)
        ]);
        const lineMaterial = new THREE.LineBasicMaterial({
            color: 0x4ade80,
            transparent: true,
            opacity: 0.6
        });
        this.cameraLine = new THREE.Line(lineGeometry, lineMaterial);
        this.scene.add(this.cameraLine);
    }

    createBlenderCamera() {
        const THREE = this.THREE;

        // Create a proper hierarchy: yawNode -> pitchNode -> frustum
        this.yawNode = new THREE.Group();
        this.pitchNode = new THREE.Group();

        // Camera body (square box facing -Z by default)
        const bodyGeometry = new THREE.BoxGeometry(1.5, 1.0, 0.3);
        const bodyEdges = new THREE.EdgesGeometry(bodyGeometry);
        const bodyMaterial = new THREE.LineBasicMaterial({ color: 0x4ade80 });
        this.cameraBody = new THREE.LineSegments(bodyEdges, bodyMaterial);
        this.cameraBody.position.z = 0;
        this.pitchNode.add(this.cameraBody);

        // Lens (smaller box on front, which is -Z direction)
        const lensGeometry = new THREE.BoxGeometry(1.0, 0.7, 0.2);
        const lensEdges = new THREE.EdgesGeometry(lensGeometry);
        const lensMaterial = new THREE.LineBasicMaterial({ color: 0x60a5fa });
        this.cameraLens = new THREE.LineSegments(lensEdges, lensMaterial);
        this.cameraLens.position.z = -0.4;
        this.pitchNode.add(this.cameraLens);

        // Direction triangle on top
        const triShape = new THREE.Shape();
        triShape.moveTo(0, 0.5);
        triShape.lineTo(-0.3, 0);
        triShape.lineTo(0.3, 0);
        triShape.lineTo(0, 0.5);
        const triGeometry = new THREE.ShapeGeometry(triShape);
        const triMaterial = new THREE.MeshBasicMaterial({
            color: 0x4ade80,
            side: THREE.DoubleSide,
            transparent: true,
            opacity: 0.6
        });
        const triangle = new THREE.Mesh(triGeometry, triMaterial);
        triangle.position.set(0, 0.6, 0);
        this.pitchNode.add(triangle);

        // Build hierarchy: yawNode -> pitchNode -> frustum
        this.yawNode.add(this.pitchNode);
        this.scene.add(this.yawNode);

        // Create frustum as a child of pitchNode
        this.createFrustum();

        console.log('[Camera3D] Blender camera created');
    }

    createFrustum() {
        const THREE = this.THREE;

        if (this.frustumGroup) {
            this.pitchNode.remove(this.frustumGroup);
        }

        this.frustumGroup = new THREE.Group();

        const fov = 60;
        const near = 0.5;
        const far = 25;
        const halfFov = THREE.MathUtils.degToRad(fov / 2);

        const nearH = near * Math.tan(halfFov);
        const nearW = nearH;
        const farH = far * Math.tan(halfFov);
        const farW = farH;

        const nearTL = new THREE.Vector3(-nearW, nearH, -near);
        const nearTR = new THREE.Vector3(nearW, nearH, -near);
        const nearBL = new THREE.Vector3(-nearW, -nearH, -near);
        const nearBR = new THREE.Vector3(nearW, -nearH, -near);

        const farTL = new THREE.Vector3(-farW, farH, -far);
        const farTR = new THREE.Vector3(farW, farH, -far);
        const farBL = new THREE.Vector3(-farW, -farH, -far);
        const farBR = new THREE.Vector3(farW, -farH, -far);

        const frustumMaterial = new THREE.LineBasicMaterial({
            color: 0x60a5fa,
            transparent: true,
            opacity: 0.6
        });

        const edges = [
            [nearTL, nearTR], [nearTR, nearBR], [nearBR, nearBL], [nearBL, nearTL],
            [farTL, farTR], [farTR, farBR], [farBR, farBL], [farBL, farTL],
            [nearTL, farTL], [nearTR, farTR], [nearBL, farBL], [nearBR, farBR]
        ];

        edges.forEach(([start, end]) => {
            const geometry = new THREE.BufferGeometry().setFromPoints([start, end]);
            const line = new THREE.LineSegments(geometry, frustumMaterial);
            this.frustumGroup.add(line);
        });

        const planeMaterial = new THREE.MeshBasicMaterial({
            color: 0x60a5fa,
            transparent: true,
            opacity: 0.08,
            side: THREE.DoubleSide
        });

        const nearGeo = new THREE.PlaneGeometry(nearW * 2, nearH * 2);
        const nearPlane = new THREE.Mesh(nearGeo, planeMaterial);
        nearPlane.position.z = -near;
        this.frustumGroup.add(nearPlane);

        const farGeo = new THREE.PlaneGeometry(farW * 2, farH * 2);
        const farPlane = new THREE.Mesh(farGeo, planeMaterial);
        farPlane.position.z = -far;
        this.frustumGroup.add(farPlane);

        this.frustumGroup.position.z = -0.5;

        this.pitchNode.add(this.frustumGroup);
    }

    createTrail() {
        const THREE = this.THREE;
        const trailGeometry = new THREE.BufferGeometry();
        const trailPositions = new Float32Array(this.maxHistoryLength * 3);
        trailGeometry.setAttribute('position', new THREE.BufferAttribute(trailPositions, 3));

        const trailMaterial = new THREE.LineBasicMaterial({
            color: 0x4ade80,
            transparent: true,
            opacity: 0.4
        });

        this.trailLine = new THREE.Line(trailGeometry, trailMaterial);
        this.scene.add(this.trailLine);
    }

    connect() {
        try {
            this.ws = new WebSocket(this.CAMERA_WS_URL);

            this.ws.onopen = () => {
                this.connected = true;
                console.log('[Camera3D] Connected');
                this.updateConnectionStatus(true);
            };

            this.ws.onmessage = (event) => {
                if (typeof event.data === 'string') {
                    try {
                        const msg = JSON.parse(event.data);
                        if (msg.type === 'camera') {
                            this.updateCameraData(msg);
                        }
                    } catch (e) {
                        console.error('[Camera3D] Parse error:', e);
                    }
                }
            };

            this.ws.onclose = () => {
                this.connected = false;
                this.updateConnectionStatus(false);
                setTimeout(() => this.connect(), 2000);
            };

            this.ws.onerror = (error) => {
                console.error('[Camera3D] WebSocket error:', error);
            };

        } catch (e) {
            console.error('[Camera3D] Connection error:', e);
            setTimeout(() => this.connect(), 2000);
        }
    }

    updateCameraData(data) {
        this.cameraData = data;
        this.lastUpdateTime = performance.now();

        console.log('[Camera3D] Data:', {
            pitch: data.pitch.toFixed(1),
            yaw: data.yaw.toFixed(1),
            fov: data.fov
        });

        this.targetPosition = {
            x: data.x,
            y: data.y,
            z: data.z
        };

        if (this.currentPosition.x === 0 && this.currentPosition.y === 0 && this.currentPosition.z === 0) {
            this.currentPosition.x = data.x;
            this.currentPosition.y = data.y;
            this.currentPosition.z = data.z;
        }

        const posEl = document.getElementById('cam-pos');
        const pitchEl = document.getElementById('cam-pitch');
        const yawEl = document.getElementById('cam-yaw');
        const fovEl = document.getElementById('cam-fov');
        const typeEl = document.getElementById('cam-type');
        const detachedEl = document.getElementById('cam-detached');

        if (posEl) posEl.textContent = `(${data.x.toFixed(1)}, ${data.y.toFixed(1)}, ${data.z.toFixed(1)})`;
        if (pitchEl) pitchEl.textContent = `${data.pitch.toFixed(1)}°`;
        if (yawEl) yawEl.textContent = `${data.yaw.toFixed(1)}°`;
        if (fovEl) fovEl.textContent = `${data.fov.toFixed(0)}°`;

        if (typeEl) {
            const types = ['First Person', 'Third Person Back', 'Third Person Front'];
            typeEl.textContent = types[data.cameraType] || 'Unknown';
        }
        if (detachedEl) detachedEl.textContent = data.detached ? 'Yes' : 'No';

        this.updateCameraVisualization(data);
        this.addToTrail(data.x, data.y, data.z);
    }

    updateCameraVisualization(data) {
        const THREE = this.THREE;

        this.currentPosition.x += (this.targetPosition.x - this.currentPosition.x) * this.smoothFactor;
        this.currentPosition.y += (this.targetPosition.y - this.currentPosition.y) * this.smoothFactor;
        this.currentPosition.z += (this.targetPosition.z - this.currentPosition.z) * this.smoothFactor;

        if (this.cameraMarker) {
            this.cameraMarker.position.x = this.currentPosition.x;
            this.cameraMarker.position.z = this.currentPosition.z;
        }

        if (this.pulseRing) {
            this.pulseRing.position.x = this.currentPosition.x;
            this.pulseRing.position.z = this.currentPosition.z;
        }

        if (this.cameraLine) {
            const positions = this.cameraLine.geometry.attributes.position.array;
            positions[0] = this.currentPosition.x;
            positions[1] = 0;
            positions[2] = this.currentPosition.z;
            positions[3] = this.currentPosition.x;
            positions[4] = this.currentPosition.y + 3;
            positions[5] = this.currentPosition.z;
            this.cameraLine.geometry.attributes.position.needsUpdate = true;
        }

        const pitchDeg = -data.pitch;
        const yawDeg = 180 - data.yaw;

        if (this.yawNode && this.pitchNode) {
            this.yawNode.position.set(
                this.currentPosition.x,
                this.currentPosition.y + 1.5,
                this.currentPosition.z
            );

            this.yawNode.rotation.y = THREE.MathUtils.degToRad(yawDeg);
            this.pitchNode.rotation.x = THREE.MathUtils.degToRad(pitchDeg);
        }
    }

    addToTrail(x, y, z) {
        if (!this.settings.showTrail) return;

        this.positionHistory.push({ x, y, z });
        if (this.positionHistory.length > this.maxHistoryLength) {
            this.positionHistory.shift();
        }

        const positions = this.trailLine.geometry.attributes.position.array;
        for (let i = 0; i < this.positionHistory.length; i++) {
            const pos = this.positionHistory[i];
            positions[i * 3] = pos.x;
            positions[i * 3 + 1] = pos.y;
            positions[i * 3 + 2] = pos.z;
        }
        this.trailLine.geometry.attributes.position.needsUpdate = true;
        this.trailLine.geometry.setDrawRange(0, this.positionHistory.length);
    }

    updateConnectionStatus(connected) {
        const statusEl = document.getElementById('cam3d-status');
        if (statusEl) {
            statusEl.textContent = connected ? 'Connected' : 'Disconnected';
            statusEl.className = `status-badge ${connected ? 'connected' : ''}`;
        }
    }

    setupControls() {
        this.canvas.addEventListener('mousedown', (e) => {
            this.isDragging = true;
            this.previousMousePosition = { x: e.clientX, y: e.clientY };
            this.canvas.style.cursor = 'grabbing';
        });

        this.canvas.addEventListener('mousemove', (e) => {
            if (!this.isDragging) return;

            const deltaX = e.clientX - this.previousMousePosition.x;
            const deltaY = e.clientY - this.previousMousePosition.y;

            this.targetSpherical.theta -= deltaX * 0.01;
            this.targetSpherical.phi -= deltaY * 0.01;
            this.targetSpherical.phi = Math.max(0.1, Math.min(Math.PI - 0.1, this.targetSpherical.phi));

            this.previousMousePosition = { x: e.clientX, y: e.clientY };
        });

        this.canvas.addEventListener('mouseup', () => {
            this.isDragging = false;
            this.canvas.style.cursor = 'grab';
        });

        this.canvas.addEventListener('mouseleave', () => {
            this.isDragging = false;
            this.canvas.style.cursor = 'grab';
        });

        this.canvas.addEventListener('wheel', (e) => {
            e.preventDefault();
            this.targetSpherical.radius += e.deltaY * 0.05;
            this.targetSpherical.radius = Math.max(5, Math.min(500, this.targetSpherical.radius));
        });

        this.canvas.style.cursor = 'grab';
    }

    updateFreeCamera() {
        const THREE = this.THREE;

        this.spherical.radius += (this.targetSpherical.radius - this.spherical.radius) * 0.1;
        this.spherical.theta += (this.targetSpherical.theta - this.spherical.theta) * 0.1;
        this.spherical.phi += (this.targetSpherical.phi - this.spherical.phi) * 0.1;

        const x = this.spherical.radius * Math.sin(this.spherical.phi) * Math.cos(this.spherical.theta);
        const y = this.spherical.radius * Math.cos(this.spherical.phi);
        const z = this.spherical.radius * Math.sin(this.spherical.phi) * Math.sin(this.spherical.theta);

        this.camera.position.set(x, Math.max(y, 0.5), z);
        this.camera.lookAt(0, 1, 0);
    }

    onResize() {
        const container = this.canvas.parentElement;
        if (!container) return;

        const width = container.clientWidth || 450;
        const height = container.clientHeight || 350;

        this.camera.aspect = width / height;
        this.camera.updateProjectionMatrix();
        this.renderer.setSize(width, height);
    }

    animate() {
        requestAnimationFrame(() => this.animate());

        if (this.pulseRing) {
            const scale = 1 + Math.sin(performance.now() * 0.003) * 0.3;
            this.pulseRing.scale.set(scale, scale, 1);
            this.pulseRing.material.opacity = 0.4 - Math.sin(performance.now() * 0.003) * 0.2;
        }

        this.updateFreeCamera();

        if (this.renderer && this.scene && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }

        this.updateFPS();
    }

    updateFPS() {
        const fpsEl = document.getElementById('cam3d-fps');
        if (!fpsEl) return;

        const now = performance.now();
        this.frameCount++;

        if (now - this.lastFpsUpdate >= 1000) {
            fpsEl.textContent = this.frameCount;
            this.frameCount = 0;
            this.lastFpsUpdate = now;
        }
    }
}

let camera3DViewer = null;

window.addEventListener('DOMContentLoaded', () => {
    if (typeof THREE !== 'undefined') {
        camera3DViewer = new Camera3DViewer();
    } else {
        console.warn('Three.js not loaded yet, waiting...');
        const checkThree = setInterval(() => {
            if (typeof THREE !== 'undefined') {
                clearInterval(checkThree);
                camera3DViewer = new Camera3DViewer();
            }
        }, 100);
    }
});

window.camera3DViewer = camera3DViewer;
