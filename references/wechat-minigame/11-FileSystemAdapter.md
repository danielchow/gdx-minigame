# 文件系统适配

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/FileSystemAdapter.html

本文档介绍微信小游戏文件系统适配模块的使用方法。

## 概述

Emscripten 默认使用虚拟文件系统（MEMFS），所有文件数据存储在内存中，不仅无法持久化，还会占用宝贵的内存空间。本适配模块将文件操作映射到微信小游戏的文件系统 API，实现真正的磁盘文件读写能力。

> ⚠ **重要：涉及文件存储的路径必须以 `/CustomWritablePath` 开头**
> - `/CustomWritablePath/...` → 映射到微信小游戏用户文件系统，数据存储在磁盘，可持久化、不占用内存
> - 其他路径 → 走 Emscripten 默认的 MEMFS，数据存储在内存中，重启后丢失且占用内存空间

使用标准 C 文件操作接口（fopen、fread、fwrite 等）即可，无需修改代码逻辑，只需确保路径以 `/CustomWritablePath` 开头。

### 功能特性

| 功能 | 说明 |
|------|------|
| 标准 C 文件操作 | 支持 fopen/fread/fwrite/fseek/ftell/fclose 等标准接口 |
| 目录操作 | 支持 mkdir/rmdir/access 等标准接口 |
| 路径自动映射 | 以 `/CustomWritablePath` 开头的路径自动映射到微信文件系统 |
| 扩展接口 | 递归删除目录、TAR 包加载、ZIP 解压、文件同步等 |

## 快速开始

### CMakeLists.txt 配置

```cmake
cmake_minimum_required(VERSION 3.10)
project(MyGame)

set(WXGAMESDK_DIR "${CMAKE_CURRENT_SOURCE_DIR}/wxgamesdk")

add_executable(mygame main.cpp)

target_include_directories(mygame
  PRIVATE ${WXGAMESDK_DIR}/include
)

target_link_libraries(mygame
  ${WXGAMESDK_DIR}/lib/libwxgamesdk.a
)

set_target_properties(mygame PROPERTIES
  LINK_FLAGS "\
    -s WASM=1 \
    -s EXPORTED_RUNTIME_METHODS='[\"ccall\", \"cwrap\", \"stringToUTF8\", \"UTF8ToString\", \"lengthBytesUTF8\", \"allocateUTF8\"]' \
    --js-library ${WXGAMESDK_DIR}/jslib/libwxfs_open.jslib \
    -Wl,--whole-archive ${WXGAMESDK_DIR}/lib/libwxgamesdk.a -Wl,--no-whole-archive"
)
```

## 基本文件操作

### 路径规则

| 路径前缀 | 说明 |
|----------|------|
| /CustomWritablePath/ | 映射到微信小游戏用户文件系统（可读写、可持久化） |
| /xxx/ | MEMFS内存资源 |

### 文件读写示例

```cpp
#include <cstdio>
#include <cstring>
#include <cassert>
#include <unistd.h>

// 写入文件
void WriteFile() {
  FILE* fp = fopen("/CustomWritablePath/awesomeface.txt", "w");
  if (fp) {
    fprintf(fp, "hello world");
    fclose(fp);
  }
  assert(access("/CustomWritablePath/awesomeface.txt", F_OK) == 0);
}

// 读取文件
void ReadFile() {
  FILE* fp = fopen("/CustomWritablePath/awesomeface.txt", "r");
  if (fp) {
    char buffer[1024];
    size_t readSize = fread(buffer, 1, sizeof(buffer), fp);
    buffer[readSize] = '\0';
    printf("读取内容: %s\n", buffer);
    fclose(fp);
  }
}
```

### 文件定位（Seek）操作

```cpp
void SeekTest() {
  const char* path = "/CustomWritablePath/seek_test.txt";
  FILE* fp = fopen(path, "w");
  fprintf(fp, "0123456789");
  fclose(fp);

  fp = fopen(path, "r");
  fseek(fp, 2, SEEK_SET);
  char buffer[5] = {0};
  fread(buffer, 1, 4, fp);  // buffer = "2345"

  fseek(fp, 0, SEEK_END);
  long fileSize = ftell(fp);  // fileSize = 10
  fclose(fp);
}
```

### 目录操作

```cpp
#include "filesystem/wx_fs_interface.h"
#include <sys/stat.h>

void DirectoryExample() {
  // 递归创建多级目录
  int ret = WXGameSDK::MkdirRecursive("/CustomWritablePath/Library/Application_Support/1.0.0/UpgradeCocos");
  if (ret == 0) {
    printf("目录创建成功\n");
  }
  // 创建单级目录
  mkdir("/CustomWritablePath/save", 0755);
}
```

## 扩展接口

### 头文件

```cpp
#include "filesystem/wx_fs_interface.h"
```

### 接口列表

