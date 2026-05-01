# 启动流程

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/LaunchProcess.html

本文档说明微信小游戏通用适配方案的小游戏启动流程，包括启动插件的注册机制、启动流程各阶段、启动封面配置以及CDN资源的下载与加载过程。

## 概述

微信小游戏通用适配方案集成了小游戏启动插件（WXGameKit），通过微信小游戏的requirePlugin机制来注册启动插件，由插件统一管理小游戏的启动流程。启动流程主要包括以下几个关键环节：

- **插件注册与初始化** - 通过requirePlugin机制加载启动插件
- **配置加载** - 读取并应用启动配置
- **启动封面显示** - 展示加载进度和游戏相关内容
- **资源加载** - 并行下载首资源包和WebAssembly代码包
- **引擎初始化** - 编译Wasm代码并初始化游戏引擎
- **游戏启动** - 完成初始化后进入游戏

## 插件注册机制

### game.json配置

首先需要在game.json中声明使用WXGameKit插件：

```json
{
  "plugins": {
    "WXGameKit": {
      "version": "major.minor.patch",
      "provider": "wxe5a48f1ed5f544b7",
      "contexts": [
        { "type": "isolatedContext" }
      ]
    }
  }
}
```

### 插件加载与注册

在小游戏代码中通过requirePlugin加载并注册插件。首先通过loader-config.js（/minigame/wx-game-kit/loader-config.js）加载启动插件相关配置，随后利用该配置完成插件初始化，游戏可以在插件初始化之前自定义修改loaderConfig配置值。

### 启动游戏

在game.js（/minigame/game.js）中导入插件并启动游戏：

```javascript
import './wx-game-kit/index';
import './wx-game-kit/custom/index';
import commFrameWork from "./framework";

if (typeof WXGameKit.loader !== 'undefined') {
  WXGameKit.loader.setModuleFunctionHandler(commFrameWork);
  WXGameKit.loader.start();
}
```

- `setModuleFunctionHandler` - 传入封装好的胶水层代码函数
- `start` - 启动游戏加载流程

## 启动流程详解

### 创建Loader阶段

创建Loader时会执行以下步骤：
- **版本检查** - 检查基础库版本是否满足要求
- **初始化各模块** - 初始化网络、文件系统、日志等模块
- **加载特性开关** - 根据环境加载功能特性配置
- **准备启动封面** - 初始化启动封面模块

### Start阶段

调用loader.start()后执行以下流程：
1. **显示启动封面** - 展示加载页面，显示进度条和加载文案
2. **并行加载任务**：
   - 加载并解压首资源包（从分包或CDN）
   - 加载并编译WebAssembly代码包
3. **处理资源数据** - 将资源数据写入内存
4. **引擎初始化** - 调用胶水层代码，初始化游戏引擎
5. **调用Main函数** - 执行游戏主函数
6. **隐藏启动封面** - 加载完成后隐藏封面

### 步骤提前

为了优化启动体验，以下步骤可以在start之前提前执行：

```javascript
// 提前显示启动封面（避免黑屏）
WXGameKit.splash.loadingPage.show();

// 提前开始加载首资源包
WXGameKit.loader.loadAsset();

// 提前开始加载WebAssembly代码包
WXGameKit.loader.loadWasm();

// 执行其他自定义逻辑...

// 最后调用start完成启动
WXGameKit.loader.start();
```

### 生命周期事件

启动插件提供了丰富的生命周期事件，允许在启动流程的不同阶段插入自定义逻辑：

```javascript
// 监听生命周期事件
WXGameKit.loader.lifeCycle.on(WXGameKit.loader.lifeCycle.event.PluginLaunch, (e) => {
  console.log('插件启动完成');
});

// 监听区间事件
WXGameKit.lifeCycle.on(WXGameKit.lifeCycle.event.ParallelLoad, (e) => {
  if (e.type == WXGameKit.lifeCycle.eventType.IntervalStart) {
    console.log('并行加载开始');
  } else if (e.type == WXGameKit.lifeCycle.eventType.IntervalEnd) {
    console.log('并行加载完成，耗时：', e.data.costTimeMs, 'ms');
  }
});
```

