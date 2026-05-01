# WebSocket 适配

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/WebSocketAdapter.html

本文档介绍如何在 Emscripten 项目中集成微信小游戏 WebSocket 通信模块。

## 概述

本模块提供完整的 WebSocket 客户端实现，支持 WS 和 WSS 协议，适用于实时通信场景。

### 功能特性

| 功能 | 说明 |
|------|------|
| 协议支持 | WS (ws://) 和 WSS (wss://) |
| 消息类型 | 文本消息和二进制数据 |
| 自定义头 | 支持设置 HTTP 请求头 |
| 子协议 | 支持 Sec-WebSocket-Protocol |
| 压缩 | 支持 permessage-deflate |
| 并发连接 | 最多 5 个并发连接 |

> ⚠ **重要提示**
> - ✅ 真机必须使用 WSS 协议（wss://）- 加密连接
> - ❌ 不支持 WS 协议（ws://）- 非加密连接仅微信开发者工具支持
> - ✅ 需要在微信公众平台配置域名白名单（生产环境）

## 快速开始

### CMakeLists.txt 配置

```cmake
set(EXPORTED_FUNCS "[\
  \"_WebSocketOnSuccess\", \
  \"_WebSocketOnError\", \
  \"_WebSocketOnClose\", \
  \"_WebSocketOnStrData\", \
  \"_WebSocketOnBinData\", \
  \"_main\", \"_free\", \"_malloc\"]")

set_target_properties(mygame PROPERTIES
  LINK_FLAGS "\
    -s WASM=1 \
    -s EXPORTED_RUNTIME_METHODS='[\"ccall\", \"cwrap\", \"stringToUTF8\", \"UTF8ToString\", \"lengthBytesUTF8\", \"allocateUTF8\"]' \
    -s EXPORTED_FUNCTIONS='${EXPORTED_FUNCS}' \
    --js-library ${WXGAMESDK_DIR}/jslib/libwxwebsocket_open.jslib \
    -Wl,--whole-archive ${WXGAMESDK_DIR}/lib/libwxgamesdk.a -Wl,--no-whole-archive"
)
```

## API 参考

### 头文件

```cpp
#include "websocket/wx_websocket_interface.h"
```

### 客户端接口

```cpp
namespace WXSocket {
  class WebSocketClientInterface {
  public:
    // 配置连接
    virtual int SetConnectUrl(const std::string& url) = 0;
    virtual int SetHeader(const std::string& key, const std::string& value) = 0;
    virtual int SetTimeout(int timeout) = 0;

    // 设置回调
    virtual int SetMessageReceiveCallback(
      void (*callback)(const void* message, int len, void* user_data), void* user_data) = 0;
    virtual int SetSuccessCallback(
      void (*callback)(const std::unordered_map<std::string, std::string>& header, void* user_data), void* user_data) = 0;
    virtual int SetFailCallback(
      void (*callback)(const std::string& errmsg, void* user_data), void* user_data) = 0;
    virtual int SetCloseCallback(
      void (*callback)(int code, const std::string& reason, void* user_data), void* user_data) = 0;

    // 高级选项
    virtual int SetProtocols(const std::vector<std::string>& protocols) = 0;
    virtual int SetEnableTcpNoDelay(bool enable) = 0;
    virtual int SetPerMessageDeflate(bool enable) = 0;
    virtual int SetEnableForceCellularNetwork(bool enable) = 0;
  };

  // 全局函数
  WebSocketClientInterface* CreateWebSocketClient(int* client_id);
  int ConnectWebSocket(int client_id);
  int SendWebSocketMessage(int client_id, const void* message, int len);
  int CloseWebSocketClient(int client_id, int code = 1000, const std::string& reason = "");
}
```

## 使用示例

### 基本使用

```cpp
#include <emscripten.h>
#include "websocket/wx_websocket_interface.h"

using namespace WXSocket;

static int g_client_id = -1;
static WebSocketClientInterface* g_ws_client = nullptr;

void OnMessageReceived(const void* message, int len, void* user_data) {
  std::string msg((char*)message, len);
  std::cout << "收到消息: " << msg << std::endl;
}

void OnConnectSuccess(const std::unordered_map<std::string, std::string>& header, void* user_data) {
  std::cout << "连接成功!" << std::endl;
  SendWebSocketMessage(g_client_id, "Hello WebSocket!", 17);
}

void OnConnectFail(const std::string& errmsg, void* user_data) {
  std::cout << "连接失败: " << errmsg << std::endl;
}

void OnConnectionClose(int code, const std::string& reason, void* user_data) {
  std::cout << "连接关闭: " << code << " - " << reason << std::endl;
}

int main() {
  g_ws_client = CreateWebSocketClient(&g_client_id);
  if (!g_ws_client) return -1;

  g_ws_client->SetConnectUrl("wss://echo.websocket.org/");
  g_ws_client->SetTimeout(10000);
  g_ws_client->SetMessageReceiveCallback(OnMessageReceived, nullptr);
  g_ws_client->SetSuccessCallback(OnConnectSuccess, nullptr);
  g_ws_client->SetFailCallback(OnConnectFail, nullptr);
  g_ws_client->SetCloseCallback(OnConnectionClose, nullptr);

  ConnectWebSocket(g_client_id);
  emscripten_set_main_loop([](){}, 0, 1);
  return 0;
}
```

### 高级配置

```cpp
auto* client = WXSocket::CreateWebSocketClient(&client_id);

client->SetConnectUrl("wss://game.example.com/ws");
client->SetTimeout(15000);
client->SetHeader("Authorization", "Bearer your-token");

std::vector<std::string> protocols = {"game-protocol", "chat"};
client->SetProtocols(protocols);
client->SetEnableTcpNoDelay(true);
client->SetPerMessageDeflate(true);

WXSocket::ConnectWebSocket(client_id);
```

### 关闭码说明

| 关闭码 | 含义 |
|--------|------|
| 1000 | 正常关闭 |
| 1001 | 端点离开 |
| 1002 | 协议错误 |
| 1003 | 不支持的数据类型 |
| 1006 | 异常关闭（未收到关闭帧） |
| 1007 | 数据格式错误 |
| 1008 | 策略违规 |
| 1009 | 消息过大 |
| 1010 | 缺少扩展 |
| 1011 | 服务器内部错误 |

## 常见问题

**Q: 连接失败，提示"未完成的操作"？**
A: 检查是否使用了 WSS 协议，微信小游戏真机只支持 WSS，开发者工具支持采用WS进行调试。

**Q: 如何在真机测试？**
A: 确保服务器有有效的 SSL 证书，并在微信公众平台配置域名白名单。

**Q: 最多支持多少个连接？**
A: 最多 5 个并发 WebSocket 连接。

## 下一步

- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [TCP/UDP Socket 适配](./12-SocketAdapter.md)
- 了解 [微信开放接口](./15-WXOpenAPI.md)