```cpp
namespace WXGameSDK {
  // 递归创建目录
  int MkdirRecursive(const char* path);

  // 递归删除目录
  int RmdirRecursive(const char* path);

  // 文件同步（将磁盘文件索引同步到内存）
  int Sync(int num = 300);

  // 解压 ZIP 文件（异步）
  int Unzip(const char* zipFile, const char* destDir, void* userData,
    std::function<void(int callbackId, int result, const std::string& errmsg, void* userData)> callback);

  // 加载 TAR 包到内存（创建虚拟文件索引）
  int LoadPackage(const char* packagePath, const char* mountPath);

  // 卸载 TAR 包
  int UnloadPackage(const char* packagePath);
}
```

### TAR 包加载

将 TAR 包加载到内存，创建虚拟文件索引，可以直接通过挂载路径访问 TAR 包内的文件：

```cpp
void LoadTarPackage() {
  const char* packagePath = "/CustomWritablePath/tartest.tar";
  const char* mountPath = "/CustomWritablePath/Library/Application_Support/1.1.0.0";

  if (access(packagePath, F_OK) != 0) return;

  int fileCount = WXGameSDK::LoadPackage(packagePath, mountPath);
  if (fileCount >= 0) {
    printf("加载成功，包含 %d 个文件\n", fileCount);
    FILE* fp = fopen("/CustomWritablePath/Library/Application_Support/1.1.0.0/tartest/3.txt", "r");
    if (fp) {
      char buffer[1024];
      size_t readSize = fread(buffer, 1, sizeof(buffer), fp);
      buffer[readSize] = '\0';
      printf("文件内容: %s\n", buffer);
      fclose(fp);
    }
  }
}
```

TAR 包特性说明：
- TAR 包内的文件为**只读**，不能删除、重命名或修改
- 卸载 TAR 包后，其中的文件将不再可访问
- 适用于游戏资源包的快速加载场景，无需解压到磁盘

### TAR 包加载 vs ZIP 解压

| 对比项 | TAR 包加载 (LoadPackage) | ZIP 解压 (Unzip) |
|--------|------------------------|-----------------|
| 工作原理 | 整个 TAR 包读入内存，建立文件索引 | 异步解压到磁盘 |
| 并发读取 | ✅ 纯内存操作，无 I/O 竞争 | ⚠ 磁盘 I/O，存在竞争 |
| 读取性能 | 微秒级 | 毫秒级 |
| 磁盘占用 | 不额外占用 | 解压后占用磁盘 |
| 内存占用 | TAR 包常驻内存 | 按需加载，较低 |
| 文件权限 | 只读 | 可读写 |
| 适用场景 | 高频并发只读资源 | 需要修改的文件 |

## JS 与 C++ 层文件互通

### C++ 写入 → JS 读取

C++ 层通过 POSIX 接口写入的文件，JS 层可以直接读取。

**路径映射规则**：C++ 层的 `/CustomWritablePath/xxx` 对应 JS 层的 `wx.env.USER_DATA_PATH + '/xxx'`

```cpp
// C++ 写入
FILE* fp = fopen("/CustomWritablePath/save/player_data.json", "w");
fwrite(data, 1, strlen(data), fp);
fclose(fp);
```

```javascript
// JS 读取
const fs = wx.getFileSystemManager();
const wxPath = `${wx.env.USER_DATA_PATH}/save/player_data.json`;
const data = fs.readFileSync(wxPath, 'utf-8');
```

### JS 写入 → C++ 读取

JS 层写入的文件，C++ 层默认无法访问。需要在 JS 层写入后调用 `syncCreateFile` 同步：

```javascript
// JS 写入并同步
const fs = wx.getFileSystemManager();
const wxPath = `${wx.env.USER_DATA_PATH}/config/settings.json`;
fs.writeFileSync(wxPath, JSON.stringify({ volume: 0.8 }), 'utf-8');

// 关键步骤：同步给 C++ 层
GameGlobal.WXGameKit.gameInstance.IDBFS.syncCreateFile('/CustomWritablePath/config/settings.json');
```

> ⚠ 禁止同时通过 JS 和 C++ 写同一个文件

## 存储限制

| 限制项 | 数值 |
|--------|------|
| 用户数据目录上限 | 200 MB |
| 单个文件上限 | 200 MB |

文件大小最佳实践：
- 推荐单个文件大小：1 ~ 2 MB，最大不超过 10 MB
- 大文件建议拆分为多个小文件
- 零散小文件建议使用 TAR 包合并存储

## 错误码

| 错误码 | 说明 |
|--------|------|
| 0 | 成功 |
| -1 | 参数无效或文件/目录不存在 |
| -44 | 目录不存在 (RmdirRecursive) |
| > 0 | LoadPackage 返回文件数量 |

## 注意事项

- **路径前缀**：只有以 `/CustomWritablePath` 开头的路径才会映射到微信文件系统
- **TAR 包只读**：通过 LoadPackage 加载的文件为只读，无法修改或删除
- **异步操作**：Unzip 为异步操作，需要通过回调获取结果
- **并发访问**：避免多处同时读写同一文件

## 下一步

- 了解 [TCP/UDP Socket 适配](./12-SocketAdapter.md)
- 了解 [WebSocket 适配](./13-WebSocketAdapter.md)
- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [键盘适配](./14-KeyboardAdapter.md)