生命周期事件列表：

| 事件类型 | 事件名称 | 说明 |
|----------|----------|------|
| 点事件 | PluginLaunch | 插件启动完成 |
| 点事件 | FeatureFlagReady | 特性开关就绪 |
| 点事件 | LoadingPageReady | 启动封面就绪 |
| 点事件 | PreparedModule | 模块准备完成 |
| 点事件 | ProcessedAsset | 资源处理完成 |
| 区间事件 | ParallelLoad | 并行加载阶段 |
| 区间事件 | LoadWasm | Wasm代码加载 |
| 区间事件 | InstantiateWasm | Wasm实例化 |
| 区间事件 | PrepareAsset | 资源准备阶段 |
| 区间事件 | LoadAsset | 资源加载阶段 |
| 区间事件 | ReadAsset | 资源读取阶段 |
| 区间事件 | UnzipAsset | 资源解压阶段 |
| 区间事件 | CallMain | Main函数调用 |

## 启动封面配置

启动封面用于在小游戏加载期间展示游戏相关内容，避免玩家面对黑屏等待。配置位于config.json（/minigame/wx-game-kit/config.json）的loadingPage模块。

### 基础配置

```json
{
  "loadingPage": {
    "using": true,
    "hideAfterCallMain": true,
    "totalLaunchTime": 7000,
    "animationDuration": 100,
    "designWidth": 1600,
    "designHeight": 720,
    "scaleMode": "EXACT_FIT",
    "visible": true
  }
}
```

缩放模式说明：

| 模式 | 说明 |
|------|------|
| EXACT_FIT | 精确适配，可能变形 |
| NO_BORDER | 不留黑边，可能裁剪 |
| SHOW_ALL | 完整显示，可能有黑边 |
| FIXED_WIDTH | 宽度适配 |
| FIXED_HEIGHT | 高度适配 |

### 素材配置

```json
{
  "loadingPage": {
    "materialConfig": {
      "backgroundImage": "wx-game-kit/images/background.jpg",
      "backgroundVideo": "",
      "iconImage": "wx-game-kit/images/unity_logo.png"
    }
  }
}
```

> 注意：背景图片和图标图片应放置在小游戏包内；背景视频需要使用网络URL。

### 文本配置

```json
{
  "loadingPage": {
    "textConfig": {
      "firstStartText": "首次加载请耐心等待",
      "downloadingText": ["正在加载资源"],
      "compilingText": "编译中",
      "initText": "初始化中",
      "completeText": "开始游戏",
      "textDuration": 1500,
      "style": {
        "bottom": 115,
        "height": 45,
        "width": 1045,
        "lineHeight": 45,
        "color": "#ffffff",
        "fontSize": 28
      }
    }
  }
}
```

### 进度条配置

```json
{
  "loadingPage": {
    "barConfig": {
      "style": {
        "width": 1045,
        "height": 15,
        "padding": 2,
        "bottom": 78,
        "backgroundColor": "#66b71f"
      }
    }
  }
}
```

### 图标配置

```json
{
  "loadingPage": {
    "iconConfig": {
      "visible": true,
      "style": {
        "width": 64,
        "height": 23,
        "bottom": 20
      }
    }
  }
}
```

### 代码控制启动封面

```javascript
// 手动显示启动封面
WXGameKit.splash.loadingPage.show();

// 手动隐藏启动封面
WXGameKit.splash.loadingPage.hide();

// 动态控制显示/隐藏（不会结束封面）
WXGameKit.config.loadingPage.visible = false;
```

## CDN资源下载与加载

### 资源加载方式

小游戏首资源包支持两种加载方式：
- **分包加载** - 资源作为小游戏分包加载，适用于资源量较小的游戏
- **CDN下载** - 从CDN服务器下载资源，适用于资源量较大的游戏

### CDN配置

在config.json（/minigame/wx-game-kit/config.json）中配置CDN地址：

```json
{
  "network": {
    "dataFileCDN": "http://localhost:8000/",
    "usedAutoStreaming": false,
    "streamingCDN": "",
    "preload": {
      "preloadList": [],
      "maxConcurrent": 10
    }
  }
}
```

