# TCP/UDP Socket 适配

Source: https://developers.weixin.qq.com/minigame/dev/guide/game-engine/common-adaptation/Design/SocketAdapter.html

本文档介绍如何在 Emscripten 项目中集成微信小游戏 Socket 网络通信模块（WXSocketLib），支持 TCP 和 UDP 协议。

## 概述

WXSocketLib 是一个基于 Emscripten 的 WebAssembly 网络库，为微信小游戏环境提供类 POSIX Socket 接口。该库封装了微信小游戏的网络 API，使得原生 C/C++ 网络代码可以在 WebAssembly 环境中运行。

**支持的协议**：TCP (SOCK_STREAM)、UDP (SOCK_DGRAM)
**支持的地址族**：IPv4 (AF_INET)、IPv6 (AF_INET6)

## POSIX 接口支持情况

### 1. 原生支持的接口（无需修改）

以下接口与标准 POSIX 接口完全兼容，可直接使用：

- `socket()` - Socket 创建
- `shutdown()` - 关闭连接
- `bind()` - 地址绑定
- `connect()` - 连接（支持 IP 地址）
- `send()` / `recv()` - 数据收发
- `sendto()` / `recvfrom()` - UDP 数据收发
- `getsockname()` / `getpeername()` - 地址信息获取
- `getsockopt()` / `setsockopt()` - Socket 选项

> ⚠ 注意：小游戏环境基于安全考虑，在非调试模式时不支持直接 IP 地址连接，需要采用域名连接。

**域名连接接口：**

```cpp
int WXSocket::connect(int sockfd, const char *domain, int port,
  bool enable_http_dns = false, const char *http_dns_serviceid = nullptr);
```

### 2. 需要修改的接口

#### select() - 使用 WXSocket 命名空间

```cpp
// 使用 WXSocket 命名空间的 select
int result = WXSocket::select(nfds, &readfds, &writefds, &exceptfds, NULL);
```

#### ioctl() - 仅支持 FIONREAD

```cpp
int available = 0;
int result = WXSocket::ioctl(sockfd, FIONREAD, &available);
```

#### close() - 关闭 socket

```cpp
int result = WXSocket::close(sockfd);
```

> ⚠ 确保调用 `WXSocket::close` 才能正确释放 socket 资源。

### 3. 不支持的接口

以下接口不支持（微信小游戏环境不支持服务端 Socket 功能）：
- `listen()` / `accept()` / `accept4()` - 服务端接口
- `socketpair()` / `sendmsg()` / `recvmsg()` - 其他

## 编译集成方法

### CMakeLists.txt 配置

```cmake
set(EXPORTED_FUNCS "[\
  \"_UdpSocketOnMessage\", \
  \"_UdpSocketOnError\", \
  \"_TcpSocketOnConnect\", \
  \"_TcpSocketOnConnectError\", \
  \"_TcpSocketOnError\", \
  \"_TcpSocketOnMessage\", \
  \"_SocketOnClose\", \
  \"_main\", \"_free\", \"_malloc\"]")

set_target_properties(mygame PROPERTIES
  LINK_FLAGS "\
    -s WASM=1 \
    -s DYNCALLS=1 \
    -s ALLOW_MEMORY_GROWTH=0 \
    -s EXPORTED_RUNTIME_METHODS='[\"ccall\", \"cwrap\", \"stringToUTF8\", \"UTF8ToString\", \"lengthBytesUTF8\", \"allocateUTF8\"]' \
    -s EXPORTED_FUNCTIONS='${EXPORTED_FUNCS}' \
    -Wl,--whole-archive ${WXGAMESDK_DIR}/lib/libwxgamesdk.a -Wl,--no-whole-archive \
    --js-library ${WXGAMESDK_DIR}/jslib/libwxsocket_open.jslib"
)
```

## 使用示例

### TCP 客户端连接

```cpp
#include "socket/wx_socket_interface.h"
#include <arpa/inet.h>
#include <fcntl.h>
#include <netinet/in.h>

// 1. 创建 TCP socket
int sockfd = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);

// 2. 设置为非阻塞模式
fcntl(sockfd, F_SETFL, O_NONBLOCK);

// 3. 连接服务器
struct sockaddr_in server_addr;
memset(&server_addr, 0, sizeof(server_addr));
server_addr.sin_family = AF_INET;
server_addr.sin_port = htons(8080);
inet_pton(AF_INET, "192.168.1.100", &server_addr.sin_addr);

int ret = connect(sockfd, (struct sockaddr *)&server_addr, sizeof(server_addr));
if (ret == -1 && errno == EINPROGRESS) {
  // 连接正在进行中，使用 select 等待
}

// 4. 使用 select 检查状态
fd_set writefds;
FD_ZERO(&writefds);
FD_SET(sockfd, &writefds);
WXSocket::select(sockfd + 1, NULL, &writefds, NULL, NULL);

// 5. 发送数据
send(sockfd, "Hello", 5, 0);

// 6. 接收数据
char buffer[1024];
recv(sockfd, buffer, sizeof(buffer), 0);

// 7. 关闭
WXSocket::close(sockfd);
```

### TCP 域名连接

```cpp
// 使用域名连接（启用 HTTP DNS）
int ret = WXSocket::connect(sockfd, "www.example.com", 80, true, "your_service_id");
```

### UDP 客户端

```cpp
// 创建 UDP socket
int sockfd = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
fcntl(sockfd, F_SETFL, O_NONBLOCK);

// 发送数据
sendto(sockfd, MESSAGE, strlen(MESSAGE), 0, (struct sockaddr *)&server_addr, sizeof(server_addr));

// 接收响应
fd_set readfds;
FD_ZERO(&readfds);
FD_SET(sockfd, &readfds);
WXSocket::select(sockfd + 1, &readfds, NULL, NULL, NULL);
recvfrom(sockfd, buffer, sizeof(buffer), 0, (struct sockaddr *)&from_addr, &from_len);

WXSocket::close(sockfd);
```

## 注意事项

1. **非阻塞模式**：必须使用非阻塞模式，配合 `WXSocket::select()` 使用
2. **错误处理**：正确处理 `EAGAIN`/`EWOULDBLOCK`/`EINPROGRESS`
3. **主循环**：使用 `emscripten_set_main_loop()` 而不是 while 循环
4. **缓冲区大小**：TCP 默认 8MB，UDP 默认 2MB
5. **连接超时**：最大 20 秒，通过 `setsockopt(SO_SNDTIMEO)` 设置
6. **Socket 数量限制**：最大 512 个（TCP: 0-511, UDP: 512-1023）
7. **IPv6 支持**：支持 `AF_INET6` 地址族
8. **线程安全**：当前不是线程安全的，所有操作应在同一线程中
9. **资源清理**：始终使用 `WXSocket::close()` 关闭 socket

## 常见问题

**Q: 为什么 select() 需要使用 WXSocket::select()？**
A: WXSocketLib 实现了自定义的 socket 管理机制，标准的 select() 无法检测到 WXSocket 的状态变化。

**Q: 可以使用 poll() 或 epoll() 吗？**
A: 不可以，当前仅支持 select() 接口。

**Q: 为什么不支持服务端功能？**
A: 微信小游戏环境出于安全考虑，不允许应用监听端口。

## 下一步

- 了解 [WebSocket 适配](./13-WebSocketAdapter.md)
- 了解 [HTTP 客户端适配](./10-HttpAdapter.md)
- 了解 [微信开放接口](./15-WXOpenAPI.md)
