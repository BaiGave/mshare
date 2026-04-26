// Camera 3D Viewer using Three.js
// Blender-style camera visualization with correct MC coordinate system
// Now includes integrated mesh rendering

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
            showFrustum: true,
            showMesh: true
        };

        this.ws = null;
        this.meshWs = null;
        this.connected = false;
        this.meshConnected = false;
        this.cameraData = null;
        this.lastUpdateTime = 0;

        this.CAMERA_WS_URL = 'ws://localhost:3002';
        this.MESH_WS_URL = 'ws://localhost:3003';
        this.THREE = null;

        // Mesh rendering
        this.meshesContainer = null;
        this.meshGroups = new Map();
        this.currentMeshCount = 0;
        this.currentVertexCount = 0;
        this.currentTriangleCount = 0;
        this.meshFrameCount = 0;
        this.lastMeshFpsUpdate = 0;
        this.meshFpsFrameCount = 0;

        // View mode
        this.viewMode = 'combined'; // 'combined', 'mesh', 'camera'

        // Smooth interpolation
        this.targetPosition = { x: 0, y: 0, z: 0 };
        this.currentPosition = { x: 0, y: 0, z: 0 };
        this.smoothFactor = 0.2;

        // Free camera controls
        this.isDragging = false;
        this.previousMousePosition = { x: 0, y: 0 };
        this.spherical = { radius: 30, theta: Math.PI / 4, phi: Math.PI / 3 };
        this.targetSpherical = { radius: 30, theta: Math.PI / 4, phi: Math.PI / 3 };

        // User zoom tracking - prevents auto-zoom from overriding user zoom
        this.userHasZoomed = false;
        this.lastUserZoomTime = 0;
        this.autoZoomCompleted = false;

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
        this.createMeshContainer();
        this.setupControls();
        this.connect();
        this.connectMesh();
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

    createMeshContainer() {
        const THREE = this.THREE;

        // Container for mesh objects
        this.meshesContainer = new THREE.Group();
        this.meshesContainer.name = 'meshes';
        this.scene.add(this.meshesContainer);

        console.log('[Camera3D] Mesh container created');
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
        this.cameraBody.position.z = 0; // Center of pitch node
        this.pitchNode.add(this.cameraBody);

        // Lens (smaller box on front, which is -Z direction)
        const lensGeometry = new THREE.BoxGeometry(1.0, 0.7, 0.2);
        const lensEdges = new THREE.EdgesGeometry(lensGeometry);
        const lensMaterial = new THREE.LineBasicMaterial({ color: 0x60a5fa });
        this.cameraLens = new THREE.LineSegments(lensEdges, lensMaterial);
        this.cameraLens.position.z = -0.4; // Front of camera = negative Z
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

        const fov = 60; // degrees
        const near = 0.5;
        const far = 25;
        const halfFov = THREE.MathUtils.degToRad(fov / 2);

        // Calculate frustum dimensions
        const nearH = near * Math.tan(halfFov);
        const nearW = nearH; // Square frustum
        const farH = far * Math.tan(halfFov);
        const farW = farH;

        // Vertices for square pyramid
        // Camera looks in -Z direction, so frustum extends backward
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

        // Create wireframe edges
        const edges = [
            // Near rectangle
            [nearTL, nearTR], [nearTR, nearBR], [nearBR, nearBL], [nearBL, nearTL],
            // Far rectangle
            [farTL, farTR], [farTR, farBR], [farBR, farBL], [farBL, farTL],
            // Connecting lines
            [nearTL, farTL], [nearTR, farTR], [nearBL, farBL], [nearBR, farBR]
        ];

        edges.forEach(([start, end]) => {
            const geometry = new THREE.BufferGeometry().setFromPoints([start, end]);
            const line = new THREE.LineSegments(geometry, frustumMaterial);
            this.frustumGroup.add(line);
        });

        // Filled planes (semi-transparent)
        const planeMaterial = new THREE.MeshBasicMaterial({
            color: 0x60a5fa,
            transparent: true,
            opacity: 0.08,
            side: THREE.DoubleSide
        });

        // Near plane
        const nearGeo = new THREE.PlaneGeometry(nearW * 2, nearH * 2);
        const nearPlane = new THREE.Mesh(nearGeo, planeMaterial);
        nearPlane.position.z = -near;
        this.frustumGroup.add(nearPlane);

        // Far plane
        const farGeo = new THREE.PlaneGeometry(farW * 2, farH * 2);
        const farPlane = new THREE.Mesh(farGeo, planeMaterial);
        farPlane.position.z = -far;
        this.frustumGroup.add(farPlane);

        // Position frustum in front of camera body
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

    // Mesh rendering methods
    connectMesh() {
        try {
            this.meshWs = new WebSocket(this.MESH_WS_URL);

            this.meshWs.onopen = () => {
                this.meshConnected = true;
                console.log('[Camera3D] Mesh WebSocket connected');
                this.updateMeshConnectionStatus(true);
            };

            this.meshWs.onmessage = (event) => {
                if (typeof event.data === 'string') {
                    try {
                        const msg = JSON.parse(event.data);
                        if (msg.type === 'mesh') {
                            this.onMeshData(msg);
                        }
                    } catch (e) {
                        console.error('[Camera3D] Mesh parse error:', e);
                    }
                }
            };

            this.meshWs.onclose = () => {
                this.meshConnected = false;
                this.updateMeshConnectionStatus(false);
                setTimeout(() => this.connectMesh(), 2000);
            };

            this.meshWs.onerror = (error) => {
                console.error('[Camera3D] Mesh WebSocket error:', error);
            };

        } catch (e) {
            console.error('[Camera3D] Mesh connection error:', e);
            setTimeout(() => this.connectMesh(), 2000);
        }
    }

    onMeshData(data) {
        this.meshFpsFrameCount++;
        this.meshFrameCount = data.frame || 0;
        this.currentMeshCount = data.meshCount || 0;
        this.currentVertexCount = data.vertexCount || 0;
        this.currentTriangleCount = data.triangleCount || 0;

        console.log(`[Camera3D] Mesh data received: ${data.meshCount} meshes, ${data.vertexCount} vertices, frame ${data.frame}`);
        console.log(`[Camera3D] Vertex data present: ${!!data.vertexData}, length: ${data.vertexData ? data.vertexData.length : 0}`);
        console.log(`[Camera3D] Meshes array: ${data.meshes ? data.meshes.length : 0} items`);

        // Update UI stats
        const meshCountEl = document.getElementById('mesh-count');
        const vertCountEl = document.getElementById('mesh-vertices');
        const triCountEl = document.getElementById('mesh-triangles');
        const frameEl = document.getElementById('mesh-frame');

        if (meshCountEl) meshCountEl.textContent = this.currentMeshCount;
        if (vertCountEl) vertCountEl.textContent = this.currentVertexCount.toLocaleString();
        if (triCountEl) triCountEl.textContent = this.currentTriangleCount.toLocaleString();
        if (frameEl) frameEl.textContent = this.meshFrameCount;

        // Render mesh data
        if (data.vertexData && data.meshes && data.meshes.length > 0) {
            try {
                console.log('[Camera3D] Decoding vertex data...');
                const vertices = this.decodeVertexData(data.vertexData);
                console.log(`[Camera3D] Decoded ${vertices.length} vertices`);
                console.log('[Camera3D] Rendering meshes...');
                this.renderMeshes(data.meshes, vertices);
                console.log('[Camera3D] Mesh rendering complete');
            } catch (e) {
                console.error('[Camera3D] Vertex decode/render error:', e);
            }
        } else {
            console.warn('[Camera3D] No vertex data or meshes to render');
            if (!data.vertexData) console.warn('[Camera3D] - vertexData is missing');
            if (!data.meshes) console.warn('[Camera3D] - meshes is missing');
            if (data.meshes && data.meshes.length === 0) console.warn('[Camera3D] - meshes array is empty');
        }
    }

    decodeVertexData(base64Data) {
        const binaryString = atob(base64Data);
        const bytes = new Uint8Array(binaryString.length);
        for (let i = 0; i < binaryString.length; i++) {
            bytes[i] = binaryString.charCodeAt(i);
        }

        const view = new DataView(bytes.buffer, bytes.byteOffset, bytes.byteLength);
        const vertices = [];
        const stride = 36;
        const vertexCount = bytes.length / stride;

        for (let i = 0; i < vertexCount; i++) {
            const offset = i * stride;
            vertices.push({
                x: view.getFloat32(offset, true),
                y: view.getFloat32(offset + 4, true),
                z: view.getFloat32(offset + 8, true),
                color: view.getInt32(offset + 12, true),
                u: view.getFloat32(offset + 16, true),
                v: view.getFloat32(offset + 20, true),
                nx: view.getFloat32(offset + 24, true),
                ny: view.getFloat32(offset + 28, true),
                nz: view.getFloat32(offset + 32, true)
            });
        }

        return vertices;
    }

    renderMeshes(meshInfo, vertices) {
        const THREE = this.THREE;

        // Clear old meshes
        while (this.meshesContainer.children.length > 0) {
            const child = this.meshesContainer.children[0];
            if (child.geometry) child.geometry.dispose();
            if (child.material) {
                if (Array.isArray(child.material)) {
                    child.material.forEach(m => m.dispose());
                } else {
                    child.material.dispose();
                }
            }
            this.meshesContainer.remove(child);
        }

        // Render each mesh
        let meshIndex = 0;
        const maxMeshes = 500;

        console.log(`[Camera3D] Processing ${meshInfo.length} meshes from shared memory`);

        for (const mesh of meshInfo) {
            if (meshIndex >= maxMeshes) break;

            const startVert = mesh.vertexOffset;
            const vertCount = mesh.vertexCount;

            console.log(`[Camera3D] Mesh ${meshIndex}: name="${mesh.name}", offset=${startVert}, verts=${vertCount}`);

            if (vertCount < 3) {
                console.log(`[Camera3D] Skipping mesh ${meshIndex} - not enough vertices (${vertCount})`);
                continue;
            }

            const meshVertices = vertices.slice(startVert, startVert + vertCount);
            if (meshVertices.length === 0) {
                console.log(`[Camera3D] Skipping mesh ${meshIndex} - no vertices found`);
                continue;
            }

            console.log(`[Camera3D] Creating geometry for mesh ${meshIndex} with ${vertCount} vertices`);

            const geometry = new THREE.BufferGeometry();
            const positions = new Float32Array(vertCount * 3);
            const colors = new Float32Array(vertCount * 3);
            const normals = new Float32Array(vertCount * 3);

            // Sample first vertex for debugging
            if (meshIndex === 0 && meshVertices.length > 0) {
                const v0 = meshVertices[0];
                console.log(`[Camera3D] First vertex sample: pos=(${v0.x}, ${v0.y}, ${v0.z}), color=0x${v0.color.toString(16)}, uv=(${v0.u}, ${v0.v})`);
            }

            for (let i = 0; i < vertCount; i++) {
                const v = meshVertices[i];
                positions[i * 3] = v.x;
                positions[i * 3 + 1] = v.y;
                positions[i * 3 + 2] = v.z;

                const a = (v.color >> 24) & 0xFF;
                const r = (v.color >> 16) & 0xFF;
                const g = (v.color >> 8) & 0xFF;
                const b = v.color & 0xFF;
                colors[i * 3] = r / 255;
                colors[i * 3 + 1] = g / 255;
                colors[i * 3 + 2] = b / 255;

                normals[i * 3] = v.nx;
                normals[i * 3 + 1] = v.ny;
                normals[i * 3 + 2] = v.nz;
            }

            geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
            geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));
            geometry.setAttribute('normal', new THREE.BufferAttribute(normals, 3));

            const indices = [];
            const triangleCount = Math.floor(vertCount / 3);
            for (let i = 0; i < triangleCount; i++) {
                indices.push(i * 3, i * 3 + 1, i * 3 + 2);
            }
            geometry.setIndex(indices);

            const name = mesh.name || '';
            const baseColor = this.getColorForType(name);

            const material = new THREE.MeshStandardMaterial({
                vertexColors: true,
                roughness: 0.7,
                metalness: 0.1,
                side: THREE.DoubleSide
            });

            const meshObj = new THREE.Mesh(geometry, material);
            meshObj.name = name;

            this.meshesContainer.add(meshObj);
            meshIndex++;
        }

        console.log(`[Camera3D] Added ${meshIndex} meshes to scene`);

            // Only auto-zoom on first successful render, and only if user hasn't zoomed manually
            if (!this.autoZoomCompleted && this.meshesContainer.children.length > 0) {
                const box = new THREE.Box3().setFromObject(this.meshesContainer);
                const center = new THREE.Vector3();
                box.getCenter(center);

                // Adjust camera and meshes for better view
                const size = box.getSize(new THREE.Vector3());
                const maxDim = Math.max(size.x, size.y, size.z);
                if (maxDim > 0) {
                    const targetRadius = maxDim * 2.5;
                    this.targetSpherical.radius = Math.max(10, Math.min(500, targetRadius));
                    console.log(`[Camera3D] Initial auto-zoom set to radius: ${this.targetSpherical.radius.toFixed(1)}`);
                }

                this.autoZoomCompleted = true;
                console.log(`[Camera3D] Rendered ${meshIndex} meshes, center: (${center.x.toFixed(1)}, ${center.y.toFixed(1)}, ${center.z.toFixed(1)})`);
            } else if (this.meshesContainer.children.length > 0) {
                console.log(`[Camera3D] Rendered ${meshIndex} meshes (keeping user zoom)`);
            }
    }

    getColorForType(name) {
        const nameLower = (name || '').toLowerCase();

        if (nameLower.includes('solid') || nameLower.includes('block')) {
            return 0x8B4513;
        } else if (nameLower.includes('cutout')) {
            return 0x228B22;
        } else if (nameLower.includes('translucent') || nameLower.includes('water')) {
            return 0x4169E1;
        } else if (nameLower.includes('entity') || nameLower.includes('item')) {
            return 0xFFD700;
        }
        return 0x808080;
    }

    updateMeshConnectionStatus(connected) {
        const statusEl = document.getElementById('cam3d-status');
        if (statusEl) {
            const text = connected ? 'Connected' : 'Disconnected';
            const meshText = this.meshConnected ? ' +Mesh' : '';
            statusEl.textContent = text + meshText;
            statusEl.className = `status-badge ${connected ? 'connected' : ''}`;
        }
    }

    updateConnectionStatus(connected) {
        this.connected = connected;
        const statusEl = document.getElementById('cam3d-status');
        if (statusEl) {
            const meshText = this.meshConnected ? ' +Mesh' : '';
            statusEl.textContent = (connected ? 'Connected' : 'Disconnected') + meshText;
            statusEl.className = `status-badge ${connected ? 'connected' : ''}`;
        }
    }

    setViewMode(mode) {
        this.viewMode = mode;

        // Update mesh visibility
        if (this.meshesContainer) {
            this.meshesContainer.visible = (mode === 'combined' || mode === 'mesh');
        }

        // Update camera model visibility
        if (this.yawNode) {
            this.yawNode.visible = (mode === 'combined' || mode === 'camera');
        }

        // Adjust view
        if (mode === 'top') {
            this.targetSpherical.theta = 0;
            this.targetSpherical.phi = 0.05;
            this.targetSpherical.radius = 100;
        } else if (mode === 'front') {
            this.targetSpherical.theta = Math.PI;
            this.targetSpherical.phi = Math.PI / 2;
            this.targetSpherical.radius = 50;
        } else {
            this.targetSpherical.theta = Math.PI / 4;
            this.targetSpherical.phi = Math.PI / 3;
            this.targetSpherical.radius = 30;
        }
    }

    setupControls() {
        const THREE = this.THREE;

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

            // Mark that user has zoomed manually - this will prevent auto-zoom overrides
            this.userHasZoomed = true;
            this.lastUserZoomTime = performance.now();
            this.autoZoomCompleted = true; // Once user zooms, don't auto-zoom again
        });

        this.canvas.style.cursor = 'grab';
    }

    updateFreeCamera() {
        const THREE = this.THREE;

        // Smooth interpolation of orbit parameters
        this.spherical.radius += (this.targetSpherical.radius - this.spherical.radius) * 0.1;
        this.spherical.theta += (this.targetSpherical.theta - this.spherical.theta) * 0.1;
        this.spherical.phi += (this.targetSpherical.phi - this.spherical.phi) * 0.1;

        // Spherical to Cartesian (Y up)
        const x = this.spherical.radius * Math.sin(this.spherical.phi) * Math.cos(this.spherical.theta);
        const y = this.spherical.radius * Math.cos(this.spherical.phi);
        const z = this.spherical.radius * Math.sin(this.spherical.phi) * Math.sin(this.spherical.theta);

        // User's orbit camera is FIXED - it does NOT follow the player
        // It orbits around world origin (0, 0, 0)
        // Only the 3D camera model in the scene follows the game camera
        this.camera.position.set(x, Math.max(y, 0.5), z);
        this.camera.lookAt(0, 1, 0);
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

        // Debug output
        console.log('[Camera3D] Data:', {
            pitch: data.pitch.toFixed(1),
            yaw: data.yaw.toFixed(1),
            fov: data.fov
        });

        // Update target position
        this.targetPosition = {
            x: data.x,
            y: data.y,
            z: data.z
        };

        // Initialize position if first update
        if (this.currentPosition.x === 0 && this.currentPosition.y === 0 && this.currentPosition.z === 0) {
            this.currentPosition.x = data.x;
            this.currentPosition.y = data.y;
            this.currentPosition.z = data.z;
        }

        // Update UI
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

        // Update 3D visualization
        this.updateCameraVisualization(data);

        // Add to trail
        this.addToTrail(data.x, data.y, data.z);
    }

    updateCameraVisualization(data) {
        const THREE = this.THREE;

        // Smooth interpolation for position
        this.currentPosition.x += (this.targetPosition.x - this.currentPosition.x) * this.smoothFactor;
        this.currentPosition.y += (this.targetPosition.y - this.currentPosition.y) * this.smoothFactor;
        this.currentPosition.z += (this.targetPosition.z - this.currentPosition.z) * this.smoothFactor;

        // Update marker position
        if (this.cameraMarker) {
            this.cameraMarker.position.x = this.currentPosition.x;
            this.cameraMarker.position.z = this.currentPosition.z;
        }

        // Update pulse ring
        if (this.pulseRing) {
            this.pulseRing.position.x = this.currentPosition.x;
            this.pulseRing.position.z = this.currentPosition.z;
        }

        // Update vertical line
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

        // MC Camera 旋转系统 (来自 Camera.java setRotation):
        // rotationYXZ(π - yRot, -xRot, 0)
        //
        // MC 坐标系:
        // - Y is up
        // - xRot (pitch): 正值 = 往下看, 负值 = 往上看
        // - yRot (yaw): 0 = South, 90 = West, 180 = North, -90 = East
        //
        // Three.js YXZ Euler 转换:
        // - pitchNode.rotation.x = -xRot (负号来自 MC 的 -xRot)
        // - yawNode.rotation.y = π - yRot (π 偏移来自 MC 的 π - yRot)
        //
        // 测试结果:
        // - MC pitch 正值=往下看 -> Three.js rotation.x 正值=往下看 (需要保持一致)
        // - MC yaw 0=South(-Z) -> Three.js 需要 180° 偏移

        const pitchDeg = -data.pitch; // 负号：MC setRotation 是 -xRot
        const yawDeg = 180 - data.yaw; // 偏移180度：MC π - yRot

        // Update Blender-style camera using yaw/pitch nodes
        if (this.yawNode && this.pitchNode) {
            // Position at player eye level
            this.yawNode.position.set(
                this.currentPosition.x,
                this.currentPosition.y + 1.5,
                this.currentPosition.z
            );

            // Apply yaw rotation to yawNode (around world Y axis)
            this.yawNode.rotation.y = THREE.MathUtils.degToRad(yawDeg);

            // Apply pitch rotation to pitchNode (around local X axis)
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

        // Pulse animation
        if (this.pulseRing) {
            const scale = 1 + Math.sin(performance.now() * 0.003) * 0.3;
            this.pulseRing.scale.set(scale, scale, 1);
            this.pulseRing.material.opacity = 0.4 - Math.sin(performance.now() * 0.003) * 0.2;
        }

        // Update free camera
        this.updateFreeCamera();

        // Render
        if (this.renderer && this.scene && this.camera) {
            this.renderer.render(this.scene, this.camera);
        }

        this.updateFPS();
        this.updateMeshFPS();
    }

    updateFPS() {
        // Camera FPS is handled in updateMeshFPS
    }

    updateMeshFPS() {
        const fpsEl = document.getElementById('cam3d-fps');
        if (!fpsEl) return;

        const now = performance.now();
        if (!this.lastFpsUpdate) this.lastFpsUpdate = now;
        if (!this.frameCount) this.frameCount = 0;
        if (!this.meshFpsFrameCount) this.meshFpsFrameCount = 0;

        this.frameCount++;
        this.meshFpsFrameCount++;

        if (now - this.lastFpsUpdate >= 1000) {
            const camFps = this.frameCount;
            const meshFps = this.meshFpsFrameCount;

            if (this.viewMode === 'combined') {
                fpsEl.textContent = `${camFps}/${meshFps}`;
            } else if (this.viewMode === 'mesh') {
                fpsEl.textContent = meshFps;
            } else {
                fpsEl.textContent = camFps;
            }

            this.frameCount = 0;
            this.meshFpsFrameCount = 0;
            this.lastFpsUpdate = now;
        }
    }

    setViewMode(mode) {
        this.viewMode = mode;

        // Update mesh visibility
        if (this.meshesContainer) {
            this.meshesContainer.visible = (mode === 'combined' || mode === 'mesh');
        }

        // Update camera model visibility
        if (this.yawNode) {
            this.yawNode.visible = (mode === 'combined' || mode === 'camera');
        }

        // Adjust view based on mode
        if (mode === 'top') {
            this.targetSpherical.theta = 0;
            this.targetSpherical.phi = 0.05;
            this.targetSpherical.radius = 100;
            this.userHasZoomed = false;
            this.autoZoomCompleted = false;
        } else if (mode === 'front') {
            this.targetSpherical.theta = Math.PI;
            this.targetSpherical.phi = Math.PI / 2;
            this.targetSpherical.radius = 50;
            this.userHasZoomed = false;
            this.autoZoomCompleted = false;
        } else if (mode === 'combined') {
            this.targetSpherical.theta = Math.PI / 4;
            this.targetSpherical.phi = Math.PI / 3;
            this.targetSpherical.radius = 30;
        } else if (mode === 'mesh') {
            // Keep current radius but ensure reasonable default
            if (this.targetSpherical.radius < 10) {
                this.targetSpherical.radius = 50;
            }
        } else if (mode === 'camera') {
            this.targetSpherical.theta = Math.PI / 4;
            this.targetSpherical.phi = Math.PI / 3;
            this.targetSpherical.radius = 30;
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