### 首资源包配置

```json
{
  "launch": {
    "data": {
      "loadFromSubpackage": false,
      "fileMD5": "bf4af4f0956c43e",
      "fileSize": 54558229,
      "optFileSize": 13509324,
      "isCompressed": true,
      "subPrefix": "",
      "maxTryCount": 2
    }
  }
}
```

资源包路径规则：
- 分包加载：`minigame/data-package/{fileMD5}.data.bin`
- CDN下载：`{dataFileCDN}{subPrefix}{fileMD5}.data.bin`

### Wasm代码包配置

Wasm代码包只支持分包加载方式，存放于minigame/wasm-code目录：

```json
{
  "launch": {
    "code": {
      "fileMD5": "9605d21ac39af64",
      "useWasmCodeSplit": false
    }
  }
}
```

代码包命名格式：`{fileMD5}.wasm.br`

### 缓存机制

启动插件内置了资源缓存机制：
- **自动缓存** - 首次下载的资源会自动缓存到本地
- **缓存复用** - 后续启动会优先使用本地缓存
- **缓存管理** - 支持配置最大缓存容量

```json
{
  "fileSystem": {
    "maxStorage": 1024,
    "releaseMemorySize": 31457280
  }
}
```

### 网络模块使用

启动插件提供带缓存机制的网络请求模块：

```javascript
// 使用CacheXHR发起请求
const xhr = new WXGameKit.network.XMLHttpRequest();
xhr.open('GET', 'http://example.com/cdn/resource.bundle');
xhr.responseType = 'arraybuffer';
xhr.onload = () => {
  if (xhr.status === 200) {
    console.log('下载成功', xhr.response);
    console.log('是否来自缓存:', xhr.isReadFromCache);
  }
};
xhr.send();
```

### 动态修改CDN地址

运行时可以动态修改CDN地址：

```javascript
// 方式一：直接修改配置
WXGameKit.config.network.dataFileCDN = 'http://new-cdn.example.com/';

// 方式二：使用网络模块接口
WXGameKit.network.setDataCDN('http://new-cdn.example.com/');
```

### 下载失败处理

启动插件支持下载失败重试和错误回调：

```javascript
// 监听下载错误事件
WXGameKit.network.dataPackage.on(
  WXGameKit.network.dataPackage.eventType.DownloadError,
  (error, tryCount) => {
    console.log('下载错误:', error);
    console.log('第', tryCount, '次尝试');
    // 可以在此切换CDN地址重试
    if (tryCount < 3) {
      WXGameKit.config.network.dataFileCDN = 'http://backup-cdn.example.com/';
    }
  }
);
```

### 配置验证

启动插件会在加载配置时自动验证配置的完整性和正确性。必填配置字段：

| 模块 | 字段 | 类型 | 说明 |
|------|------|------|------|
| project | engine.name | string | 引擎名称 |
| project | engine.version | string | 引擎版本 |
| project | convertPluginVersion | string | 转换插件版本 |
| launch | code.fileMD5 | string | 代码文件MD5 |
| launch | code.useWasmCodeSplit | boolean | 是否使用代码分包 |
| launch | data.loadFromSubpackage | boolean | 是否分包加载 |
| launch | data.fileMD5 | string | 资源文件MD5 |
| launch | data.fileSize | number | 资源文件大小 |
| launch | data.optFileSize | number | 优化后文件大小 |

### 调试配置

开发阶段可以开启调试功能：

```json
{
  "develop": {
    "hideTimeLogModal": false,
    "enableDebugLog": true,
    "monitor": {
      "showSuggestModal": true,
      "enable": true,
      "fps": 10,
      "showResultAfterLaunch": true,
      "duration": 30000
    }
  }
}
```

## 下一步

- 了解 [技术原理](./02-TechPrinciple.md)
- 了解 [文件系统适配](./11-FileSystemAdapter.md)
- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [键盘适配](./14-KeyboardAdapter.md)
- 查看 [常见问题](./17-FAQ.md)
