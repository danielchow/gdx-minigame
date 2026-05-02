> Source: https://developers.weixin.qq.com/minigame/dev/api/network/websocket/SocketTask.html
# SocketTask
基础库 1.7.0 开始支持，低版本需做[兼容处理](../../../guide/runtime/client-lib/compatibility.html)。

相关文档: [网络使用说明](../../../guide/base-ability/network.html)、[局域网通信]((mDNS))

WebSocket 任务，可通过 wx.connectSocket() 接口创建返回
## 方法
### SocketTask.send(Object object)
通过 WebSocket 连接发送数据
### SocketTask.close(Object object)
关闭 WebSocket 连接
### SocketTask.onOpen(function listener)
监听 WebSocket 连接打开事件
### SocketTask.onClose(function listener)
监听 WebSocket 连接关闭事件
### SocketTask.onError(function listener)
监听 WebSocket 错误事件
### SocketTask.onMessage(function listener)
监听 WebSocket 接收到服务器的消息事件
