# Minecraft Screen Capture + Camera - Shared Memory Web Viewer

## 概述

本项目使用 **Windows 命名共享内存** 实现：
1. Minecraft 画面实时传输到浏览器
2. 玩家摄像机数据实时共享到浏览器
3. 网页端 3D 视图可视化摄像机位置和朝向

## 技术架构

```
┌─────────────────┐     共享内存      ┌─────────────────┐     stdout     ┌─────────────────┐
│ Minecraft Mod   │ ───────────────→ │ Java 子进程     │ ────────────→ │ Node.js 服务器 │
│ (写入帧数据)     │  Global\        │ (JNA 读取)      │  PNG base64   │ (WebSocket)    │
│ + 摄像机数据     │  MinecraftSC    │                  │               │                 │
└─────────────────┘  + MCCameraData  └─────────────────┘               └─────────────────┘
                                                                            ↓
                                                                    ┌─────────────────┐
                                                                    │ 浏览器          │
                                                                    │ 画面 + 3D 视图  │
                                                                    └─────────────────┘
```

## 核心组件

### 1. Java 端 (Minecraft Mod)

| 文件 | 作用 |
|------|------|
| `NamedSharedMemory.java` | 使用 JNA 调用 Windows API 创建/管理共享内存 |
| `ScreenCapture.java` | 通过 GPU API 读取像素，写入共享内存 |
| `CameraDataHeader.java` | 摄像机数据结构定义 |
| `CameraSharedMemory.java` | 摄像机数据共享内存写入 |
| `CameraDataWriter.java` | 摄像机数据捕获管理器 |
| `CameraMixin.java` | Mixin 注入捕获摄像机更新 |
| `CameraDataReader.java` | Java 子进程，读取摄像机共享内存并输出 JSON |

### 2. Node.js 端 (Web 服务器)

| 文件 | 作用 |
|------|------|
| `server.js` | 启动 Java 子进程，通过 WebSocket 转发帧和摄像机数据 |
| `viewer.js` | 浏览器端，显示画面 |
| `camera-viewer.js` | Three.js 3D 视图，显示摄像机位置和朝向 |
| `index.html` | 前端页面 |

## Windows 共享内存

### 屏幕捕获共享内存

**名称**: `Global\MinecraftScreenCapture`

**头结构 (64 bytes)**:
| 偏移 | 大小 | 内容 |
|------|------|------|
| 0 | 4 | Magic: `0x4D435348` ("MCSH") |
| 4 | 4 | Version: 2 |
| 8 | 4 | Screen Width (像素) |
| 12 | 4 | Screen Height (像素) |
| 16 | 4 | Capture Width (像素) |
| 20 | 4 | Capture Height (像素) |
| 24 | 4 | Format: 1 (RGBA) |
| 28 | 4 | Stride (每行字节数) |
| 36 | 8 | Timestamp (纳秒) |
| 44 | 8 | FrameCount (帧计数) |
| 52 | 4 | Status: 0=idle, 1=writing, 2=ready |

**数据格式**:
- 像素数据从偏移 64 开始
- RGBA 格式，每像素 4 字节
- 行顺序：底部到顶部（需要翻转）

### 摄像机数据共享内存

**名称**: `Global\MCCameraData`

**头结构 (128 bytes)**:
| 偏移 | 大小 | 内容 |
|------|------|------|
| 0 | 4 | Magic: `0x4D434344` ("MCCD") |
| 4 | 4 | Version: 1 |
| 8 | 8 | Timestamp (纳秒) |
| 16 | 8 | pos_x (double) |
| 24 | 8 | pos_y (double) |
| 32 | 8 | pos_z (double) |
| 40 | 4 | xRot (pitch, float) |
| 44 | 4 | yRot (yaw, float) |
| 48 | 4 | quat_x (float) |
| 52 | 4 | quat_y (float) |
| 56 | 4 | quat_z (float) |
| 60 | 4 | quat_w (float) |
| 64 | 4 | fov (float) |
| 68 | 4 | Status: 0=idle, 1=writing, 2=ready |
| 72 | 4 | cameraType: 0=first_person, 1=third_back, 2=third_front |
| 76 | 4 | isDetached: 0/1 |
| 80 | 48 | Reserved |

## 为什么用 Java 子进程？

Windows Session 隔离机制：
- PowerShell / Node.js 原生代码 → ❌ 拒绝访问 (Error 5)
- Java 子进程 → ✅ 可以访问

原因：Java 通过 JNA 创建的共享内存对象，只有 Java 进程能够访问。

## 启动步骤

### 1. 编译 Minecraft Mod
```bash
cd d:\Projects\mshare
.\gradlew build
```

### 2. 启动 Minecraft
```bash
.\gradlew runClient
```

### 3. 启动 Web 服务器
```bash
cd d:\Projects\mshare\web
node server.js
```

### 4. 打开浏览器
```
http://localhost:3000
```

### 5. 在 Minecraft 中移动/观察
进入游戏后，移动角色或旋转视角，3D 视图会实时更新摄像机位置和朝向。

## 3D 视图功能

- **绿色球体**: 代表摄像机位置
- **黄色箭头**: 代表摄像机朝向
- **绿色线框圆锥**: 代表 FOV 视野范围
- **网格地面**: 用于参考位置
- **坐标轴**: X(红)、Y(绿)、Z(蓝)

## 摄像机数据类型

| 字段 | 类型 | 说明 |
|------|------|------|
| x, y, z | double | 世界坐标位置 |
| pitch | float | 俯仰角 (-90° 到 90°) |
| yaw | float | 偏航角 (0° 到 360°) |
| quatX/Y/Z/W | float | 四元数旋转 |
| fov | float | 视野角度 |
| cameraType | int | 0=第一人称, 1=第三人称后, 2=第三人称前 |
| detached | bool | 是否分离视角 |

## Java 子进程通信协议

### 屏幕帧协议

Java 子进程通过 stdout 输出帧数据：

```
[Reader] FRAME_START
[Reader] WIDTH:854
[Reader] HEIGHT:480
[Reader] SIZE:413199
[Reader] DATA:<base64编码的PNG数据>
[Reader] FRAME_END
```

### 摄像机数据协议

Java 子进程通过 stdout 输出摄像机数据：

```
[CameraReader] CAMERA_START
[CameraReader] DATA:{"type":"camera","x":0.0,"y":64.0,"z":0.0,"pitch":0.0,"yaw":0.0,"quatX":0.0,"quatY":0.0,"quatZ":0.0,"quatW":1.0,"fov":70.0,"cameraType":0,"detached":0}
[CameraReader] CAMERA_END
```

## WebSocket 端口

| 端口 | 用途 |
|------|------|
| 3000 | HTTP 服务器 (静态文件) |
| 3001 | 屏幕帧 WebSocket |
| 3002 | 摄像机数据 WebSocket |

## 依赖

### Java 端
- JNA 5.15.0 (用于调用 Windows API)
- Java 25 (必须与 Minecraft 相同版本)

### Node.js 端
- Node.js 22.x
- ws (WebSocket 库)

## 已知问题

1. **Session 隔离**: 必须使用 Java 子进程读取共享内存
2. **Java 版本**: 必须使用 JDK 25 与 Minecraft 版本一致
3. **JNA 警告**: JNA 加载时会有 `--enable-native-access` 警告，不影响功能

## 文件路径

### JNA Jar 路径 (硬编码)
```
C:\Users\Admin\.gradle\caches\modules-2\files-2.1\net.java.dev.jna\jna\5.15.0\...\jna-5.15.0.jar
C:\Users\Admin\.gradle\caches\modules-2\files-2.1\net.java.dev.jna\jna-platform\5.15.0\...\jna-platform-5.15.0.jar
```

### Java 可执行文件路径
```
C:\Users\Admin\.jdks\jdk-25.0.2\bin\java.exe
```

## 性能

- 分辨率: 854 x 480 (默认)
- FPS: ~20-30
- 每帧 PNG 大小: ~400KB

## 扩展

### 调整分辨率
修改 `ScreenCapture.java` 中的 `downscaleFactor`:
```java
screenCapture.setDownscaleFactor(2); // 缩小2倍
```

### 调整 FPS
```java
screenCapture.setTargetFps(60);
```
